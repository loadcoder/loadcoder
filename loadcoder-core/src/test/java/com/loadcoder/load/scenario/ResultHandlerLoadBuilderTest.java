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
package com.loadcoder.load.scenario;

import static org.testng.Assert.fail;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.exceptions.ResultHandlerException;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.TransactionExecutionResult;

public class ResultHandlerLoadBuilderTest extends TestNGBase {

	@Test
	public void create() {

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
			}
		};

		Load l = new LoadBuilder(ls).build();
		new ExecutionBuilder(l).resultFormatter(null).storeResultRuntime().build();

		RateLimiter rl = RateLimiter.create(1);
		ResultHandlerLoadBuilder<String> resultHandlerBuilder = new ResultHandlerLoadBuilder<String>("t1", () -> {
			LoadUtility.sleep(100);
			return "";
		}, ls, rl);
		resultHandlerBuilder.handleResult((a) -> {
			a.changeTransactionName("t2");
		}).perform();

		Assert.assertEquals(ls.getTransactionExecutionResultBuffer().getBufferForTesting().size(), 1);
		TransactionExecutionResult result = ls.getTransactionExecutionResultBuffer().getBufferForTesting().get(0);
		Assert.assertEquals(result.getName(), "t2");
		Assert.assertTrue(result.isStatus());
		Assert.assertTrue(result.getValue() >= 100);
	}

	@Test
	public void handeResultThrowsException() {

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
			}
		};

		Load l = new LoadBuilder(ls).build();
		new ExecutionBuilder(l).resultFormatter(null).storeResultRuntime().build();

		RateLimiter rl = RateLimiter.create(1);

		ResultHandlerLoadBuilder<String> resultHandlerBuilder = new ResultHandlerLoadBuilder<String>("t1", () -> {
			return "";
		}, ls, rl);

		try {
			resultHandlerBuilder.handleResult((a) -> {
				a.changeTransactionName("t2");
				throw new RuntimeException("unexpected exception");
			}).perform();
			fail("An exception should have been thrown");
		} catch (ResultHandlerException rhe) {

		} catch (Exception e) {
			fail("Expected ResultHandlerException instead of caught" + e.getClass().getSimpleName());
		}
		Assert.assertEquals(ls.getTransactionExecutionResultBuffer().getBufferForTesting().size(), 1);
		TransactionExecutionResult result = ls.getTransactionExecutionResultBuffer().getBufferForTesting().get(0);
		Assert.assertEquals(result.getName(), "t2");
		Assert.assertFalse(result.isStatus());
	}
}
