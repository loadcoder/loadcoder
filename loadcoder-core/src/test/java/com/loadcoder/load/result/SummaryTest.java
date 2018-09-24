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
package com.loadcoder.load.result;

import static com.loadcoder.statics.SummaryUtils.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import com.loadcoder.load.result.Summary.SummaryWithResultActions.Table.SummaryWithTable;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;
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

	public SummaryWithTable fullSummary(Result result, Method method) {
		Summary resultSummarizer = new Summary(result);
		SummaryWithTable summaryWithTable = resultSummarizer.firstDo((a) -> {
		}).log((a) -> {
			return String.format("Summary for %s:%s", this.getClass().getSimpleName(), method.getName());
		}).log(throughput()).log(amountOfTransactions()).log(amountOfFails()).table()
				.column("Transaction", transactionNames()).column("Amount", transactions()).column("MAX", max())
				.column("AVG", avg()).column("80%", percentile(80)).column("FAILS", fails());

		return summaryWithTable;
	}

	class ResultExtention extends Result {
		public ResultExtention(Map<String, List<TransactionExecutionResult>> resultLists) {
			super(resultLists);
		}
	}

	@Test
	public void seriesSummaryTest() {
		Result result = new ResultExtention(getResultList());
		Summary summary = new Summary(result);
		String summaryString = summary.log(a -> "Foo").log(a -> "Bar").getAsString();
		assertTrue(summaryString.contains("Foo"));
		assertTrue(summaryString.contains("Bar"));

	}

	@Test
	public void commonSummaryTest(Method m) {
		Result result = new ResultExtention(getResultList());
		SummaryWithTable summaryWithTable = fullSummary(result, m);
		assertTrue(
				summaryWithTable.getAsString().contains("Throughput: 2.0TPS"));
	}
}
