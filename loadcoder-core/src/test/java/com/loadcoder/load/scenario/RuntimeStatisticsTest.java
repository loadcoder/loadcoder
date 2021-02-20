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
package com.loadcoder.load.scenario;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeStatisticsTest {

	long now = System.currentTimeMillis();

	@Test
	public void isStatsCorrectWhenEmptyMap() {
		RuntimeStatistics runtimeStatistics = new RuntimeStatistics();
		runtimeStatistics.useData(new HashMap<>());

		assertEquals(runtimeStatistics.getAmountOfFails(), 0);
		assertEquals(runtimeStatistics.getFailRate(), 0.0);
	}

	@Test
	public void isStatsCorrectWhenAllSucceeds() {
		RuntimeStatistics runtimeStatistics = new RuntimeStatistics();
		runtimeStatistics.useData(getTransListAllSucceeds(2, 1));

		assertEquals(runtimeStatistics.getAmountOfFails(), 0);
		assertEquals(runtimeStatistics.getFailRate(), 0.0);
	}

	@Test
	public void isStatsCorrectWhenAllFails() {
		RuntimeStatistics runtimeStatistics = new RuntimeStatistics();
		runtimeStatistics.useData(getTransListAllFails(2, 1));

		assertEquals(runtimeStatistics.getAmountOfFails(), 2);
		assertEquals(runtimeStatistics.getFailRate(), 1.0);
	}

	@Test
	public void isStatsCorrectAfter2StatsUpdates() {
		RuntimeStatistics runtimeStatistics = new RuntimeStatistics();
		runtimeStatistics.useData(getTransListAllFails(2, 1));
		runtimeStatistics.useData(getTransListAllSucceeds(2, 1));

		assertEquals(runtimeStatistics.getAmountOfFails(), 2);
		assertEquals(runtimeStatistics.getFailRate(), 0.5);
	}

	public Map<String, List<TransactionExecutionResult>> getTransListAllSucceeds(int amountOfTypes,
			int failsForEachType) {
		return getTransList(amountOfTypes, failsForEachType, true);
	}

	public Map<String, List<TransactionExecutionResult>> getTransListAllFails(int amountOfTypes, int failsForEachType) {
		return getTransList(amountOfTypes, failsForEachType, false);
	}

	public Map<String, List<TransactionExecutionResult>> getTransList(int amountOfTypes, int failsForEachType,
			boolean statusForAll) {
		Map<String, List<TransactionExecutionResult>> transactionsMap = new HashMap<>();
		for (int i = 0; i < amountOfTypes; i++) {
			List<TransactionExecutionResult> list = new ArrayList<>();
			for (int j = 0; j < failsForEachType; j++) {
				transactionsMap.put("t" + i, Arrays.asList(trans(statusForAll)));
			}
		}
		return transactionsMap;
	}

	private TransactionExecutionResult trans(boolean status) {
		return new TransactionExecutionResult(now + 1, 0, status, null);
	}
}
