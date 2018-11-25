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

import java.util.Arrays;

import com.loadcoder.result.ResultFormatter;

public class ExecutionBuilder {

	private ResultFormatter resultFormatter;
	private RuntimeResultUser user;
	private final Load[] loads;

	/**
	 * sets a RuntimeResultUser that will use the results in runtime. You can use a
	 * {@code com.loadcoder.load.chart.logic.RuntimeChart} instance here.
	 * 
	 * @param runtimeResultUser is the instance that will consume the runtime result
	 *                          data during the execution
	 * @return the builder instance
	 */
	public ExecutionBuilder runtimeResultUser(RuntimeResultUser runtimeResultUser) {
		this.user = runtimeResultUser;
		return this;
	}

	/**
	 * sets a ResultFormatter used to format the results that is going to be logged
	 * in the result file. Default formatter is the
	 * com.loadcoder.statics.Formatter.SIMPLE_RESULT_FORMATTER
	 * 
	 * @param resultFormatter is the instance that will format the
	 *                        TransactionExecutionResult to and from loggable
	 *                        Strings
	 * @return the builder instance
	 */
	public ExecutionBuilder resultFormatter(ResultFormatter resultFormatter) {
		this.resultFormatter = resultFormatter;
		return this;
	}

	/**
	 * Constructor for the ExecutionBuilder
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
		return new Execution(resultFormatter, user, Arrays.asList(loads));
	}
}