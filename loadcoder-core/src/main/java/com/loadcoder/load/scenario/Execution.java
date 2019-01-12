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

import static com.loadcoder.load.exceptions.ExceptionMessages.LOAD_ALREADY_STARTED;
import static com.loadcoder.load.exceptions.ExceptionMessages.SCENARIO_BELONGS_TO_OTHER_LOAD;

import java.util.List;

import com.loadcoder.load.exceptions.InvalidLoadStateException;
import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.result.ResultFormatter;
import com.loadcoder.statics.Formatter;

public class Execution {

	private Thread runtimeResultUpdaterThread;

	private final RuntimeResultUser user;
	private final ResultFormatter resultFormatter;

	private StartedExecution startedExecution;
	private List<Load> loads;

	private long startTime;

	private TransactionExecutionResultBuffer transactionExecutionResultBuffer = new TransactionExecutionResultBuffer();

	protected ResultFormatter getResultFormatter() {
		return resultFormatter;
	}

	protected List<Load> getLoads() {
		return loads;
	}

	protected Thread getRuntimeResultUpdaterThread() {
		return runtimeResultUpdaterThread;
	}

	protected TransactionExecutionResultBuffer getTransactionExecutionResultBuffer() {
		return transactionExecutionResultBuffer;
	}

	protected Execution(ResultFormatter resultFormatter, RuntimeResultUser resultUser, List<Load> loads) {
		this.resultFormatter = resultFormatter == null ? Formatter.SIMPLE_RESULT_FORMATTER : resultFormatter;
		this.user = resultUser;
		this.loads = loads;
		loads.stream().forEach((load) -> {
			load.setExecution(this);
		});
		if (user != null) {
			runtimeResultUpdaterThread = new Thread(new RuntimeResultUpdaterRunner(this, user));
		}
	}

	/**
	 * Start the load
	 * 
	 * @return a StartedLoad instance
	 */
	public synchronized StartedExecution execute() {

		for (Load load : loads) {
			if (load.getStartedLoad() != null) {
				throw new InvalidLoadStateException(LOAD_ALREADY_STARTED.toString());
			}

			Load setLoad = load.getLoadScenario().getLoad();
			if (!setLoad.equals(load)) {
				throw new InvalidLoadStateException(SCENARIO_BELONGS_TO_OTHER_LOAD.toString());
			}
		}

		start();

		if (runtimeResultUpdaterThread != null) {
			runtimeResultUpdaterThread.start();
		}

		for (Load l : loads) {
			l.runLoad();
		}

		startedExecution = new StartedExecution(this);
		return startedExecution;
	}

	protected void start() {
		this.startTime = System.currentTimeMillis();
	}

	public long getStartTime() {
		return startTime;
	}

}
