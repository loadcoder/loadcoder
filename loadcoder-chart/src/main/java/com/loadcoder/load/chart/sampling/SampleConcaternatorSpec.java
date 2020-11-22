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
package com.loadcoder.load.chart.sampling;

public class SampleConcaternatorSpec {
	final long howLongAfterStartShouldThisBeAdded;

	final int amoutThatShouldBeConcaternated;
	
	SampleConcaternatorRunDecider sampleConcaternatorRunDecider;

	public String toString(){
		return "{relStart:" + howLongAfterStartShouldThisBeAdded +
				"concat:" + amoutThatShouldBeConcaternated +"}";
	}
	public long getHowLongAfterStartShouldThisBeAdded() {
		return howLongAfterStartShouldThisBeAdded;
	}

	public int getAmoutThatShouldBeConcaternated() {
		return amoutThatShouldBeConcaternated;
	}

	public SampleConcaternatorRunDecider getSampleConcaternatorRunDecider() {
		return sampleConcaternatorRunDecider;
	}

	public SampleConcaternatorSpec(long howLongAfterStartShouldThisBeAdded, int amoutThatShouldBeConcaternated,
			SampleConcaternatorRunDecider sampleConcaternatorRunDecider) {
		super();
		this.howLongAfterStartShouldThisBeAdded = howLongAfterStartShouldThisBeAdded;
		this.amoutThatShouldBeConcaternated = amoutThatShouldBeConcaternated;
		this.sampleConcaternatorRunDecider = sampleConcaternatorRunDecider;
	}
}
