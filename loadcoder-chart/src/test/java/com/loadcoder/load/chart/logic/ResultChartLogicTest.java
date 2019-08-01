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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.ResultExtension;
import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.menu.SteppingSlider;
import com.loadcoder.load.chart.menu.settings.DetailsSettings;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertTrue;

public class ResultChartLogicTest extends TestNGBase {

	Logger log = LoggerFactory.getLogger(ResultChartLogicTest.class);

	Map<String, Boolean> map;

	RuntimeChartLogic logic;

	@BeforeMethod
	public void setup() {
		map = new HashMap<String, Boolean>();
	}

	private void printArray(Integer[] ints) {
		log.info(Arrays.asList(ints).toString());
	}

	@Test
	public void testSampleLengthSliderValues(Method method) {

		int tickSize = 1;
		long sampleLengthToUse = 2 * tickSize * 1000;

		List<Integer> resultList = ResultChartLogic.getValuesForSliderAsList(sampleLengthToUse, tickSize);
		log.info(resultList.toString());

		assertTrue(resultList.contains(1));
		assertTrue(resultList.contains(3));
		assertTrue(resultList.contains(6));

		tickSize = 4;
		sampleLengthToUse = 2 * tickSize * 1000;

		resultList = ResultChartLogic.getValuesForSliderAsList(sampleLengthToUse, tickSize);
		log.info(resultList.toString());

		assertTrue(resultList.contains(1));
		assertTrue(resultList.contains(8));
		assertTrue(resultList.contains(24));

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
	public void testPoints() {
		int amountOfTransaction = 10;

		Result r = new ResultExtension(ResultChartTestUtility.getTranses(amountOfTransaction));
		ResultChartLogic logic = new ResultChartLogic(true, CommonSeries.values(), false, r);
		Map<String, XYSeriesExtension> dottedSerieses = logic.getDottedSeries();

		Assert.assertEquals(dottedSerieses.size(), 1);
		dottedSerieses.values().forEach(series -> Assert.assertEquals(series.getItems().size(), amountOfTransaction));

	}

	@Test
	public void testCommons() {
		Result r = new ResultExtension(ResultChartTestUtility.getTranses2(new long[][] { { 0, 0 }, { 10, 10 } }));
		ResultChartLogic logic = new ResultChartLogic(true, CommonSeries.values(), false, r);
//		List<XYSeriesExtension> commonSerieses = logic.getCommonSeries();
		Map<String, XYSeriesExtension> commonSerieses = logic.getCommonSeriesMap();
		// Check that the sampleLength is what it should be, since the assertions below
		// is dependant of this
		long sampleLengthMillis = logic.getSampleLengthToUse();
		Assert.assertEquals(sampleLengthMillis, 1000);

		for (XYSeriesExtension commonSeries : commonSerieses.values()) {
			if (commonSeries.getKey().equals(CommonSeries.THROUGHPUT.getName())) {
				List<XYDataItem> items = commonSeries.getItems();

				// 2 items there are the first sample where the transactions belongs, and the
				// ending sample
				Assert.assertEquals(items.size(), 2);
				// the throughput shall be 2, since there are 2 transactions made within one
				// second, equal to the sample length.
				Assert.assertEquals(items.get(0).getY(), 2.0D);
				// the next sample doesnt have any transaction, and the throughput value is
				// therefore 0.
				Assert.assertEquals(items.get(1).getY(), 0.0D);
			}
		}
	}

	@Test
	public void testSurroundingTimestampsForCommons() {
		Result r = new ResultExtension(
				ResultChartTestUtility.getTranses2(new long[][] { { 0, 0 }, { 3000, 10 }, { 6000, 13 } }));
		ResultChartLogic logic = new ResultChartLogic(true, CommonSeries.values(), false, r);
		Map<String, XYSeriesExtension> commonSerieses = logic.getCommonSeriesMap();

		// Check that the sampleLength is what it should be, since the assertions below
		// is dependant of this
		long sampleLengthMillis = logic.getSampleLengthToUse();
		Assert.assertEquals(sampleLengthMillis, 1000);

		for (XYSeriesExtension commonSeries : commonSerieses.values()) {
			if (commonSeries.getKey().equals(CommonSeries.THROUGHPUT.getName())) {
				List<XYDataItem> items = commonSeries.getItems();

				Assert.assertEquals(items.size(), 7);
				Assert.assertEquals(items.get(0).getY(), 1.0D);
				Assert.assertEquals(items.get(1).getY(), 0.0D);
				Assert.assertEquals(items.get(2).getY(), 0.0D);
				Assert.assertEquals(items.get(3).getY(), 1.0D);
				Assert.assertEquals(items.get(4).getY(), 0.0D);
				Assert.assertEquals(items.get(5).getY(), 0.0D);
				Assert.assertEquals(items.get(6).getY(), 1.0D);
			}
		}

		// verify that the sampleSeries is not affected by the SurroundingTimestamps
		// functionality
		XYSeries series = logic.getSeriesCollection().getSeries("a");
		List<XYDataItem> items = series.getItems();
		Assert.assertEquals(items.size(), 3);
		Assert.assertEquals(items.get(0).getY(), 0.0D);
		Assert.assertEquals(items.get(1).getY(), 10.0D);
		Assert.assertEquals(items.get(2).getY(), 13.0D);

	}
}
