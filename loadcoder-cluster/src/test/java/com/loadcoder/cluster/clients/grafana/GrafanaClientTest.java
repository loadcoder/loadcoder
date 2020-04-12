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
package com.loadcoder.cluster.clients.grafana;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.loadcoder.cluster.clients.HttpResponse;
import com.loadcoder.cluster.clients.docker.DockerClusterClient;
import com.loadcoder.cluster.clients.grafana.dto.Folder;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.utils.DateTimeUtil;

public class GrafanaClientTest extends TestNGBase {

	/**
	 * This test creates a new dashbord in Grafana. Since Grafana needs to be
	 * available, this test is manual Run the test, then log in to Grafana and see
	 * if a Dashboard has been created.
	 */
	@Test(groups = "manual")
	public void listAndCreateDataSource(Method method) {

		DockerClusterClient dockerClusterClient = new DockerClusterClient();
		// base64 encoded default grafana user:password
		String authorizationValue = "Basic YWRtaW46YWRtaW4=";
		GrafanaClient cli = new GrafanaClient(dockerClusterClient, false, authorizationValue);
		List<String> responseBody = cli.listDataSources();

		HttpResponse responseCode = cli.createDataSource("listAndCreateDataSource2");
		assertEquals(responseCode, 200);
	}

	/**
	 * This test creates a new dashbord in Grafana. Since Grafana needs to be
	 * available, this test is manual Run the test, then log in to Grafana and see
	 * if a Dashboard has been created.
	 */
	@Test(groups = "manual")
	public void createDashboard(Method method) {

		DockerClusterClient dockerClusterClient = new DockerClusterClient();
		// base64 encoded default grafana user:password
		String authorizationValue = "Basic YWRtaW46YWRtaW4=";
		GrafanaClient cli = new GrafanaClient(dockerClusterClient, false, authorizationValue);

		List<Folder> folders = cli.listDashboardFolders();
		HttpResponse responseCode = cli.createNewDashboard(folders.get(0), method.getName(), Arrays.asList("foo"),
				"bar");
		assertEquals(responseCode.getStatusCode(), 200);

	}

	@Test
	public void testCalendarUtil(Method method) {

		String dateTime = DateTimeUtil.convertMilliSecondsToFormattedDate(System.currentTimeMillis(),
				GrafanaClient.TIMESPAN_FORMAT);
		assertTrue(dateTime.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.000"));
	}

}
