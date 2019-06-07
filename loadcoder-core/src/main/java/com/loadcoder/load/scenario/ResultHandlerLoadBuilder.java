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

import org.slf4j.Logger;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.intensity.LoadThreadsSynchronizer;
import com.loadcoder.load.scenario.Load.Transaction;
import com.loadcoder.load.scenario.LoadScenario.ResultHandler;
import com.loadcoder.result.ResultLogger;
import com.loadcoder.result.TransactionExecutionResult;

public class ResultHandlerLoadBuilder<R> extends ResultHandlerBuilder<R> {

	public static Logger resultLogger = ResultLogger.resultLogger;

	private String thisThreadName;

	protected ResultHandlerLoadBuilder(String defaultName, Transaction<R> trans, LoadScenario ls, RateLimiter limiter) {
		super(trans, ls.getTransactionExecutionResultBuffer(), ls.getLoad().getExecution().getResultFormatter(),
				limiter, ls.getLoad().getLoadThreadsSynchronizer());
		this.transactionName = defaultName;
	}

	/**
	 * By using this method, the result of the transaction (received in the
	 * ResultModel instance) can be used to take transaction related actions. For
	 * example, if the transaction threw an Exception, the ResultHandler can be used
	 * to set the status of the transaction to false {@code
	 * (resultModel)->{ if(resultModel.getException() != null)
	 * resultModel.setStatus(false); } }
	 * 
	 * @param resultHandler is the implementation of the functional interface
	 *                      ResultHandler
	 * @return the builder instance
	 */
	@Override
	public ResultHandlerLoadBuilder<R> handleResult(ResultHandler<R> resultHandler) {
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
	public ResultModel<R> performAndGetModel() {
		if (limiter != null) {
			limiter.acquire();
		}
		if (thisThreadName == null) {
			thisThreadName = Thread.currentThread().getName();
		}
		resultModel = new ResultModel<R>(transactionName);
		performResultHandeled();
		return resultModel;
	}

	private ResultModel<R> performResultHandeled() {

		if (amountToPeak > 1) {
			loadThreadsSynchronizer.peakMe(transactionName, amountToPeak, chanceOfPeakOccuring);
		}

		long end = 0;
		long rt = 0;
		long value = 0;
		long start = System.currentTimeMillis();

		try {
			R r = trans.transaction();
			end = System.currentTimeMillis();
			rt = end - start;
			resultModel.setResp(r);
		} catch (Exception e) {
			end = System.currentTimeMillis();
			rt = end - start;
			resultModel.setException(e);
			// status will be default false if an exception is thrown
			resultModel.setStatus(false);

		} finally {
			resultModel.setResponseTimeAndValue(rt);
			String name;
			boolean status;
			String message;
			try {
				if (resultHandler != null) {
					resultHandler.handle(resultModel);
				}
				name = resultModel.getTransactionName();
				status = resultModel.getStatus();
				message = resultModel.getMessage();
				value = resultModel.getValue();
			} catch (Exception e) {
				name = this.transactionName;
				status = false;
				message = e.getClass().getSimpleName() + " when performing handleResult";
				value = rt;
				resultModel.reportTransaction(true);

			}

			if (resultModel.reportTransaction()) {
				TransactionExecutionResult result = new TransactionExecutionResult(name, start, value, status, message,
						thisThreadName);

				if (transactionExecutionResultBuffer != null) {
					transactionExecutionResultBuffer.addResult(result);
					;
				}

				if (resultFormatter != null) {
					String toBeLogged = resultFormatter.toString(result);
					resultLogger.info(toBeLogged);
				}
			}
		}
		return resultModel;
	}

}
