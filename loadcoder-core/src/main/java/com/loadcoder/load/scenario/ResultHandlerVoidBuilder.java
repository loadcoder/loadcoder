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
import com.loadcoder.load.scenario.Load.TransactionVoid;
import com.loadcoder.load.scenario.LoadScenario.ResultHandlerVoid;
import com.loadcoder.result.ResultFormatter;

public class ResultHandlerVoidBuilder extends ResultHandlerBuilderBase {

	protected final TransactionVoid trans;
	protected ResultModelVoid resultModel;
	protected ResultHandlerVoid resultHandler;

	protected ResultHandlerVoidBuilder(TransactionVoid trans,
			TransactionExecutionResultBuffer transactionExecutionResultBuffer, ResultFormatter resultFormatter,
			RateLimiter limiter, LoadThreadsSynchronizer loadThreadsSynchronizer) {
		super(transactionExecutionResultBuffer, resultFormatter, limiter, loadThreadsSynchronizer);
		this.trans = trans;
	}

	public ResultHandlerVoidBuilder handleResult(ResultHandlerVoid resultHandler) {
		this.resultHandler = resultHandler;
		return this;
	}

	/**
	 * Performs the transaction just stated
	 */

	public void perform() {
		performAndGetModel();
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
	public ResultModelVoid performAndGetModel() {
		resultModel = new ResultModelVoid(transactionName);
		performResultHandeled();
		return resultModel;
	}

	private ResultModelVoid performResultHandeled() {
		long start = System.currentTimeMillis();
		long end = 0;
		long rt = 0;

		try {
			trans.transaction();
			end = System.currentTimeMillis();
			rt = end - start;
		} catch (Exception e) {
			end = System.currentTimeMillis();
			rt = end - start;
			resultModel.setException(e);
			// status will be default false if an exception is thrown
			resultModel.setStatus(false);

		} finally {
			resultModel.setResponseTime(rt);
			try {
				if (resultHandler != null) {
					resultHandler.handle(resultModel);
				}
			} catch (Exception e) {

			}

		}
		return resultModel;
	}

	public ResultHandlerVoidBuilder peak(int amountToPeak, double chanceOfPeakOccuring) {
		this.amountToPeak = amountToPeak;
		this.chanceOfPeakOccuring = chanceOfPeakOccuring;
		return this;
	}

}
