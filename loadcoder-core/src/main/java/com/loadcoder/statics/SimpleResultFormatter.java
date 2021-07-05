/*******************************************************************************
 * Copyright (C) 2019 Team Loadcoder
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
package com.loadcoder.statics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.result.ResultFormatter;
import com.loadcoder.result.TransactionExecutionResult;

public class SimpleResultFormatter extends ResultFormatter{


	private String getValueOfParameter(String line, String parameter) {
		String[] splitted = line.split(parameter + "=\"");
		String value = splitted[1].split("\"")[0];
		return value;
	}

	
	/**
	 * Generate TransactionExecutionResult from the provided String
	 * @param transactionResult is the String used to generate the TransactionExecutionResult
	 * @return the generated TransactionExecutionResult
	 */
	private TransactionExecutionResult toTransactionExecutionResult(String transactionResult) {

		String name = getValueOfParameter(transactionResult, "id");
		String ts = getValueOfParameter(transactionResult, "ts");
		String status = getValueOfParameter(transactionResult, "st");
		String rt = getValueOfParameter(transactionResult, "vl");
		String message = null;
		String threadId = null;

		try{
			threadId = getValueOfParameter(transactionResult, "th");
		}catch(RuntimeException rte){
			//message is optional. OK with silent rte
		}

		try{
			message = getValueOfParameter(transactionResult, "ms");
		}catch(RuntimeException rte){
			//message is optional. OK with silent rte
		}
		return new TransactionExecutionResult(name, Long.valueOf(ts), Long.valueOf(rt), Boolean.valueOf(status), message, threadId);
	}

	@Override
	public String toString(TransactionExecutionResult transactionExecutionResult) {
		String msg = transactionExecutionResult.getMessage() == null ? "" : String.format("ms=\"%s\"", transactionExecutionResult.getMessage()) + " ";
		String thread = transactionExecutionResult.getThreadId() == null ? "" : String.format("th=\"%s\"", transactionExecutionResult.getThreadId()) + " ";
		String asString = String.format("<t id=\"%s\" ts=\"%s\" vl=\"%s\" st=\"%s\" " + msg + thread + "/>",
				transactionExecutionResult.getName(), transactionExecutionResult.getTs(),
				transactionExecutionResult.getValue(), transactionExecutionResult.getStatus());

		return asString;
	}

	@Override
	public Map<String, List<TransactionExecutionResult>> toResultLists(File file) throws IOException {
		Logger log = LoggerFactory.getLogger(Formatter.class);
		
		Map<String, List<TransactionExecutionResult>> transactions = new HashMap<String, List<TransactionExecutionResult>>();
		int lineNumber = 0;
		log.debug("reading file {}" ,file);
		List<String> fileAsLineList = LoadUtility.readFile(file);
		log.debug("file sucessfully read!");
		
		for (String line : fileAsLineList) {
			
			//skip empty lines
			if(line.length() <2)
				continue;
			
			lineNumber++;
			try {
				TransactionExecutionResult result = toTransactionExecutionResult(line);
				List<TransactionExecutionResult> s = transactions.get(result.getName());

				if (s == null) {
					s = new ArrayList<TransactionExecutionResult>();
					transactions.put(result.getName(), s);
				}
				s.add(result);
			} catch (ArrayIndexOutOfBoundsException aioube) {
				log.debug(String.format("Line %s in file %s could not be formatted", lineNumber, file.getAbsolutePath()));
			}
		}
		return transactions;
	}

}
