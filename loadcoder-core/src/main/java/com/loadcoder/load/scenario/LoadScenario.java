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

public abstract class LoadScenario {

	private final List<Thread> threads = new ArrayList<Thread>();

	public abstract void loadScenario();

	private Load load;

	protected void setLoad(Load load) {
		this.load = load;
	}

	protected Load getLoad() {
		return load;
	}

	/**
	 * synchronized since multiple ThreadRunner potentially could invoke this method
	 * simultaneously and they probably will be used to update ThreadLocal storages,
	 * which is not thread safe.
	 */
	protected synchronized void pre() {
		Executable pre = load.getPreExecution();
		if (pre != null) {
			pre.execute();
		}
	}

	/**
	 * synchronized since multiple ThreadRunner potentially could invoke this method
	 * simultaneously and they probably will be used to update ThreadLocal storages,
	 * which is not thread safe.
	 */
	protected synchronized void post() {
		Executable post = load.getPostExecution();
		if (post != null) {
			post.execute();
		}
	}

	protected TransactionExecutionResultBuffer getTransactionExecutionResultBuffer() {
		return load.getExecution().getTransactionExecutionResultBuffer();
	}

	/**
	 * @param <T> is the generic type for the return type ResultHandlerBuilder and the provided Transaction
	 * @param defaultName is the name of the transaction you are about to state. The
	 *                    name of the transaction can be changed after the
	 *                    transaction is made, in the handleResult method
	 * @param transaction is the transaction, done with implementing functional
	 *                    interface Transaction
	 * @return the builder instance
	 */
	public <T> ResultHandlerBuilder<T> load(String defaultName, Transaction<T> transaction) {
		RateLimiter limiterToBeUsed = null;

		if (load.getThrottler() != null) {
			limiterToBeUsed = load.getThrottler().getRateLimiter(Thread.currentThread());
		}

		ResultHandlerBuilder<T> resultHandlerBuilder = new ResultHandlerBuilder<T>(defaultName, transaction, this,
				limiterToBeUsed);

		return resultHandlerBuilder;
	}

	/**
	 * @param defaultName is the name of the transaction you are about to state. The
	 *                    name of the transaction can be changed after the
	 *                    transaction is made, in the handleResult method
	 * @param transaction is the transaction, done with implementing functional
	 *                    interface TransactionVoid
	 * @return the builder instance
	 */
	public ResultHandlerVoidBuilder load(String defaultName, TransactionVoid transaction) {
		RateLimiter limiterToBeUsed = null;

		if (load.getThrottler() != null) {
			limiterToBeUsed = load.getThrottler().getRateLimiter(Thread.currentThread());
		}

		ResultHandlerVoidBuilder resultHandlerBuilder = new ResultHandlerVoidBuilder(defaultName, transaction, this,
				limiterToBeUsed);

		return resultHandlerBuilder;
	}

	/**
	 * Get the amount per second equivalent to the provided Intensity
	 * 
	 * @param intensity is the Intensity to get equivalent amount per second out of
	 * @return a double value for the amount / second equivalent to intensity
	 */
	public static double getAmountPerSecond(Intensity intensity) {
		return getAmountPerSecond(intensity.getAmount(), intensity.getPerTimeUnit());
	}

	/**
	 * Get the amount of milliseconds equivalent to the provided amount and timeUnit
	 * 
	 * @param amount is the amount of the time to be converted
	 * @param unit is the TimeUnit of the time to be converted
	 * @return amount of millis
	 */
	public static long getMillis(long amount, TimeUnit unit) {
		long secondsForOneUnit = getTimeMultiplyer(unit);
		long amountInMillis = amount * secondsForOneUnit * 1000;
		return amountInMillis;
	}

	/**
	 * Get the amount per second equivalent to the amount per TimeUnit
	 * 
	 * @param amount to be converted to seconds
	 * @param timeUnit of the amount to be converted
	 * @return a double value for the amount / second equivalent to intensity
	 */
	private static double getAmountPerSecond(long amount, TimeUnit timeUnit) {

		// 1, minute: divider = 60, amountPerSecond = 1 /60
		long divider = getTimeMultiplyer(timeUnit);
		double amountPerSecond = ((double) amount) / divider;
		return amountPerSecond;
	}

	private static long getTimeMultiplyer(TimeUnit unit) {
		long multiplyer = 0;
		if (unit == TimeUnit.SECOND) {
			multiplyer = 1;
		} else if (unit == TimeUnit.MINUTE) {
			multiplyer = 60;
		} else if (unit == TimeUnit.HOUR) {
			multiplyer = 60 * 60;
		}
		return multiplyer;
	}

	/**
	 * Interface to handle the results of a performed Transaction
	 * 
	 * @param <R> type R will be the same type as for the Transaction
	 */
	@FunctionalInterface
	public interface ResultHandler<R> {

		/**
		 * Implementation of the handling of the Transaction's result
		 * 
		 * @param resultModel is the ResultModel of type R that consists of the result
		 *                    of the Transaction
		 */
		public void handle(ResultModel<R> resultModel);
	}

	/**
	 * Interface to handle the results of a performed Transaction
	 */
	@FunctionalInterface
	public interface ResultHandlerVoid {

		/**
		 * Implementation of the handling of the Transaction's result
		 * 
		 * @param resultModel is the ResultModel that consists of the result of the
		 *                    Transaction
		 */
		public void handle(ResultModelVoid resultModel);
	}
}
