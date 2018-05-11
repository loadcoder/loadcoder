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
package com.loadcoder.load.chart.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.sampling.Sample;

public class ChartLogicTest {

	@Test
	public void addSurroundingTimestampsAsUpdatesTest() {

		long sampleLength = 1000;
		HashSet<Long> hashesGettingUpdated = new HashSet<Long>();
		HashSet<Long> sampleTimestamps = new HashSet<Long>();
		sampleTimestamps.add(5000L);
		sampleTimestamps.add(9000L);
		List<Range> ranges = Arrays.asList(new Range(Long.MIN_VALUE, Long.MAX_VALUE, sampleLength));
		ChartLogic.addSurroundingTimestampsAsUpdates(hashesGettingUpdated, 7000, 1000, 15000, ranges,
				sampleLength, sampleTimestamps, new HashMap<Long, Sample>());

		Assert.assertEquals(hashesGettingUpdated.size(), 2);
	}
}
