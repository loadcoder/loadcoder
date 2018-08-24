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
import java.util.List;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.intensity.Intensity;
import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.load.scenario.Load.Executable;
import com.loadcoder.load.scenario.Load.Transaction;
import com.loadcoder.load.scenario.Load.TransactionVoid;
import com.loadcoder.statics.TimeUnit;

public abstract class LoadScenario{
	
	private final List<Thread> threads = new ArrayList<Thread>();

	public abstract void loadScenario();
	
	private Load l;	
	
	Execution e;
	
	protected void setLoad(Load l){
		this.l = l;
	}
	
	protected Load getLoad(){
		return l;
	}
	
	public synchronized void pre(){
		Executable pre = l.getPreExecution();
		if(pre!= null) {
			pre.execute();
		}
	}	
	
	public synchronized void post(){
		Executable post = l.getPostExecution();
		if(post!= null) {
			post.execute();
		}
	}	
	
	protected TransactionExecutionResultBuffer getTransactionExecutionResultBuffer(){
		return l.getExecution().getTransactionExecutionResultBuffer();
	}
	
	public int getAmountOfThreads(){
		return l.getAmountOfThreads();
	}
	
	/**
	 * @param defaultName 
	 * is the name of the transaction you are about to state. 
	 * The name of the transaction can be changed after the transaction is made, in the handleResult method
	 * @param transaction
	 * is the transaction, done with implementing functional interface Transaction
	 * @return the builder instance
	 */
	public <T> ResultHandlerBuilder<T> load(String defaultName, Transaction<T> transaction){
		RateLimiter limiterToBeUsed = null;
		
		if(l.getThrottler() != null){
			limiterToBeUsed = l.getThrottler().getRateLimiter(Thread.currentThread());
		}
		
		ResultHandlerBuilder<T> resultHandlerBuilder = 
				new ResultHandlerBuilder<T>(
						defaultName,
						transaction,
						this,
						limiterToBeUsed);

		return resultHandlerBuilder;
	} 

	
	/**
	 * @param defaultName 
	 * is the name of the transaction you are about to state. 
	 * The name of the transaction can be changed after the transaction is made, in the handleResult method
	 * @param transaction
	 * is the transaction, done with implementing functional interface TransactionVoid
	 * @return the builder instance
	 */
	public <T> ResultHandlerVoidBuilder load(String defaultName, TransactionVoid transaction){
		RateLimiter limiterToBeUsed = null;
		
		if(l.getThrottler() != null){
			limiterToBeUsed = l.getThrottler().getRateLimiter(Thread.currentThread());
		}
		
		ResultHandlerVoidBuilder resultHandlerBuilder = 
				new ResultHandlerVoidBuilder(
						defaultName,
						transaction,
						this,
						limiterToBeUsed);

		return resultHandlerBuilder;
	} 
	
	public static double getAmountPerSecond(Intensity i){
		return getAmountPerSecond(i.getAmount(), i.getPerTimeUnit());
	}

	public static long getMillis(long amount, TimeUnit unit){
		long secondsForOneUnit = getTimeMultiplyer(unit);
		long amountInMillis = amount * secondsForOneUnit * 1000;
		return amountInMillis;
	}

	/**
	 * 1, minute:
	 * divider = 60
	 * amountPerSecond = 1 /60
	 */
	public static double getAmountPerSecond(long amount, TimeUnit unit){
		long divider = getTimeMultiplyer(unit);
		double amountPerSecond = ((double)amount) / divider;
		return amountPerSecond; 
	}
	
	private static long getTimeMultiplyer(TimeUnit unit){
		long multiplyer = 0;
		if(unit == TimeUnit.SECOND){
			multiplyer = 1;
		}else if(unit == TimeUnit.MINUTE){
			multiplyer = 60;
		}else if(unit == TimeUnit.HOUR){
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
