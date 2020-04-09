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
package com.loadcoder.cluster.clients.grafana;

import com.loadcoder.cluster.clients.docker.DockerClusterClient;
import com.loadcoder.cluster.clients.influxdb.InfluxDBClient;

public class GrafanaHelper {

	public static void createGrafanaDashboard(DockerClusterClient dockerClusterClient, String groupName, String testName, String executionIdRegexp) {
		
		String authenticationValue = "Basic YWRtaW46YWRtaW4=";
		
		InfluxDBClient incluxDBClient = new InfluxDBClient(dockerClusterClient, groupName, testName);
		GrafanaClient grafanaClient = new GrafanaClient(dockerClusterClient, false, authenticationValue);
		
		grafanaClient.createGrafanaDashboard(groupName, testName, executionIdRegexp, incluxDBClient);
	}
}
