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
package com.loadcoder.load.scenario;

import static org.testng.Assert.assertEquals;
import static com.loadcoder.statics.StopDesisions.*;
import static com.loadcoder.statics.Time.*;
import static com.loadcoder.statics.ThrottleMode.*;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ScenarioTest {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test 
	public void testPeak() {
		Scenario s = new Scenario();
		List<String> result = new ArrayList<String>();
		s.load("peakMe", ()->{result.add(""); return "";}).peak(2, 1.0).perform();
		s.load("peakMeVoid", ()->{result.add("");}).peak(2, 1.0).perform();
		
		s.load("peakMe2", ()->{result.add(""); return "";}).peak(2, 0.0).perform();
		s.load("peakMeVoid2", ()->{result.add("");}).peak(2, 0.0).perform();
		assertEquals(result.size(), 4);
		
		LoadScenario ls = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				load("peakMe", ()->{result.add(""); return "";}).peak(2, 1.0).perform();
				load("peakMeVoid", ()->{result.add("");}).peak(2, 1.0).perform();
				
				load("peakMe2", ()->{result.add(""); return "";}).peak(2, 0.0).perform();
				load("peakMeVoid2", ()->{result.add("");}).peak(2, 0.0).perform();
						
			}
		};
		new ExecutionBuilder(new LoadBuilder(ls)
				.amountOfThreads(2)
				.stopDecision(iterations(2))
				.build()).build().execute().andWait();
		assertEquals(result.size(), 12);
		
	}
	
	@Test(groups = "timeconsuming")
	public void testPeak2() {

		LoadScenario ls = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				load("peak", ()->{}).peak(5, 0.5).perform();
			}
		};
			
		long start = System.currentTimeMillis();
		Result r = new ExecutionBuilder(new LoadBuilder(ls)
				.amountOfThreads(5)
				.throttle(10, PER_SECOND, SHARED)
				.stopDecision(iterations(50))
				.build()).build().execute().andWait().getReportedResultFromResultFile();
		long executionTime = System.currentTimeMillis() - start;
		
		assertThat(executionTime, greaterThan(4000L));
		assertThat(executionTime, lessThan(6000L));
		
		List<TransactionExecutionResult> results = r.getResultLists().get("peak");
		int peaksOccured = 0;
		int transactionsWithSimilareExecutionTimeWithinPeak = 1;
		long executionTimeForFirstTransactionWithingPeak = -1;
		
		for(TransactionExecutionResult transaction : results) {
			if(executionTimeForFirstTransactionWithingPeak == -1) {
				executionTimeForFirstTransactionWithingPeak = transaction.getTs();
				transactionsWithSimilareExecutionTimeWithinPeak = 1;
			}else {
				double diff = transaction.getTs() - executionTimeForFirstTransactionWithingPeak;
				diff = (diff * 2) / 2;
				if(diff < 100) {
					transactionsWithSimilareExecutionTimeWithinPeak++;
					if(transactionsWithSimilareExecutionTimeWithinPeak == 5) {
						peaksOccured++;
					}
				}else {
					executionTimeForFirstTransactionWithingPeak = transaction.getTs();
					transactionsWithSimilareExecutionTimeWithinPeak = 1;
				}
				
			}
		}
		
		logger.info("peaks occured was:{}", peaksOccured);
		logger.info("execution time was:{}", executionTime);
		assertThat(peaksOccured, greaterThan(3));
		assertThat(peaksOccured, lessThan(15));
		
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
		new ExecutionBuilder(new LoadBuilder(ls).build()).build().execute().andWait();
		Assert.assertEquals(verifier.remove(0), "1");

		TypedLoadScenario<String> ls2 = new TypedLoadScenario<String>() {

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
		new ExecutionBuilder(new LoadBuilder(ls2).build()).build().execute().andWait();
		Assert.assertTrue(verifier.contains("2"));
		Assert.assertTrue(verifier.contains("3"));

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
