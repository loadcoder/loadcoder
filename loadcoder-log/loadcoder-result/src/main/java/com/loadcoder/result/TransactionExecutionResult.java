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

	final String name;
	final long ts;
	final long rt;
	final boolean status;
	final String message;
	final String threadId;
	
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public String toString(){
		return String.format("name:%s, ts:%s, rt:%s)", name, ts, rt);
	}

	public TransactionExecutionResult(String name, long ts, long rt, boolean status, String message) {
		this(name, ts, rt, status, message, Thread.currentThread().getName());
	}

	public TransactionExecutionResult(String name, long ts, long rt, boolean status, String message, String threadId) {

		this.name = name;
		this.ts = ts;
		this.rt = rt;
		this.status = status;
		this.message = message;
		this.threadId = threadId;
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

	public String getThread(){
		return threadId;
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
