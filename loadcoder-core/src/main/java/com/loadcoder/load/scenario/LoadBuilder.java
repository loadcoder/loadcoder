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

import static com.loadcoder.load.exceptions.ExceptionMessages.PreviousLoadStillRunning;

import com.loadcoder.load.exceptions.InvalidLoadStateException;
import com.loadcoder.load.intensity.Intensity;
import com.loadcoder.load.scenario.Load.Executable;
import com.loadcoder.statics.ThrottleMode;
import com.loadcoder.statics.TimeUnit;

public class LoadBuilder {
	final LoadScenario ls;
	private StopDecision stopDecision;
	private int amountOfthreads = 1;
	private long rampupMillis;
	private Executable preExecution;
	private Executable postExecution;
	private Intensity intensity;

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
			 */
			if(previouslyStartedLoad != null &&! previouslyStartedLoad.isScenarioTerminated())
				throw new InvalidLoadStateException(PreviousLoadStillRunning.toString());
		}
			
		Load l = new Load(
				ls, 
				stopDecision,
				amountOfthreads,
				rampupMillis,
				preExecution,
				postExecution,
				intensity 
				);
		
		ls.setLoad(l);
		return l;
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

	protected StopDecision getStopDecision() {
		return stopDecision;
	}

	public LoadBuilder stopDecision(StopDecision stopDecision) {
		this.stopDecision = stopDecision;
		return this;
	}

	protected Executable getPreExecution() {
		return preExecution;
	}

	protected Executable getPostExecution() {
		return postExecution;
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
		TimeUnit timeUnit;
		
		Rampup(long amount, TimeUnit timeUnit){
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

}