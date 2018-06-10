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
package com.loadcoder.load.scenario;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeResultUpdaterRunner implements Runnable{
	
	Load load;
	RuntimeDataUser runtimeDataUser;
	boolean quit = false;
	
	Map<String, List<TransactionExecutionResult>> map = new HashMap<String, List<TransactionExecutionResult>>();
	
	public RuntimeResultUpdaterRunner(Load l, RuntimeDataUser runtimeDataUser){
		this.load = l;
		this.runtimeDataUser = runtimeDataUser;
	}
	
	protected void useResult(Result r, List<List<TransactionExecutionResult>> listOfListOfList) {
		List<List<TransactionExecutionResult>> transactionExecutionResults = r.getResultLists();
		synchronized (transactionExecutionResults) {

			for (List<TransactionExecutionResult> listToBeCopiedAndCleared : transactionExecutionResults) {
				List<TransactionExecutionResult> newList = new ArrayList<TransactionExecutionResult>();
				listOfListOfList.add(newList);
				newList.addAll(listToBeCopiedAndCleared);
				listToBeCopiedAndCleared.clear(); // todo?
			}
		}
	}
	
	public void run() {

		while (true) {

			try {
				Thread.sleep(3_000);
			} catch (InterruptedException ie) {
			}

			State scenarioStateManagerState = load.getLoadStateThread().getState();
			if (scenarioStateManagerState == State.TERMINATED) {
				quit = true;
			}
			swapOutDataAndCallUser(map);
			if (quit) {
				break;
			}
		}
	}

	protected void swapOutDataAndCallUser(Map<String, List<TransactionExecutionResult>> map) {
		List<TransactionExecutionResult> switchDestination;

		//swap the bucket. The running load threads will after this add results to the new list
		synchronized (load.getTransactionExecutionResultBuffer()) {
			switchDestination = load.getTransactionExecutionResultBuffer().getBuffer();
			load.getTransactionExecutionResultBuffer().setBuffer(new ArrayList<TransactionExecutionResult>());
		}

		//add all the swaped out results to the runtimeResultList
		synchronized (load.getRuntimeResultList()) {
			List<List<TransactionExecutionResult>> runtimeResultList = load.getRuntimeResultList();
			for (TransactionExecutionResult transactionExecutionResult : switchDestination) {
				String name = transactionExecutionResult.getName();
				List<TransactionExecutionResult> listToAddTo = map.get(name);
				if (listToAddTo == null) {
					listToAddTo = new ArrayListExtension<TransactionExecutionResult>();
					runtimeResultList.add(listToAddTo);
					map.put(name, listToAddTo);
				}
				listToAddTo.add(transactionExecutionResult);
			}
		}

		runtimeDataUser.useData(load.getRuntimeResultList());
		load.getRuntimeResultList().clear();
		map.clear();
	}

	public static class ArrayListExtension <E> extends ArrayList <E>{
		private static final long serialVersionUID = 1L;
		
		public boolean add(E e) {
			return super.add(e);
		}
	}
}
