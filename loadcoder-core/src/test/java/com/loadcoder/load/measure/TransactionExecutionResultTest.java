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
package com.loadcoder.load.measure;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.TransactionExecutionResult;

public class TransactionExecutionResultTest extends TestNGBase {

	@Test
	public void testGetResultListAsMap() {
		List<TransactionExecutionResult> l = Arrays.asList(new TransactionExecutionResult("foo", 0, 0, true, null),
				new TransactionExecutionResult("bar", 0, 0, true, null));
		Map<String, List<TransactionExecutionResult>> m = TransactionExecutionResult.getResultListAsMap(l);
		assertThat(m.get("foo").get(0), equalTo(l.get(0)));
		assertThat(m.get("bar").get(0), equalTo(l.get(1)));

	}

}
