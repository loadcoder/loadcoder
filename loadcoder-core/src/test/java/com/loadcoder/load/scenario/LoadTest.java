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

import static com.loadcoder.statics.LogbackLogging.getNewLogDir;
import static com.loadcoder.statics.LogbackLogging.setResultDestination;
import static com.loadcoder.statics.StopDesisions.duration;
import static com.loadcoder.statics.StopDesisions.iterations;
import static com.loadcoder.statics.Time.PER_SECOND;
import static com.loadcoder.statics.Time.SECOND;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.exceptions.RuntimeResultStorageNotActivatedException;
import com.loadcoder.load.exceptions.NoResultOrFormatterException;
import com.loadcoder.load.scenario.Execution;
import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.FinishedExecution;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.scenario.StartedExecution;
import com.loadcoder.load.scenario.StopDecision;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;
import com.loadcoder.statics.StopDesisions;
import com.loadcoder.statics.ThrottleMode;

public class LoadTest extends TestNGBase {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testWThatLoadScenarioIsInvoked1TimeDefault(Method m) {

		LoadScenario ls = mock(LoadScenario.class);
		Load l = new LoadBuilder(ls).build();
		when(ls.getLoad()).thenReturn(l);
		new ExecutionBuilder(l).build().execute().andWait();
		verify(ls, times(1)).loadScenario();
	}

	@Test
	public void testInMemoryResultStorage(Method m) {

		setResultDestination(getNewLogDir(rootResultDir, m.getName()));
		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				load("t1", () -> {
					return "";
				}).perform();
			}
		};

		LoadScenario ls2 = new LoadScenario() {
			public void loadScenario() {
				load("t2", () -> {
					LoadUtility.sleep(100);
				}).perform();
			}
		};

		Load l2 = new LoadBuilder(ls2).stopDecision(StopDesisions.iterations(3)).build();

		FinishedExecution finishedExecution = new ExecutionBuilder(l2).build().execute().andWait();
		Result resultFromFile = finishedExecution.getReportedResultFromResultFile();
		Assert.assertEquals(resultFromFile.getAmountOfTransactions(), 3);

		try {
			Result result = finishedExecution.getResultFromMemory();
			fail("Expected an exception here, since in memory storage was not activated");
		} catch (RuntimeResultStorageNotActivatedException imsnae) {

		} catch (Exception e) {
			fail("Caught an unexpected exception. Expected a InMemoryStorageNotActivatedException", e);
		}

		File newLogDir = new File(getNewLogDir(rootResultDir, m.getName()).getAbsolutePath() + "_inMemory");
		setResultDestination(newLogDir);

		Load l3 = new LoadBuilder(ls).build();
		Load l4 = new LoadBuilder(ls2).stopDecision(StopDesisions.iterations(3)).build();

		FinishedExecution finishedExecution2 = new ExecutionBuilder(l3, l4).resultFormatter(null).storeResultRuntime()
				.build().execute().andWait();

		Result result = finishedExecution2.getResultFromMemory();
		Assert.assertEquals(result.getAmountOfTransactions(), 4);

		try {
			Result resultFromFile2 = finishedExecution2.getReportedResultFromResultFile();
			fail("Expected an exception here, since resultFormatter is null for this execution");
		} catch (NoResultOrFormatterException nrofe) {
		} catch (Exception e) {
			fail("Caught an unexpected exception. Expected a NoResultOrFormatterException", e);
		}

	}

	@Test
	public void twoLoads(Method m) {

		setResultDestination(getNewLogDir(rootResultDir, m.getName()));
		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				load("t1", () -> {
					return "";
				}).perform();
			}
		};

		LoadScenario ls2 = new LoadScenario() {
			public void loadScenario() {
				load("t2", () -> {
					LoadUtility.sleep(100);
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).build();
		Load l2 = new LoadBuilder(ls2).stopDecision(StopDesisions.iterations(3)).build();

		FinishedExecution finishedExecution = new ExecutionBuilder(l, l2).resultFormatter(null).storeResultRuntime()
				.build().execute().andWait();
		Result result = finishedExecution.getResultFromMemory();
		Assert.assertEquals(result.getAmountOfTransactions(), 4);

	}

	@Test(groups = "timeconsuming")
	public void testDuration() {
		long duration = 1 * 1000;
		long startTime = System.currentTimeMillis();
		StopDecision continueToExecute = duration(duration);

		Assert.assertFalse(continueToExecute.stopLoad(startTime, 0));
		LoadUtility.sleep(500);
		Assert.assertFalse(continueToExecute.stopLoad(startTime, 0));
		LoadUtility.sleep(1000);
		Assert.assertTrue(continueToExecute.stopLoad(startTime, 0));

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				LoadUtility.sleep(100);
			}
		};

		Load l = new LoadBuilder(ls).stopDecision(duration(duration)).build();
		Execution e = new ExecutionBuilder(l).build();

		long start = System.currentTimeMillis();
		e.execute().andWait();
		long end = System.currentTimeMillis();
		long diff = end - start;
		long faultMargin = 500;

		String message = String.format("Duration was %s ms. Diff was %s ms", duration, diff);
		Assert.assertTrue(diff > duration - faultMargin, message);
		Assert.assertTrue(diff < duration + faultMargin, message);
	}

	@Test
	public void testThreadsAndIterations() {

		List<Thread> list = new ArrayList<Thread>();
		List<Object> o = new ArrayList<Object>();
		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				LoadUtility.sleep(1);
				synchronized (list) {
					if (!list.contains(Thread.currentThread()))
						list.add(Thread.currentThread());
				}
				synchronized (o) {
					o.add(new Object());
				}
			}
		};

		Load l = new LoadBuilder(ls).amountOfThreads(10).stopDecision(iterations(1000)).build();

		new ExecutionBuilder(l).build().execute().andWait();
		Assert.assertEquals(list.size(), 10);
		Assert.assertEquals(o.size(), 1000);

	}

	@Test(groups = "timeconsuming")
	public void testRampup() {
		List<Thread> list = new ArrayList<Thread>();

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {

				synchronized (list) {
					if (!list.contains(Thread.currentThread())) {
						list.add(Thread.currentThread());
					}
				}

			}
		};

		Load l = new LoadBuilder(ls).stopDecision(duration(7 * SECOND)).rampup(6 * SECOND).amountOfThreads(3).build();

		StartedExecution started = new ExecutionBuilder(l).build().execute();

		LoadUtility.sleep(500);
		Assert.assertEquals(list.size(), 1);
		LoadUtility.sleep(2000); // 2.5sec past
		Assert.assertEquals(list.size(), 1);
		LoadUtility.sleep(1000); // 3.5sec past
		Assert.assertEquals(list.size(), 2);
		LoadUtility.sleep(2000); // 5.5sec past
		Assert.assertEquals(list.size(), 2);
		LoadUtility.sleep(1000); // 6.5sec past
		Assert.assertEquals(list.size(), 3);

		started.andWait();
	}

	@Test(groups = "timeconsuming")
	public void testOneThrottle(Method m) {

		int transactionsToBeMade = 3;
		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("t1", () -> "").perform();
				logger.info("after 1st");
				load("asyncVoid", () -> {
				}).performAsync();
				logger.info("after 3rd");
				load("async", () -> "").performAsync();
				logger.info("after 2nd");

			}
		};

		Load l = new LoadBuilder(ls).throttle(1, PER_SECOND, ThrottleMode.PER_THREAD).stopDecision(iterations(1))
				.build();

		Execution e = new ExecutionBuilder(l).resultFormatter(null).build();
		long start = System.currentTimeMillis();
		e.execute().andWait();
		long end = System.currentTimeMillis();

		// set to a high value since it's affected by the asyncronous wait in Scenarion
		// andWait
		long faultMargin = 1000;

		long target = (transactionsToBeMade - 1) * 1000;
		long diff = end - start;
		// assert that the iterations divided at multiple threads don't take too less or
		// too long time
		Assert.assertTrue(diff > target - faultMargin && diff < target + faultMargin,
				"diff was:" + diff + " ms. Target is " + target);
	}

	@Test(groups = "timeconsuming")
	public void testMultipleThreadsWithThrottleModePerThread(Method m) {

		List<Object> list = new ArrayList<Object>();
		int threads = 4;
		int iterationsPerThread = 4;

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				LoadUtility.sleep(100);

				synchronized (list) {
					list.add(new Object());
				}
				load("t1", () -> {
					return "";
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).stopDecision(iterations(threads * iterationsPerThread))
				.throttle(1, PER_SECOND, ThrottleMode.PER_THREAD).amountOfThreads(threads).build();

		Execution e = new ExecutionBuilder(l).resultFormatter(null).build();
		long start = System.currentTimeMillis();
		e.execute().andWait();
		long end = System.currentTimeMillis();
		long diff = end - start;

		// assert that the test is correct
		Assert.assertEquals(list.size(), threads * iterationsPerThread);

		// set to a high value since it's affected by the asyncronous wait in Scenarion
		// andWait
		long faultMargin = 1000;

		long target = (iterationsPerThread - 1) * 1000;
		// assert that the iterations divided at multiple threads don't take too less or
		// too long time
		Assert.assertTrue(diff > target - faultMargin && diff < target + faultMargin,
				"diff was:" + diff + " ms. Target is " + target);
	}
}
