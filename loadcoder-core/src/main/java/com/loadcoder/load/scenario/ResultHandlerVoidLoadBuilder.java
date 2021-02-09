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

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.scenario.Load.TransactionVoid;
import com.loadcoder.load.scenario.LoadScenario.ResultHandlerVoid;
import com.loadcoder.result.TransactionExecutionResult;

public class ResultHandlerVoidLoadBuilder extends ResultHandlerVoidBuilder {

	protected ResultHandlerVoidLoadBuilder(String defaultName, TransactionVoid trans, LoadScenario ls,
			RateLimiter limiter) {
		super(trans, ls.getTransactionExecutionResultBuffer(), ls.getLoad().getExecution().getResultFormatter(),
				limiter, ls.getLoad().getLoadThreadsSynchronizer(), defaultName, ls.getLoad().getStopOnErrorLimit());
	}

	/**
	 * By using this method, the result of the transaction (received in the
	 * ResultModelVoid instance) can be used to take transaction related actions.
	 * For example, if the transaction threw an Exception, the ResultHandlerVoid can
	 * be used to set the status of the transaction to false {@code
	 * (resultModelVoid)->{
	 * 	if(resultModelVoid.getException() != null) resultModelVoid.setStatus(false);
	 * } }
	 * 
	 * @param resultHandler is the implementation of the functional interface
	 *                      ResultHandlerVoid
	 * @return the builder instance
	 */
	@Override
	public ResultHandlerVoidLoadBuilder handleResult(ResultHandlerVoid resultHandler) {
		this.resultHandler = resultHandler;
		return this;
	}

	/**
	 * This method will create a new Thread, start it and then return immediately.
	 * The created Thread will execute the perform method. The result will be that
	 * the transaction is called asynchronously.
	 */
	public void performAsync() {
		thisThreadName = Thread.currentThread().getName();

		if (limiter != null) {
			limiter.acquire();
			// set limiter to null in order to prohibit this transaction to be throttled
			// both here and in performAndGetModel
			limiter = null;
		}
		Thread t = new Thread() {
			public void run() {
				perform();
			}
		};
		t.start();
	}

	/**
	 * Performs the transaction you just stated
	 * 
	 * @return the result model of the transaction
	 */
	public ResultModelVoid performAndGetModel() {
		if (limiter != null) {
			limiter.acquire();
		}
		if (thisThreadName == null) {
			thisThreadName = Thread.currentThread().getName();
		}
		performResultHandeled();
		return resultModel;
	}

	@Override
	protected ResultModelVoid performResultHandeled() {

		if (amountToPeak > 1) {
			loadThreadsSynchronizer.peakMe(resultModel.getTransactionName(), amountToPeak, chanceOfPeakOccuring);
		}
		ResultModelVoid r = super.performResultHandeled();
		return r;
	}

	@Override
	protected void useTransactionExecutionResult(TransactionExecutionResult transactionExecutionResult) {
		bufferAndLogTransactionExecutionResult(transactionExecutionResult);
	}

}
