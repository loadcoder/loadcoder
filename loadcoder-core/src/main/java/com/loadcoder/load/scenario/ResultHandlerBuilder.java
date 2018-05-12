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
import com.loadcoder.load.measure.TransactionExecutionResult;
import com.loadcoder.load.scenario.Load.Transaction;
import com.loadcoder.load.scenario.LoadScenario.ResultHandler;
import com.loadcoder.log.ResultLogger;

public class ResultHandlerBuilder <R> extends ResultHandlerBuilderBase{

	public static Logger resultLogger = ResultLogger.resultLogger;

	private Transaction<R> trans;
	private ResultHandler<R> resultHandler;
	private ResultModel<R> resultModel;

	public ResultHandlerBuilder(
			String defaultName,
			Transaction<R> trans, 
			LoadScenario ls,
			RateLimiter limiter){
		super(ls.getTransactionExecutionResultBuffer(), ls.getLoad().getResultFormatter(), limiter);
		this.transactionName = defaultName;
		this.trans = trans;
	}
	
	public ResultHandlerBuilder <R> handleResult(ResultHandler<R> resultHandler){
		this.resultHandler = resultHandler;
		return this;
	}

	public R perform(){
		return performAndGetModel().getResponse();
	}
	
	public ResultModel<R> performAndGetModel(){
		if(limiter != null){
			limiter.acquire();
		}
		resultModel = new ResultModel<R>(transactionName);
		performResultHandeled();
		return resultModel;
	}

	private ResultModel<R> performResultHandeled(){
		
		long start = System.currentTimeMillis();
		long end = 0;
		long rt = 0;
		
		try{
			R r = trans.transaction();
			end = System.currentTimeMillis();
			rt = end - start;
			resultModel.setResp(r);
		}catch(Exception e){
			end = System.currentTimeMillis();
			rt = end - start;
			resultModel.setException(e);
			//status will be default false if an exception is thrown
			resultModel.setStatus(false);
			
		}finally{
			resultModel.setResponseTime(rt);
			String name;
			boolean status;
			String message;
			try {
				if(resultHandler != null){
					resultHandler.handle(resultModel);
				}
				name = resultModel.getTransacionName();
				status = resultModel.getStatus();
				message = resultModel.getMessage();
			}catch(Exception e) {
				name = this.transactionName;
				status = false;
				message = e.getClass().getSimpleName() + " when performing handleResult";
				resultModel.reportTransaction(true);
			}
			
			if(resultModel.reportTransaction()){
				TransactionExecutionResult result = 
						new TransactionExecutionResult(name, start, rt, status, message);
				
				synchronized (transactionExecutionResultBuffer) {
					transactionExecutionResultBuffer.getBuffer().add(result);
				}

				if(resultFormatter != null){
					String toBeLogged = resultFormatter.toString(result);
					resultLogger.info(toBeLogged);
				}
			}
		}
	return resultModel;
	}
}
