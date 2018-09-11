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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeResultUpdaterRunner implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(RuntimeResultUpdaterRunner.class);

	private final Execution execution;

	private final RuntimeResultUser runtimeDataUser;

	private final Map<String, List<TransactionExecutionResult>> map = new HashMap<String, List<TransactionExecutionResult>>();

	private final List<List<TransactionExecutionResult>> runtimeResultList = new ArrayList<List<TransactionExecutionResult>>();

	public RuntimeResultUpdaterRunner(Execution execution, RuntimeResultUser runtimeDataUser) {
		this.execution = execution;
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

			boolean oneLoadStillNotTerminated = false;

			for (Load load : execution.getLoads()) {
				State scenarioStateManagerState = load.getLoadStateThread().getState();
				if (scenarioStateManagerState != State.TERMINATED) {
					oneLoadStillNotTerminated = true;
					break;
				}
			}

			swapOutDataAndCallUser(map);

			if (!oneLoadStillNotTerminated) {
				break;
			}
		}
	}

	protected void swapOutDataAndCallUser(Map<String, List<TransactionExecutionResult>> map) {
		List<TransactionExecutionResult> switchDestination;

		// swap the bucket. The running load threads will after this add results to the
		// new list listForComingTransactions
		synchronized (execution.getTransactionExecutionResultBuffer()) {
			switchDestination = execution.getTransactionExecutionResultBuffer().getBuffer();

			List<TransactionExecutionResult> listForComingTransactions = new ArrayList<TransactionExecutionResult>();
			execution.getTransactionExecutionResultBuffer().setBuffer(listForComingTransactions);
		}

		// add all the swaped out results to the runtimeResultList
		synchronized (runtimeResultList) {
			for (TransactionExecutionResult transactionExecutionResult : switchDestination) {
				String name = transactionExecutionResult.getName();
				List<TransactionExecutionResult> listToAddTo = map.get(name);
				if (listToAddTo == null) {
					listToAddTo = new ArrayList<TransactionExecutionResult>();
					runtimeResultList.add(listToAddTo);
					map.put(name, listToAddTo);
				}
				listToAddTo.add(transactionExecutionResult);
			}
		}

		try {
			runtimeDataUser.useData(runtimeResultList);
		} catch (RuntimeException rte) {
			logger.error("An exception occured when trying to use the runtime result data", rte);
		}

		runtimeResultList.clear();
		map.clear();
	}
}
