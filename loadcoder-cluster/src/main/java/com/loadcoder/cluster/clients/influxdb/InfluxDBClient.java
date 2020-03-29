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
package com.loadcoder.cluster.clients.influxdb;

import static com.loadcoder.statics.Statics.getConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.loadcoder.cluster.clients.HttpClient;
import com.loadcoder.cluster.clients.HttpResponse;
import com.loadcoder.cluster.clients.docker.DockerClusterClient;
import com.loadcoder.cluster.clients.influxdb.InfluxDBClient.InfluxDBTestExecution;
import com.loadcoder.load.scenario.RuntimeResultConsumer;
import com.loadcoder.network.CodeGeneratable;
import com.loadcoder.result.TransactionExecutionResult;
import com.loadcoder.utils.DateTimeUtil;
import com.loadcoder.utils.FileUtil;

import net.minidev.json.JSONArray;

public class InfluxDBClient extends HttpClient{

	static Logger log = LoggerFactory.getLogger(InfluxDBClient.class);
	private final String dbName;

	private final static String WRITE_URL_TEMPLATE = "%s://%s:%s/write?db=";
	private final static String QUERY_URL_TEMPLATE = "%s://%s:%s/query";
	private final static String DB_NAME_TEMPLATE = "%s__%s";
	
	public static CodeGeneratable influxReporter(String groupName, String testName) {
		return codeTemplate -> InfluxDBClient.generateCodeCallStoreAndConsumeResultRuntime(codeTemplate, groupName, testName);
	}
	
	private final static String WRITE_ENTRY_BODY_TEMPLATE = "%s,transaction=%s,status=%s value=%s %s000000";
	
	private final String WRITE_URL;
	private final String QUERY_URL;
	 
	/**
	 * Constructor for the InfluxDBClient
	 * @param host is the host for where the influx DB is hosted
	 * @param port is the port where the influx DB exposes its API
	 * @param https is boolean for whether or not the communication is encrypted or not.
	 * @param db is the name of the database inside the influx db, containig the results
	 */
	public InfluxDBClient(String host, int port, boolean https, String db) {
		String protocol = protocolAsString(https);
		WRITE_URL = String.format(WRITE_URL_TEMPLATE, protocol, host, port);
		QUERY_URL = String.format(QUERY_URL_TEMPLATE, protocol, host, port);
		this.dbName = db;
	}
	
	public InfluxDBClient(DockerClusterClient client, String testGroup, String testName) {
		this(client.getMasterNode().getHost(), getConfiguration("influxdb.port"), false, String.format(DB_NAME_TEMPLATE, testGroup, testName));
	}

	public InfluxDBClient(String host, String port, boolean https, String db) {
		this(host, Integer.parseInt(port), https, db);
	}
	
	public static InfluxDBClient getInfluxDBClient(DockerClusterClient dockerClusterClient, String dbName) {
		InfluxDBClient influxClient = new InfluxDBClient(
				dockerClusterClient.getMasterNode().getHost(),
				getConfiguration("influxdb.port"), false, dbName);
		return influxClient;
	}
	
	public static RuntimeResultConsumer setupInfluxDataConsumer(DockerClusterClient dockerClusterClient, String testGroup, String testName) {
		
		String dbName = String.format(DB_NAME_TEMPLATE, testGroup, testName);
		
		InfluxDBClient influxClient = new InfluxDBClient(dockerClusterClient.getMasterNode().getHost(), getConfiguration("influxdb.port"), false, dbName);
		List<String> databases = influxClient.listDatabases();
		if(!databases.contains(dbName)) {
			
			influxClient.createDB();
		}

		String executionId = getConfiguration("LOADCODER_EXECUTION_ID");
		if(executionId == null) {
			executionId = DateTimeUtil.getDateTimeNowString();
		}
		InfluxDBTestExecution exe = influxClient.createTestExecution(executionId);
		log.info("Using executionId:" + executionId);

		RuntimeResultConsumer influxConsumer = (res) -> exe.writeTransactions(res);
		return influxConsumer;
	}
	
	public String getDatabaseName() {
		return this.dbName;
	}
	public HttpResponse createDB() {
		log.info("Creating new InfluxDB database:" + dbName);
		String body = String.format("q=CREATE DATABASE %s", dbName);
		return sendPost(body, QUERY_URL, Arrays.asList());
	}

	public List<String> showMeasurements() {
		List<String> result = new ArrayList<String>();
		String body = String.format("q=SHOW MEASUREMENTS ON %s", dbName);
		HttpResponse resp = sendPost(body, QUERY_URL, Arrays.asList());
		String respBody = resp.getBody();
		JSONArray array = JsonPath.read(respBody, "$['results'][0]['series'][0]['values'][*][*]");
		array.stream().forEach(db -> {
			result.add(db.toString());
		});
		return result;
	}
	
	public List<String> listDatabases() {
		List<String> result = new ArrayList<String>();
		String body = String.format("q=SHOW DATABASES");
		HttpResponse resp = sendPost(body, QUERY_URL, Arrays.asList());
		String respBody = resp.getBody();
		
		JSONArray array = JsonPath.read(respBody, "$['results'][0]['series'][0]['values'][*][0]");
		array.stream().forEach(db -> {
			result.add(db.toString());
		});
		return result;
	}
	
	public List<String> listDistinctTransactions(String measurement) {
		List<String> result = new ArrayList<String>();
		String body = String.format("q=	show tag values on " + dbName + " from \""+measurement+"\" with key = transaction");
		HttpResponse resp = sendPost(body, QUERY_URL, Arrays.asList());
		String respBody = resp.getBody();
		
		JSONArray array = JsonPath.read(respBody, "$['results'][0]['series'][0]['values'][*][1]");
		array.stream().forEach(db -> {
			result.add(db.toString());
		});
		return result;
	}	
	private HttpResponse writeEntries(String body) {
		return sendPost(body, WRITE_URL + dbName, Arrays.asList());
	}
	
	public InfluxDBTestExecution createTestExecution(String executionId) {
		return new InfluxDBTestExecution(executionId, this);
	}
	
	/**
	 * Class that keeps the influxDB connection together with the current execution
	 */
	public static class InfluxDBTestExecution{
		final String executionId;
		final InfluxDBClient client;
		
		private InfluxDBTestExecution(String executionId, InfluxDBClient client){
			this.client = client;
			this.executionId = executionId;
		}
		
		private InfluxDBTestExecution(InfluxDBClient client){
			this.client = client;
			this.executionId = null;
		}
		
		/**
		 * Write a list of transaction results into the influx DB
		 * @param transactionResults that is going be be written into the influx DB
		 * @return the HTTP status code for the influx DB response
		 */
		public HttpResponse writeTransactions(Map<String, List<TransactionExecutionResult>> transactionResults) {
			String body = convertTransactionsToWriteBody(transactionResults, executionId);
			HttpResponse responseCode = client.writeEntries(body);
			return responseCode;
		}
		
		protected String convertTransactionsToWriteBody(Map<String, List<TransactionExecutionResult>> transactionResults, String executionId) {
			if(executionId == null || executionId.isEmpty()) {
				throw new RuntimeException("ExecutionId is null or empty");
			}
			StringBuilder builder = new StringBuilder();
			for (String key : transactionResults.keySet()) {
				List<TransactionExecutionResult> list = transactionResults.get(key);
				for (TransactionExecutionResult t : list) {
					String urlParameters = String.format(WRITE_ENTRY_BODY_TEMPLATE, executionId, key, t.isStatus(), t.getValue(), t.getTs());
					
					builder.append(urlParameters);
					builder.append("\n");
				}
			}
			return builder.toString();
		}
	}
	
	/**
	 * Generates the code for the storeAndConsumeResultRuntime call where the results are sent to a InfluxDB
	 * @param originalCode
	 * @param groupName
	 * @param testName
	 * @return
	 */
	public static String generateCodeCallStoreAndConsumeResultRuntime(String originalCode, String groupName, String testName) {
		String result = originalCode;
		File f = FileUtil.getFileFromResources("cluster-codeTemplate/storeAndConsumeResultRuntime.tmp");
		String testContent = FileUtil.readFile(f);
		testContent = testContent.replace("${groupName}", groupName);
		testContent = testContent.replace("${testName}", testName);
		
		result = result.replace("${storeAndConsumeResultRuntime}", testContent);

		result = result.replace("${importList}", "import com.loadcoder.cluster.clients.docker.DockerClusterClient;\n${importList}");
		result = result.replace("${importList}", "import com.loadcoder.cluster.clients.influxdb.InfluxDBClient;\n${importList}");
		
		return result;
	}

}