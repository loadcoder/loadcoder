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

import org.testng.annotations.Test;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.TransactionExecutionResult;

import junit.framework.Assert;

public class ResultHandlerVoidBuilderTest extends TestNGBase {

	@Test
	public void create() {

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
			}
		};

		Load l = new LoadBuilder(ls).build();
		new ExecutionBuilder(l).build();

		RateLimiter rl = RateLimiter.create(1);
		ResultHandlerVoidBuilder resultHandlerVoidBuilder = new ResultHandlerVoidBuilder("t1", () -> {
			LoadUtility.sleep(100);
		}, ls, rl);
		resultHandlerVoidBuilder.handleResult((a) -> {
			a.changeTransactionName("t2");
		}).perform();

		Assert.assertEquals(1, ls.getTransactionExecutionResultBuffer().getBuffer().size());
		TransactionExecutionResult result = ls.getTransactionExecutionResultBuffer().getBuffer().get(0);
		Assert.assertEquals("t2", result.getName());
		Assert.assertEquals(true, result.isStatus());
		Assert.assertTrue(result.getRt() >= 100);
	}

	@Test
	public void handeResultThrowsException() {

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
			}
		};

		Load l = new LoadBuilder(ls).build();
		new ExecutionBuilder(l).build();

		RateLimiter rl = RateLimiter.create(1);
		ResultHandlerVoidBuilder resultHandlerVoidBuilder = new ResultHandlerVoidBuilder("t1", () -> {
		}, ls, rl);
		resultHandlerVoidBuilder.handleResult((a) -> {
			a.changeTransactionName("t2");
			throw new RuntimeException("unexpected exception");
		}).perform();

		Assert.assertEquals(1, ls.getTransactionExecutionResultBuffer().getBuffer().size());
		TransactionExecutionResult result = ls.getTransactionExecutionResultBuffer().getBuffer().get(0);
		Assert.assertEquals("t1", result.getName());
		Assert.assertEquals(false, result.isStatus());
	}
}
