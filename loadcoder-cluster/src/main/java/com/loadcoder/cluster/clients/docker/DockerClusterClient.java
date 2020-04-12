/*******************************************************************************
 * Copyright (C) 2020 Team Loadcoder
 * 
 * This file is part of Loadcoder.
 * 
 * Loadcoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Loadcoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.loadcoder.cluster.clients.docker;

import static com.loadcoder.statics.Statics.*;
import static com.loadcoder.cluster.clients.ClientUtils.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateVolumeResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.loadcoder.cluster.clients.ClientUtils;
import com.loadcoder.cluster.clients.docker.exceptions.ContainersStillRunningException;
import com.loadcoder.cluster.util.ZipUtil;
import com.loadcoder.statics.DockerConfigurationHelper;
import com.loadcoder.utils.DateTimeUtil;
import com.loadcoder.utils.FileUtil;
import static com.loadcoder.cluster.clients.docker.MasterContainers.*;

/**
 * @author stefan
 *
 */
public class DockerClusterClient {

	private static Logger log = LoggerFactory.getLogger(DockerClusterClient.class);
	private final List<Node> nodes;

	private final Map<String, Node> nodesMap;

	private Node masterNode = null;

	private final String clusterId;

	final ZipUtil zip = new ZipUtil();

	final String[] MAVEN_FILE_AND_DIR_NAME_WHITELIST_DEFAULT = { "pom.xml", "src", "test.sh", "settings.xml" };

	final Map<String, String> hostIpMapping;

	final private static String HOSTIP_REGEXP = "hostip[.].*";
	public DockerClusterClient() {
		nodes = new ArrayList<Node>();
		nodesMap = new HashMap<String, Node>();

		String masterNodeId = getConfiguration("cluster.masternode");
		Set<String> ids = DockerConfigurationHelper.getAllNodeIds();
		ids.stream().forEach(id -> {
			String apiHost = getConfiguration("node." + id + ".host");
			String apiPortFromConfig = getConfiguration("node." + id + ".dockerapi.port");
			
			Node node = new Node(id, apiHost, apiPortFromConfig);
			nodes.add(node);
			nodesMap.put(id, node);
			if (id.equals(masterNodeId)) {
				masterNode = node;
			}
		});
		clusterId = getConfiguration("cluster.id");
		
		hostIpMapping = getMatchingConfiguration(HOSTIP_REGEXP);
	}
	
	public Node getMasterNode() {
		return masterNode;
	}

	public void pullImageIfNeeded(Node node, String image) {
		if(image == null || image.isEmpty()) {
			throw new RuntimeException("");
		}
		
		List<Image> images = node.getDockerClient().listImagesCmd().withImageNameFilter(image).exec();
		if (images.isEmpty()) {
			log.info("pulling image " + image);
			pullImage(node, image);
		}
	}

	public void setupMasterContainer(String component, Map<String, String> envs, String defaultServerPort) {
		String image = getConfiguration(component + ".image");
		throwIfTrue(()-> image == null || image.isEmpty(), "There are no docker image defined for component " + component);
		pullImageIfNeeded(masterNode, image);
		String portFromConfiguration = getConfiguration(component + ".server.port");

		String portToUse = portFromConfiguration == null ? defaultServerPort : portFromConfiguration;
		setupMasterContainer(getConfiguration(component + ".image"), component, portToUse, envs);
	}

	public void setupMaster(MasterContainers... containers) {
		List<MasterContainers> list = Arrays.asList(containers);
		setupMaster(list);
	}

	public void setupMaster() {

		List<MasterContainers> list = Arrays.asList(LOADSHIP, INFLUXDB, GRAFANA, ARTIFACTORY);
		setupMaster(list);
	}

	public void setupMaster(List<MasterContainers> map) {
		map.stream().forEach(entry -> entry.setup(this));
	}

//	public void setupMasterWithConfiguration(Map<String, Map<String, String>> envs) {
//		setupMasterContainer("loadship", envs.get("loadship"), "6210");
//		setupMasterContainer("influx", envs.get("influx"), "8086");
//		setupMasterContainer("grafana", envs.get("grafana"), "3000");
//		setupMasterContainer("artifactory", envs.get("artifactory"), "8081");
//	}

	public void createVolume(Node node) {
		CreateVolumeResponse volume1Info = node.getDockerClient().createVolumeCmd().withName("JavaVolume").exec();
	}

	private void setupMasterContainer(String image, String containerName, String portToExpose,
			Map<String, String> envs) {
		setupMasterContainer(image, containerName, Integer.parseInt(portToExpose), envs);
	}

	public Ports getPortBinding(int portToExpose) {
		Ports portBindings = new Ports();
		portBindings.bind(ExposedPort.tcp(portToExpose), Ports.Binding.bindPort(portToExpose));
		return portBindings;
	}
	
	private void setupMasterContainer(String image, String containerName, int portToExpose, Map<String, String> envs) {

		String containerIdToStart;
		List<Container> cont = masterNode.getDockerClient().listContainersCmd()
				.withStatusFilter(Arrays.asList("created", "running", "exited"))
				.withNameFilter(Arrays.asList("/" + containerName)).exec();
		if (!cont.isEmpty()) {
			String state = cont.get(0).getState();
			if (state.equals("running")) {
				return;
			} else {
				containerIdToStart = cont.get(0).getId();
			}
		} else {
			log.info("creating container " + containerName);
			Ports portBindings = getPortBinding(portToExpose);
			
			HostConfig hostConfig = getNewHostConfig();
			hostConfig.withPortBindings(portBindings);

			CreateContainerCmd createContainerCmd = masterNode.getDockerClient().createContainerCmd(image)
					.withName(containerName).withExposedPorts(ExposedPort.tcp(portToExpose)).withHostConfig(hostConfig);
			if (envs != null) {
				List<String> envList = new ArrayList<>();
				envs.entrySet().stream().forEach(entry -> {
					envList.add(entry.getKey() + "=" + entry.getValue());
				});
				createContainerCmd.withEnv(envList);
			}

			CreateContainerResponse resp = createContainerCmd.exec();
			containerIdToStart = resp.getId();
		}

		log.info("starting container " + containerName);
		masterNode.getDockerClient().startContainerCmd(containerIdToStart)

				.exec();
	}

	public List<Container> getAllRunningContainers(Node node, String... states) {
		List<Container> cont = node.getDockerClient().listContainersCmd().withStatusFilter(Arrays.asList(states))
				.exec();
		return cont;
	}

	public void checkNoRunningContainers() {
		List<Node> containsRunningContainers = new ArrayList<>();
		nodes.stream().forEach(node -> {

			List<Container> cont = getAllRunningContainers(node, "running");

			boolean nodeContainsRunningClusterContainer = cont.stream().anyMatch(container -> {
				List<String> names = Arrays.asList(container.getNames());
				return names.stream().anyMatch(name -> {
					return name.contains(clusterId);
				});
			});
			if (nodeContainsRunningClusterContainer) {
				containsRunningContainers.add(node);
			}
		});

		if (!containsRunningContainers.isEmpty()) {
			StringBuffer nodesWithRunningClusterContainers = new StringBuffer();
			containsRunningContainers.stream()
					.forEach(node -> nodesWithRunningClusterContainers.append(node.getId() + ", "));
			throw new ContainersStillRunningException(
					"The following nodes has running containers with name containing the cluster.id " + clusterId + ": "
							+ nodesWithRunningClusterContainers.toString());
		}
	}

	public void stopAndRemoveContainersAtAllNodes(MasterContainers... containerNames) {

		List<String> nameMatcherToBeStopedAndRemoved = Arrays.asList(containerNames).stream().map(c -> c.toString())
				.collect(Collectors.toList());
		stopAndRemoveContainer(masterNode, nameMatcherToBeStopedAndRemoved);
		nameMatcherToBeStopedAndRemoved = Arrays.asList(clusterId + ".*");

		stopAndRemoveClusterInstances();
	}

	public void stopAndRemoveClusterInstances() {
		nodes.stream().forEach(node -> {
			log.info("Removing conainers at node " + node.getId());
			List<String> nameMatcherOfClusterInstance = Arrays.asList(clusterId + ".*");
			stopAndRemoveContainer(node, nameMatcherOfClusterInstance);
		});
	}

	public void stopAndRemoveAllMasterContainers() {
		MasterContainers[] containerNames = { GRAFANA, INFLUXDB, LOADSHIP };
		Node masterNode = getMasterNode();
		if(masterNode == null) {
			throw new RuntimeException("No masternode is defined");
		}
		List<String> s = Arrays.asList(containerNames).stream().map(c -> c.toString()).collect(Collectors.toList());
		stopAndRemoveContainer(masterNode, s);
	}

	public void stopAndRemoveMasterContainers(MasterContainers... containerNames) {
		Node masterNode = getMasterNode();

		List<String> s = Arrays.asList(containerNames).stream().map(c -> c.toString()).collect(Collectors.toList());
		stopAndRemoveContainer(masterNode, s);
	}

	private void stopAndRemoveContainer(Node node, List<String> stopAndRemoveNames) {
		List<Container> cont = node.getDockerClient().listContainersCmd()
				.withStatusFilter(Arrays.asList("running", "exited")).exec();

		cont.stream().forEach(container -> {
			List<String> names = Arrays.asList(container.getNames());
			for (String stopAndRemoveName : stopAndRemoveNames) {
				for (String containerName : names) {
					if (containerName.matches("/" + stopAndRemoveName.toLowerCase())) {
						try {
							node.getDockerClient().stopContainerCmd(container.getId()).exec();
						} catch (RuntimeException rte) {
							log.info(
									"Could not stop container with name " + containerName + ". Maybe already stopped?");
						}

						try {
							node.getDockerClient().removeContainerCmd(container.getId()).exec();
							log.info("Removed " + containerName);
						} catch (RuntimeException rte) {
							log.info("Could not stop container with name " + containerName);
							throw rte;
						}

					}

				}
			}
		});
	}

	public void pullImage(Node node, String image) {
		PullImageResultCallback cb = new PullImageResultCallback();
		node.getDockerClient().pullImageCmd(image).exec(cb);

		try {

			cb.awaitCompletion(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a new HostConfig with a hostname set to master:<IP of the master
	 * node>
	 * 
	 * @return the created HostConfig
	 */
	private HostConfig getNewHostConfig() {
		HostConfig hostConfig = new HostConfig();
		hostIpMapping.entrySet().stream().forEach(entry -> {

			String host = getComponentNameFromHostIpMapping(entry.getKey());
			hostConfig.withExtraHosts(new String[] { host + ":" + entry.getValue() });
				
		});
		return hostConfig;
	}

	protected static String getComponentNameFromHostIpMapping(String variableName) {
		String[] splitted = variableName.split("[.]", 2);
		throwIfTrue(() ->splitted.length != 2, "Could not read the host from config variable " + variableName);
		String host = splitted[1];
		return host;
	}
	
	public void startNewContainer(Node node, String containerId, String image, String executionId) {

		pullImageIfNeeded(node, image);

		Volume volume2 = new Volume("/root/docker_mavenrepo");
		Bind bind1 = new Bind("JavaVolume", volume2);

		HostConfig hostConfig = getNewHostConfig();
		hostConfig.withBinds(bind1);
		CreateContainerResponse resp = node.getDockerClient().createContainerCmd(image)
				.withEnv("LOADCODER_EXECUTION_ID=" + executionId,
						"LOADCODER_CLUSTER_INSTANCE_ID=" + clusterId + "-" + containerId,
						"LOADSHIP_HOST="+ ClientUtils.getHostValue("loadship.host"), 
						"LOADSHIP_PORT="+ MasterContainers.LOADSHIP.getExposedPort())
				.withName(clusterId + "-" + containerId).withHostConfig(hostConfig).exec();

		node.getDockerClient().startContainerCmd(resp.getId()).exec();
	}

	public void createAndStartNode(String nodeId, String image, String executionId) {
		Node node = nodesMap.get(nodeId);
		startNewContainer(node, com.loadcoder.utils.DateTimeUtil.getDateTimeNowString(), image, executionId);
	}

	public void zipAndSendToLoadship(File directory) {
		zipAndSendToCache(directory, MAVEN_FILE_AND_DIR_NAME_WHITELIST_DEFAULT);
	}

	public void zipAndSendToCache(File directory, String... fileAndDirNamesWhitelist) {
		byte[] bytes = zip.zipToBytes(new File("."), fileAndDirNamesWhitelist);

		String url = "http://" + masterNode.getHost() + ":" + MasterContainers.LOADSHIP.getPort() + "/loadship/data";

		PackageSender.performPOSTRequest(url, bytes);
	}

	/**
	 * Https is not yet supported, hence the private access modifier
	 * 
	 * @param directory
	 */
	private void zipAndSendToCacheTLS(File directory) {
		byte[] bytes = zip.zipToBytes(new File("."), "pom.xml", "src", "test.sh");
		String url = "https://" + masterNode.getHost() + ":" + getConfiguration("loadship.port") + "/loadship/data";
		PackageSender.performPOSTRequest(url, bytes);
	}

	public void startCluster(int amountOfContainersToStart, String executionId) {
		checkNoRunningContainers();
		stopAndRemoveClusterInstances();
		int i = 0;
		whileloop: while (true) {
			for (Node node : nodes) {
				startNewContainer(node, "" + i, getConfiguration("loadcoder.image"), executionId);
				log.info("Started new loadinstance at node:" + node.getId());
				i++;
				if (i >= amountOfContainersToStart) {
					break whileloop;
				}
			}
		}
	}

	public void startNewExecution(int amountOfContainersToStart) {
		startNewExecution(amountOfContainersToStart, DateTimeUtil.getDateTimeNowString());
	}

	public void startNewExecution(int amountOfContainersToStart, String executionId) {
		FileUtil.writeFile(executionId.getBytes(), new File("executionId.txt"));
		startCluster(amountOfContainersToStart, executionId);
	}

	public void scaleExistingExecution(String nodeId) {
		Node node = nodesMap.get(nodeId);
		List<Container> running = getAllRunningContainers(node, "running", "exited");

		List<Integer> i = running.stream().map(container -> {
			for (String name : container.getNames()) {
				if (name.matches("/" + clusterId + "-[0-9]*")) {
					String instanceNumber = name.split("-")[1];
					int number = Integer.parseInt(instanceNumber);
					return number;
				}
			}
			return -1;
		}).sorted().collect(Collectors.toList());
		int highest = i.get(i.size() - 1);
		String executionId = FileUtil.readFile("executionId.txt");
		int newNumber = highest + 1;
		startNewContainer(node, "" + newNumber, getConfiguration("loadcoder.image"), executionId);

	}

	public List<Node> getNodes() {
		return nodes;
	}

	public Node getNode(String key) {
		return nodesMap.get(key);
	}
}
