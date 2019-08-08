/*******************************************************************************
 * Copyright (C) 2019 Stefan Vahlgren at Loadcoder
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

import com.loadcoder.load.scenario.StopDecision;

public class StopDecisions {

	/**
	 * Decision of whether to continue the test or not in regards to how many
	 * iterations of the LoadScenario that have been made so far. If the amount of
	 * made iterations is higher than the provided amountOfIterations, the test will
	 * stop.
	 * 
	 * @param targetIterationsToBeMade
	 *            it the target total amount of
	 * @return ContinueDecision
	 */
	public static final StopDecision iterations(int targetIterationsToBeMade) {
		StopDecision s2 = (startTime, madeIterations) -> {
			if (madeIterations < targetIterationsToBeMade)
				return false;
			return true;
		};
		return s2;
	}

	/**
	 * Decision of whether to continue the test or not in regards to how long the
	 * test been running so far. If the execution of the test becomes longer than
	 * the provided executionTimeMillis, the test will stop.
	 * 
	 * @param executionTimeMillis
	 *            is the target duration for the test in milliseconds
	 * @return ContinueDecision
	 */
	public static final StopDecision duration(long executionTimeMillis) {
		StopDecision s2 = (startTime, madeIterations) -> {
			long now = System.currentTimeMillis();
			long endTime = (startTime + executionTimeMillis);
			long diff = now - endTime;
			if (diff < 0)
				return false;
			return true;
		};
		return s2;
	}

}
