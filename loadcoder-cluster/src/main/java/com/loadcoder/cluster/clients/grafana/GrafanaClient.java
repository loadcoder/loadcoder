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

import static com.loadcoder.statics.Statics.getConfiguration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;
import com.loadcoder.cluster.clients.Header;
import com.loadcoder.cluster.clients.HttpClient;
import com.loadcoder.cluster.clients.HttpResponse;
import com.loadcoder.cluster.clients.docker.DockerClusterClient;
import com.loadcoder.cluster.clients.grafana.dto.Folder;
import com.loadcoder.cluster.clients.influxdb.InfluxDBClient;
import com.loadcoder.result.Result;
import com.loadcoder.utils.DateTimeUtil;

import net.minidev.json.JSONArray;

/**
 * This is an incredibly simlpe Grafana client build just upon java's net
 * package
 */
public class GrafanaClient extends HttpClient {

	private Logger log = LoggerFactory.getLogger(GrafanaClient.class);

	protected static final String GRAFANA_DASHBOARD_TIMESPAN_DATETIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.000";
//	protected static final String GRAFANA_DASHBOARD_TIMESPAN_DATETIMEFORMAT = "yyyy-MM-dd-hh:mm:ss.000";

	protected static final DateTimeFormatter TIMESPAN_FORMAT = DateTimeFormatter.ofPattern(
			GRAFANA_DASHBOARD_TIMESPAN_DATETIMEFORMAT);
	private final String DB_URL_TEMPLATE = "%s://%s:%s/api/dashboards/db";
	private final String DATASOURCES_URL_TEMPLATE = "%s://%s:%s/api/datasources";
	private final String FOLDERS_URL_TEMPLATE = "%s://%s:%s/api/folders";

	private final String DB_URL;
	private final String DATASOURCES_URL;
	private final String FOLDERS_URL;

	private static final long TIMESPAN_MILLIS_PRECREATED_DASHBOARD = 50_000;

	private final String authorizationValue;

	/**
	 * Constructor for the GrafanaClient
	 * 
	 * @param host is the hostname of where Grafana is hosted
	 * @param port is the port where Grafana exposes is API
	 * @param https is boolean if the communcation is encrypted or not. 
	 * @param authorizationValue the value for the HTTP header Authorization used in the request towards
	 * Grafana in order to authenticate the client
	 */
	private GrafanaClient(String host, boolean https, String authorizationValue) {
		String port = getConfiguration("grafana.port");
		String protocol = protocolAsString(https);
		DB_URL = String.format(DB_URL_TEMPLATE, protocol, host, port);
		DATASOURCES_URL = String.format(DATASOURCES_URL_TEMPLATE, protocol, host, port);
		FOLDERS_URL = String.format(FOLDERS_URL_TEMPLATE, protocol, host, port);
		this.authorizationValue = authorizationValue;
	}

	public GrafanaClient(DockerClusterClient dockerClusterClient, boolean https, String authorizationValue) {
		this(dockerClusterClient.getMasterNode().getHost(), https, authorizationValue);
	}
//	public GrafanaClient(String host, String port, boolean https, String authorizationValue) {
//		this(host, Integer.parseInt(port), https, authorizationValue);
//	}

	private static class Type {
		String type;
		String refId;

		private Type(String type, String refId) {
			this.type = type;
			this.refId = refId;
		}
	}

	private List<Type> types = Arrays.asList(new Type("mean", "A"));

	private String getFileAsString(String filename) {
		InputStream in = getClass().getResourceAsStream("/" + filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		List<String> fileAsLineList = new ArrayList<String>();
		reader.lines().forEach(line -> fileAsLineList.add(line));
		try {
			String fileAsString = "";
			for (String line : fileAsLineList) {
				fileAsString = fileAsString + line + "\n";
			}
			return fileAsString;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String getTargets(Set<String> measurementNames, String executionId) {

		String targetTagTemplate = "";
		if (executionId != null) {
			targetTagTemplate = getFileAsString("grafana_5.2.4/grafana_target_tag_body_template.json");
			targetTagTemplate = targetTagTemplate.replace("${executionid}", executionId);

		}
		String targetTemplate = getFileAsString("grafana_5.2.4/grafana_target_body_template.json");

		targetTemplate = targetTemplate.replace("${tags}", targetTagTemplate);

		List<String> targetsList = new ArrayList<String>();
		for (String name : measurementNames) {
			for (Type t : types) {
				String target = targetTemplate.replace("${measurement_name}", name).replace("${refid}", t.refId)
						.replace("${select_type}", t.type);
				targetsList.add(target);
			}
		}
		String result = "";
		for (int i = 0; i < targetsList.size(); i++) {
			String delimiter = i == targetsList.size() - 1 ? "\n" : ",\n";
			result = result + targetsList.get(i) + delimiter;
		}
		return result;
	}

	public HttpResponse createNewDashboardBase(long start, long end, String name, String executionId,
			Set<String> transactionNamesSet, boolean refresh, String datasource, int folderId) {

		long usedEnd = (end - start) < TIMESPAN_MILLIS_PRECREATED_DASHBOARD
				? start + TIMESPAN_MILLIS_PRECREATED_DASHBOARD
				: end;
		String startTimespan = DateTimeUtil.convertMilliSecondsToFormattedDate(start, TIMESPAN_FORMAT);
		String endTimespan = DateTimeUtil.convertMilliSecondsToFormattedDate(usedEnd, TIMESPAN_FORMAT);

		log.debug("panel will have timespan: " + startTimespan + " - " + endTimespan);
		String dateTimeLabel = DateTimeUtil.convertMilliSecondsToFormattedDate(start);
		String fileAsString = getFileAsString("grafana_5.2.4/grafana_post_dashboard_body_template2.json");

		fileAsString = fileAsString.replace("${datasource}", datasource);

		fileAsString = fileAsString.replace("${time_from}", startTimespan);
		fileAsString = fileAsString.replace("${time_to}", endTimespan);
		fileAsString = fileAsString.replace("${title}", name + "_" + dateTimeLabel);

		fileAsString = fileAsString.replace("${refresh}", refresh ? "\"5s\"" : "false");
		fileAsString = fileAsString.replace("${folder.id}", ""+folderId);

		String targets = getTargets(transactionNamesSet, executionId);
		fileAsString = fileAsString.replace("${targets}", targets);
		fileAsString = fileAsString.replace("${requestid}", "" + System.currentTimeMillis());

		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Authorization", authorizationValue));
		HttpResponse resp = sendPost(fileAsString, DB_URL, headers);
		if(resp.getStatusCode() != 200) {
			throw new RuntimeException("Get the following response then trying to create dashboard. Http Status:" + resp.getStatusCode() + "   message:" + resp.getBody());
		}
		return resp;
	}

	
	/**
	 * Create a new Grafana Dashboard.
	 *  
	 * @param folder is the name of the Dashboard folder
	 * @param dashboardName is the name of the Dashboard
	 * @param transactionNames is a list of the transaction names
	 * @param datasource is the same of the datasource
	 * @return the http status code for the Grafana request
	 */
	public HttpResponse createNewDashboard(Folder folder, String dashboardName, List<String> transactionNames, String datasource) {

		Set<String> transactionNamesSet = new HashSet<String>();
		for (String transactionName : transactionNames) {
			transactionNamesSet.add(transactionName);
		}
		long now = System.currentTimeMillis();
		return createNewDashboardBase(now, now, dashboardName, null, transactionNamesSet, true, datasource, folder.getId());
	}

	/**
	 * Create a new Grafana Dashboard.
	 *  
	 * @param name is the name of the Dashboard
	 * @param executionId is the id for a specific set of results, for instance all results for
	 * a particular test execution
	 * @param transactionNames is a list of the transaction names
	 * @return the http status code for the Grafana request
	 */
//	public HttpResponse createNewDashboard(String name, String executionId, List<String> transactionNames, String datasource) {
//
//		Set<String> transactionNamesSet = new HashSet<String>();
//		for (String transactionName : transactionNames) {
//			transactionNamesSet.add(transactionName);
//		}
//		long now = System.currentTimeMillis();
//		return createNewDashboardBase(now, now, name, executionId, transactionNamesSet, true, datasource);
//	}

	/**
	 * Create a new Grafana Dashboard.
	 * 
	 * @param name is the name of the Dashboard
	 * @param result is the Result of an already executed test
	 * @return the http status code for the Grafana request
	 */
	public HttpResponse createNewDashboardFromResult(String name, Result result, String datasource) {
		return createNewDashboardBase(result.getStart(), result.getEnd(), name, null, result.getResultLists().keySet(),
				false, datasource, -1);
	}

	/**
	 * Create a new Grafana Dashboard.
	 * 
	 * @param name is the name of the Dashboard
	 * @param executionId is the id for a specific set of results, for instance all results for
	 * a particular test execution
	 * @param result is the Result of an already executed test
	 * @param datasource is the datasource
	 * @return the http status code for the Grafana request
	 */
	public HttpResponse createNewDashboardFromResult(String name, String executionId, Result result, String datasource) {
		return createNewDashboardBase(result.getStart(), result.getEnd(), name, executionId,
				result.getResultLists().keySet(), false, datasource, -1);
	}

	public List<String> listDataSources() {
		List<String> result = new ArrayList<String>();
		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Accept", "application/json"),
				new Header("Authorization", authorizationValue));
		HttpResponse resp = sendGet(DATASOURCES_URL, headers);
		
		JSONArray array = JsonPath.read(resp.getBody(), "[*]['name']");
		array.stream().forEach(dataSourceNames ->{
			result.add(dataSourceNames.toString());
		});
		
		return result;
//		return sendGet("http://loadcoder.com", headers);
	}

	public List<Folder> listDashboardFolders() {
		List<Folder> result = new ArrayList<>();
		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Accept", "application/json"),
				new Header("Authorization", authorizationValue));
		HttpResponse resp = sendGet(FOLDERS_URL, headers);
		Map<String,String> d;
		ArrayList<Map<String, Object>>  array = JsonPath.read(resp.getBody(), "[*]");
		for(Map<String, Object> ar :array) {
			
			Folder f = new Folder(ar.get("title").toString(), (int)ar.get("id"), ar.get("uid").toString());
			result.add(f);
			
		}
		
		return result;
//		return sendGet("http://loadcoder.com", headers);
	}
	
	public HttpResponse createDataSource(String datasource) {
		String fileAsString = getFileAsString("grafana_5.2.4/grafana_post_create_datasource.json");

		fileAsString = fileAsString.replace("${datasource}", datasource);

		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Authorization", authorizationValue));
		return sendPost(fileAsString, DATASOURCES_URL, headers);
	}

	public Folder createDashboardFolder(String folderName) {
		String fileAsString = getFileAsString("grafana_5.2.4/grafana_post_create_folder.json");

		fileAsString = fileAsString.replace("${uid}", ""+System.currentTimeMillis());
		fileAsString = fileAsString.replace("${title}", folderName);

		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Authorization", authorizationValue));
		HttpResponse resp =  sendPost(fileAsString, FOLDERS_URL, headers);
		String respBody = resp.getBody();
		Map<String, Object> map = JsonPath.<Map<String, Object>>read(respBody, "$");
		Folder f = new Folder(map.get("title").toString(), (int)(map.get("id")), map.get("uid").toString());
		return f;
	}

	public void createGrafanaDashboard(String groupName, String testName, String executionIdRegexp, InfluxDBClient influxClient) {
		createGrafanaDashboard(groupName, testName, executionIdRegexp, getConfiguration("grafana.port"), influxClient);
	}
	
	private void createGrafanaDashboard(String groupName, String testName, String executionIdRegexp, String grafanaPort,
			InfluxDBClient influxClient) {

		String dbName = influxClient.getDatabaseName();
		
		String measurement = null;
		List<String> measurements = influxClient.showMeasurements();
		for(String m : measurements) {
			if(m.matches(executionIdRegexp)) {
				measurement = m;
			}
		}
		if(measurement == null) {
			throw new RuntimeException("There was no measurement with name matching " + executionIdRegexp + " in the database " + dbName);
		}
		
		List<String> transaction = influxClient.listDistinctTransactions(measurement);
		
		Folder folder = null;
		List<Folder> folders = listDashboardFolders();
		for(Folder f : folders) {
			if(f.getName().equals(groupName)) {
				folder = f;
				break;
			}
		}
		if(folder == null) {
			folder = createDashboardFolder(groupName);
		}

		List<String> dataSources = listDataSources();
		if(!dataSources.contains(dbName)) {
			createDataSource(dbName);
		}
		
		createNewDashboard(folder, testName, transaction, dbName);
	}
	
}