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

import static com.loadcoder.load.exceptions.ExceptionMessages.LoadAlreadyStarted;
import static com.loadcoder.load.exceptions.ExceptionMessages.PreviousLoadStillRunning;
import static com.loadcoder.load.exceptions.ExceptionMessages.ScenarioConnectedToOtherLoad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.loadcoder.load.exceptions.InvalidLoadStateException;
//import com.loadcoder.load.Scenario;
//import com.loadcoder.load.Scenario.ContinueToExecute;
//import com.loadcoder.load.Scenario.ExceptionUser;
//import com.loadcoder.load.Scenario.Executable;
//import com.loadcoder.load.Scenario.ExecutableMeasure;
import com.loadcoder.load.intensity.Intensity;
import com.loadcoder.load.intensity.ThrottleMode;
import com.loadcoder.load.intensity.Throttler;
//import com.loadcoder.load.measure.Result.ResultDestination;
import com.loadcoder.load.measure.ResultFormatter;
import com.loadcoder.load.measure.TransactionExecutionResult;
import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.statics.PerTimeUnit;
import com.loadcoder.statics.TimeUnit;

public class Load {

	final LoadScenario ls;
	
	StartedLoad startedLoad;
	private ContinueDecision continueToExecute;
	private int amountOfthreads;
	private long rampup;
	private ResultFormatter resultFormatter;
	
	boolean continueRuntimeResultUpdaterThread = true;

	private Throttler throttler;

	List<Thread> threads;
	
	private long startTime;
	
	Thread runtimeResultUpdaterThread;

	RuntimeDataUser user;
	
	Thread loadStateThread = new Thread(new LoadStateRunner(this));

	List<List<TransactionExecutionResult>> runtimeResultList = new ArrayList<List<TransactionExecutionResult>>();
	
	protected long timesExecuted = 0;

	protected Executable getPreExecution() {
		return preExecution;
	}
	
	protected Executable getPostExecution() {
		return postExecution;
	}

	private Executable preExecution;
	private Executable postExecution;
	
	ContinueDecision defaultContinueToExecute = (a, b)->{
		if(b > 0)
			return false;
		return true;
	};
	
	TransactionExecutionResultBuffer transactionExecutionResultBuffer = new TransactionExecutionResultBuffer();
	
	private StartedLoad getStartedLoad() {
		return startedLoad;
	}
	protected Throttler getThrottler() {
		return throttler;
	}

	protected TransactionExecutionResultBuffer getTransactionExecutionResultBuffer() {
		return transactionExecutionResultBuffer;
	}

	protected void start(){
		this.startTime = System.currentTimeMillis();
	}
	
	protected long getTimesExecuted(){
		return timesExecuted;
	}
	protected synchronized void increaseTimesExecuted(){
		timesExecuted++;
	}
	public interface Executable {
		public void execute();
	}
	
	public interface ContinueDecision {
		public boolean continueToExecute(long startTime, long timesExecuted);
	}
	
	public interface ExceptionUser {
		public void handleRunTimeException(Exception e);
	}
	
	protected ResultFormatter getResultFormatter(){
		return resultFormatter;
	}
	protected long getRampup(){
		return rampup;
	}
	
	protected List<List<TransactionExecutionResult>> getRuntimeResultList() {
		return runtimeResultList;
	}
	
	protected long getStartTime(){
		return startTime;
	}
	protected ContinueDecision getContinueToExecute(){
		return continueToExecute;
	}
	protected LoadScenario getLoadScenario(){
		return ls;
	}
	
	protected int getAmountOfThreads(){
		return amountOfthreads;
	}
	
	Object continueDecisionSynchronizer = new Object();
	
	public interface Transaction<T>{
		T transaction() throws Exception;
	}

	public interface TransactionVoid{
		void transaction() throws Exception;
	}

	private Load(	 
			 LoadScenario ls,
				ContinueDecision continueToExecute,
				int amountOfthreads,
				long rampup,
				Executable preExecution,
				Executable postExecution,
				Intensity intensity,
				ResultFormatter resultFormatter,
				RuntimeDataUser user
				){
		this.ls = ls;
		this.continueToExecute = continueToExecute == null ? defaultContinueToExecute : continueToExecute ;
		this.amountOfthreads = amountOfthreads;
		this.rampup = rampup;
		this.preExecution = preExecution;
		this.postExecution = postExecution;
		this.resultFormatter = resultFormatter == null ? TransactionExecutionResult.resultStringFormatterDefault : resultFormatter;
		this.user = user;
		
		List<Thread> temporaryThreadList = new ArrayList<Thread>();
		for(int i =0; i<amountOfthreads; i++){
			Thread thread = new Thread(new ThreadRunner(this) );	
			temporaryThreadList.add(thread);
		}
		threads = Collections.unmodifiableList(temporaryThreadList);
		
		if(intensity != null){
			this.throttler = new Throttler(intensity, threads);
		}
		
		if(user != null) {
			runtimeResultUpdaterThread = new Thread(new RuntimeResultUpdaterRunner(this, user));
		}
	}
	
	public static class LoadBuilder {
		final LoadScenario ls;
		private ContinueDecision continueToExecute;
		private int amountOfthreads = 1;
		private long rampupMillis;
		private Executable preExecution;
		private Executable postExecution;
		private Intensity intensity;
		private ResultFormatter resultFormatter;
		private RuntimeDataUser user;

		public LoadBuilder (LoadScenario ls){
			this.ls = ls;
		}
		
		
		/**
		 * Build the Load from the LoadBuilders load definition
		 * 
		 * @return a Load instance 
		 */
		public Load build(){
			Load previousLoad = ls.getLoad();
			if(previousLoad != null) {
				StartedLoad previouslyStartedLoad = previousLoad.getStartedLoad();
				
				/*
				 * throw InvalidLoadStateException if there was a previously started load
				 * and it didnt finish yet
				 * 
				 */
				if(previouslyStartedLoad != null &&! previouslyStartedLoad.isScenarioTerminated())
					throw new InvalidLoadStateException(PreviousLoadStillRunning.toString());
			}
				
			Load l = new Load(
					ls, 
					continueToExecute,
					amountOfthreads,
					rampupMillis,
					preExecution,
					postExecution,
					intensity, 
					resultFormatter,
					user
					);
			
			ls.setLoad(l);
			return l;
		}

		protected ResultFormatter getResultFormatter() {
			return resultFormatter;
		}

		protected int getAmountOfthreads() {
			return amountOfthreads;
		}

		
		/**
		 * state the amount of threads for the load test
		 * 
		 * @param amountOfthreads is the target amount of threads that should be started and run the load
		 * @return the builder instance
		 */
		public LoadBuilder amountOfThreads(int amountOfthreads) {
			this.amountOfthreads = amountOfthreads;
			return this;
		}

		protected Intensity getIntensity() {
			return intensity;
		}

		protected ContinueDecision getContinueToExecute() {
			return continueToExecute;
		}

		public LoadBuilder continueCriteria(ContinueDecision continueToExecute) {
			this.continueToExecute = continueToExecute;
			return this;
		}

		protected Executable getPreExecution() {
			return preExecution;
		}

		protected Executable getPostExecution() {
			return postExecution;
		}

		public LoadBuilder resultUser(RuntimeDataUser user){
			this.user = user;
			return this;
		}
		public LoadBuilder preExecution(Executable preExecution) {
			this.preExecution = preExecution;
			return this;
		}

		public LoadBuilder postExecution(Executable postExecution) {
			this.postExecution = postExecution;
			return this;
		}

		protected long getRampup() {
			return rampupMillis;
		}

		public class Rampup{
			long amount;
			PerTimeUnit timeUnit;
			
			Rampup(long amount, PerTimeUnit timeUnit){
				this.amount = amount;
				this.timeUnit = timeUnit;
			}
		}
		
		/**
		 * rampup is process of increasing the running amount of thread from 1 to
		 * the stated amount of threads for the load test over rampupMillis milliseconds.
		 * 
		 * Note that the rampup functionality only ramps up threads, not the intensity.
		 * 
		 * @param rampupMillis is the duration of the rampup in milliseconds
		 * @return the builder instance
		 */
		public LoadBuilder rampup(long rampupMillis) {
			this.rampupMillis = rampupMillis;
			return this;
		}

		/**
 
		 * 
		 * @param rampupMillis is the duration of the rampup in milliseconds
		 * @return the builder instance
		 */
		
		
		/**
		 * The throughput can be throttled by using this method.
		 * So if the throttle is set to 5 PerSecond with SHARED ThrottleMode
		 * The total throughput wont't be over 5 TPS
		 * 
		 * @param amount of stated PerTimeUnit will be the throttle limit 
		 * @param perTimeUnit is the unit of which to throttle limit is defined by
		 * @param throttleMode states whether the limit should be shared (SHARED) among
		 * the threads, or if the there should be a separate throttle per thread (PER_THREAD)
		 * The difference between these two is that if SHARED is used, the total throughput won't
		 * be higher than the defined limit, no matter the amount of threads.
		 * If PER_THREAD is used, the separate thread can't produce a higher throughput than the limit,
		 * but the total througput for all threads together will theoretically be limit * amountOfThreads
		 * 
		 * @return the builder instance
		 */
		public LoadBuilder throttle(int amount, TimeUnit perTimeUnit, ThrottleMode throttleMode) {
			this.intensity = new Intensity(amount, perTimeUnit, throttleMode);
			return this;
		}

		public LoadBuilder reportResultAs(ResultFormatter resultFormatter) {
			this.resultFormatter = resultFormatter;
			return this;
		}
	}

	List<Thread> getThreads(){
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
	 * @return
	 * a StartedLoad instance
	 */
	public synchronized StartedLoad runLoad() {
		if (startedLoad != null) {
			throw new InvalidLoadStateException(LoadAlreadyStarted.toString());
		}
		
		Load setLoad = ls.getLoad();
		if(! setLoad.equals(this)) {
			throw new InvalidLoadStateException(ScenarioConnectedToOtherLoad.toString());
		}
		
		if(runtimeResultUpdaterThread != null) {
			runtimeResultUpdaterThread.start();
		}
		
		startTime = System.currentTimeMillis();
		Thread scenarioStarter = new Thread(new ScenarioRunner(this) );
		scenarioStarter.start();

		startedLoad = new StartedLoad(this);
		return startedLoad;
	}


	Thread getLoadStateThread(){
		return loadStateThread;
	}

	protected Thread getRuntimeResultUpdaterThread(){
		return runtimeResultUpdaterThread;
	}
}
