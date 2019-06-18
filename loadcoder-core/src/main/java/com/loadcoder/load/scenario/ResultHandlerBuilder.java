/*******************************************************************************
 * Copyright (C) 2019 Stefan Vahlgren at Loadcoder
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
import com.loadcoder.load.exceptions.FailedTransactionException;
import com.loadcoder.load.exceptions.ResultHandlerException;
import com.loadcoder.load.intensity.LoadThreadsSynchronizer;
import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.load.scenario.Load.Transaction;
import com.loadcoder.load.scenario.LoadScenario.ResultHandler;
import com.loadcoder.result.ResultFormatter;
import com.loadcoder.result.ResultLogger;
import com.loadcoder.result.TransactionExecutionResult;

public class ResultHandlerBuilder<R> extends ResultHandlerBuilderBase {

	Logger log = LoggerFactory.getLogger(this.getClass());
	public Logger resultLogger = ResultLogger.resultLogger;

	protected Transaction<R> trans;
	protected ResultHandler<R> resultHandler;
	protected final ResultModel<R> resultModel;// = new ResultModel<R>(transactionName);

	protected ResultHandlerBuilder(Transaction<R> trans,
			TransactionExecutionResultBuffer transactionExecutionResultBuffer, ResultFormatter resultFormatter,
			RateLimiter limiter, LoadThreadsSynchronizer loadThreadsSynchronizer, String defaultName) {
		super(transactionExecutionResultBuffer, resultFormatter, limiter, loadThreadsSynchronizer);
		this.trans = trans;
		this.resultModel = new ResultModel<R>(defaultName);

	}

	public ResultHandlerBuilder<R> handleResult(ResultHandler<R> resultHandler) {
		this.resultHandler = resultHandler;
		return this;
	}

	/**
	 * Performs the transaction just stated
	 * 
	 * @return the return object from the transaction
	 */
	public R perform() {
		return performAndGetModel().getResponse();
	}

	/**
	 * This method will create a new Thread, start it and then return immediately.
	 * The created Thread will execute the perform method. The result will be that
	 * the transaction is called asynchronously.
	 */
	public void performAsync() {

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
		performResultHandeled();
		return resultModel;
	}

	protected ResultModel<R> performResultHandeled() {

		long end = 0;
		long rt = 0;
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

		}
		resultModel.setResponseTimeAndValue(rt);
		try {
			if (resultHandler != null) {
				resultHandler.handle(resultModel);
			}
		} catch (Exception e) {
			log.error("Caught exception the resultHandler for transaction " + resultModel.getTransactionName(), e);
			resultModel.setStatus(false);
			resultModel.reportTransaction(true);
			throw new ResultHandlerException(resultModel.getTransactionName(), e);
		} finally {

			if (resultModel.reportTransaction()) {
				TransactionExecutionResult result = new TransactionExecutionResult(resultModel.getTransactionName(),
						start, resultModel.getValue(), resultModel.getStatus(), resultModel.getMessage(),
						thisThreadName);
				useTransactionExecutionResult(result);
			}
		}

		if (throwIfFailed && !resultModel.getStatus()) {
			throw new FailedTransactionException(resultModel.getTransactionName());
		}

		return resultModel;
	}

	public ResultHandlerBuilder<R> peak(int amountToPeak, double chanceOfPeakOccuring) {
		this.amountToPeak = amountToPeak;
		this.chanceOfPeakOccuring = chanceOfPeakOccuring;
		return this;
	}

	public ResultHandlerBuilder<R> throwIfFailed() {
		super.throwIfFailed = true;
		return this;
	}
}
