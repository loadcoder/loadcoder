/*******************************************************************************
 * Copyright (C) 2019 Stefan Vahlgren at Loadcoder
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

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.ResultExtension;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeChartTest extends TestNGBase {

	Logger log = LoggerFactory.getLogger(RuntimeChartTest.class);

	@Test(groups = "manual")
	public void testManyTransactions(Method method) {
		long start = System.currentTimeMillis();
		List<TransactionExecutionResult> list = new ArrayList<TransactionExecutionResult>();
		list.add(new TransactionExecutionResult(start, 1050, true, null));
		list.add(new TransactionExecutionResult(start + 1200, 950, true, null));
		list.add(new TransactionExecutionResult(start + 5500, 200, true, null));
		list.add(new TransactionExecutionResult(start + 62500, 700, true, null));
		list.add(new TransactionExecutionResult(start + 180500, 500, true, null));

		Map<String, List<TransactionExecutionResult>> testdata = new HashMap<String, List<TransactionExecutionResult>>();
		testdata.put("a0", list);

		Result result = new ResultExtension(testdata);

		RuntimeChart chart = new RuntimeChart();
		chart.useData(testdata);
		List<XYDataItem> items = chart.getLogic().getSeriesCollection().getSeries(2).getItems();
		assertEquals(items.get(0).getY().longValue(), 1050L);

		list.clear();
		chart.useData(testdata);

		assertEquals(items.get(0).getY().longValue(), 1000L);

		chart.waitUntilClosed();
	}

}
