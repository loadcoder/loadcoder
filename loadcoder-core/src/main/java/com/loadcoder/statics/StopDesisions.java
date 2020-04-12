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
package com.loadcoder.statics;

import com.loadcoder.load.scenario.StopDecision;

/**
 * Use StopDecisions instead. This class will be removed in coming versions.
 */
@Deprecated
public class StopDesisions {

	/**
	 * This method is deprecated and will be removed in coming versions.
	 * It is replaced by StopDecisions.iterations
	 */
	@Deprecated
	public static final StopDecision iterations(int targetIterationsToBeMade) {
		StopDecision s2 = (startTime, madeIterations) -> {
			if (madeIterations < targetIterationsToBeMade)
				return false;
			return true;
		};
		return s2;
	}

	/**
	 * This method is deprecated and will be removed in coming versions.
	 * It is replaced by StopDecisions.duration
	 */
	@Deprecated
	public static final StopDecision duration(long executionTimeMillis) {
		return StopDecisions.duration(executionTimeMillis);
	}

}
