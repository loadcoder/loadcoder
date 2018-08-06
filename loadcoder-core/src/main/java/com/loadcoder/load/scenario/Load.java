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
import java.util.Collections;
import java.util.List;

import com.loadcoder.load.intensity.Intensity;
import com.loadcoder.load.intensity.Throttler;

public class Load {

	final LoadScenario ls;

	StartedLoad startedLoad;
	private StopDecision stopDecision;
	private int amountOfthreads;
	private long rampup;

	boolean continueRuntimeResultUpdaterThread = true;

	private Throttler throttler;

	List<Thread> threads;

	Thread loadStateThread = new Thread(new LoadStateRunner(this));

	protected long timesExecuted = 0;

	Execution e;

	protected Execution getExecution() {
		return this.e;
	}

	protected void setExecution(Execution e) {
		this.e = e;
	}

	protected Executable getPreExecution() {
		return preExecution;
	}

	protected Executable getPostExecution() {
		return postExecution;
	}

	private Executable preExecution;
	private Executable postExecution;

	StopDecision defaultStopDecision = (a, b) -> {
		if (b > 0)
			return true;
		return false;
	};

	protected StartedLoad getStartedLoad() {
		return startedLoad;
	}

	protected Throttler getThrottler() {
		return throttler;
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

	// protected ResultFormatter getResultFormatter(){
	// return resultFormatter;
	// }
	protected long getRampup() {
		return rampup;
	}

	// protected long getStartTime(){
	// return startTime;
	// }
	protected StopDecision getStopDecision() {
		return stopDecision;
	}

	protected LoadScenario getLoadScenario() {
		return ls;
	}

	protected int getAmountOfThreads() {
		return amountOfthreads;
	}

	Object continueDecisionSynchronizer = new Object();

	public interface Transaction<T> {
		T transaction() throws Exception;
	}

	public interface TransactionVoid {
		void transaction() throws Exception;
	}

	protected Load(LoadScenario ls, StopDecision stopDecision, int amountOfthreads, long rampup,
			Executable preExecution, Executable postExecution, Intensity intensity) {
		this.ls = ls;
		this.stopDecision = stopDecision == null ? defaultStopDecision : stopDecision;
		this.amountOfthreads = amountOfthreads;
		this.rampup = rampup;
		this.preExecution = preExecution;
		this.postExecution = postExecution;

		List<Thread> temporaryThreadList = new ArrayList<Thread>();
		for (int i = 0; i < amountOfthreads; i++) {
			Thread thread = new Thread(new ThreadRunner(this));
			temporaryThreadList.add(thread);
		}
		threads = Collections.unmodifiableList(temporaryThreadList);

		if (intensity != null) {
			this.throttler = new Throttler(intensity, threads);
		}

	}

	List<Thread> getThreads() {
		return threads;
	}

	long calculateRampUpSleepTime(long rampup, int amountOfThreads) {
		long rampUpSleepTime = 0;
		if (amountOfThreads > 1)
			rampUpSleepTime = rampup / (amountOfThreads - 1);
		return rampUpSleepTime;
	}

	/**
	 * Start the load
	 * 
	 * @return a StartedLoad instance
	 */
	protected synchronized StartedLoad runLoad() {
		Thread scenarioStarter = new Thread(new ScenarioRunner(this));
		scenarioStarter.start();

		startedLoad = new StartedLoad(this);
		return startedLoad;
	}

	Thread getLoadStateThread() {
		return loadStateThread;
	}

}
