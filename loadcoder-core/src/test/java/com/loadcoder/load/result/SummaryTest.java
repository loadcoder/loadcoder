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
package com.loadcoder.load.result;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.TransactionExecutionResult;

public class SummaryTest extends TestNGBase {

	public Map<String, List<TransactionExecutionResult>> getResultList() {

		Map<String, List<TransactionExecutionResult>> resultLists = new HashMap<String, List<TransactionExecutionResult>>();

		List<TransactionExecutionResult> incrementalResponseTimes = new ArrayList<TransactionExecutionResult>();
		resultLists.put("0-100", incrementalResponseTimes);

		List<TransactionExecutionResult> someErrors = new ArrayList<TransactionExecutionResult>();
		resultLists.put("sometimesError", someErrors);

		for (int i = 0; i < 100; i++) {
			TransactionExecutionResult result = new TransactionExecutionResult((long) i * 1000, i, true, "");
			incrementalResponseTimes.add(result);

			if (i == 0 || i == 50 || i == 90)
				result = new TransactionExecutionResult((long) i * 1000, i, false, "");
			else
				result = new TransactionExecutionResult((long) i * 1000, i, true, "");
			someErrors.add(result);
		}

		return resultLists;
	}


	@Test
	public void SummaryGetDurationInSecondsTest(Method m) {
		int seconds = Summary.getDurationInSeconds(2000);
		assertEquals(seconds, 2);

		seconds = Summary.getDurationInSeconds(0);
		assertEquals(seconds, 1);
	}

}
