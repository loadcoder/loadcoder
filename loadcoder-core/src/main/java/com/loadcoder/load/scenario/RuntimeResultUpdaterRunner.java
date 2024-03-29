/*******************************************************************************
 * Copyright (C) 2018 Team Loadcoder
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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeResultUpdaterRunner implements Runnable {

	private final Logger logger = LoggerFactory.getLogger(RuntimeResultUpdaterRunner.class);

	private final Execution execution;

	private final RuntimeResultConsumer[] runtimeDataUser;

	protected RuntimeResultUpdaterRunner(Execution execution, RuntimeResultConsumer... runtimeDataUser) {
		this.execution = execution;
		this.runtimeDataUser = runtimeDataUser;
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

			swapOutDataAndCallUser();

			if (!oneLoadStillNotTerminated) {
				break;
			}
		}
	}

	protected void swapOutDataAndCallUser() {

		// Swap bucket. The filled bucket is switched out and a new one is replaced
		// inside the TransactionExecutionResultBuffer
		List<TransactionExecutionResult> filledBucket = execution.getTransactionExecutionResultBuffer().swap();

		// Add all the swaped out results to the runtimeResultList
		Map<String, List<TransactionExecutionResult>> map = TransactionExecutionResult.getResultListAsMap(filledBucket);

		try {
			for(RuntimeResultConsumer r : runtimeDataUser) {
				r.useData(map);
			}
		} catch (RuntimeException rte) {
			logger.error("An exception occured when trying to use the runtime result data", rte);
		}

		map.clear();
	}

}
