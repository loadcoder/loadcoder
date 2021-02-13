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

import com.loadcoder.load.exceptions.InvalidLoadStateException;
import com.loadcoder.load.intensity.Intensity;
import com.loadcoder.statics.ThrottleMode;
import com.loadcoder.statics.TimeUnit;

import static com.loadcoder.load.exceptions.ExceptionMessages.PREVIOUS_LOAD_STILL_RUNNING;

public class LoadBuilder {
	final LoadScenario ls;
	private StopDecision[] stopDecision;
	private int amountOfthreads = 1;
	private long rampupMillis;
	private Intensity intensity;
	private Intensity intensityIterations;

	/**
	 * Constructor for LoadBuilder
	 * 
	 * @param loadScenario is the LoadScenario that will be used for the Load about
	 *                     to be built
	 */
	public LoadBuilder(LoadScenario loadScenario) {
		this.ls = loadScenario;

		Load previousLoad = ls.getLoad();
		if (previousLoad != null) {
			StartedLoad previouslyStartedLoad = previousLoad.getStartedLoad();

			/*
			 * throw InvalidLoadStateException if there was a previously started load and it
			 * didnt finish yet
			 */
			if (previouslyStartedLoad != null && !previouslyStartedLoad.isScenarioTerminated())
				throw new InvalidLoadStateException(PREVIOUS_LOAD_STILL_RUNNING.toString());
		}
	}

	/**
	 * Build the Load from the LoadBuilders load definition
	 * 
	 * @return a Load instance
	 */
	public Load build() {

		Load l = new Load(ls, stopDecision, amountOfthreads, rampupMillis, intensity, intensityIterations);
		return l;
	}

	protected LoadScenario getLoadScenario() {
		return ls;
	}

	protected int getAmountOfthreads() {
		return amountOfthreads;
	}

	/**
	 * State the amount of threads for the load test.<br>
	 * Default is 1 thread.
	 * 
	 * @param amountOfthreads is the target amount of threads that should be started
	 *                        and run the load
	 * @return The builder instance
	 */
	public LoadBuilder amountOfThreads(int amountOfthreads) {
		this.amountOfthreads = amountOfthreads;
		return this;
	}

	protected Intensity getIntensity() {
		return intensity;
	}

	protected StopDecision[] getStopDecision() {
		return stopDecision;
	}

	/**
	 * State when the load is going to be stopped. This is done implementing a
	 * StopDecision. See the built in StopDecisions in
	 * com.loadcoder.statics.StopDesisions.<br>
	 * Default is a StopDecision that will stop the load after one iteration.
	 * 
	 * @param stopDecision is the target amount of threads that should be started
	 *                     and run the load
	 * @return The builder instance
	 */
	public LoadBuilder stopDecision(StopDecision... stopDecision) {
		this.stopDecision = stopDecision;
		return this;
	}

	protected long getRampup() {
		return rampupMillis;
	}

	protected Intensity getThrottleIteration() {
		return intensityIterations;
	}

	/**
	 * rampup is the process of increasing the running amount of threads from 1 to
	 * the stated amount of threads for the load test over rampupMillis
	 * milliseconds.<br>
	 * Default is no rampup.
	 * 
	 * @param rampupMillis is the duration of the rampup in milliseconds
	 * @return the builder instance
	 */
	public LoadBuilder rampup(long rampupMillis) {
		this.rampupMillis = rampupMillis;
		return this;
	}

	/**
	 * The intensity of transactions to be executed can be throttled (limited) by
	 * using this method. So if the throttle is set to 5 PER_SECOND with
	 * ThrottleMode SHARED, the total throughput wont't be over 5 TPS.<br>
	 * Default is no throttle.
	 * 
	 * 
	 * @param amount       of stated PerTimeUnit will be the throttle limit
	 * @param perTimeUnit  is the unit of which to throttle limit is defined by
	 * @param throttleMode states whether the limit should be shared (SHARED) among
	 *                     the threads, or if the there should be a separate
	 *                     throttle per thread (PER_THREAD) The difference between
	 *                     these two is that if SHARED is used, the total throughput
	 *                     won't be higher than the defined limit, no matter the
	 *                     amount of threads. If PER_THREAD is used, the separate
	 *                     thread can't produce a higher throughput than the limit,
	 *                     but the total througput for all threads together will
	 *                     theoretically be limit * amountOfThreads
	 * 
	 * @return the builder instance
	 */
	public LoadBuilder throttle(int amount, TimeUnit perTimeUnit, ThrottleMode throttleMode) {
		this.intensity = new Intensity(amount, perTimeUnit, throttleMode);
		return this;
	}

	/**
	 * 
	 * The intensity of iterations to be executed can be throttled (limited) by
	 * using this method. So if the throttle is set to 5 PER_SECOND with
	 * ThrottleMode SHARED, the intensity of starting new iteration won't be over 5
	 * per second<br>
	 * Default is no throttle.
	 * 
	 * 
	 * @param amount       of stated PerTimeUnit will be the throttle limit
	 * @param perTimeUnit  is the unit of which to throttle limit is defined by
	 * @param throttleMode states whether the limit should be shared (SHARED) among
	 *                     the threads, or if the there should be a separate
	 *                     throttle per thread (PER_THREAD) The difference between
	 *                     these two is that if SHARED is used, the total throughput
	 *                     won't be higher than the defined limit, no matter the
	 *                     amount of threads. If PER_THREAD is used, the separate
	 *                     thread can't produce a higher throughput than the limit,
	 *                     but the total througput for all threads together will
	 *                     theoretically be limit * amountOfThreads
	 * 
	 * @return the builder instance
	 */
	public LoadBuilder throttleIterations(int amount, TimeUnit perTimeUnit, ThrottleMode throttleMode) {
		this.intensityIterations = new Intensity(amount, perTimeUnit, throttleMode);
		return this;
	}

}