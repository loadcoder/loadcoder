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
package com.loadcoder.load.chart;

import java.awt.Color;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.sampling.Sample;
import com.loadcoder.load.chart.sampling.SampleConcaternator;
import com.loadcoder.load.chart.sampling.SampleGroup;
import com.loadcoder.load.chart.utilities.ChartUtils;
import com.loadcoder.load.testng.TestNGBase;

public class SampleGroupTest extends TestNGBase {

	XYSeriesExtension series = new XYSeriesExtension("foo", true, false, Color.RED);

	@Test
	public void test0() {
		long sampleLength = 1000;
		SampleGroup group = new SampleGroup(sampleLength, series, false);
		Sample s = group.getOrCreateSample(0, series.getKey(), 1000);
		Sample s2 = group.getOrCreateSample(0, series.getKey(), 1000);

		Assert.assertEquals(0, s.getFirstTs());
		Assert.assertEquals(999, s.getLastTs());
		Assert.assertEquals(s, s2);
	}

	@Test
	public void testMinusSampleLength() {
		long sampleLength = 1000;
		long tsToTest = -1000;

		SampleGroup group = new SampleGroup(sampleLength, series, false);
		Sample s = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);
		Sample s2 = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);

		Assert.assertEquals(-1000, s.getFirstTs());
		Assert.assertEquals(-1, s.getLastTs());
		Assert.assertEquals(s, s2);
	}

	@Test
	public void testPlusSampleLength() {
		long sampleLength = 1000;
		long tsToTest = 1000;

		SampleGroup group = new SampleGroup(sampleLength, series, false);
		Sample s = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);
		Sample s2 = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);

		Assert.assertEquals(1000, s.getFirstTs());
		Assert.assertEquals(1999, s.getLastTs());
		Assert.assertEquals(s, s2);
	}

	@Test
	public void testRest1() {

		long sampleLength = 1000;
		long tsToTest = -999;

		SampleGroup group = new SampleGroup(sampleLength, series, false);
		Sample s = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);
		Sample s2 = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);

		Assert.assertEquals(-1000, s.getFirstTs());
		Assert.assertEquals(-1, s.getLastTs());
		Assert.assertEquals(s, s2);
	}

	@Test
	public void test2SamplesBelow0AndWithRest() {

		long sampleLength = 1000;
		long tsToTest = -1500;

		SampleGroup group = new SampleGroup(sampleLength, series, false);
		Sample s = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);
		Sample s2 = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);

		Assert.assertEquals(-2000, s.getFirstTs());
		Assert.assertEquals(-1001, s.getLastTs());
		Assert.assertEquals(s, s2);
	}

	@Test
	public void test2SamplesOver0AndWithRest() {
		long sampleLength = 1000;
		long tsToTest = 1500;

		SampleGroup group = new SampleGroup(sampleLength, series, false);
		Sample s = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);
		Sample s2 = group.getOrCreateSample(tsToTest, series.getKey(), sampleLength);

		Assert.assertEquals(1000, s.getFirstTs());
		Assert.assertEquals(1999, s.getLastTs());
		Assert.assertEquals(s, s2);
	}

	@Test
	public void testConcaternation() {
		XYSeriesExtension serie = new XYSeriesExtension("foo", true, false, Color.RED);

		SampleGroup group = new SampleGroup(1000, serie, false);
		Sample a = group.getOrCreateSample(0, "foo", 1000);
		a.addPoint(new Point(1, 10, true));

		Sample b = group.getOrCreateSample(1000, "foo", 1000);
		b.addPoint(new Point(1001, 0, true));

		ChartUtils.populateSeriesWithSamples(a, serie);
		ChartUtils.populateSeriesWithSamples(b, serie);

		Range oldRange = new Range(0, Long.MAX_VALUE, 1000);
		Range newRange = new Range(Long.MIN_VALUE, -1, 2000);

		SampleConcaternator sampleConcaternator = new SampleConcaternator(oldRange, newRange, 2, (c) -> {
			return true;
		});

		Sample aBeforeConcat = group.getExistingSample(0, 1000);
		Sample bBeforeConcat = group.getExistingSample(999, 1000);
		Sample nextBeforeConcat = group.getExistingSample(1000, 1000);

		Assert.assertEquals(aBeforeConcat, bBeforeConcat);
		Assert.assertNotSame(bBeforeConcat, nextBeforeConcat);
		group.concaternate(sampleConcaternator);

		Sample aAfterConcat = group.getExistingSample(0, 2000);
		Sample bAfterConcat = group.getExistingSample(1999, 2000);

		Assert.assertEquals(aAfterConcat, bAfterConcat);
	}

}
