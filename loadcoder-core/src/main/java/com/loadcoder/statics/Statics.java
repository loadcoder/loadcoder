/*******************************************************************************
 * Copyright (C) 2020 Stefan Vahlgren at Loadcoder
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
package com.loadcoder.statics;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.scenario.StopDecision;
import com.loadcoder.result.Result;

public class Statics {

	private static Logger log = LoggerFactory.getLogger(Statics.class);

	public String getConfig(String key) {
		return getConfiguration(key);
	}
	
	public static String getConfiguration(String key) {
		return Configuration.getConfig(key);
	}
	
	public static Map<String, String> getMatchingConfiguration(String keyMatchingRegexp) {
		Map<String, String> result = Configuration.getConfigurationInstance().getMatchingConfig(keyMatchingRegexp);
		return result;
	}
	/**
	 * Decision of whether to continue the test or not in regards to how many
	 * iterations of the LoadScenario that have been made so far. If the amount of
	 * made iterations is higher than the provided amountOfIterations, the test will
	 * stop.
	 * 
	 * @param targetIterationsToBeMade it the target total amount of
	 * @return ContinueDecision
	 */
	public static final StopDecision iterations(int targetIterationsToBeMade) {
		return StopDecisions.iterations(targetIterationsToBeMade);
	}

	/**
	 * Decision of whether to continue the test or not in regards to how long the
	 * test been running so far. If the execution of the test becomes longer than
	 * the provided executionTimeMillis, the test will stop.
	 * 
	 * @param executionTimeMillis is the target duration for the test in
	 *                            milliseconds
	 * @return ContinueDecision
	 */
	public static final StopDecision duration(long executionTimeMillis) {
		return StopDecisions.duration(executionTimeMillis);
	}

	public static void printSimpleSummary(Result result, String resultName) {
		SummaryUtils.printSimpleSummary(result, resultName);
	}

	/*
	 * Throttling
	 */

	/**
	 * If using PER_THREAD, the intensity will be per thread. So if using PER_THREAD
	 * with 10 / SECOND and with two threads running, the total load will be 20 /
	 * SECOND
	 */
	public static final ThrottleMode PER_THREAD = ThrottleMode.PER_THREAD;

	/**
	 * If using SHARED, the intensity will be shared among all threads. So if using
	 * SHARED with 10 / SECOND and with two threads running, the total load will be
	 * 10 / SECOND
	 */
	public static final ThrottleMode SHARED = ThrottleMode.SHARED;

	/*
	 * Time
	 */
	/**
	 * One second in milliseconds
	 */
	public static final long SECOND = 1_000;

	/**
	 * One minute in milliseconds
	 */
	public static final long MINUTE = 60 * SECOND;

	/**
	 * One hour in milliseconds
	 */
	public static final long HOUR = 60 * MINUTE;

	/**
	 * One day in milliseconds
	 */
	public static final long DAY = 24 * HOUR;

	public static final TimeUnit PER_SECOND = TimeUnit.SECOND;
	public static final TimeUnit PER_MINUTE = TimeUnit.MINUTE;
	public static final TimeUnit PER_HOUR = TimeUnit.HOUR;
}
