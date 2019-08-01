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
package com.loadcoder.statics;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.loadcoder.load.result.ResultExtension;
import com.loadcoder.load.result.Summary.ResultSummarizer;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class SummaryUtilsTest {

	@Test
	public void testThroughput() {
		ResultSummarizer throughput = SummaryUtils.throughput();

		Map<String, List<TransactionExecutionResult>> map = new HashMap<String, List<TransactionExecutionResult>>();
		map.put("foo", new ArrayList<TransactionExecutionResult>());
		map.get("foo").add(new TransactionExecutionResult(0, 10, true, null));
		map.get("foo").add(new TransactionExecutionResult(132_000, 10, true, null));

		Result r = new ResultExtension(map);
		String s = throughput.summarize(r);
		assertEquals(s, "Throughput: 0.02 TPS");
	}
}
