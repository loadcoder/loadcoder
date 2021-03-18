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
package com.loadcoder.load.anotherthirtpartypackage;

import static com.loadcoder.statics.LogbackLogging.getNewLogDir;
import static com.loadcoder.statics.LogbackLogging.setResultDestination;
import static com.loadcoder.statics.Statics.PER_MINUTE;
import static com.loadcoder.statics.Statics.PER_SECOND;
import static com.loadcoder.statics.Statics.PER_THREAD;
import static com.loadcoder.statics.Statics.SECOND;
import static com.loadcoder.statics.Statics.SHARED;
import static com.loadcoder.statics.Statics.duration;
import static com.loadcoder.statics.Statics.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.chart.logic.Chart;
import com.loadcoder.load.chart.logic.ResultChart;
import com.loadcoder.load.chart.logic.RuntimeChart;
import com.loadcoder.load.result.Summary;
import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.FinishedExecution;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.scenario.RuntimeStatistics;
import com.loadcoder.load.sut.SUT;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Logs;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class FullTest extends TestNGBase {

	Logger log = LoggerFactory.getLogger(FullTest.class);

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

		Summary summary = finished.getResultFromMemory().summaryStandard().build();
		summary.prettyPrint();

		chart.waitUntilClosed();
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

					if (a.getResponseTime() > 300) {
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
		Load l = new LoadBuilder(s).stopDecision(duration(20_000))

				.amountOfThreads(2).throttle(10, PER_MINUTE, PER_THREAD)

				.rampup(10 * SECOND).build();

		FinishedExecution finished = new ExecutionBuilder(l).storeAndConsumeResultRuntime(runtimeChart).build()
				.execute().andWait();

		Result result = finished.getReportedResultFromResultFile();
		new ResultChart(result);

		Summary summary = result.summaryStandard().build();
		summary.prettyPrint();
		runtimeChart.waitUntilClosed();
	}

}
