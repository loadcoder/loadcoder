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
package com.loadcoder.load;

import static com.loadcoder.statics.LogbackLogging.getNewLogDir;
import static com.loadcoder.statics.LogbackLogging.setResultDestination;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.logic.Chart;
import com.loadcoder.load.chart.logic.ResultChart;
import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.FinishedExecution;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Logs;
import com.loadcoder.result.Result;

public class SeriesColorTest extends TestNGBase {

	@Test(groups = { "manual" })
	public void testManyTransactionTypes(Method method) {
		setResultDestination(getNewLogDir(rootResultDir, method.getName()));

		Load l = new LoadBuilder(TestUtils.s).build();
		FinishedExecution finished = new ExecutionBuilder(l).build().execute().andWait();

		Result result = finished.getReportedResultFromResultFile(Logs.getResultFileInLogDir());
		Chart c2 = new ResultChart(new CommonSeries[] {}, result);
		c2.waitUntilClosed();
	}

	@Test(groups = { "manual" })
	public void customizedColorsTest(Method method) {
		setResultDestination(getNewLogDir(rootResultDir, method.getName()));

		Load l = new LoadBuilder(TestUtils.s).build();

		FinishedExecution finished = new ExecutionBuilder(l).build().execute().andWait();

		Result result = finished.getReportedResultFromResultFile(Logs.getResultFileInLogDir());
		Chart c2 = new ResultChart(new CommonSeries[] {}, result);
		c2.waitUntilClosed();

	}
}
