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
package com.loadcoder.load.anotherthirtpartypackage;

import static com.loadcoder.statics.Formatter.SIMPLE_RESULT_FORMATTER;
import static com.loadcoder.statics.LogbackLogging.getNewLogDir;
import static com.loadcoder.statics.LogbackLogging.setResultDestination;
//import static com.loadcoder.statics.ThrottleMode.PER_THREAD;
//import static com.loadcoder.statics.ThrottleMode.SHARED;
//import static com.loadcoder.statics.Time.PER_MINUTE;
//import static com.loadcoder.statics.Time.PER_SECOND;
//import static com.loadcoder.statics.Time.SECOND;
import static com.loadcoder.statics.Statics.PER_MINUTE;
import static com.loadcoder.statics.Statics.PER_SECOND;
import static com.loadcoder.statics.Statics.PER_THREAD;
import static com.loadcoder.statics.Statics.SECOND;
import static com.loadcoder.statics.Statics.SHARED;
import static com.loadcoder.statics.StopDesisions.duration;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.chart.logic.Chart;
import com.loadcoder.load.chart.logic.ResultChart;
import com.loadcoder.load.chart.logic.RuntimeChart;
import com.loadcoder.load.scenario.Execution;
import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.FinishedExecution;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.sut.SUT;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Logs;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;
import com.loadcoder.statics.SummaryUtils;

public class FullTest extends TestNGBase {

	Logger log = LoggerFactory.getLogger(FullTest.class);

	@Test(groups = "manual")
	public void create(Method method) {

		LoadScenario s = new LoadScenario() {

			@Override
			public void loadScenario() {

				ThreadLocal<Exception> tl = new ThreadLocal<Exception>();
				load("t1", () -> {
					return new NullPointerException();
				}).handleResult((a) -> {
					// getters
					Exception e = a.getException();
					long rt = a.getResponseTime();
					NullPointerException npe = a.getResponse();

					// setters
					a.changeTransactionName("newTransactionName");
					a.setStatus(false);
					a.setReportTransaction(true);
					a.setMessage("message for the report");
				}).perform();

				load("t1", () -> {
					/* nothing to return */}).handleResult((a) -> {
						// getters
						Exception e = a.getException();
						long rt = a.getResponseTime();

						// setters
						a.changeTransactionName("newTransactionName");
						a.setStatus(false);
						a.setReportTransaction(true);
						a.setMessage("message for the report");
					}).perform();
			}
		};

		Load l = new LoadBuilder(s).throttle(1, PER_SECOND, PER_THREAD).build();
		new ExecutionBuilder(l).build().execute().andWait();

	}

	@Test(groups = "manual")
	public void createLoadScenarioPreAndPost(Method method) {

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));
		List<?> list = new ArrayList<Exception>();

		LoadScenario s = new LoadScenario() {

			@Override
			public void loadScenario() {

				load("t1", () -> {
					return new NullPointerException();
				}).handleResult((a) -> {
					// getters
					Exception e = a.getException();
					long rt = a.getResponseTime();
					NullPointerException npe = a.getResponse();

					// setters
					a.changeTransactionName("newTransactionName");
					a.setStatus(false);
					a.setReportTransaction(true);
					a.setMessage("message for the report");
				}).perform();

				load("t1", () -> {
					/* nothing to return */}).handleResult((a) -> {
						// getters
						Exception e = a.getException();
						long rt = a.getResponseTime();

						// setters
						a.changeTransactionName("newTransactionName");
						a.setStatus(false);
						a.setReportTransaction(true);
						a.setMessage("message for the report");
					}).perform();
			}
		};

		Load l = new LoadBuilder(s).throttle(1, PER_SECOND, PER_THREAD).build();
		new ExecutionBuilder(l).build().execute().andWait();
	}

	@Test(groups = "manual")
	public void oneTransaction(Method method) {
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();

			@Override
			public void loadScenario() {
				load("t2", () -> {
					sut.sleepCos();
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).build();

		new ExecutionBuilder(l).storeAndConsumeResultRuntime(new RuntimeChart()).build().execute().andWait();
	}

	@Test(groups = "manual")
	public void testPeakMethod(Method method) {

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));

		RuntimeChart chart = new RuntimeChart();
		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("t2", () -> {
					LoadUtility.sleep(LoadUtility.random(50, 70));
				}).peak(15, 0.03).perform();
			}
		};

		Load l = new LoadBuilder(ls).stopDecision(duration(60 * SECOND)).amountOfThreads(20).rampup(2 * SECOND)
				.throttle(2, PER_SECOND, SHARED).build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(chart).build().execute()
				.andWait();

		chart.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void benchmarkOnlyLog(Method method) {

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));

		Logger logsLog = LoggerFactory.getLogger(Logs.class);
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() < start + 20_000) {
			logsLog.info("<t name=\"fast\" ts=\"1527536832574\" rt=\"0\" status=\"true\"/>");
		}
	}

	@Test(groups = "manual")
	public void highestPossibleLoad(Method method) {

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));

		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();

			@Override
			public void loadScenario() {
				load("fast", () -> {
				}).perform();
				load("fast", () -> "").perform();
			}
		};

		RuntimeChart chart = new RuntimeChart();
		Load l = new LoadBuilder(ls).stopDecision(duration(20 * SECOND)).amountOfThreads(1).build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(chart).build().execute()
				.andWait();

		SummaryUtils.printSimpleSummary(finished.getReportedResultFromResultFile(), method.getName());

		chart.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void highestPossibleLoad2(Method method) {

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));

		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();

			@Override
			public void loadScenario() {
				load("fast", () -> {
					LoadUtility.sleep(5);

				}).perform();
			}
		};

		RuntimeChart chart = new RuntimeChart();
		Load l = new LoadBuilder(ls).throttle(20, PER_MINUTE, PER_THREAD).stopDecision(duration(20 * SECOND))
				.amountOfThreads(10).build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(chart).build().execute()
				.andWait();

		SummaryUtils.printSimpleSummary(finished.getReportedResultFromResultFile(), method.getName());

		chart.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void twoLoads(Method method) {
		RuntimeChart chart = new RuntimeChart();
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();

			@Override
			public void loadScenario() {
				load("t1", () -> {
					LoadUtility.sleep(LoadUtility.random(10, 20));
				}).perform();
			}
		};

		LoadScenario ls2 = new LoadScenario() {
			SUT sut = new SUT();

			@Override
			public void loadScenario() {
				load("t2", () -> {
					LoadUtility.sleep(LoadUtility.random(30, 40));
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).stopDecision(duration(60 * SECOND)).throttle(10, PER_SECOND, SHARED).build();

		Load l2 = new LoadBuilder(ls2).stopDecision(duration(40 * SECOND)).throttle(15, PER_SECOND, SHARED).build();

		FinishedExecution finished = new ExecutionBuilder(l, l2).storeAndConsumeResultRuntime(chart).build().execute()
				.andWait();
		chart.waitUntilClosed();

	}

	@Test(groups = "manual")
	public void testRuntimeChart2(Method method) {
		RuntimeChart chart = new RuntimeChart();
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();

			@Override
			public void loadScenario() {
				load("t2", () -> {
					LoadUtility.sleep(LoadUtility.random(300, 400));
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).stopDecision(duration(300_000)).throttle(23, PER_MINUTE, SHARED).build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(chart).build().execute()
				.andWait();

		chart.waitUntilClosed();

	}

	@Test(groups = "manual")
	public void testSurrounding(Method method) {
		RuntimeChart chart = new RuntimeChart();
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();

			@Override
			public void loadScenario() {
				load("t2", () -> {
					LoadUtility.sleep(LoadUtility.random(2, 10));
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).throttle(20, PER_MINUTE, SHARED).stopDecision(duration(300_000)).build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(chart).build().execute()
				.andWait();

		chart.waitUntilClosed();

	}

	@Test(groups = "manual")
	public void testLowIntensity(Method method) {
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();

			@Override
			public void loadScenario() {
				load("t2", () -> {
					LoadUtility.sleep(LoadUtility.random(1000, 15_000));
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).stopDecision(duration(900_000)).throttle(20, PER_MINUTE, SHARED).amountOfThreads(2)
				.build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(new RuntimeChart()).build()
				.execute().andWait();

	}

	@Test(groups = "manual")
	public void testRuntimeChart(Method method) {
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();

			@Override
			public void loadScenario() {
				load("t2", () -> {
					sut.sleepCos(130);
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).stopDecision(duration(300_000)).amountOfThreads(2).build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(new RuntimeChart()).build()
				.execute().andWait();

	}

	@Test(groups = "manual")
	public void twoScenariosTest(Method method) {

		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("t1", () -> {
					LoadUtility.sleep(20);
				}).perform();
			}
		};
		LoadScenario ls2 = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("t2", () -> {
					LoadUtility.sleep(20);
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).throttle(2, PER_MINUTE, SHARED).stopDecision(duration(60_000)).build();

		new ExecutionBuilder(l).storeAndConsumeResultRuntime(new RuntimeChart()).build().execute().andWait();

	}

	@Test(groups = "manual")
	public void testDynamicChart(Method method) {

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));

		SUT sut = new SUT();
		LoadScenario s = new LoadScenario() {

			@Override
			public void loadScenario() {
				
				load("get", () -> {
					
					sut.methodWhereResponseTimeFollowSomeKindOfPattern(sut);
					
				}).handleResult((a) -> {
					
					if(a.getResponseTime() >300 ) {
						a.setStatus(false);
					}
				}).perform();
				load("create", () -> {
					sut.methodWhereResponseTimeFollowSomeKindOfPattern2(this);
					return "";
				}).handleResult((a) -> {
				}).perform();

				load("change", () -> sut.methodThatTakesBetweenTheseResponseTimes(300, 320)).handleResult((a) -> {
				}).perform();

				load("commit", () -> {
					sut.methodThatSomeTimesThrowsCheckedException();
				}).handleResult((a) -> {
				}).perform();

			}
		};

		RuntimeChart runtimeChart = new RuntimeChart();
		Load l = new LoadBuilder(s)
				.stopDecision((a,b)->{
					return false;
				})
				
				.amountOfThreads(2000)
				.throttle(10, PER_MINUTE, PER_THREAD)
				
				.rampup(10 * SECOND)
				.build();

		FinishedExecution finished = new ExecutionBuilder(l)
				.storeAndConsumeResultRuntime(runtimeChart).build()
				.execute().andWait();

		Result result = finished.getReportedResultFromResultFile();
		ResultChart resultChart = new ResultChart(result);

		SummaryUtils.printSimpleSummary(result, "simleTest");
		runtimeChart.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void peaks(Method method) {

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));

		LoadScenario s = new LoadScenario() {

			int rt = 50;
			boolean modder = false;

			@Override
			public void loadScenario() {

				long mod = 0;

				if (LoadUtility.random(0, 100) == 1) {
					modder = true;
				}
				if (LoadUtility.random(0, 40) == 1) {
					modder = false;
				}

				if (modder)
					while (LoadUtility.random(1, 2) == 1)
						mod = mod + LoadUtility.random(rt, rt * 2);

				long modifier = mod;

				load("t1", () -> {
					LoadUtility.sleep(LoadUtility.random(rt, rt + 4) + modifier);
				}).handleResult((a) -> {
				}).perform();
			}
		};

		RuntimeChart runtimeChart = new RuntimeChart();
		Load l = new LoadBuilder(s).stopDecision(duration(120 * SECOND)).amountOfThreads(10).rampup(5 * SECOND).build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(runtimeChart).build()
				.execute().andWait();

		Execution execution = new ExecutionBuilder(l).storeAndConsumeResultRuntime(new RuntimeChart())
				.resultFormatter(SIMPLE_RESULT_FORMATTER).build();

		Result result = finished.getReportedResultFromResultFile();
		ResultChart resultChart = new ResultChart(result);

		SummaryUtils.printSimpleSummary(result, "simleTest");
		runtimeChart.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void testResultChart(Method method) {
		setResultDestination(getNewLogDir(rootResultDir, method.getName()));
		LoadScenario s = new LoadScenario() {
			@Override
			public void loadScenario() {
				SUT sut = new SUT();
				load("t1", () -> {
					sut.methodThatTakesBetweenTheseResponseTimes(100, 120);
					return "";
				}).handleResult((a) -> {
				}).perform();

				load("t2", () -> {
					sut.methodThatTakesBetweenTheseResponseTimes(200, 250);
					return "";
				}).handleResult((a) -> {
				}).perform();
			}
		};
		Load l = new LoadBuilder(s).stopDecision(duration(30_000)).build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(new RuntimeChart()).build()
				.execute().andWait();

		Result result = finished.getReportedResultFromResultFile();

		Result r = new Result(new File("src/test/resources/testresults/2min.log"));

		Chart c = new ResultChart(result);
		c.waitUntilClosed();

	}

	@Test(groups = "manual")
	public void readResultForResultChartTest_2min() {
		Result r = new Result(new File("src/test/resources/testresults/2min.log"));
		Chart c = new ResultChart(r);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void readResult8HBeforeGMTForResultChartTest() {
		Result r = new Result(new File("src/test/resources/testresults/result_8H_before_GMT.log"));
		Chart c = new ResultChart(r);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void resultChartAndRuntimeChartWithConstructedData() {
		Result r = new Result(new File("src/test/resources/testresults/result15min.log"));
		Chart c = new ResultChart(r);

		RuntimeChart runtimeChart = new RuntimeChart();
		runtimeChart.useData(r.getResultLists());
		runtimeChart.useData(new HashMap<String, List<TransactionExecutionResult>>());
		runtimeChart.useData(new HashMap<String, List<TransactionExecutionResult>>());

		runtimeChart.waitUntilClosed();
	}
}
