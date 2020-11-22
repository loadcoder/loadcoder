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

import java.util.Arrays;

import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.result.ResultFormatter;
import com.loadcoder.statics.Formatter;

public class ExecutionBuilder {

	protected ResultFormatter resultFormatter = Formatter.SIMPLE_RESULT_FORMATTER;
	protected RuntimeResultConsumer user;
	protected TransactionExecutionResultBuffer transactionExecutionResultBuffer = null;
	protected final Load[] loads;

	/**
	 * The use case for this method is for shorter performance tests and for unit
	 * testing, where the amount of transactions are being limited.
	 * <p>
	 * Use this with caution. Since every TransactionExecutionResult are being
	 * stored, memory can run out over time, causing both the test to crash and the
	 * results to be affected.
	 * <p>
	 * Activates the storage of all TransactionExecutionResult in runtime memory.
	 * This storage can be accessed through the FinishedExecution instance after an
	 * executed test, with the method FinishedExecution:getResultFromMemory, that
	 * returns a Result instance based on this storage.
	 * 
	 * @return the builder instance
	 */
	public ExecutionBuilder storeResultRuntime() {
		transactionExecutionResultBuffer = new TransactionExecutionResultBuffer();
		return this;
	}

	/**
	 * Activates the storage of all TransactionExecutionResult in runtime memory.
	 * This storage content will be consumed by the provided runtimeResultUser.
	 * 
	 * @param runtimeResultUser is a functional interface that if provided, will be
	 *                          invoked every 3 seconds during the entire execution.
	 *                          It will be invoked with the stored result in a Map,
	 *                          that will be cleared (and consumed) afterwards.
	 *                          {@code com.loadcoder.load.chart.logic.RuntimeChart}
	 *                          implements RuntimeResultUser and will use the stored
	 *                          runtime result as a graph, where response times,
	 *                          throughput and amount of fails can be monitored
	 *                          during the test execution
	 * 
	 * @return the builder instance
	 */
	public ExecutionBuilder storeAndConsumeResultRuntime(RuntimeResultConsumer runtimeResultUser) {
		transactionExecutionResultBuffer = new TransactionExecutionResultBuffer();
		this.user = runtimeResultUser;
		return this;
	}

	/**
	 * Sets a ResultFormatter used to format the results that is going to be logged
	 * in the result file. Default formatter is the
	 * com.loadcoder.statics.Formatter.SIMPLE_RESULT_FORMATTER
	 * 
	 * If the argument is null, it will disable the default logging of the results
	 * for the transactions
	 * 
	 * @param resultFormatter is the instance that will format the
	 *                        TransactionExecutionResult to and from loggable
	 *                        Strings. A null value will make the
	 *                        TransactionExecutionResult not being logged
	 * @return the builder instance
	 */
	public ExecutionBuilder resultFormatter(ResultFormatter resultFormatter) {
		this.resultFormatter = resultFormatter;
		return this;
	}

	/**
	 * Constructor for the ExecutionBuilder
	 * 
	 * @param loads is the Load instances that the test will consist of
	 */
	public ExecutionBuilder(Load... loads) {
		this.loads = loads;
	}

	/**
	 * Builds an Execution instance.
	 * 
	 * @return an Execution instance.
	 */
	public Execution build() {
		return new Execution(resultFormatter, transactionExecutionResultBuffer, user, Arrays.asList(loads));
	}
}