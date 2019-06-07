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

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.intensity.LoadThreadsSynchronizer;
import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.load.scenario.Load.Transaction;
import com.loadcoder.load.scenario.LoadScenario.ResultHandler;
import com.loadcoder.result.ResultFormatter;

public class ResultHandlerBuilder<R> extends ResultHandlerBuilderBase {

	protected Transaction<R> trans;
	protected ResultHandler<R> resultHandler;
	protected ResultModel<R> resultModel;

	protected ResultHandlerBuilder(Transaction<R> trans,
			TransactionExecutionResultBuffer transactionExecutionResultBuffer, ResultFormatter resultFormatter,
			RateLimiter limiter, LoadThreadsSynchronizer loadThreadsSynchronizer) {
		super(transactionExecutionResultBuffer, resultFormatter, limiter, loadThreadsSynchronizer);
		this.trans = trans;
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
		resultModel = new ResultModel<R>(transactionName);
		performResultHandeled();
		return resultModel;
	}

	private ResultModel<R> performResultHandeled() {

		long start = System.currentTimeMillis();
		long end = 0;
		long rt = 0;
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
			try {
				if (resultHandler != null) {
					resultHandler.handle(resultModel);
				}
			} catch (Exception e) {
			}

		}
		return resultModel;
	}

	public ResultHandlerBuilder<R> peak(int amountToPeak, double chanceOfPeakOccuring) {
		this.amountToPeak = amountToPeak;
		this.chanceOfPeakOccuring = chanceOfPeakOccuring;
		return this;
	}
}
