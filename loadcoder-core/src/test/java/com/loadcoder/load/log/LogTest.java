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
package com.loadcoder.load.log;

import static com.loadcoder.statics.LogbackLogging.getNewLogDir;
import static com.loadcoder.statics.LogbackLogging.setResultDestination;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.TestUtility;
import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class LogTest extends TestNGBase {

	@Test
	public void testThatResultLogContainsExpectedContent(Method method) {

		File f = getNewLogDir(rootResultDir, method.getName());
		setResultDestination(f);
		File resultFile = new File(f, "result.log");

		List<String> rows = TestUtility.readFile(resultFile);
		int sizeBeforeTest = rows.size();

		String uniqueTransactionId = this.getClass().getName() + System.currentTimeMillis();
		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load(uniqueTransactionId, () -> {
					/* some fancy transaction */}).perform();
			}
		};

		Load l = new LoadBuilder(ls).build();
		new ExecutionBuilder(l).build().execute().andWait();
		List<String> rowsAfterTest = TestUtility.readFile(resultFile);
		Assert.assertEquals(rowsAfterTest.size(), sizeBeforeTest + 1);
		Assert.assertTrue(rowsAfterTest.get(rowsAfterTest.size() - 1).contains(uniqueTransactionId));

	}

	@Test
	public void testAsyncThreadResult(Method method) throws FileNotFoundException, IOException {

		List<String> threadNames = new ArrayList<String>();
		File resultDir = new File(rootResultDir + "/" + method.getName() + "/" + System.currentTimeMillis());
		setResultDestination(resultDir);

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {

				threadNames.add(Thread.currentThread().getName());

				load("performAsync", () -> {
				}).performAsync();

				load("performAsync2", () -> {
				}).performAsync();

				load("performAsyncWithReturn", () -> {
					return "";
				}).performAsync();

				load("perform", () -> {
				}).perform();

				load("performWithReturn", () -> {
					// Sleeping so that the async calls are finished before finishing the execution
					LoadUtility.sleep(30);
					return "";
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).amountOfThreads(1).build();

		Result result = new ExecutionBuilder(l).resultFormatter(null).storeResultRuntime().build().execute().andWait()
				.getResultFromMemory();

		Assert.assertEquals(result.getAmountOfTransactions(), 5);
		Set<String> keys = result.getResultLists().keySet();
		for (String key : keys) {
			List<TransactionExecutionResult> e = result.getResultLists().get(key);
			assertEquals(e.get(0).getThreadId(), threadNames.get(0));
		}
	}
}
