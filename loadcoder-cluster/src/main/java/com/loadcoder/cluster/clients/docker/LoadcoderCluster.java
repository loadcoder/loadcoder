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

import static com.loadcoder.cluster.clients.ClientUtils.throwIfTrue;
import static com.loadcoder.cluster.clients.docker.MasterContainers.GRAFANA;
import static com.loadcoder.cluster.clients.docker.MasterContainers.INFLUXDB;
import static com.loadcoder.cluster.clients.docker.MasterContainers.LOADSHIP;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.loadcoder.cluster.clients.docker.exceptions.ContainersStillRunningException;
import com.loadcoder.cluster.clients.grafana.GrafanaClient;
import com.loadcoder.cluster.clients.influxdb.InfluxDBClient;
import com.loadcoder.cluster.util.ZipUtil;
import com.loadcoder.statics.Configuration;
import com.loadcoder.statics.Statics;
import com.loadcoder.utils.DateTimeUtil;
import com.loadcoder.utils.FileUtil;

public class LoadcoderCluster {

	private static Logger log = LoggerFactory.getLogger(LoadcoderCluster.class);

	private static int IMAGE_PULL_TIMEOUT_MIN_DEFAULT = 5;
	private final List<Node> workers = new ArrayList<Node>();;

	private final Map<String, Node> nodesMap;

	private Node masterNode = null;

	private final String clusterId;
	private final String CLUSTER_ID_DEFAULT = "loadcoder";

	final ZipUtil zip = new ZipUtil();

	final String[] MAVEN_FILE_AND_DIR_NAME_WHITELIST_DEFAULT = { "pom.xml", "src", "test.sh", "settings.xml" };

	final Map<String, String> hostIpMapping;

	final private static String HOSTIP_REGEXP = "hostip[.].*";

	final Configuration config;

	GrafanaClient grafana;
	InfluxDBClient influxDB;

	public LoadcoderCluster() {
		this(new Configuration());
	}

	private Node getNodeFromConfiguration(String id, boolean useDockerMTLS, String mtlsPassword) {
		String publicHost = config.getConfiguration("node." + id + ".host");
		String internalHost = config.getConfiguration("node." + id + ".internal.host");

		String apiPortFromConfig = config.getConfiguration("node." + id + ".dockerapi.port");

		Node node = new Node(id, publicHost, internalHost, apiPortFromConfig, useDockerMTLS, mtlsPassword);
		return node;
	}

	protected LoadcoderCluster(Configuration config) {
		this.config = config;
		nodesMap = new HashMap<String, Node>();

		String masterNodeId = config.getConfiguration("cluster.masternode");
		Set<String> ids = getAllNodeIds();

		String useDockerMTLSConfiguration = config.getConfiguration("docker.mtls", "true");
		boolean useDockerMTLS = useDockerMTLSConfiguration != null && useDockerMTLSConfiguration.equals("false") ? false
				: true;

		String mtlsPassword = getPassword();

		ids.stream().forEach(id -> {
			Node node = getNodeFromConfiguration(id, useDockerMTLS, mtlsPassword);
			String useAsWorker = config.getConfiguration("node." + id + ".use-as-worker");
			if (useAsWorker != null && useAsWorker.toLowerCase().equals("false")) {

			} else {
				workers.add(node);
				nodesMap.put(id, node);
			}

			if (id.equals(masterNodeId)) {
				masterNode = node;
			}
		});

		clusterId = config.getConfiguration("cluster.id", CLUSTER_ID_DEFAULT);

		hostIpMapping = getHostIpMap();

	}

	public Configuration getConfiguration() {
		return config;
	}

	private Map<String, String> getHostIpMap() {
		Map<String, String> hostIpMapping = new HashMap<String, String>();
		Map<String, String> hostIpConf = config.getMatchingConfig(HOSTIP_REGEXP);

		hostIpConf.entrySet().stream().forEach(entry -> {
			String host = getHostNameFromHostIpMapping(entry.getKey());
			hostIpMapping.put(host, entry.getValue());
		});
		return hostIpMapping;
	}

	void checkIfThereAreWorkers() {
		if (workers.size() == 0) {
			throw new RuntimeException(
					"No nodes configured. A node is found in the configuration with the following pattern: node.<node ID>.host");
		}
	}

	private String getPassword() {
		String result;
		String passwordFromJVMArgs = System.getProperty("docker.mtls.password");

		String passwordFromConfig = config.getConfiguration("docker.mtls.password");

		if (passwordFromJVMArgs != null) {
			result = passwordFromJVMArgs;
		} else if (passwordFromConfig != null) {
			result = passwordFromConfig;
		} else {
			result = null;
		}
		return result;
	}

	protected Set<String> getAllNodeIds() {

		Set<String> result = new HashSet<>();

		Map<String, String> map = config.getConfiguration();
		map.entrySet().stream().forEach(entry -> {
			String key = entry.getKey();
			if (key.matches("node\\..*\\.host") && !key.contains("internal")) {
				String id = key.replace("node.", "").replace(".host", "");
				result.add(id);
			}
		});
		return result;
	}

	private Node getMasterNode() {
		return masterNode;
	}

	public List<Container> listContainers() {
		List<Container> containers = getMasterNode().getDockerClient().listContainersCmd().exec();
		return containers;
	}

	private void pullImageIfNeeded(Node node, String image) {
		if (image == null || image.isEmpty()) {
			throw new RuntimeException("");
		}
		List<Image> images = node.getDockerClient().listImagesCmd().withImageNameFilter(image).exec();

		if (images.isEmpty()) {
			log.info("pulling image " + image);
			pullImage(node, image);
		}
	}

	protected void setupMasterContainer(String component, Map<String, String> envs, String port) {
		String image = config.getConfiguration(component + ".image");
		throwIfTrue(() -> image == null || image.isEmpty(),
				"There are no docker image defined for component " + component);
		pullImageIfNeeded(masterNode, image);

		setupMasterContainer(config.getConfiguration(component + ".image"), component, port, envs);
	}

	public void setupMaster(MasterContainers... containers) {
		List<MasterContainers> list = Arrays.asList(containers);
		setupMaster(list);
	}

	public void setupMaster() {

		List<MasterContainers> list = Arrays.asList(LOADSHIP, INFLUXDB, GRAFANA);
		setupMaster(list);
	}

	private void setupMaster(List<MasterContainers> map) {
		map.stream().forEach(entry -> {
			setupMasterContainer(entry.name().toLowerCase(), entry.getEnv(), entry.getServerPort(config));
		});
	}

	private void setupMasterContainer(String image, String containerName, String portToExpose,
			Map<String, String> envs) {
		setupMasterContainer(image, containerName, Integer.parseInt(portToExpose), envs);
	}

	private Ports getPortBinding(int portToExpose) {
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

	private List<Container> getAllRunningContainers(Node node, String... states) {
		List<Container> cont = node.getDockerClient().listContainersCmd().withStatusFilter(Arrays.asList(states))
				.exec();
		return cont;
	}

	private void checkNoRunningContainers() {
		checkIfThereAreWorkers();
		List<Node> containsRunningContainers = new ArrayList<>();
		workers.stream().forEach(node -> {

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

	public void stopExecution() {
		checkIfThereAreWorkers();
		workers.stream().forEach(node -> {
			log.info("Removing conainers at node " + node.getId());
			List<String> nameMatcherOfClusterInstance = Arrays.asList(clusterId + ".*");
			stopAndRemoveContainer(node, nameMatcherOfClusterInstance);
		});
	}

	public void stopAndRemoveAllMasterContainers() {
		MasterContainers[] containerNames = { GRAFANA, INFLUXDB, LOADSHIP };
		Node masterNode = getMasterNode();
		if (masterNode == null) {
			throw new RuntimeException("No masternode is defined");
		}
		List<String> s = Arrays.asList(containerNames).stream().map(c -> c.toString()).collect(Collectors.toList());
		stopAndRemoveContainer(masterNode, s);
	}

	public void stopAndRemoveAllContainers() {
		stopAndRemoveAllMasterContainers();
		stopExecution();
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

	private int getImagePullTimeoutMin() {
		String imagePullTimeoutMinString = config.getConfiguration("docker.image-pull-timeout-minutes");
		if (imagePullTimeoutMinString == null) {
			return IMAGE_PULL_TIMEOUT_MIN_DEFAULT;
		} else {
			int imagePullTimeoutMin = Integer.parseInt(imagePullTimeoutMinString);
			if (imagePullTimeoutMin < 1) {
				throw new RuntimeException("docker.image-pull-timeout-minutes was set to " + imagePullTimeoutMin
						+ " which is invalid. Set the value to higher than 0");
			}
			return imagePullTimeoutMin;
		}
	}

	private void pullImage(Node node, String image) {
		PullImageResultCallback cb = new PullImageResultCallback();
		node.getDockerClient().pullImageCmd(image).exec(cb);

		int imagePullTimeoutMin = getImagePullTimeoutMin();
		boolean complete = false;
		long timeoutCountStart = System.currentTimeMillis();
		long timeoutCountEnd = timeoutCountStart + imagePullTimeoutMin * Statics.MINUTE;
		int pollSeconds = 10;

		while (true) {
			if (System.currentTimeMillis() > timeoutCountEnd) {
				log.debug("The pull image timeout at " + imagePullTimeoutMin + " minute/s occured");
				break;
			}
			try {
				log.debug("Pulling...");
				complete = cb.awaitCompletion(pollSeconds, TimeUnit.SECONDS);
				log.debug("Pulling image " + image + " succeeded with status {}", complete);
				break;
			} catch (DockerClientException e) {
				if (e.getMessage().contains("complete")) {
					pollSeconds = 1;
				}
				log.debug("Exception pull message: " + e.getMessage());
			} catch (InterruptedException e) {
				throw new RuntimeException("Unexpected exception caught during image " + image + " pull" + e);
			}

		}
		if (!complete) {
			throw new RuntimeException("Did not succeed to pull image " + image);
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
		List<String> list = hostIpMapping.entrySet().stream().map((hostIp) -> {
			return hostIp.getKey() + ":" + hostIp.getValue();
		}).collect(Collectors.toList());
		String[] hostIpArray = list.toArray(new String[list.size()]);
		hostConfig.withExtraHosts(hostIpArray);

		return hostConfig;
	}

	protected static String getHostNameFromHostIpMapping(String variableName) {
		String[] splitted = variableName.split("[.]", 2);
		throwIfTrue(() -> splitted.length != 2, "Could not read the host from config variable " + variableName);
		String host = splitted[1];
		return host;
	}

	private void startNewContainer(Node node, String containerId, String image, String executionId, String md5sum) {

		pullImageIfNeeded(node, image);

		Volume volume2 = new Volume("/root/host-volume");
		Bind bind1 = new Bind("LoadcoderVolume", volume2);

		HostConfig hostConfig = getNewHostConfig();
		hostConfig.withBinds(bind1);
		String timezone = config.getConfiguration("cluster.timezone");

		List<String> envs = Arrays
				.asList("LOADCODER_EXECUTION_ID=" + executionId,
						"LOADCODER_CLUSTER_INSTANCE_ID=" + clusterId + "-" + containerId,
						"LOADSHIP_HOST=" + getInternalHost(MasterContainers.LOADSHIP),
						"LOADSHIP_PORT=" + MasterContainers.LOADSHIP.getExposedPort(config), "TEST_MD5SUM=" + md5sum)
				.stream().collect(Collectors.toList());
		if (timezone != null && !timezone.isEmpty()) {
			envs.add("TZ=" + timezone);
		}
		CreateContainerResponse resp = node.getDockerClient().createContainerCmd(image).withEnv(envs)
				.withName(clusterId + "-" + containerId).withHostConfig(hostConfig).exec();

		node.getDockerClient().startContainerCmd(resp.getId()).exec();
	}

	public void uploadTest(byte[] bytes) {
		String md5 = md5Bytes(bytes);
		FileUtil.writeFile(md5.getBytes(), new File("test-md5sum.txt"));
		String url = "http://" + masterNode.getHost() + ":" + MasterContainers.LOADSHIP.getPort(config)
				+ "/loadship/data";

		PackageSender.performPOSTRequest(url, bytes);
	}

	private static String md5Bytes(byte[] bytes) {
		return rebase(md5(bytes));
	}

	private static byte[] md5(byte[] bytes) {
		try {
			byte[] md5Byte = MessageDigest.getInstance("MD5").digest(bytes);
			return md5Byte;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String rebase(byte[] bytes) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		// return complete hash
		return sb.toString();
	}

	private void startCluster(int amountOfContainersToStart, String executionId, String md5sum) {
		checkIfThereAreWorkers();
		checkNoRunningContainers();
		stopExecution();
		int i = 0;
		whileloop: while (true) {
			for (Node node : workers) {
				startNewContainer(node, "" + i, config.getConfiguration("loadcoder.image"), executionId, md5sum);
				log.info("Started new loadinstance at node:" + node.getId());
				i++;
				if (i >= amountOfContainersToStart) {
					break whileloop;
				}
			}
		}
	}

	public StartedClusterExecution startNewExecution(int amountOfContainersToStart) {
		String md5sum = FileUtil.readFile("test-md5sum.txt");
		String executionId = DateTimeUtil.getDateTimeNowString();
		startNewExecution(amountOfContainersToStart, executionId, md5sum);
		return new StartedClusterExecution(executionId);
	}

	private void startNewExecution(int amountOfContainersToStart, String executionId, String md5sum) {
		FileUtil.writeFile(executionId.getBytes(), new File("executionId.txt"));
		startCluster(amountOfContainersToStart, executionId, md5sum);
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
		String md5sum = FileUtil.readFile("test-md5sum.txt");
		int newNumber = highest + 1;
		startNewContainer(node, "" + newNumber, config.getConfiguration("loadcoder.image"), executionId, md5sum);

	}

	protected List<Node> getNodes() {
		return workers;
	}

	/**
	 * Returns the MasterContainer's interal host. If the internal host is
	 * configured at image lever, that will be chosen in first hand. 2nd choise will
	 * be the master node's internal host 3rd and final choise will be the master
	 * (public) host
	 * 
	 * @param masterContainers
	 * @return
	 */
	public String getInternalHost(MasterContainers masterContainers) {
		String result;
		String internalImage = config.getConfiguration(masterContainers.name().toLowerCase() + ".internal.host");
		String internalMaster = getMasterNode().getInternalHost();
		if (internalImage != null && !internalImage.isEmpty()) {
			result = internalImage;
		} else if (internalMaster != null && !internalMaster.isEmpty()) {
			result = internalMaster;
		} else {
			result = getMasterNode().getHost();
		}

		return result;
	}

	public String getHost(MasterContainers masterContainers) {
		String result;
		String hostImage = config.getConfiguration(masterContainers.name().toLowerCase() + ".host");
		if (hostImage != null && !hostImage.isEmpty()) {
			result = hostImage;
		} else {
			result = getMasterNode().getHost();
		}

		return result;
	}

	public GrafanaClient getGrafanaClient(InfluxDBClient incluxDBClient) {

		String configuredBasicAuth = config.getConfiguration("grafana.basic.auth");
		String defaultAdminBasicAuth = "YWRtaW46YWRtaW4=";
		String authenticationValue = "Basic "
				+ (configuredBasicAuth != null ? configuredBasicAuth : defaultAdminBasicAuth);

		if (this.grafana == null) {
			this.grafana = new GrafanaClient(getHost(MasterContainers.GRAFANA),
					getInternalHost(MasterContainers.INFLUXDB), authenticationValue, incluxDBClient, config);
		}
		return this.grafana;

	}

	public InfluxDBClient getInfluxDBClient(String testGroup, String testName) {

		if (this.influxDB == null) {
			this.influxDB = new InfluxDBClient(getHost(MasterContainers.INFLUXDB),
					Integer.parseInt(MasterContainers.INFLUXDB.getPort(config)), false, testGroup, testName);
		}
		return this.influxDB;
	}
}
