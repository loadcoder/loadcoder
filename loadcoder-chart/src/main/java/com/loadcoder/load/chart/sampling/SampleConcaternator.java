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
package com.loadcoder.load.chart.sampling;

import com.loadcoder.load.chart.data.Range;

public class SampleConcaternator {

	SampleConcaternatorRunDecider sampleConcaternatorRunDecider;

	Range oldRange;
	
	int amountToConcaternate = 2;
	
	Range newRange;

	public SampleConcaternatorRunDecider getSampleConcaternatorRunDecider() {
		return sampleConcaternatorRunDecider;
	}

	public int getAmountToConcaternate() {
		return amountToConcaternate;
	}

	public SampleConcaternator(Range oldRange, Range newRange, int amountToConcaternate,
			SampleConcaternatorRunDecider sampleConcaternatorRunDecider) {
		this.oldRange = oldRange;
		this.newRange = newRange;
		this.amountToConcaternate = amountToConcaternate;
		this.sampleConcaternatorRunDecider = sampleConcaternatorRunDecider;
	}

	public Range getOldRange() {
		return oldRange;
	}

	public Range getNewRange() {
		return newRange;
	}

	public void setNewRange(Range newRange) {
		this.newRange = newRange;
	}
}
