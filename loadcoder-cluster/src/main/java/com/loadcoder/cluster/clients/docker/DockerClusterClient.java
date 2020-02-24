/*******************************************************************************
 * Copyright (C) 2020 Stefan Vahlgren at Loadcoder
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

import static com.loadcoder.statics.Statics.getConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import com.loadcoder.cluster.clients.docker.exceptions.ContainersStillRunningException;
import com.loadcoder.cluster.util.ZipUtil;
import com.loadcoder.statics.DockerConfigurationHelper;

public class DockerClusterClient {

	private final List<Node> nodes;
	private final Map<String, Node> nodesMap;

	private Node masterNode = null;

	private final String clusterId;

	ZipUtil zip = new ZipUtil();

	public DockerClusterClient() {
		nodes = new ArrayList<Node>();
		nodesMap = new HashMap<String, Node>();

		String masterNodeId = getConfiguration("cluster.masternode");
		Set<String> ids = DockerConfigurationHelper.getAllNodeIds();
		ids.stream().forEach(id -> {
			String apiHost = getConfiguration("node." + id + ".host");
			String apiPort = getConfiguration("node." + id + ".dockerapi.port");
			Node node = new Node(id, apiHost, Integer.parseInt(apiPort));
			nodes.add(node);
			nodesMap.put(id, node);
			if (id.equals(masterNodeId)) {
				masterNode = node;
				masterNode.setContainerHost(getConfiguration("node." + id + ".containerhost"));
			}
		});
		clusterId = getConfiguration("cluster.id");
	}

	public Node getMasterNode() {
		return masterNode;
	}

	public static void main(String[] args) {
		DockerClusterClient cli = new DockerClusterClient();
		cli.getAllContainers(cli.nodes.get(0));
	}

	public void pullImageIfNeeded(Node node, String image) {
		List<Image> images = node.getDockerClient().listImagesCmd().withImageNameFilter(image).exec();
		if (images.isEmpty()) {
			System.out.println("pulling image " + image);
			pullImage(node, image);
		}
	}

	public void setupContainer(String component, Map<String, String> envs) {
		pullImageIfNeeded(masterNode, getConfiguration(component + ".image"));
		setupMasterContainer(getConfiguration(component + ".image"), component, getConfiguration(component + ".port"),
				envs);
	}

	public void setupMaster() {
		Map<String, String> loadshipMap = new HashMap<>();
		loadshipMap.put("HTTP_ENABLED", "true");
		loadshipMap.put("MODECHOOSER", "LOADSHIP");
		setupContainer("loadship", loadshipMap);
		setupContainer("influx", null);
		setupContainer("grafana", null);
		setupContainer("artifactory", null);
	}
	
	public void setupMaster(Map<String, Map<String, String>> envs) {
		setupContainer("loadship", envs.get("loadship"));
		setupContainer("influx", envs.get("influx"));
		setupContainer("grafana", envs.get("grafana"));
		setupContainer("artifactory", envs.get("artifactory"));
	}

	public void createVolume(Node node) {
		CreateVolumeResponse volume1Info = node.getDockerClient().createVolumeCmd().withName("JavaVolume").exec();
	}

	public void getAllContainers(Node node) {

//		List<Image> images = nodesMap.get("2").getDockerClient().listImagesCmd().withImageNameFilter("fiske").exec();
//		createVolume(node);
//		setupMaster();
//		startCluster(clusterId, 1, getConfiguration("cluster.testrunner.image"));
		createAndStartNode("1", getConfiguration("testrunner.image"),
				com.loadcoder.utils.DateTimeUtil.getDateTimeNowString());

//		setupContainer(getConfiguration("cluster.testcache.image"), "testcache", 8490);

//		pullImage(masterNode, getConfiguration("influx.image"));
//		setupContainer(getConfiguration("influx.image"), "influxdb", 8086);

//		pullImage(nodesMap.get("2"), getConfiguration("testrunner.image"));
//		setupMasterContainer(getConfiguration("testcache.image"), "testcache", getConfiguration("testcache.port"));

//		setupMasterContainer(getConfiguration("grafana.image"), "grafana", getConfiguration("grafana.port"));
//		setupMasterContainer(getConfiguration("influx.image"), "influx", getConfiguration("influx.port"));

//		startTestCache();

//		List<Container> cont = node.getDockerClient().listContainersCmd().withStatusFilter(Arrays.asList("exited")).exec();

//		CreateContainerResponse resp= dockerClient.createContainerCmd("registry.master.com/testrunner")
//				.withName("testrunner1")
//				.exec();

//		dockerClient.startContainerCmd(resp.getId()).exec();
//		dockerClient.removeContainerCmd("cde76178c540").exec();
//		System.out.println("done");
	}

	private void setupMasterContainer(String image, String containerName, String portToExpose,
			Map<String, String> envs) {
		setupMasterContainer(image, containerName, Integer.parseInt(portToExpose), envs);
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
			System.out.println("creating container " + containerName);
			Ports portBindings = new Ports();
			portBindings.bind(ExposedPort.tcp(portToExpose), Ports.Binding.bindPort(portToExpose));
			HostConfig h = new HostConfig();

			h.withPortBindings(portBindings);
			h.withExtraHosts(new String[] { "master:" + masterNode.getContainerHost() });
//			CreateContainerResponse resp = 

			CreateContainerCmd createContainerCmd = masterNode.getDockerClient().createContainerCmd(image)
					.withName(containerName).withExposedPorts(ExposedPort.tcp(portToExpose)).withHostConfig(h);
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

		System.out.println("starting container " + containerName);
		masterNode.getDockerClient().startContainerCmd(containerIdToStart)

				.exec();
	}

	public void checkNoRunningContainers() {
		List<Node> containsRunningContainers = new ArrayList<>();
		nodes.stream().forEach(node -> {
			List<Container> cont = node.getDockerClient().listContainersCmd().withStatusFilter(Arrays.asList("running"))
					.exec();

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

	public void removeExitedContainers() {
		nodes.stream().forEach(node -> {
			List<Container> cont = node.getDockerClient().listContainersCmd().withStatusFilter(Arrays.asList("exited"))
					.exec();

			cont.stream().filter(container -> {
				List<String> names = Arrays.asList(container.getNames());
				return names.stream().anyMatch(name -> {
					return name.contains(clusterId);
				});
			}).forEach(container -> {
				node.getDockerClient().removeContainerCmd(container.getId()).exec();
			});
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

	public void startNewContainer(Node node, String containerId, String image, String executionId) {

		pullImageIfNeeded(node, image);
//		node.getDockerClient().listVolumesCmd().exec().
//		Volume v = new Volume("/root/docker_mavenrepo");
//		Bind b = new Bind("/tmp/docker_mavenrepo", v);

		Volume volume2 = new Volume("/root/docker_mavenrepo");
		Bind bind1 = new Bind("JavaVolume", volume2);

//		.withNetworkMode("loadcoder_net")
		HostConfig hostConfig = new HostConfig()
//				.withBinds(b);
				.withBinds(bind1);
		hostConfig.withExtraHosts(new String[] { "master:" + masterNode.getContainerHost() });
		CreateContainerResponse resp = node.getDockerClient().createContainerCmd(image)
//				.withVolumes(volumes)
				.withEnv("LOADCODER_EXECUTION_ID=" + executionId,
						"LOADCODER_CLUSTER_INSTANCE_ID=" + clusterId + "-" + containerId)

//				.withEnv("loadcoder.execution.id="+ executionId)

				.withName(clusterId + "-" + containerId).withHostConfig(hostConfig).exec();

		node.getDockerClient().startContainerCmd(resp.getId()).exec();
	}

	public void createAndStartNode(String nodeId, String image, String executionId) {
		Node node = nodesMap.get(nodeId);
		startNewContainer(node, com.loadcoder.utils.DateTimeUtil.getDateTimeNowString(), image, executionId);
	}

	public void zipAndSendToCache2(File directory) {
		byte[] bytes = zip.zipToBytes(new File("."), "pom.xml", "src", "test.sh");

		PackageSender.performPOSTRequest("https://fiskserver.com:8490/loadship/data", bytes);
//		PackageSender.performPOSTRequest("https://master.loadcoder.com:8490/loadship/data", bytes);

		// PackageSender.performPOSTRequest("https://stefan.laptop.com:8490/loadship/data",
		// bytes);
		// PackageSender.performPOSTRequest("https://registry.master.com:8490/loadship/data",
		// bytes);

		// PackageSender.performPOSTRequest("https://registry.master.com:8490/loadship/data",
		// bytes);
//		PackageSender.performPOSTRequest("https://192.168.1.104:8490/loadship/data", bytes);

		// PackageSender.performPOSTRequest("https://localhost:8490/loadship/data",
		// bytes);

	}

	public void zipAndSendToCache_https(File directory) {
		byte[] bytes = zip.zipToBytes(new File("."), "pom.xml", "src", "test.sh");
		
		PackageSender.performPOSTRequest("https://localhost:6211/loadship/data", bytes);
	}

	public void zipAndSendToCache_http(File directory) {
		byte[] bytes = zip.zipToBytes(new File("."), "pom.xml", "src", "test.sh");

		String url = "http://"+masterNode.getHost() +":" + getConfiguration("loadship.port") + "/loadship/data";
		
		PackageSender.performPOSTRequest(url, bytes);
	}

	public void zipAndSendToCache(File directory) {
		byte[] bytes = zip.zipToBytes(new File("."), "pom.xml", "src", "test.sh");
		PackageSender.performPOSTRequest("https://192.168.1.104:8490/testcache/zip", bytes);
	}

	public void startCluster(int amountOfContainersToStart, String executionId) {
		checkNoRunningContainers();
		removeExitedContainers();
		for (int i = 0; i < amountOfContainersToStart; i++) {
			for (Node node : nodes) {
				startNewContainer(node, "" + i, getConfiguration("testrunner.image"), executionId);
			}
		}
	}
}
