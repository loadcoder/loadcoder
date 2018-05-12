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
package com.loadcoder.load.measure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.LoadUtility;

public class TransactionExecutionResult {

	final String name;
	final long ts;
	final long rt;
	final boolean status;
	final String message;
	
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public String toString(){
		return String.format("name:%s, ts:%s, rt:%s)", name, ts, rt);
	}
	private static Logger log = LoggerFactory.getLogger(TransactionExecutionResult.class);

	public static final ResultFormatter resultStringFormatterDefault = new ResultFormatter() {

		public String getValueOfParameter(String line, String parameter) {
			String[] splitted = line.split(parameter + "=\"");
			String value = splitted[1].split("\"")[0];
			return value;
		}

		public TransactionExecutionResult toTransactionExecutionResult(String string) {

			String name = getValueOfParameter(string, "name");
			String ts = getValueOfParameter(string, "ts");
			String status = getValueOfParameter(string, "status");
			String rt = getValueOfParameter(string, "rt");
			String message = null;
			try{
				message = getValueOfParameter(string, "message");
			}catch(RuntimeException rte){
				//message is optional. OK with silent rte
			}
			return new TransactionExecutionResult(name, new Long(ts), new Long(rt), new Boolean(status), message);
		}

		@Override
		public String toString(TransactionExecutionResult transactionExecutionResult) {
			String msg = transactionExecutionResult.getMessage() == null ? "" : String.format("message=\"%s\"", transactionExecutionResult.getMessage());
			String asString = String.format("<t name=\"%s\" ts=\"%s\" rt=\"%s\" status=\"%s\" "+msg+" />",
					transactionExecutionResult.getName(), transactionExecutionResult.getTs(),
					transactionExecutionResult.getRt(), transactionExecutionResult.isStatus());

			return asString;
		}

		@Override
		public List<List<TransactionExecutionResult>> toResultLists(File file) throws IOException {
			List<List<TransactionExecutionResult>> dataSets = new ArrayList<List<TransactionExecutionResult>>();
			Map<String, List<TransactionExecutionResult>> dataSetMap = new HashMap<String, List<TransactionExecutionResult>>();
			int lineNumber = 0;
			List<String> fileAsLineList = LoadUtility.readFile(file);
			for (String line : fileAsLineList) {
				
				//skip empty lines
				if(line.length() <2)
					continue;
				
				lineNumber++;
				try {
					TransactionExecutionResult result = toTransactionExecutionResult(line);

					List<TransactionExecutionResult> s = dataSetMap.get(result.getName());
					
					if (s == null) {
						s = new ArrayList<TransactionExecutionResult>();
						dataSetMap.put(result.getName(), s);
						dataSets.add(s);
					}
					s.add(result);
				} catch (ArrayIndexOutOfBoundsException aioube) {
					log.debug(String.format("Line %s in file %s could not be formatted", lineNumber, file.getAbsolutePath()));
				}
			}
			return dataSets;
		}
	};

	public TransactionExecutionResult(String name, long ts, long rt, boolean status, String message) {

		this.name = name;
		this.ts = ts;
		this.rt = rt;
		this.status = status;
		this.message = message;
	}


	public String getName() {
		return name;
	}

	public long getTs() {
		return ts;
	}

	public long getRt() {
		return rt;
	}

	public boolean isStatus() {
		return status;
	}

	public String getMessage(){
		return message;
	}

	public static List<List<TransactionExecutionResult>> mergeList(
			List<List<TransactionExecutionResult>> listOfListOfList) {

		Map<String, List<TransactionExecutionResult>> m = new HashMap<String, List<TransactionExecutionResult>>();
		List<List<TransactionExecutionResult>> mergeToThisList = new ArrayList<List<TransactionExecutionResult>>();

		
		for (List<TransactionExecutionResult> list : listOfListOfList) {
			if (list.isEmpty()) {
				continue;
			}
			TransactionExecutionResult firstOne = list.get(0);
			List<TransactionExecutionResult> lista = m.get(firstOne.getName());
			if (lista == null) {
				lista = new ArrayList<TransactionExecutionResult>();
				m.put(firstOne.getName(), lista);
				mergeToThisList.add(lista);
			}
			lista.addAll(list);
		}
		return mergeToThisList;
	}

}
