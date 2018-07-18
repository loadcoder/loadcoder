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

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.loadcoder.load.TestUtils;
import com.loadcoder.load.chart.logic.RuntimeChart;
import com.loadcoder.load.sut.SUT;
import com.loadcoder.load.testng.TestNGBase;

public class DefaultPackageTests extends TestNGBase {

	@Test(groups = "manual")
	public void sendInPreHistoricResultsIntoRuntimeChart_NPE(Method method) {

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
			}
		};
		Load l = new LoadBuilder(s).stopDecision(duration(500_000)).build();

		Execution execution = new ExecutionBuilder(l).runtimeResultUser(new RuntimeChart()).build();
		TestUtils.add(execution.getTransactionExecutionResultBuffer().getBuffer(), 500_000, 1);

		execution.execute().andWait();
	}

	@Test(groups = "manual")
	public void sendInPreHistoricResultsIntoRuntimeChart(Method method) {

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
			}
		};
		Load l = new LoadBuilder(s).stopDecision(duration(300_000)).build();
		Execution execution = new ExecutionBuilder(l).runtimeResultUser(new RuntimeChart()).build();

		TestUtils.add(execution.getTransactionExecutionResultBuffer().getBuffer(), 20000_000, 2);

		execution.execute().andWait();
	}
}
