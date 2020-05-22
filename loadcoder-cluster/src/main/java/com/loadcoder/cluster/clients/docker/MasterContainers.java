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

import java.util.HashMap;
import java.util.Map;

import com.loadcoder.statics.Configuration;
import com.loadcoder.statics.Statics;

import static com.loadcoder.statics.Statics.*;
public enum MasterContainers{
	

	
	LOADSHIP(()->{
		Map<String, String> loadshipMap = new HashMap<>();
		loadshipMap.put("HTTP_ENABLED", "true");
		loadshipMap.put("MODECHOOSER", "LOADSHIP");
		return loadshipMap;
		}, 6210), 
	INFLUXDB(()-> null, 8086),
	GRAFANA(()-> null, 3000),
	ARTIFACTORY(()-> null, 8081);

	Configuration config = Configuration.getConfigurationInstance();
	final Map<String, String> loadshipMap;
	final int port;
	
	MasterContainers(CreateEnvironmentVariableMap masterContainerSetupable, int port){
		this.loadshipMap = masterContainerSetupable.createEnvironmentVariableMap();
		this.port = port;
	}
	
	protected void setConfiguration(Configuration config) {
		this.config = config;
	}

//	public String getHost(DockerClusterClient dockerClusterClient){
//		String configVariableName = name().toLowerCase() + ".host";
//		String s = config.getConfiguration(configVariableName);
//		return s == null ? dockerClusterClient.getMasterNode().getHost() : s;
//	}
//	
//	public String getInternalHost(DockerClusterClient dockerClusterClient){
//		String configVariableName = name().toLowerCase() + ".internal.host";
//		String s = config.getConfiguration(configVariableName);
//		return s == null ? dockerClusterClient.getMasterNode().getInternalHost() : s;
//	}
	
	public String getPort(){
		String configVariableName = name().toLowerCase() + ".port";
		String s = config.getConfiguration(configVariableName);
		return s == null ? getExposedPort() : s;
	}
	
	public String getExposedPort(){
		String configVariableName = name().toLowerCase() + ".exposed.port";
		String s = config.getConfiguration(configVariableName);
		return s == null ? getServerPort() : s;
	}
	
	public String getServerPort(){
		String configVariableName = name().toLowerCase() + ".server.port";
		String s = config.getConfiguration(configVariableName);
		return s == null ? ""+this.port : s;
	}
	
	void setup(DockerClusterClient client){
		client.setupMasterContainer(this.name().toLowerCase(), loadshipMap, "" + port);
	}
	
	private interface CreateEnvironmentVariableMap{
		Map<String, String> createEnvironmentVariableMap();
	}
}