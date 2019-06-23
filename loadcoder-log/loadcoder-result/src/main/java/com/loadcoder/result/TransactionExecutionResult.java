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
package com.loadcoder.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionExecutionResult {

	private final String name;
	private final long ts;
	private final long val;
	private final boolean status;
	private final String message;
	private final String threadId;

	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public String toString() {
		return String.format("name:%s, ts:%s, val:%s)", name, ts, val);
	}

	public TransactionExecutionResult(long ts, long rt, boolean status, String message) {
		this(null, ts, rt, status, message, Thread.currentThread().getName());
	}

	public TransactionExecutionResult(String name, long ts, long rt, boolean status, String message) {
		this(name, ts, rt, status, message, Thread.currentThread().getName());
	}

	public TransactionExecutionResult(String name, long ts, long val, boolean status, String message, String threadId) {

		this.name = name;
		this.ts = ts;
		this.val = val;
		this.status = status;
		this.message = message;
		this.threadId = threadId;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return the timestamp for when the transaction was executed (started)
	 */
	public long getTs() {
		return ts;
	}

	/**
	 * @return the execution time of the transaction
	 */
	public long getValue() {
		return val;
	}

	/**
	 * @return the status of the executed transaction
	 */
	public boolean isStatus() {
		return status;
	}

	/**
	 * @return the message of the re
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the Id for the thread that initiated the transaction to be performed
	 */
	public String getThreadId() {
		return threadId;
	}

	/**
	 * Creates and returns a new Map with the TransactionExecutionResult grouped in Lists with the transaction id as key
	 * @param filledBucket
	 * @return the new Map
	 */
	public static Map<String, List<TransactionExecutionResult>> getResultListAsMap(
			List<TransactionExecutionResult> filledBucket) {
		Map<String, List<TransactionExecutionResult>> map = new HashMap<String, List<TransactionExecutionResult>>();
		for (TransactionExecutionResult transactionExecutionResult : filledBucket) {
			String name = transactionExecutionResult.getName();
			List<TransactionExecutionResult> listToAddTo = map.get(name);
			if (listToAddTo == null) {
				listToAddTo = new ArrayList<TransactionExecutionResult>();
				map.put(name, listToAddTo);
			}
			listToAddTo.add(transactionExecutionResult);
		}
		return map;
	}
}
