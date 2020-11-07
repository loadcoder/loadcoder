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
package com.loadcoder.result;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;
import com.loadcoder.statics.Formatter;

public class ResultTest extends TestNGBase {

	@Test
	public void testResultFromFileWithDefaulFormatter() {

		Result r = new Result(new File("src/test/resources/testresults/default_formatter_test.log"));

		Result result_long_names = new Result(
				new File("src/test/resources/testresults/default_formatter_long_names_test.log"),
				Formatter.RESULT_FORMATTER_LONG_VARIABLE_NAMES);

		assertEquals(r.getAmountOfTransactions(), 4);
		assertEquals(r.getAmountOfFails(), 1);
		assertEquals(r.getResultLists().get("a0").size(), 2);
		assertEquals(r.getResultLists().get("a1").get(0).isStatus(), false,
				"1st a1 transaction did not have status false");

		assertEquals(result_long_names.getAmountOfTransactions(), 4);
		assertEquals(result_long_names.getAmountOfFails(), 1);
		assertEquals(result_long_names.getResultLists().get("a0").size(), 2);
		assertEquals(result_long_names.getResultLists().get("a1").get(0).isStatus(), false,
				"1st a1 transaction did not have status false");

	}

	@Test
	public void testToMerge() {
		Map<String, List<TransactionExecutionResult>> resultList = new HashMap<String, List<TransactionExecutionResult>>();

		List<TransactionExecutionResult> result = new ArrayList<TransactionExecutionResult>();
		resultList.put("a", result);
		for (int i = 0; i < 10; i++)
			result.add(new TransactionExecutionResult(10 + i * 1000, 10, i == 2 ? false : true, ""));

		List<TransactionExecutionResult> result2 = new ArrayList<TransactionExecutionResult>();
		resultList.put("b", result2);
		for (int i = 0; i < 10; i++)
			result2.add(new TransactionExecutionResult(70 + i * 1000, 20, i == 2 ? false : true, ""));

		Result r = new Result(resultList);

		Map<String, List<TransactionExecutionResult>> resultList2 = new HashMap<String, List<TransactionExecutionResult>>();

		List<TransactionExecutionResult> result3 = new ArrayList<TransactionExecutionResult>();
		resultList2.put("a", result3);
		for (int i = 0; i < 10; i++)
			result3.add(new TransactionExecutionResult("a", 20 + i * 1000, 30, i == 2 ? false : true, ""));

		List<TransactionExecutionResult> result4 = new ArrayList<TransactionExecutionResult>();
		resultList2.put("b", result4);
		for (int i = 0; i < 10; i++)
			result4.add(new TransactionExecutionResult("b", 50 + i * 1000, 40, i == 2 ? false : true, ""));

		Result r2 = new Result(resultList2);

		r.mergeResult(r2);
		assertEquals(r.getAmountOfTransactions(), 40);
		assertEquals(r.getAmountOfFails(), 4);
		assertEquals(r.getStart(), 10);
		assertEquals(r.getEnd(), 9070);
		assertEquals(r.getDuration(), 9060);

	}
}
