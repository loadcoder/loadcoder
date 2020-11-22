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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.testng.TestNGBase;

public class GroupTest extends TestNGBase {

	@Test
	public void testToGetSample() {
		long firstTs = Group.calculateFirstTs(25_005, 1000);
		Assert.assertEquals(25_000, firstTs);
	}

	// 4 4 4 4 2 2 (2) 2 1 1 1
	@Test
	public void testPositiveConcaternation() {
		long firstTs = Group.calculateFirstTs(2000 + 5, 2000);
		Assert.assertEquals(2_000, firstTs);
		firstTs = Group.calculateFirstTs(2000 + 0, 2000);
		Assert.assertEquals(2_000, firstTs);
	}

	// -1 -1 -1 1 1 1
	@Test
	public void testNegative() {
		long firstTs = Group.calculateFirstTs(2000 + 5, 1000);
		Assert.assertEquals(2_000, firstTs);
		firstTs = Group.calculateFirstTs(2000 + 0, 2000);
		Assert.assertEquals(2_000, firstTs);
	}
}
