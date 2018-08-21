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
package com.loadcoder.load.chart.logic;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.LoadcoderRenderer;
import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.chart.logic.ResultChartLogic;
import com.loadcoder.load.chart.logic.ResultChartTest.ResultExtention;
import com.loadcoder.load.chart.logic.RuntimeChartLogic;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;

public class ResultChartLogicTest extends TestNGBase {

	XYSeriesCollectionExtention collection;

	LoadcoderRenderer renderer;

	Map<Comparable, Boolean> map;

	XYPlotExtension plot;

	RuntimeChartLogic logic;

	@BeforeMethod
	public void setup() {
		collection = new XYSeriesCollectionExtention();
		renderer = new LoadcoderRenderer(true, false, collection);
		map = new HashMap<Comparable, Boolean>();
		plot = ChartFrame.createXYPlotExtension("y", "x", collection, renderer);
	}

	@Test
	public void testcalculateSampleLengthDefault() {

		long result;
		try {
			result = ResultChartLogic.calculateSampleLengthDefault(0, -1, 1);
			Assert.fail("Expected an exception when max < min");
		} catch (RuntimeException rte) {
		}

		result = ResultChartLogic.calculateSampleLengthDefault(0, 0, 1);
		Assert.assertEquals(result, 1000);

		result = ResultChartLogic.calculateSampleLengthDefault(0, 1, 1);
		Assert.assertEquals(result, 1000);

		result = ResultChartLogic.calculateSampleLengthDefault(0, 5_000_000L, 1);
		Assert.assertEquals(result, 5_000);

		result = ResultChartLogic.calculateSampleLengthDefault(0, 5_000_000L, 3);
		Assert.assertEquals(result, 6_000);

		result = ResultChartLogic.calculateSampleLengthDefault(0, 15_000_000L, 1);

		result = ResultChartLogic.calculateSampleLengthDefault(0, 17_000_000L, 1);
		Assert.assertEquals(result, 18_000);

		result = ResultChartLogic.calculateSampleLengthDefault(0, 5_000_000L, 20);
		Assert.assertEquals(result, 10_000);

		result = ResultChartLogic.calculateSampleLengthDefault(0, 5_000_000L, 20);
		Assert.assertEquals(result, 10_000);

		result = ResultChartLogic.calculateSampleLengthDefault(0, 5_000_000L, 50);
		Assert.assertEquals(result, 13_000);
	}

	@Test
	public void updateTest() {
		Result r = new ResultExtention(ResultChartTestUtility.getTranses(10));
		ResultChartLogic logic = new ResultChartLogic(collection, plot, renderer, map, true, CommonSeries.values(),
				null, false, r);
		logic.createHashesAndUpdate(false);

	}
}
