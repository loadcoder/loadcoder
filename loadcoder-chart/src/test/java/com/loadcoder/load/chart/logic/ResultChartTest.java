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

import static com.loadcoder.load.chart.logic.ResultChartTestUtility.getTranses;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.chart.ResultExtension;
import com.loadcoder.load.chart.data.DataSet;
import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.data.Ranges;
import com.loadcoder.load.chart.jfreechart.ChartFrame.DataSetUser;
import com.loadcoder.load.chart.sampling.Sample;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;

public class ResultChartTest extends TestNGBase {

	Logger log = LoggerFactory.getLogger(ResultChartTest.class);

	List<DataSet> getNewDataList(int size) {
		return getNewDataList(size, 1);
	}

	List<DataSet> getNewDataList(int size, int serieses) {
		List<DataSet> list = new ArrayList<DataSet>();
		for (int j = 0; j < serieses; j++) {
			List<Point> pointList = new ArrayList<Point>();
			for (int i = 0; i < size; i++) {
				pointList.add(new Point(i, i, true));
			}
			DataSet d = new DataSet("foo" + j, pointList);

			list.add(d);
		}
		return list;
	}

	interface YGiver {
		long y(int i);
	}

	@Test(groups = "manual")
	public void testManyTransactions(Method method) {
		Result result = new ResultExtension(getTranses(1000, 20, 20, (i) -> {
			return LoadUtility.random(5, 9);
		}));
		ResultChart c = new ResultChart(result);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void testHighIntensity(Method method) {

		Result result = new ResultExtension(getTranses(1000, 10, 20, (i) -> {
			return LoadUtility.random(5, 9);
		}));
		ResultChart c = new ResultChart(result);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void startResultChart(Method method) {

		Result result = new ResultExtension(getTranses(10));
		ResultChart c = new ResultChart(result);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void oneSlide(Method method) {

		Result result = new ResultExtension(getTranses(10));
		ResultChart f = new ResultChart(result);
		ResultChartLogic c = (ResultChartLogic) f.getLogic();
		c.chartSliderAjustment(2000);
		f.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void twoSlide(Method method) {

		Result result = new ResultExtension(getTranses(10));
		ResultChart f = new ResultChart(result);
		ResultChartLogic c = (ResultChartLogic) f.getLogic();
		c.chartSliderAjustment(2000);
		c.chartSliderAjustment(4000);
		f.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void dotting(Method method) {

		Result result = new ResultExtension(getTranses(10));
		ResultChart c = new ResultChart(result);
		c.ajustDottedMode(true);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void dottingAndSample(Method method) {

		Result result = new ResultExtension(getTranses(10));
		ResultChart c = new ResultChart(result);
		c.ajustDottedMode(true);
		c.ajustDottedMode(false);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void dottingSampleDotting(Method method) {

		Result result = new ResultExtension(getTranses(10));
		ResultChart c = new ResultChart(result);
		c.ajustDottedMode(true);
		c.ajustDottedMode(false);
		c.ajustDottedMode(true);
		c.waitUntilClosed();
	}

	@Test(groups = "manual")
	public void removeHighest(Method method) {

		Result result = new ResultExtension(getTranses(100));
		ResultChart c = new ResultChart(result);
		c.ajustDottedMode(true);
		c.ajustDottedMode(false);
		c.ajustDottedMode(true);
		c.waitUntilClosed();
	}

	@Test
	public void testRemovalFilterHighestPercent() {

		List<DataSet> list = getNewDataList(100);
		DataSetUser percentRemovalFilter = ResultChartLogic.removePercentile(10);
		percentRemovalFilter.useDataSet(list);
		List<Point> pointList = list.get(0).getPoints();
		Assert.assertEquals(pointList.size(), 100);
		int amountDisabled = 0;
		for (Point point : pointList) {
			if (!point.isEnabled())
				amountDisabled++;
		}
		Assert.assertEquals(amountDisabled, 10);
	}

	@Test
	public void addSurroundingTimestampsWhenEmptyTest() {

		long sampleLength = 1000;
		HashSet<Long> hashesGettingUpdated = new HashSet<Long>();
		HashSet<Long> sampleTimestamps = new HashSet<Long>();

		Ranges ranges = new Ranges();
		ranges.addRange(new Range(Long.MIN_VALUE, Long.MAX_VALUE, sampleLength));
		ChartLogic.addSurroundingTimestampsAsUpdates(hashesGettingUpdated, 7000, 1000, 15000, ranges, sampleLength,
				sampleTimestamps, new HashMap<Long, Sample>());

		Assert.assertEquals(hashesGettingUpdated.size(), 14);
	}

	@Test
	public void addSurroundingTimestampsAsUpdates2RangesTest() {

		long sampleLength = 1000;
		HashSet<Long> hashesGettingUpdated = new HashSet<Long>();
		HashSet<Long> sampleTimestamps = new HashSet<Long>();
		sampleTimestamps.add(5000L);
		sampleTimestamps.add(25_000L);

		Ranges ranges = new Ranges();
		ranges.addRange(new Range(Long.MIN_VALUE, 14_999, sampleLength));
		ranges.addRange(new Range(15_000, Long.MAX_VALUE, sampleLength * 2));
		ChartLogic.addSurroundingTimestampsAsUpdates(hashesGettingUpdated, 10_000, 1_000, 30_000, ranges, sampleLength,
				sampleTimestamps, new HashMap<Long, Sample>());

		Assert.assertTrue(hashesGettingUpdated.contains(9000L));

		Assert.assertTrue(hashesGettingUpdated.contains(11_000L));
		Assert.assertTrue(hashesGettingUpdated.contains(12_000L));
		Assert.assertTrue(hashesGettingUpdated.contains(13_000L));
		Assert.assertTrue(hashesGettingUpdated.contains(14_000L));
		Assert.assertTrue(hashesGettingUpdated.contains(15_000L));
		Assert.assertFalse(hashesGettingUpdated.contains(16_000L)); // 16 should not exist
		Assert.assertTrue(hashesGettingUpdated.contains(17_000L));

	}

	@Test
	public void amountOfSeriesesFactorTest() {

		double factor = ResultChartLogic.amountOfSeriesesFactor(1);
		log.info("factor: {}", factor);
		double factor2 = ResultChartLogic.amountOfSeriesesFactor(5);
		log.info("factor: {}", factor);

		double diff1 = (factor2 - factor) / (5 - 1);
		log.info("factor/series: {}", diff1);

		factor = ResultChartLogic.amountOfSeriesesFactor(10);
		log.info("factor: {}", factor);
		factor = ResultChartLogic.amountOfSeriesesFactor(30);
		log.info("factor: {}", factor);
		factor = ResultChartLogic.amountOfSeriesesFactor(50);
		log.info("factor: {}", factor);
		factor2 = ResultChartLogic.amountOfSeriesesFactor(150);
		log.info("factor: {}", factor);

		double diff2 = (factor2 - factor) / (150 - 50);
		log.info("factor/series: {}", diff2);
		assertTrue(diff1 > diff2);

	}

	@Test
	public void testRemovalFilterHighestPercent0() {

		List<DataSet> list = getNewDataList(0);
		DataSetUser percentRemovalFilter = ResultChartLogic.removePercentile(10);
		percentRemovalFilter.useDataSet(list);
		List<Point> pointList = list.get(0).getPoints();

		int amountDisabled = 0;
		for (Point point : pointList) {
			if (!point.isEnabled())
				amountDisabled++;
		}
		Assert.assertEquals(amountDisabled, 0);

	}

	@Test
	public void testRemovalFilterHighestPercent1() {

		List<DataSet> list = getNewDataList(1);
		DataSetUser percentRemovalFilter = ResultChartLogic.removePercentile(10);
		percentRemovalFilter.useDataSet(list);
		List<Point> pointList = list.get(0).getPoints();

		int amountDisabled = 0;
		for (Point point : pointList) {
			if (!point.isEnabled())
				amountDisabled++;
		}
		Assert.assertEquals(amountDisabled, 0);

	}

}
