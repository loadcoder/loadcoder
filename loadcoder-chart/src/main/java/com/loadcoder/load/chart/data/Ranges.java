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
package com.loadcoder.load.chart.data;

import java.util.ArrayList;
import java.util.List;

public class Ranges {

	private final List<Range> ranges = new ArrayList<Range>();

	public void clearRanges() {
		ranges.clear();
	}

	public void addRange(Range range) {
		ranges.add(range);
	}

	public Range getRange(int index) {
		return ranges.get(index);
	}

	public Range getLastRange() {
		return ranges.get(ranges.size() - 1);
	}

	public boolean isRangesEmpty() {
		return ranges.isEmpty();
	}

	// only used for test
	public Range lookupCorrectRange(long ts) {
		for (Range range : ranges) {
			if (range.isTimestampInThisRange(ts))
				return range;
		}
		return null;
	}

	public long findSampleLength(long timeStamp) {
		for (Range range : ranges) {
			if (range.getStart() <= timeStamp && range.getEnd() >= timeStamp) {
				return range.getSampleLength();
			}
		}
		throw new RuntimeException("no matching range found. This should never happend!");
	}
}
