/*******************************************************************************
 * Copyright (C) 2019 Team Loadcoder
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
package com.loadcoder.statics;

import static org.mockito.Matchers.contains;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.loadcoder.load.result.ResultExtension;
import com.loadcoder.load.result.Summary;
import com.loadcoder.load.result.TransactionValueCalculators;
import com.loadcoder.load.result.Summary.ResultSummarizer;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

import static com.loadcoder.load.result.SummaryBuilder.*;

public class SummaryUtilsTest {

	@Test
	public void testSummaryText() {

		Map<String, List<TransactionExecutionResult>> map = getTestdata();
		Result r = new ResultExtension(map);

		Summary summary = r.summaryBuilder()
				.overall((a, c) -> a
						.use(c.fails())
						.use(c.throughput())
						.use(c.duration())
						.use(c.amountOfTransactions()))
				.perTransaction((a, c) -> a
						.use(c.amount())
						.use(c.avg())
						.use(c.fails())
						.use(c.maximum())
						.use(c.minimum())
						.use(c.percentile(90))
						.use(c.percentile(95))
						.use((list, valueHolder) -> valueHolder.build("made up value", 5.6))
						.use("maximus", c.maximum()))
				.roundValues(3).build();

		summary.prettyPrint((builder, c) -> builder.convert("95%", d -> d.noDecimals()));

		assertEquals(summary.overall("Fails").intValue(), 0);
		assertEquals(summary.overall("Throughput"), 0.922);
		assertEquals(summary.transaction("Min", "foo").intValue(), 0);
		assertEquals(summary.transaction("Max", "foo").intValue(), 99);
		assertEquals(summary.transaction("95%", "foo").intValue(), 95);
		assertEquals(summary.transaction("90%", "foo").intValue(), 90);
		
		assertEquals(summary.transaction(c -> c.percentile(90), "bar").intValue(), 190);
		assertEquals(summary.transaction(c -> c.percentile(95), "bar").intValue(), 195);
		assertEquals(summary.transaction(c -> c.maximum(), "bar").intValue(), 395);
		assertEquals(summary.transaction(cc -> cc.minimum(), "bar").intValue(), 100);
		assertEquals(summary.transaction(cc -> cc.fails(), "bar").intValue(), 0);

		assertEquals(summary.allTransactions("90%").intValue(), 180);

	}

	@Test
	public void testSummaryWithNoTransactionSummary() {

		Map<String, List<TransactionExecutionResult>> map = getTestdata();
		Result r = new ResultExtension(map);

		Summary summary = r.summaryBuilder()
				.overall((a, c) -> a
						.use(c.fails())
						.use(c.throughput())
						.use(c.duration())
						.use(c.amountOfTransactions()))
				.roundValues(3).build();

		summary.prettyPrint();

		assertEquals(summary.overall("Fails").intValue(), 0);
		assertEquals(summary.overall("Throughput"), 0.922);

	}

	@Test
	public void testSummaryWithNoOverallSummary() {

		Map<String, List<TransactionExecutionResult>> map = getTestdata();
		Result r = new ResultExtension(map);

		Summary summary = r.summaryBuilder()
				.perTransaction((a, c) -> a
						.use(c.amount()))
				.roundValues(3).build();

		summary.prettyPrint();

		assertEquals(summary.transaction("Amount", "bar").intValue(), 101);
	}
	
	@Test
	public void testSummaryStandard() {

		Map<String, List<TransactionExecutionResult>> map = getTestdata();
		Result r = new ResultExtension(map);

		Summary summary = r.summaryStandard().roundValues(1)
				.overall((a, c) -> a.use((result, value) -> value.build("hejsan", 5.6))).build();

		summary.prettyPrint((builder, c) -> builder.convert("95%", d -> d.noDecimals()));

		assertEquals(summary.overall(c -> c.fails()).intValue(), 0);
	}

	@Test
	public void testPrintingOptions() {

		Map<String, List<TransactionExecutionResult>> map = getTestdata();
		Result r = new ResultExtension(map);
		r.getResultLists();

		Summary summary = r.summaryStandard().roundValues(1).overall((builder, context) -> builder.use((result,
				value) -> value.build("First bar value", result.getResultLists().get("bar").get(0).getValue())))
				.build();

		summary.prettyPrint(
				(builder, context) -> builder.convert(context.transaction().fails(), d -> d.noDecimals() + " errors"));

		assertEquals(summary.overall(c -> c.fails()).intValue(), 0);
	}

	private Map<String, List<TransactionExecutionResult>> getTestdata() {
		Map<String, List<TransactionExecutionResult>> map = new HashMap<String, List<TransactionExecutionResult>>();
		map.put("foo", new ArrayList<TransactionExecutionResult>());
		for (int i = 0; i < 100; i++)
			map.get("foo").add(new TransactionExecutionResult(i * 1100, i, true, null));
		map.put("bar", new ArrayList<TransactionExecutionResult>());
		for (int i = 100; i < 200; i++)
			map.get("bar").add(new TransactionExecutionResult(i * 1100, i, true, null));
		map.get("bar").add(new TransactionExecutionResult(0, 395, true, null));
		Result r = new ResultExtension(map);
		return map;
	}
}
