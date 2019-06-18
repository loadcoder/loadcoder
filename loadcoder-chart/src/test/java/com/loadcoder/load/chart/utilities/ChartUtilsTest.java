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
package com.loadcoder.load.chart.utilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.testng.TestNGBase;

public class ChartUtilsTest extends TestNGBase {

	Logger log = LoggerFactory.getLogger(ChartUtilsTest.class);

	@Test
	public void calculateKeepFactorTest() {
		double test = 0.51;
		log.info("rounded {}", Math.round(test));
		double keepFactor = ChartUtils.calculateKeepFactor(100, 200);
		Assert.assertEquals(1.0, keepFactor);

		keepFactor = ChartUtils.calculateKeepFactor(100, 100);
		Assert.assertEquals(1.0, keepFactor);

		keepFactor = ChartUtils.calculateKeepFactor(200, 100);
		Assert.assertEquals(0.5, keepFactor);

		keepFactor = ChartUtils.calculateKeepFactor(1_000_000, 100);
		Assert.assertEquals(0.0001, keepFactor);

		keepFactor = ChartUtils.calculateKeepFactor(1_000_001, 100);
		Assert.assertEquals(0.0001, keepFactor);

		keepFactor = ChartUtils.calculateKeepFactor(1_000_000_001, 1_000_000_000);
		Assert.assertEquals(1.0, keepFactor);

		keepFactor = ChartUtils.calculateKeepFactor(1_000_000_001, 10);
		Assert.assertEquals(0.0001, keepFactor);
	}

	@Test
	public void populateSeriesWithPointsTest() {
		List<Point> points;
		XYSeriesExtension series;
		List items;

		/*
		 * this part tests that the correct amount is added, when the amount of poinst
		 * are dividable by the stepping size (0.1 gives 10 in steppingSize)
		 */
		points = oneGroup(1_000_000);
		series = new XYSeriesExtension("a", true, true, Color.BLACK);
		ChartUtils.populateSeriesWithPoints(points, series, 0.1);
		items = series.getItems();
		Assert.assertEquals((double) points.size() / 10, (double) items.size());

		/*
		 * this part tests that the correct amount is added, when the amount of poinst
		 * are dividable by the stepping size (0.1 gives 10 in steppingSize)
		 */
		points = oneGroup(1_000_005);
		series = new XYSeriesExtension("a", true, true, Color.BLACK);
		ChartUtils.populateSeriesWithPoints(points, series, 0.1);
		items = series.getItems();
		assertWithFaultTolerance(points.size() / 10, items.size(), 0.01);

		points = getPyramidPoints(100);

		series = new XYSeriesExtension("a", true, true, Color.BLACK);
		ChartUtils.populateSeriesWithPoints(points, series, 1);
		items = series.getItems();
		Assert.assertEquals(points.size(), items.size());

		series = new XYSeriesExtension("a", true, true, Color.BLACK);
		ChartUtils.populateSeriesWithPoints(points, series, 0.5);
		items = series.getItems();
		assertWithFaultTolerance(points.size() / 2, items.size(), 0.01);

		series = new XYSeriesExtension("a", true, true, Color.BLACK);
		ChartUtils.populateSeriesWithPoints(points, series, 0.1);
		items = series.getItems();
		assertWithFaultTolerance(points.size() / 10, items.size(), 0.01);

		points = getPyramidPoints(2000);
		series = new XYSeriesExtension("a", false, true, Color.BLACK);
		long start = System.currentTimeMillis();
		ChartUtils.populateSeriesWithPoints(points, series, 0.3);
		long diff = System.currentTimeMillis() - start;
		log.info("amount:{} took {} ms", points.size(), diff);
		items = series.getItems();
		assertWithFaultTolerance((int) (points.size() * 0.3), items.size(), 0.5);

	}

	private void assertWithFaultTolerance(int expected, int actual, double faultToleranceFactor) {

		double max = expected + expected * faultToleranceFactor;
		double min = expected - expected * faultToleranceFactor;

		log.info("difference for points reduction: " + (actual - expected));
		Assert.assertTrue(max > actual);
		Assert.assertTrue(min < actual);

	}

	List<Point> getPyramidPoints(int amountOfGroups) {
		List<Point> list = new ArrayList<Point>();
		for (int y = 1; y <= amountOfGroups; y++) {
			for (int x = 0; x < y; x++) {
				list.add(new Point(x, y, true));
			}
		}
		return list;
	}

	List<Point> getALotOfSinglePointsGroups(int amountOfGroups) {
		List<Point> list = new ArrayList<Point>();
		for (int y = 1; y <= amountOfGroups; y++) {

			if (amountOfGroups == y) {
				for (int x = 0; x < y; x++) {
					list.add(new Point(x, y, true));
				}
			} else {
				list.add(new Point(0, y, true));
			}
		}
		return list;
	}

	List<Point> oneGroup(int amountOfPoints) {
		List<Point> list = new ArrayList<Point>();
		for (int x = 1; x <= amountOfPoints; x++) {
			list.add(new Point(x, 0, true));
		}
		return list;
	}
}
