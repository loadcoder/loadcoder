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
import static org.testng.Assert.assertFalse;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.loadcoder.cluster.clients.HttpResponse;
import com.loadcoder.cluster.clients.docker.LoadcoderCluster;
import com.loadcoder.cluster.clients.grafana.dto.Folder;
import com.loadcoder.cluster.clients.influxdb.InfluxDBClient;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.utils.DateTimeUtil;

public class GrafanaClientTest extends TestNGBase {

	@Test(groups = "manual")
	void createFolderTest() {
		LoadcoderCluster dockerClusterClient = new LoadcoderCluster();
		InfluxDBClient influxDB = Mockito.mock(InfluxDBClient.class);
		GrafanaClient grafanaClient = dockerClusterClient.getGrafanaClient(influxDB);
		String folderName = "TestFolder" + System.currentTimeMillis();
		Folder resp = grafanaClient.createDashboardFolder(folderName);
		assertEquals(resp.getName(), folderName);
	}

	@Test(groups = "manual")
	void testListFolders() {
		LoadcoderCluster dockerClusterClient = new LoadcoderCluster();
		InfluxDBClient influxDB = Mockito.mock(InfluxDBClient.class);
		GrafanaClient grafanaClient = dockerClusterClient.getGrafanaClient(influxDB);
		List<Folder> resp = grafanaClient.listDashboardFolders();
		resp.forEach(folder -> System.out.println(folder.getName()));
	}

	/**
	 * This test creates a new dashbord in Grafana. Since Grafana needs to be
	 * available, this test is manual Run the test, then log in to Grafana and see
	 * if a Dashboard has been created.
	 */
	@Test(groups = "manual")
	public void listAndCreateDataSource(Method method) {

		LoadcoderCluster dockerClusterClient = new LoadcoderCluster();
		InfluxDBClient influxDB = Mockito.mock(InfluxDBClient.class);
		GrafanaClient grafanaClient = dockerClusterClient.getGrafanaClient(influxDB);

		grafanaClient.listDataSources();
		String dataSourceName = "GrafanaClientTest" + System.currentTimeMillis();
		HttpResponse responseCode = grafanaClient.createDataSource(dataSourceName);
		assertEquals(responseCode.getStatusCode(), 200);
	}

	/**
	 * This test creates a new dashbord in Grafana. Since Grafana needs to be
	 * available, this test is manual Run the test, then log in to Grafana and see
	 * if a Dashboard has been created.
	 */
	@Test(groups = "manual")
	public void createDashboard(Method method) {

		LoadcoderCluster dockerClusterClient = new LoadcoderCluster();
		// base64 encoded default grafana user:password
		InfluxDBClient influxDB = Mockito.mock(InfluxDBClient.class);
		GrafanaClient grafanaClient = dockerClusterClient.getGrafanaClient(influxDB);

		List<Folder> folders = grafanaClient.listDashboardFolders();
		HttpResponse responseCode = grafanaClient.createNewDashboard(folders.get(0), method.getName(),
				Arrays.asList("foo"), "bar");
		assertEquals(responseCode.getStatusCode(), 200);

	}

	@Test
	public void testCalendarUtil(Method method) {

		String dateTime = DateTimeUtil.convertMilliSecondsToFormattedDate(System.currentTimeMillis(),
				GrafanaClient.TIMESPAN_FORMAT);
		assertTrue(dateTime.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.000"));
	}

	@Test
	public void testMatchTitleWithTestNamePattern() {
		LoadcoderCluster dockerClusterClient = new LoadcoderCluster();
		InfluxDBClient influxDB = Mockito.mock(InfluxDBClient.class);
		GrafanaClient grafanaClient = dockerClusterClient.getGrafanaClient(influxDB);
		boolean result = grafanaClient.matchTitleWithTestNamePattern("testName", "testName_20200101-080101");
		assertTrue(result);

		result = grafanaClient.matchTitleWithTestNamePattern("testName", "testName_20200101-080101-080101");
		assertFalse(result);

		result = grafanaClient.matchTitleWithTestNamePattern("testName", "testNamee_20200101-080101");
		assertFalse(result);

		result = grafanaClient.matchTitleWithTestNamePattern("testName", "ttestName_20200101-080101");
		assertFalse(result);

		result = grafanaClient.matchTitleWithTestNamePattern("testNamee", "testName_20200101-080101");
		assertFalse(result);

		result = grafanaClient.matchTitleWithTestNamePattern("ttestName", "testName_20200101-080101");
		assertFalse(result);
	}

}
