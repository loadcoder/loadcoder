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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;

public class Node {

	String id;
	String host;
	String containerHost;
	String port;

	DockerClient dockerClient;

	public Node(String id, String host, String port) {
		this.id = id;
		this.host = host;
		this.port = port;
	}

	public String getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public DockerClient getDockerClient() {
		synchronized (this){
			if(dockerClient == null) {
				this.dockerClient = DockerClientBuilder.getInstance("tcp://" + host + ":" + port).build();
			}
		}
		return this.dockerClient;
	}

}
