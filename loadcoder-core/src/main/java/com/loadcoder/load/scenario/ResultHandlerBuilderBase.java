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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.intensity.LoadThreadsSynchronizer;
import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.result.ResultFormatter;
import com.loadcoder.result.ResultLogger;
import com.loadcoder.result.TransactionExecutionResult;

public class ResultHandlerBuilderBase {

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	public Logger resultLogger = ResultLogger.resultLogger;

	protected final TransactionExecutionResultBuffer transactionExecutionResultBuffer;
	protected final ResultFormatter resultFormatter;
	protected RateLimiter limiter;

	int amountToPeak = -1;
	double chanceOfPeakOccuring = -1;

	protected boolean throwIfFailed = false;

	protected String thisThreadName;

	final LoadThreadsSynchronizer loadThreadsSynchronizer;

	protected ResultHandlerBuilderBase(TransactionExecutionResultBuffer transactionExecutionResultBuffer,
			ResultFormatter resultFormatter, RateLimiter limiter, LoadThreadsSynchronizer loadThreadsSynchronizer) {
		this.transactionExecutionResultBuffer = transactionExecutionResultBuffer;
		this.resultFormatter = resultFormatter;
		this.limiter = limiter;
		this.loadThreadsSynchronizer = loadThreadsSynchronizer;
	}

	protected void useTransactionExecutionResult(TransactionExecutionResult transactionExecutionResult) {
	}

	protected final void bufferAndLogTransactionExecutionResult(TransactionExecutionResult transactionExecutionResult) {
		if (transactionExecutionResultBuffer != null) {
			transactionExecutionResultBuffer.addResult(transactionExecutionResult);
		}

		if (resultFormatter != null) {
			String toBeLogged = resultFormatter.toString(transactionExecutionResult);
			resultLogger.info(toBeLogged);
		}
	}
}
