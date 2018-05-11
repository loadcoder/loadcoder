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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.intensity.Intensity;
import com.loadcoder.load.intensity.PerTimeUnit;
import com.loadcoder.load.measure.TransactionExecutionResult;
import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.load.scenario.Load.Transaction;
import com.loadcoder.load.scenario.Load.TransactionVoid;

public abstract class LoadScenario{
	
	final List<Thread> threads = new ArrayList<Thread>();

	public abstract void loadScenario();
	
	List<List<TransactionExecutionResult>> runtimeResultList = new ArrayList<List<TransactionExecutionResult>>();
	
	Map<String, List<TransactionExecutionResult>> map = new HashMap<String, List<TransactionExecutionResult>>();
	
	Load l;	
	
	protected void setLoad(Load l){
		this.l = l;
	}
	
	protected Load getLoad(){
		return l;
	}
	
	public void pre(){
	}	
	
	public void post(){
	}	
	
	protected TransactionExecutionResultBuffer getTransactionExecutionResultBuffer(){
		return l.getTransactionExecutionResultBuffer();
	}
	
	public int getAmountOfThreads(){
		return l.getAmountOfThreads();
	}
	
	public <T> ResultHandlerBuilder<T> load(String defaultName, Transaction<T> t){
		RateLimiter limiterToBeUsed = null;
		
		if(l.getThrottler() != null){
			limiterToBeUsed = l.getThrottler().getRateLimiter(Thread.currentThread());
		}
		
		ResultHandlerBuilder<T> resultHandlerBuilder = 
				new ResultHandlerBuilder<T>(
						defaultName,
						t,
						this,
						limiterToBeUsed);

		return resultHandlerBuilder;
	} 

	public <T> ResultHandlerVoidBuilder load(String defaultName, TransactionVoid t){
		RateLimiter limiterToBeUsed = null;
		
		if(l.getThrottler() != null){
			limiterToBeUsed = l.getThrottler().getRateLimiter(Thread.currentThread());
		}
		
		ResultHandlerVoidBuilder resultHandlerBuilder = 
				new ResultHandlerVoidBuilder(
						defaultName,
						t,
						this,
						limiterToBeUsed);

		return resultHandlerBuilder;
	} 
	
	public static double getAmountPerSecond(Intensity i){
		return getAmountPerSecond(i.getAmount(), i.getPerTimeUnit());
	}

	public static long getMillis(long amount, PerTimeUnit unit){
		long secondsForOneUnit = getTimeMultiplyer(unit);
		long amountInMillis = amount * secondsForOneUnit * 1000;
		return amountInMillis;
	}

	/**
	 * 1, minute:
	 * divider = 60
	 * amountPerSecond = 1 /60
	 */
	public static double getAmountPerSecond(long amount, PerTimeUnit unit){
		long divider = getTimeMultiplyer(unit);
		double amountPerSecond = ((double)amount) / divider;
		return amountPerSecond; 
	}
	
	private static long getTimeMultiplyer(PerTimeUnit unit){
		long multiplyer = 0;
		if(unit == PerTimeUnit.SECOND){
			multiplyer = 1;
		}else if(unit == PerTimeUnit.MINUTE){
			multiplyer = 60;
		}else if(unit == PerTimeUnit.HOUR){
			multiplyer = 60 * 60;
		}
		return multiplyer;
	}
	
	public interface ResultHandler <R>{
		public void handle(ResultModel<R> resultModel);
	}

	public interface ResultHandlerVoid {
		public void handle(ResultModelVoid resultModel);
	}
	
	long calculateRampUpSleepTime(long rampup, int amountOfThreads) {
		long rampUpSleepTime = 0;
		if (amountOfThreads > 1)
			rampUpSleepTime = rampup / (amountOfThreads - 1);
		return rampUpSleepTime;
	}
	
	public List<Thread> getThreads(){
		return this.threads;
	}
	
}
