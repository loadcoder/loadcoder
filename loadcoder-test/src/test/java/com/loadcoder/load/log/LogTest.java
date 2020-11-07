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

import static com.loadcoder.statics.LogbackLogging.setResultDestination;
import static com.loadcoder.statics.Statics.*;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.TestUtility;
import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.sut.SUT;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.load.utils.ResultGenerator;
import com.loadcoder.result.Logs;
import com.loadcoder.result.Result;
import com.loadcoder.result.ResultLogger;
import com.loadcoder.result.TransactionExecutionResult;

import static com.loadcoder.statics.LogbackLogging.getNewLogDir;

public class LogTest extends TestNGBase {
	/*
	 * This class is dependent on the logback.xml where the resultappender defines
	 * that result.log will be used. result.log is also used in these tests.
	 */

	static Logger resultLog = ResultLogger.resultLogger;

	static Logger infoLog = LoggerFactory.getLogger(LogTest.class);

	@Test
	public void testLogging(Method method) throws IOException {

		File resultDir = new File(rootResultDir + "/" + method.getName() + "/" + System.currentTimeMillis());
		setResultDestination(resultDir);
		String infoMsg = "foo";
		String resultMsg = "bar";
		resultLog.info(resultMsg);
		infoLog.error(infoMsg);

		List<String> content = LoadUtility.readFile(new File(resultDir.getAbsolutePath() + "/info.log"));
		Assert.assertEquals(content.size(), 2);
		Assert.assertTrue(content.get(1).contains(infoMsg));

		List<String> content2 = LoadUtility.readFile(new File(resultDir.getAbsolutePath() + "/result.log"));
		Assert.assertEquals(content2.size(), 1);
		Assert.assertTrue(content2.get(0).contains(resultMsg));

	}

	@Test
	public void testLoggingWithSUT(Method method) throws IOException {
		File resultDir = new File(rootResultDir + "/" + method.getName() + "/" + System.currentTimeMillis());
		setResultDestination(resultDir);

		String infoMsg = "foo";
		String resultMsg = "bar";
		resultLog.info(resultMsg);
		infoLog.error(infoMsg);
		SUT sut = new SUT();
		String loggedInOtherClass = "logged by SUT";
		sut.loggingMethod(loggedInOtherClass);

		List<String> content2 = LoadUtility.readFile(new File(resultDir.getAbsolutePath() + "/result.log"));
		Assert.assertTrue(content2.size() == 1);
		Assert.assertTrue(content2.get(0).contains(resultMsg));

		List<String> content = LoadUtility.readFile(new File(resultDir.getAbsolutePath() + "/info.log"));
		Assert.assertEquals(content.size(), 3);
		Assert.assertTrue(content.get(1).contains(infoMsg));
		Assert.assertTrue(content.get(2).contains(loggedInOtherClass));

	}

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

	@Test
	public void logUtility(Method method) {

		String rootDir = "target/" + this.getClass().getSimpleName() + "/" + method.getName() + "/"
				+ System.currentTimeMillis();
		File rootDirFile = new File(rootDir);
		Logger log = LoggerFactory.getLogger(Logs.class);

		for (int i = 0; i < 10; i++) {
			File f = getNewLogDir(rootDir);
			setResultDestination(f);
			log.info("message{}", i);
		}

		File[] subDirs = rootDirFile.listFiles();
		Assert.assertEquals(subDirs.length, 10);
		List<String> readLines = new ArrayList<String>();
		try {
			for (File directory : subDirs) {
				List<String> content = LoadUtility.readFile(new File(directory.getAbsoluteFile() + "/result.log"));
				Assert.assertEquals(content.size(), 1, "result in " + directory + " was unexpected");
				String line = content.get(0);
				Assert.assertTrue(line.matches("message.*"));

				// make sure that all files contents are unique
				Assert.assertFalse(readLines.contains(line));
				readLines.add(line);
			}
		} catch (IOException e) {

		}
	}

	@Test
	public void testToGenerateResult(Method method) {
		File resultDir = getNewLogDir("target", method.getName());
		setResultDestination(resultDir);
		ResultGenerator.generateResult(1 * MINUTE, 2);
		List<String> resultList = TestUtility.readFile(new File(resultDir.getAbsolutePath() + "/result.log"));
		Assert.assertTrue(resultList.size() > 1);
	}
}
