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
package com.loadcoder;

import java.io.File;

import com.loadcoder.cluster.clients.docker.LoadcoderCluster;
import com.loadcoder.cluster.clients.grafana.GrafanaClient;

public class ControllerCustomHost{

	public static void main(String[] args) {
		LoadcoderCluster client = new LoadcoderCluster();
		// Creates and starts Grafana, InfluxDB, Loadship and also Artifactory
		client.setupMaster();

		// Send this Maven project as a zip file to the Loadship server
//		client.uploadTest(new File("."));

		// Start a new clustered Loadcoder test
//		client.startNewExecution(1);

//		client.stopExecution();

		// Create a Grafana Dashboard based on the data that the test wrote to InfluxDB
//		GrafanaClient grafana = client
//				.getGrafanaClient(client.getInfluxDBClient("LoadcoderClusterTests", "InfluxReportTest"));
//		grafana.createGrafanaDashboard("2020.*");

		// Stops and removes Grafana, InfluxDB and Loadship
//		client.stopAndRemoveAllMasterContainers();

	}
}
