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
package com.loadcoder.load.scenario;

import static org.testng.Assert.assertEquals;
import static com.loadcoder.load.TestUtility.*;
import static com.loadcoder.statics.Statics.*;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ScenarioTest extends TestNGBase {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testPeak() {
		Scenario s = new Scenario();
		List<String> result = new ArrayList<String>();
		s.load("peakMe", () -> {
			addValueToList(result, "");
			return "";
		}).peak(2, 1.0).perform();
		s.load("peakMeVoid", () -> {
			addValueToList(result, "");
		}).peak(2, 1.0).perform();

		s.load("peakMe2", () -> {
			addValueToList(result, "");
			return "";
		}).peak(2, 0.0).perform();
		s.load("peakMeVoid2", () -> {
			addValueToList(result, "");
		}).peak(2, 0.0).perform();
		assertEquals(result.size(), 4);

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("peakMe", () -> {
					addValueToList(result, "");
					return "";
				}).peak(2, 1.0).perform();
				load("peakMeVoid", () -> {
					addValueToList(result, "");
				}).peak(2, 1.0).perform();

				load("peakMe2", () -> {
					addValueToList(result, "");
					return "";
				}).peak(2, 0.0).perform();
				load("peakMeVoid2", () -> {
					addValueToList(result, "");
				}).peak(2, 0.0).perform();

			}
		};
		new ExecutionBuilder(new LoadBuilder(ls).amountOfThreads(2).stopDecision(iterations(2)).build())
				.resultFormatter(null).storeResultRuntime().build().execute().andWait();
		assertEquals(result.size(), 12);

	}

	/**
	 * This test takes too long time to have in every build, hence the "manual"
	 * group
	 */
	@Test(groups = "manual")
	public void testPeaksWith50PercentChange() {

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("peak", () -> {
				}).peak(5, 0.5).perform();
			}
		};

		long start = System.currentTimeMillis();
		Result r = new ExecutionBuilder(new LoadBuilder(ls).amountOfThreads(5).throttle(10, PER_SECOND, SHARED)
				.stopDecision(iterations(50)).build()).resultFormatter(null).storeResultRuntime().build().execute()
						.andWait().getResultFromMemory();
		long executionTime = System.currentTimeMillis() - start;

		assertThat(executionTime, greaterThan(4000L));
		assertThat(executionTime, lessThan(6000L));

		List<TransactionExecutionResult> results = r.getResultLists().get("peak");

		int peaksOccured = calculatePeaksOccured(results, 5);
		logger.info("peaks occured was:{}", peaksOccured);
		logger.info("execution time was:{}", executionTime);
		assertThat(peaksOccured, greaterThan(3));
		assertThat(peaksOccured, lessThan(15));

	}

	int calculatePeaksOccured(List<TransactionExecutionResult> results, int sizeOfPeaks) {
		int peaksOccured = 0;
		int transactionsWithSimilareExecutionTimeWithinPeak = 1;
		long executionTimeForFirstTransactionWithingPeak = -1;

		for (TransactionExecutionResult transaction : results) {
			if (executionTimeForFirstTransactionWithingPeak == -1) {
				executionTimeForFirstTransactionWithingPeak = transaction.getTs();
				transactionsWithSimilareExecutionTimeWithinPeak = 1;
			} else {
				double diff = transaction.getTs() - executionTimeForFirstTransactionWithingPeak;
				diff = (diff * 2) / 2;
				if (diff < 100) {
					transactionsWithSimilareExecutionTimeWithinPeak++;
					if (transactionsWithSimilareExecutionTimeWithinPeak == sizeOfPeaks) {
						peaksOccured++;
					}
				} else {
					executionTimeForFirstTransactionWithingPeak = transaction.getTs();
					transactionsWithSimilareExecutionTimeWithinPeak = 1;
				}

			}
		}
		return peaksOccured;
	}

	@Test
	public void testToPeakAsyncTransactions() {

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("async", () -> {
				}).peak(5, 1.0).performAsync();
			}
		};

		Load l = new LoadBuilder(ls).amountOfThreads(1).stopDecision(iterations(5))
				.throttleIterations(10, PER_SECOND, PER_THREAD).build();

		long start = System.currentTimeMillis();
		FinishedExecution finished = new ExecutionBuilder(l).resultFormatter(null).storeResultRuntime().build()
				.execute().andWait();
		LoadUtility.sleep(100);
		Result r = finished.getResultFromMemory();

		long diff = System.currentTimeMillis() - start;
		assertThat(diff, greaterThan(300L));

		List<TransactionExecutionResult> results = r.getResultLists().get("async");

		int amountOfPeaks = calculatePeaksOccured(results, 5);
		assertThat(amountOfPeaks, equalTo(1));
	}

	@Test
	public void testScenarioAndLoadScenarioForSameTestLogic() {
		Scenario s = new Scenario();
		testLogic(s);

		List<String> verifier = new ArrayList<String>();
		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				testLogic(this);
				verifier.add("1");
			}
		};
		new ExecutionBuilder(new LoadBuilder(ls).build()).resultFormatter(null).storeResultRuntime().build().execute()
				.andWait();
		Assert.assertEquals(verifier.remove(0), "1");

		LoadScenarioTyped<String> ls2 = new LoadScenarioTyped<String>() {

			@Override
			public String createInstance() {
				verifier.add("2");
				return "";
			}

			@Override
			public void loadScenario(String t) {
				testLogic(this);
				verifier.add("3");
			}
		};
		new ExecutionBuilder(new LoadBuilder(ls2).build()).resultFormatter(null).storeResultRuntime().build().execute()
				.andWait();
		Assert.assertTrue(verifier.contains("2"));
		Assert.assertTrue(verifier.contains("3"));

	}

	@Test
	public void testResultHandler() {
	
		LoadScenario s = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				// TODO Auto-generated method stub
				load("a", ()->{}).handleResult(resultHandler ->{
					resultHandler.getStatus();
					resultHandler.getMessage();
					
				}).perform();
			}
		};
	}
	
	public void testLogic(Scenario scenario) {

		List<String> verifier = new ArrayList<String>();
		scenario.load("foo", () -> {
		}).handleResult((resultModel) -> {
			verifier.add("1");
		}).perform();
		Assert.assertEquals(verifier.remove(0), "1");

		String result = scenario.load("bar", () -> {
			return "result";
		}).handleResult((resultModel) -> {
			verifier.add("2");
		}).perform();
		Assert.assertEquals(verifier.remove(0), "2");
		Assert.assertEquals(result, "result");

		ResultModelVoid resultModelVoid = scenario.load("foo", () -> {
		}).handleResult((rm) -> {
			rm.setMessage("foo message");
			rm.changeTransactionName("foo2");
			rm.setStatus(false);
		}).performAndGetModel();
		Assert.assertTrue(resultModelVoid.getResponseTime() > -1);
		Assert.assertFalse(resultModelVoid.getStatus());
		Assert.assertEquals(resultModelVoid.getTransactionName(), "foo2");
		Assert.assertEquals(resultModelVoid.getMessage(), "foo message");

		ResultModel<String> resultModel = scenario.load("bar", () -> {
			return "";
		}).handleResult((rm) -> {
			rm.setMessage("foo message");
			rm.changeTransactionName("foo2");
			rm.setStatus(false);
		}).performAndGetModel();
		Assert.assertTrue(resultModel.getResponseTime() > -1);
		Assert.assertFalse(resultModel.getStatus());
		Assert.assertEquals(resultModel.getTransactionName(), "foo2");
		Assert.assertEquals(resultModel.getMessage(), "foo message");

		scenario.load("foo", () -> {
		}).performAsync();
		scenario.load("foo", () -> {
			return "";
		}).performAsync();

	}
}
