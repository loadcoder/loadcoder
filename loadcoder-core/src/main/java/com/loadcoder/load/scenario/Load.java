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

import com.loadcoder.load.intensity.Intensity;
import com.loadcoder.load.intensity.LoadThreadsSynchronizer;
import com.loadcoder.load.intensity.Throttler;
import com.loadcoder.load.scenario.stopdecision.StopOnErrorLimit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Load {

	private final LoadScenario ls;

	private StartedLoad startedLoad;
	private StopDecision[] stopDecision;
	private int amountOfthreads;
	private long rampup;

	private Throttler throttler;
	private Throttler throttlerIterations;

	private List<Thread> threads;

	private Thread loadStateThread = new Thread(new LoadStateRunner(this));

	protected long timesExecuted = 0;

	private Execution e;

	LoadThreadsSynchronizer loadThreadsSynchronizer = new LoadThreadsSynchronizer();

	boolean tearDownPerformed = false;

	void tearDownLoad() {
		synchronized (this) {
			if (!tearDownPerformed) {
				loadThreadsSynchronizer.releaseAllThreadSynchronizers();
				tearDownPerformed = true;
			}
		}

	}

	public LoadThreadsSynchronizer getLoadThreadsSynchronizer() {
		return loadThreadsSynchronizer;
	}

	protected Execution getExecution() {
		return this.e;
	}

	protected void setExecution(Execution e) {
		this.e = e;
	}

	private StopDecision defaultStopDecision = (a, b) -> {
		if (b > 0)
			return true;
		return false;
	};

	StopDecision[] defaultStopDecisions = {defaultStopDecision};
	protected StartedLoad getStartedLoad() {
		return startedLoad;
	}

	protected Throttler getThrottler() {
		return throttler;
	}

	protected Throttler getThrottlerIterations() {
		return throttlerIterations;
	}

	protected long getTimesExecuted() {
		return timesExecuted;
	}

	protected synchronized void increaseTimesExecuted() {
		timesExecuted++;
	}

	public interface Executable {
		public void execute();
	}

	protected long getRampup() {
		return rampup;
	}

	protected StopDecision[] getStopDecision() {
		return stopDecision;
	}

	protected LoadScenario getLoadScenario() {
		return ls;
	}

	protected int getAmountOfThreads() {
		return amountOfthreads;
	}

	/**
	 * Interface for defining a Transaction with return type T
	 * 
	 * @param <T> type of the Transaction, which will be the type returned from the
	 *        method transaction()
	 */
	@FunctionalInterface
	public interface Transaction<T> {

		/**
		 * Implementation of the Transaction with return type T
		 * 
		 * @return an instance of type T
		 * @throws Exception whatever {@code java.lang.Exception} that a transaction
		 *                   might throw. Theses exceptions will be caught in the the
		 *                   {@code ResultHandlerBuilder} and provided as a part of the
		 *                   {@code ResultModel} that comes as the parameter for the
		 *                   {@code handleResult} call
		 */
		T transaction() throws Exception;
	}

	/**
	 * Interface for defining a Transaction with void return type
	 */
	@FunctionalInterface
	public interface TransactionVoid {
		/**
		 * Implementation of the Transaction with return type void
		 * 
		 * @throws Exception whatever {@code java.lang.Exception} that a transaction
		 *                   might throw. Theses exceptions will be caught in the the
		 *                   {@code ResultHandlerVoidBuilder} and provided as a part of
		 *                   the {@code ResultModel} that comes as the parameter for the
		 *                   {@code handleResult} call
		 */
		void transaction() throws Exception;
	}

	protected Load(LoadScenario ls, StopDecision[] stopDecision, int amountOfthreads, long rampup, Intensity intensity,
				   Intensity intensityIterations) {
		this.ls = ls;
		this.ls.setLoad(this);
		this.stopDecision = stopDecision == null ? defaultStopDecisions : stopDecision;
		this.amountOfthreads = amountOfthreads;
		this.rampup = rampup;

		List<Thread> temporaryThreadList = new ArrayList<Thread>();
		for (int i = 0; i < amountOfthreads; i++) {
			Thread thread = new Thread(new ThreadRunner(this));
			temporaryThreadList.add(thread);
		}
		threads = Collections.unmodifiableList(temporaryThreadList);

		if (intensity != null) {
			this.throttler = new Throttler(intensity, threads);
		}

		if (intensityIterations != null) {
			this.throttlerIterations = new Throttler(intensityIterations, threads);
		}
	}

	protected List<Thread> getThreads() {
		return threads;
	}

	/**
	 * Start the load
	 * 
	 * @return a StartedLoad instance
	 */
	protected synchronized StartedLoad runLoad() {
		Thread scenarioStarter = new Thread(new LoadRunner(this));
		scenarioStarter.start();

		startedLoad = new StartedLoad(this);
		return startedLoad;
	}

	protected Thread getLoadStateThread() {
		return loadStateThread;
	}

}
