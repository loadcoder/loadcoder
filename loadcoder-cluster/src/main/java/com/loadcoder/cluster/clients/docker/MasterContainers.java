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

import java.util.HashMap;
import java.util.Map;

import com.loadcoder.cluster.clients.docker.DockerClusterClient.MasterContainerSetupable;

public enum MasterContainers{
	
	
	LOADSHIP((client)->{
		Map<String, String> loadshipMap = new HashMap<>();
		loadshipMap.put("HTTP_ENABLED", "true");
		loadshipMap.put("MODECHOOSER", "LOADSHIP");
		client.setupMasterContainer("loadship", loadshipMap, "6210");}), 
	INFLUXDB((client)->{client.setupMasterContainer("influxdb", null, "8086");}),
	GRAFANA((client)->{client.setupMasterContainer("grafana", null, "3000");}),
	ARTIFACTORY((client)->{client.setupMasterContainer("artifactory", null, "8081");});

	MasterContainerSetupable masterContainerSetupable;
	
	MasterContainers(MasterContainerSetupable masterContainerSetupable){
		this.masterContainerSetupable = masterContainerSetupable;
	}
	
	MasterContainerSetupable getMasterContainerSetupable(){
		return masterContainerSetupable;
	}
}