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
package com.loadcoder.cluster.clients;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.loadcoder.cluster.clients.docker.DockerClusterClient;
import com.loadcoder.cluster.clients.grafana.GrafanaClient;
import com.loadcoder.cluster.clients.grafana.dto.Folder;

public class GrafanaAPITest {

	@Test(groups = "manual")
	void createFolderTest(){
		String authenticationValue = "Basic YWRtaW46YWRtaW4=";
		DockerClusterClient dockerClusterClient = new DockerClusterClient();

		GrafanaClient grafanaClient = new GrafanaClient(dockerClusterClient, false, authenticationValue);
		
		String folderName = "TestFolder" + System.currentTimeMillis();
		Folder resp = grafanaClient.createDashboardFolder(folderName);
		assertEquals(resp.getName(), folderName);
	}

	@Test(groups = "manual")
	void testListFolders(){
		String authenticationValue = "Basic YWRtaW46YWRtaW4=";
		DockerClusterClient dockerClusterClient = new DockerClusterClient();
		GrafanaClient grafanaClient = new GrafanaClient(dockerClusterClient, false, authenticationValue);
		List<Folder> resp = grafanaClient.listDashboardFolders();
	}
}
