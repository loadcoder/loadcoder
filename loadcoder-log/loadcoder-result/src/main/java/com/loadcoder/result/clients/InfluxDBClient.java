/*******************************************************************************
 * Copyright (C) 2018 Stefan Vahlgren at Loadcoder
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
package com.loadcoder.result.clients;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.loadcoder.result.TransactionExecutionResult;


public class InfluxDBClient extends HttpClient{

	private final String dbName;

	private final static String WRITE_URL_TEMPLATE = "%s://%s:%s/write?db=";
	private final static String QUERY_URL_TEMPLATE = "%s://%s:%s/query";
	
	private final static String WRITE_ENTRY_BODY_TEMPLATE = "%s,execution=%s value=%s %s000000";
	private final String WRITE_URL;
	private final String QUERY_URL;
	 
	public InfluxDBClient(String host, int port, boolean https, String db) {
		String protocol = protocolAsString(https);
		WRITE_URL = String.format(WRITE_URL_TEMPLATE, protocol, host, port);
		QUERY_URL = String.format(QUERY_URL_TEMPLATE, protocol, host, port);
		this.dbName = db;
	}

	public String getDbName() {
		return dbName;
	}

	protected int createDB(String dbName) {
		String body = String.format("q=CREATE DATABASE %s", dbName);
		return sendPost(body, QUERY_URL, Arrays.asList());
	}

	private int writeEntries(String body) {
		return sendPost(body, WRITE_URL + dbName, Arrays.asList());
	}
	
	public InfluxDBTestExecution createTestExecution() {
		return new InfluxDBTestExecution(this);
	}

	public InfluxDBTestExecution createTestExecution(String executionId) {
		return new InfluxDBTestExecution(executionId, this);
	}
	
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
		
		public int writeTransactions(Map<String, List<TransactionExecutionResult>> listOfListOfList) {
			String body = convertTransactionsToWriteBody(listOfListOfList, executionId);
			int responseCode = client.writeEntries(body);
			return responseCode;
		}
		
		protected String convertTransactionsToWriteBody(Map<String, List<TransactionExecutionResult>> listOfListOfList, String executionId) {
			String entireBody = "";
			
			for (String key : listOfListOfList.keySet()) {
				List<TransactionExecutionResult> list = listOfListOfList.get(key);
				for (TransactionExecutionResult t : list) {
					String urlParameters = String.format(WRITE_ENTRY_BODY_TEMPLATE, key, executionId, t.getRt(), t.getTs());
					entireBody = entireBody + urlParameters + "\n";
				}
			}
			return entireBody;
		}
	}

}