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
package com.loadcoder.load.intensity;

import com.loadcoder.statics.ThrottleMode;
import com.loadcoder.statics.TimeUnit;

public class Intensity{
	private final int amount;
	private final TimeUnit perTimeUnit;
	private final ThrottleMode throttleMode;

	
	/**
	 * Constructor for the Intensity
	 * amount = 1 and perTimeUnit = com.loadcoder.statics.Time.PER_SECOND will give
	 * an intensity equivalent to 1 per second.
	 * @param amount is the value part the expressed intensity
	 * @param perTimeUnit is the unit part of the expressed intensity
	 * @param throttleMode is mode the Intensity should be applied for
	 */
	public Intensity(int amount, TimeUnit perTimeUnit, ThrottleMode throttleMode){
		this.amount = amount;
		this.perTimeUnit = perTimeUnit;
		this.throttleMode = throttleMode;
	}

	/**
	 * @return the amount of the defined Intensity
	 */
	public long getAmount() {
		return amount;
	}
	
	/**
	 * @return the TimeUnit of the defined Intensity
	 */
	public TimeUnit getPerTimeUnit() {
		return perTimeUnit;
	}
	
	
	/**
	 * @return the ThrottleMode of the defined Intensity
	 */
	public ThrottleMode getThrottleMode() {
		return throttleMode;
	}
}
