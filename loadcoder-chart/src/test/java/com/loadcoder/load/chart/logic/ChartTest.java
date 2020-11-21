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
package com.loadcoder.load.chart.logic;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;
import com.loadcoder.result.ResultFormatter;
import com.loadcoder.result.TransactionExecutionResult;
import com.loadcoder.statics.Formatter;

public class ChartTest extends TestNGBase {

	/**
	 * Test that generates chart with 2 short graph
	 * 
	 * @throws IOException
	 */
	@Test(groups = "manual")
	public void doSimpleChart() throws IOException {
		File f = new File("src/test/resources/testresults/small_result.log");

		ResultFormatter formatter = Formatter.SIMPLE_RESULT_FORMATTER;
		Result result = formatter.toResultList(f);

		ResultChart c = new ResultChart(result);
		ChartLogic logic = c.getLogic();
		XYSeriesExtension series = new XYSeriesExtension("hello", true, false, Color.RED);
		series.add(0, 0);
		series.add(140, 140);
		logic.addSeries(series);

		c.waitUntilClosed();
	}

	/**
	 * Test that generates a chart from a file containing negative values.
	 * 
	 * @throws IOException
	 */
	@Test(groups = "manual")
	public void lotsOfTransactionTypes() throws IOException {
		File f = new File("src/test/resources/testresults/a_lot_of_transactiontypes.log");
		ResultFormatter formatter = Formatter.SIMPLE_RESULT_FORMATTER;
		Result result = formatter.toResultList(f);

		Chart c = new ResultChart(result);
		c.waitUntilClosed();
	}

	/**
	 * Test that generates a chart from a file containing negative values.
	 * 
	 * @throws IOException
	 */
	@Test(groups = "manual")
	public void testNegative() throws IOException {
		File f = new File("src/test/resources/testresults/negative_timevalue.log");
		ResultFormatter formatter = Formatter.SIMPLE_RESULT_FORMATTER;
		Result result = formatter.toResultList(f);

		ResultChart c = new ResultChart(result);
		c.waitUntilClosed();
	}

	/**
	 * Test that generates a chart from a file where the results are unordered. The
	 * chart should still look correct.
	 * 
	 * @throws IOException
	 */
	@Test(groups = "manual")
	public void unordered_results() throws IOException {
		File f = new File("src/test/resources/testresults/unordered_timevalue.log");

		ResultFormatter formatter = Formatter.SIMPLE_RESULT_FORMATTER;
		Result result = formatter.toResultList(f);

		Chart c = new ResultChart(result);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void temp() throws IOException {
		File f = new File("../loadcoder-test/target/testDynamicChart/2017-01-30_195731/result.log");

		ResultFormatter formatter = Formatter.SIMPLE_RESULT_FORMATTER;
		Result result = formatter.toResultList(f);

		Chart c = new ResultChart(result);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void addAndRemoveXYDataItems() throws IOException {

		ChartLogic logic = getNewLogic();
		ChartFrame frame = new ChartFrame(false, true, new HashMap<String, Color>(), logic);

		XYSeriesExtension series = new XYSeriesExtension("foo", true, false, Color.BLUE);

		XYDataItem toBeRemoved = new XYDataItem(1000, 20);
		series.add(new XYDataItem(0, 10));
		series.add(new XYDataItem(2000, 10));

		logic.getSeriesCollection().addSeries(series);
		logic.initiateChart();
		frame.setVisible(true);
		for (int i = 0; i < 5; i++) {
			series.add(toBeRemoved);
			LoadUtility.sleep(1000);

			series.getXYDataItems().remove(toBeRemoved);
			logic.getChart().setNotify(true);
			logic.forceRerender();
			LoadUtility.sleep(1000);
		}
		frame.waitUntilClosed();
	}

	public static ChartLogic getNewLogic() {
		ChartLogic l = new ChartLogic(CommonSeries.values(), true) {

			@Override
			protected void update(Map<String, List<TransactionExecutionResult>> listOfListOfList,
					HashSet<Long> hashesGettingUpdated) {
				// TODO Auto-generated method stub
			}

			@Override
			protected void doUpdate() {
				// TODO Auto-generated method stub
			}
		};
		return l;
	}

}
