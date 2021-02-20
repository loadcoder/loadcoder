/*******************************************************************************
 * Copyright (C) 2021 Team Loadcoder
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
package com.loadcoder.load.scenario.stopdecision;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.testng.annotations.Test;

import com.loadcoder.load.scenario.RuntimeStatistics;

public class StopOnErrorLimitTest {

	@Test
	public void noStopWhenNoFails() {
		RuntimeStatistics runtimeStatistics = mock(RuntimeStatistics.class);
		StopOnErrorLimit stopOnErrorLimit = new StopOnErrorLimit(runtimeStatistics).maxAmountOfFails(0);
		assertFalse(stopOnErrorLimit.stopLoad(0, 0));
	}

	@Test
	public void stopWhenOneFails() {
		RuntimeStatistics runtimeStatistics = mock(RuntimeStatistics.class);
		when(runtimeStatistics.getAmountOfFails()).thenReturn(1);
		StopOnErrorLimit stopOnErrorLimit = new StopOnErrorLimit(runtimeStatistics).maxAmountOfFails(0);
		assertTrue(stopOnErrorLimit.stopLoad(0, 0));
	}

	@Test
	public void noStopWhenFailRate0() {
		RuntimeStatistics runtimeStatistics = mock(RuntimeStatistics.class);
		when(runtimeStatistics.getFailRate()).thenReturn(0.0);
		StopOnErrorLimit stopOnErrorLimit = new StopOnErrorLimit(runtimeStatistics).maxFailRate(0.01);
		assertFalse(stopOnErrorLimit.stopLoad(0, 0));
	}

	@Test
	public void stopWhenFailRateGreaterThan0() {
		RuntimeStatistics runtimeStatistics = mock(RuntimeStatistics.class);
		when(runtimeStatistics.getFailRate()).thenReturn(0.1);
		StopOnErrorLimit stopOnErrorLimit = new StopOnErrorLimit(runtimeStatistics).maxFailRate(0.01);
		assertTrue(stopOnErrorLimit.stopLoad(0, 0));
	}

	@Test
	public void noStopWhenNoFailsAnFailRate0() {
		RuntimeStatistics runtimeStatistics = mock(RuntimeStatistics.class);
		when(runtimeStatistics.getFailRate()).thenReturn(0.1);
		StopOnErrorLimit stopOnErrorLimit = new StopOnErrorLimit(runtimeStatistics).maxFailRate(0.01)
				.maxAmountOfFails(0);
		assertTrue(stopOnErrorLimit.stopLoad(0, 0));
	}
}
