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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import com.loadcoder.cluster.clients.docker.MasterContainers;
import com.loadcoder.cluster.clients.docker.StartedClusterExecution;
import com.loadcoder.cluster.clients.grafana.dto.DashboardSearchResult;
import com.loadcoder.cluster.clients.grafana.dto.Folder;
import com.loadcoder.cluster.clients.influxdb.InfluxDBClient;
import com.loadcoder.result.Result;
import com.loadcoder.statics.Configuration;
import com.loadcoder.utils.DateTimeUtil;

import net.minidev.json.JSONArray;

/**
 * This is an incredibly simlpe Grafana client build just upon java's net
 * package
 */
public class GrafanaClient extends HttpClient {

	private Logger log = LoggerFactory.getLogger(GrafanaClient.class);

	protected static final String GRAFANA_DASHBOARD_TIMESPAN_DATETIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.000";

	protected static final DateTimeFormatter TIMESPAN_FORMAT = DateTimeFormatter
			.ofPattern(GRAFANA_DASHBOARD_TIMESPAN_DATETIMEFORMAT);

	String PROTOCOL = protocolAsString(false);
	final String GRAFANA_HOST;
	final String GRAFANA_PORT;

	private final String DB_URL_TEMPLATE = "%s://%s:%s/api/dashboards/db";
	private final String DATASOURCES_URL_TEMPLATE = "%s://%s:%s/api/datasources";
	private final String FOLDERS_URL_TEMPLATE = "%s://%s:%s/api/folders";
	private final String SEARCH_URL_TEMPLATE = "%s://%s:%s/api/search?type=dash-db";

	private final String DB_URL;
	private final String DATASOURCES_URL;
	private final String FOLDERS_URL;
	private final String SEARCH_URL;

	private static final long TIMESPAN_MILLIS_PRECREATED_DASHBOARD = 50_000;

	private final String authorizationValue;

	private final String dataSourceInfluxDBHost;
	private final String dataSourceInfluxDBHostPort;
	private final InfluxDBClient influxClient;

	private final Configuration config;

	/**
	 * Constructor for the GrafanaClient
	 * 
	 * @param grafanaHost            is the hostname of where Grafana is hosted
	 * @param dataSourceInfluxDBHost is the host that grafana will use in the
	 *                               datasource endpoint.
	 * @param authorizationValue     the value for the HTTP header Authorization
	 *                               used in the request towards
	 * @param influxClient           is the InfluxDB client
	 * @param config                 is the Configuration for the Loadcoder cluster
	 *                               Grafana in order to authenticate the client
	 */
	public GrafanaClient(String grafanaHost, String dataSourceInfluxDBHost, String authorizationValue,
			InfluxDBClient influxClient, Configuration config) {
		this.config = config;
		String port = MasterContainers.GRAFANA.getPort(config);
		this.dataSourceInfluxDBHostPort = MasterContainers.INFLUXDB.getExposedPort(config);

		this.dataSourceInfluxDBHost = dataSourceInfluxDBHost;
		this.GRAFANA_HOST = grafanaHost;
		this.GRAFANA_PORT = port;

		DB_URL = String.format(DB_URL_TEMPLATE, PROTOCOL, grafanaHost, port);
		DATASOURCES_URL = String.format(DATASOURCES_URL_TEMPLATE, PROTOCOL, grafanaHost, port);
		FOLDERS_URL = String.format(FOLDERS_URL_TEMPLATE, PROTOCOL, grafanaHost, port);
		SEARCH_URL = String.format(SEARCH_URL_TEMPLATE, PROTOCOL, grafanaHost, port);
		this.authorizationValue = authorizationValue;
		this.influxClient = influxClient;
	}

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

	private HttpResponse createNewDashboardBase(long start, long end, String name, String executionId,
			Set<String> transactionNamesSet, boolean refresh, String datasource, int folderId) {

		long usedEnd = (end - start) < TIMESPAN_MILLIS_PRECREATED_DASHBOARD
				? start + TIMESPAN_MILLIS_PRECREATED_DASHBOARD
				: end;
		String startTimespan = DateTimeUtil.convertMilliSecondsToFormattedDate(start, TIMESPAN_FORMAT);
		String endTimespan = DateTimeUtil.convertMilliSecondsToFormattedDate(usedEnd, TIMESPAN_FORMAT);

		log.debug("panel will have timespan: " + startTimespan + " - " + endTimespan);
		String dateTimeLabel = DateTimeUtil.convertMilliSecondsToFormattedDate(start);
		String fileAsString = getFileAsString("grafana_5.2.4/grafana_post_dashboard_body_template.json");

		fileAsString = fileAsString.replace("${datasource}", datasource);

		fileAsString = fileAsString.replace("${time_from}", startTimespan);
		fileAsString = fileAsString.replace("${time_to}", endTimespan);
		fileAsString = fileAsString.replace("${title}", name + "_" + dateTimeLabel);

		fileAsString = fileAsString.replace("${refresh}", refresh ? "\"5s\"" : "false");
		fileAsString = fileAsString.replace("${folder.id}", "" + folderId);

		String targets = "";

		String targetTemplate = getFileAsString("grafana_5.2.4/target_body.json");
		List<String> targetList = new ArrayList<>();
		transactionNamesSet.stream().forEach(transactionId -> {

			String newTarget = targetTemplate.replace("${transactionid}", transactionId);
			targetList.add(newTarget);
		});

		for (int i = 0; i < targetList.size(); i++) {
			targets += targetList.get(i);
			if (i != targetList.size() - 1) {
				targets += ",";
			}
		}

		fileAsString = fileAsString.replace("${targets}", targets);
		fileAsString = fileAsString.replace("${requestid}", "" + System.currentTimeMillis());

		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Authorization", authorizationValue));
		HttpResponse resp = sendPost(fileAsString, DB_URL, headers);
		if (resp.getStatusCode() != 200) {
			throw new RuntimeException("Get the following response then trying to create dashboard. Http Status:"
					+ resp.getStatusCode() + "   message:" + resp.getBody());
		}
		return resp;
	}

	/**
	 * Create a new Grafana Dashboard.
	 * 
	 * @param folder           is the name of the Dashboard folder
	 * @param dashboardName    is the name of the Dashboard
	 * @param transactionNames is a list of the transaction names
	 * @param datasource       is the same of the datasource
	 * @return the http status code for the Grafana request
	 */
	public HttpResponse createNewDashboard(Folder folder, String dashboardName, List<String> transactionNames,
			String datasource) {

		Set<String> transactionNamesSet = new HashSet<String>();
		for (String transactionName : transactionNames) {
			transactionNamesSet.add(transactionName);
		}
		long now = System.currentTimeMillis();
		return createNewDashboardBase(now, now, dashboardName, null, transactionNamesSet, true, datasource,
				folder.getId());
	}

	/**
	 * Create a new Grafana Dashboard.
	 * 
	 * @param name       is the name of the Dashboard
	 * @param result     is the Result of an already executed test
	 * @param datasource the dataSource to be used
	 * @return the http status code for the Grafana request
	 */
	public HttpResponse createNewDashboardFromResult(String name, Result result, String datasource) {
		return createNewDashboardBase(result.getStart(), result.getEnd(), name, null, result.getResultLists().keySet(),
				false, datasource, -1);
	}

	/**
	 * Create a new Grafana Dashboard.
	 * 
	 * @param name        is the name of the Dashboard
	 * @param executionId is the id for a specific set of results, for instance all
	 *                    results for a particular test execution
	 * @param result      is the Result of an already executed test
	 * @param datasource  is the datasource
	 * @return the http status code for the Grafana request
	 */
	public HttpResponse createNewDashboardFromResult(String name, String executionId, Result result,
			String datasource) {
		return createNewDashboardBase(result.getStart(), result.getEnd(), name, executionId,
				result.getResultLists().keySet(), false, datasource, -1);
	}

	public List<String> listDataSources() {
		List<String> result = new ArrayList<String>();
		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Accept", "application/json"), new Header("Authorization", authorizationValue));
		HttpResponse resp = sendGet(DATASOURCES_URL, headers);

		JSONArray array = JsonPath.read(resp.getBody(), "[*]['name']");
		array.stream().forEach(dataSourceNames -> {
			result.add(dataSourceNames.toString());
		});

		return result;
	}

	public List<Folder> listDashboardFolders() {
		List<Folder> result = new ArrayList<>();
		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Accept", "application/json"), new Header("Authorization", authorizationValue));
		HttpResponse resp = sendGet(FOLDERS_URL, headers);
		ArrayList<Map<String, Object>> array = JsonPath.read(resp.getBody(), "[*]");
		for (Map<String, Object> ar : array) {

			Folder f = new Folder(ar.get("title").toString(), (int) ar.get("id"), ar.get("uid").toString());
			result.add(f);

		}

		return result;
	}

	public List<DashboardSearchResult> search() {
		List<DashboardSearchResult> result = new ArrayList<>();
		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Accept", "application/json"), new Header("Authorization", authorizationValue));
		HttpResponse resp = sendGet(SEARCH_URL, headers);
		ArrayList<Map<String, Object>> array = JsonPath.read(resp.getBody(), "[*]");
		for (Map<String, Object> ar : array) {

			DashboardSearchResult f = new DashboardSearchResult(ar.get("uid").toString(), ar.get("title").toString(),
					ar.get("uri").toString(), ar.get("url").toString(), ar.get("folderTitle").toString(),
					ar.get("folderUrl").toString());
			result.add(f);
		}

		return result;
	}

	public HttpResponse createDataSource(String datasource) {
		String fileAsString = getFileAsString("grafana_5.2.4/grafana_post_create_datasource.json");
		fileAsString = fileAsString.replace("${influxDBHost}", dataSourceInfluxDBHost);

		fileAsString = fileAsString.replace("${influxDBPort}", dataSourceInfluxDBHostPort);

		fileAsString = fileAsString.replace("${datasource}", datasource);

		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Authorization", authorizationValue));
		return sendPost(fileAsString, DATASOURCES_URL, headers);
	}

	public Folder createDashboardFolder(String folderName) {
		String fileAsString = getFileAsString("grafana_5.2.4/grafana_post_create_folder.json");

		fileAsString = fileAsString.replace("${uid}", "" + System.currentTimeMillis());
		fileAsString = fileAsString.replace("${title}", folderName);

		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Authorization", authorizationValue));
		HttpResponse resp = sendPost(fileAsString, FOLDERS_URL, headers);
		String respBody = resp.getBody();
		Map<String, Object> map = JsonPath.<Map<String, Object>>read(respBody, "$");
		Folder f = new Folder(map.get("title").toString(), (int) (map.get("id")), map.get("uid").toString());
		return f;
	}

	public void createGrafanaDashboard(String executionIdRegexp) {
		String measurement = matchMeaurement(executionIdRegexp);
		createGrafanaDashboard(measurement, config.getConfiguration("grafana.port"));
	}

	public void createGrafanaDashboard() {
		String measurement = getLatestMeaurement();
		createGrafanaDashboard(measurement, config.getConfiguration("grafana.port"));
	}

	public String matchMeaurement(String executionIdRegexp) {
		String measurement = null;

		List<String> measurements = influxClient.showMeasurements();
		for (String m : measurements) {
			if (m.matches(executionIdRegexp)) {
				measurement = m;
			}
		}
		if (measurement == null) {
			throw new RuntimeException("There was no measurement with name matching " + executionIdRegexp
					+ " in the database " + influxClient.getDatabaseName());
		}
		return measurement;
	}

	class Pair {
		String measurement;
		Date date;

		Pair(String m, Date d) {
			this.measurement = m;
			this.date = d;
		}
	}

	public String getLatestMeaurement() {
		List<Pair> dates = new ArrayList<>();
		List<String> measurements = influxClient.showMeasurements();
		for (String m : measurements) {
			try {
				Date d = DateTimeUtil.getAsDate(m);
				dates.add(new Pair(m, d));

			} catch (ParseException pe) {
				log.debug("Measurement " + m + " in influxDB database " + influxClient.getDatabaseName()
						+ "cant be parsed as a Date with the default pattern "
						+ DateTimeUtil.getDefaultDateTimeFormat());
			}
		}

		if (dates.isEmpty()) {
			if (measurements.isEmpty()) {
				throw new RuntimeException(
						"There are no measurements in influxDB database" + influxClient.getDatabaseName());
			} else {
				return measurements.get(0);
			}
		}
		dates.sort((dateA, dateB) -> dateB.date.compareTo(dateA.date));
		String latestMeasurement = dates.get(0).measurement;
		return latestMeasurement;
	}

	private void createGrafanaDashboard(String measurement, String grafanaPort) {

		String dbName = influxClient.getDatabaseName();

		List<String> transaction = influxClient.listDistinctTransactions(measurement);

		Folder folder = null;
		List<Folder> folders = listDashboardFolders();
		for (Folder f : folders) {
			if (f.getName().equals(influxClient.getTestGroup())) {
				folder = f;
				break;
			}
		}
		if (folder == null) {
			folder = createDashboardFolder(influxClient.getTestGroup());
		}

		List<String> dataSources = listDataSources();
		if (!dataSources.contains(dbName)) {
			createDataSource(dbName);
		}

		createNewDashboard(folder, influxClient.getTestName(), transaction, dbName);
	}

	public GrafanaDashboardLinks getDashboardLinks(StartedClusterExecution startedExecution) {

		String urlTemplate = "%s://%s:%s%s?refresh=5s&orgId=1&var-Execution=%s";

		DashboardSearchResult foundDashboard = searchAndFindCorrectDashboard();
		String url = String.format(urlTemplate, PROTOCOL, GRAFANA_HOST, GRAFANA_PORT, foundDashboard.getUrl(),
				startedExecution.getExecutionId());

		GrafanaDashboardLinks links = new GrafanaDashboardLinks(url, url);
		return links;
	}

	private DashboardSearchResult searchAndFindCorrectDashboard() {
		List<DashboardSearchResult> searchResult = search();
		DashboardSearchResult foundDashboard = null;
		String titleToFind = influxClient.getTestName();
		String folderToFind = influxClient.getTestGroup();
		for (DashboardSearchResult dashboardSearchResult : searchResult) {

			if (matchTitleWithTestNamePattern(titleToFind, dashboardSearchResult.getTitle())
					&& dashboardSearchResult.getFolderTitle().equals(folderToFind)) {
				foundDashboard = dashboardSearchResult;
			}
		}
		if (foundDashboard == null) {
			throw new RuntimeException("There are no dashboard in folder " + folderToFind
					+ " and dashboard with a title match testname" + titleToFind);
		}
		return foundDashboard;
	}

	protected boolean matchTitleWithTestNamePattern(String testName, String titleFoundInSearch) {
		return titleFoundInSearch.matches(testName + "_[0-9]{8}-[0-9]{6}");
	}
}
