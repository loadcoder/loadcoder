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
package com.loadcoder.load.scenario;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.exceptions.FailedTransactionException;
import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.TransactionExecutionResult;

public class LoadScenarioTest extends TestNGBase {

	String rootDirPathForAllLogs = "target";

	@Test
	public void testDefault(Method method) {
		long startOfTest = System.currentTimeMillis();
		LoadScenario s = new LoadScenario() {
			@Override
			public void loadScenario() {
				load("t1", () -> {
					return "";
				}).handleResult((a) -> {
				}).perform();
			}
		};

		Load l = mockLoad(s);
		Assert.assertEquals(l.getExecution().getTransactionExecutionResultBuffer().getBufferForTesting().size(), 1);
		TransactionExecutionResult res = l.getExecution().getTransactionExecutionResultBuffer().getBufferForTesting()
				.get(0);
		Assert.assertEquals(res.isStatus(), true);
		Assert.assertNull(res.getMessage());
		Assert.assertEquals(res.getName(), "t1");
		Assert.assertTrue((res.getTs() < startOfTest + 5_000) && (res.getTs() > startOfTest - 5_000));
	}

	@Test
	public void create(Method method) {

		LoadScenario s = new LoadScenario() {
			@Override
			public void loadScenario() {
				load("t1", () -> {
					return new ArrayList<String>();
				}).handleResult((a) -> {
					// getters
					a.getException();
					a.getResponseTime();
					a.getResponse();

					// setters
					a.changeTransactionName("newTransactionName");
					a.setStatus(false);
					a.reportTransaction(true);
					a.setMessage("message for the report");
				}).perform();
			}
		};

		Load l = mockLoad(s);
		TransactionExecutionResult res = l.getExecution().getTransactionExecutionResultBuffer().getBufferForTesting()
				.get(0);

		Assert.assertEquals(res.getName(), "newTransactionName");
		Assert.assertEquals(res.isStatus(), false);
		Assert.assertEquals(res.getMessage(), "message for the report");
	}

	@Test
	public void testThrowException(Method method) {

		RuntimeException toBeThrown = new RuntimeException("unexpected exception");

		LoadScenario s = new LoadScenario() {
			@Override
			public void loadScenario() {
				ResultModel<Object> result = load("t1", () -> {
					throw toBeThrown;
				}).handleResult((a) -> {
					a.getException();
					a.setMessage(a.getException().getClass().getSimpleName());
				}).performAndGetModel();

				// just to hint that the thrown exception will be available here.
				result.getException();
			}
		};

		Load l = mockLoad(s);
		s.loadScenario(); // run scenario one more time
		Assert.assertEquals(l.getExecution().getTransactionExecutionResultBuffer().getBufferForTesting().size(), 2);
		TransactionExecutionResult res = l.getExecution().getTransactionExecutionResultBuffer().getBufferForTesting()
				.get(0);

		Assert.assertEquals(res.getMessage(), toBeThrown.getClass().getSimpleName());
		Assert.assertEquals(res.isStatus(), false);
	}

	@Test
	public void testThrowIfFailed(Method method) {

		List<FailedTransactionException> exceptions = new ArrayList<FailedTransactionException>();

		LoadScenario s = new LoadScenario() {
			@Override
			public void loadScenario() {
				try {
					load("t1", () -> {

					}).handleResult((a) -> {
						a.setStatus(false);
					}).throwIfFailed().perform();
					fail("Expected an exception to be thrown before this");
				} catch (FailedTransactionException fte) {
					exceptions.add(fte);
				} catch (Exception e) {
					fail("Caught an unexpeted exception of type " + e.getClass().getSimpleName());
				}

				try {
					load("t2", () -> {
						return "";
					}).handleResult((a) -> {
						a.setStatus(false);
					}).throwIfFailed().perform();
					fail("Expected an exception to be thrown before this");
				} catch (FailedTransactionException fte) {
					exceptions.add(fte);
				} catch (Exception e) {
					fail("Caught an unexpeted exception of type " + e.getClass().getSimpleName());
				}

			}
		};

		Load l = mockLoad(s);
		assertEquals(l.getExecution().getTransactionExecutionResultBuffer().getBufferForTesting().size(), 2);
		TransactionExecutionResult result = l.getExecution().getTransactionExecutionResultBuffer().getBufferForTesting()
				.get(0);
		TransactionExecutionResult result2 = l.getExecution().getTransactionExecutionResultBuffer()
				.getBufferForTesting().get(1);
		assertEquals(result.isStatus(), false);
		assertEquals(result2.isStatus(), false);
		assertEquals(exceptions.size(), 2);
	}

	@Test
	public void testNoReport(Method method) {
		RuntimeException toBeThrown = new RuntimeException("unexpected exception");
		LoadScenario s = new LoadScenario() {
			@Override
			public void loadScenario() {
				load("t1", () -> {
					throw toBeThrown;
				}).handleResult((a) -> {
					a.reportTransaction(false);
				}).perform();
			}
		};

		Load l = mockLoad(s);
		Assert.assertEquals(l.getExecution().getTransactionExecutionResultBuffer().getBufferForTesting().size(), 0);
	}

	private Load mockLoad(LoadScenario s) {
		TransactionExecutionResultBuffer buff = new TransactionExecutionResultBuffer();
		Load l = mock(Load.class);
		Execution e = mock(Execution.class);
		s.setLoad(l);

		when(l.getExecution()).thenReturn(e);
		when(e.getTransactionExecutionResultBuffer()).thenReturn(buff);
		s.loadScenario();
		return l;
	}

	@Test
	public void testFailedTransactionException() {

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("foo", () -> {
				}).handleResult(resultModel -> {
					resultModel.setStatus(false);
				}).throwIfFailed().perform();
			}
		};
	}

}
