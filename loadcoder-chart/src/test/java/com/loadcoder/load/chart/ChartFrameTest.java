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

import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.data.xy.XYSeries;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;

public class ChartFrameTest {

	@Test
	public void testHandleClick() {
		ChartFrame frame = new ChartFrame(true, false);
		PlotEntity e = Mockito.mock(PlotEntity.class);
		XYSeriesCollectionExtention xySeriesCollectionExtention = Mockito.mock(XYSeriesCollectionExtention.class);
		List<XYSeries> list = new ArrayList<XYSeries>();
		XYSeriesExtension xySeriesExtension = new XYSeriesExtension("foo", true, false, Color.BLACK);
		xySeriesExtension.setLegend(new LegendItem("foo"));
		XYSeriesExtension xySeriesExtension2 = new XYSeriesExtension("bar", true, false, Color.WHITE);
		xySeriesExtension2.setLegend(new LegendItem("bar"));
		
		list.add(xySeriesExtension);
		list.add(xySeriesExtension2);
		
 		Mockito.when(xySeriesCollectionExtention.getSeries()).thenReturn(list);
		frame.handleClick(1, e, xySeriesCollectionExtention);
		frame.handleClick(2, e, xySeriesCollectionExtention);
		LegendItemEntity e2 = Mockito.mock(LegendItemEntity.class);
		Mockito.when(e2.getSeriesKey()).thenReturn("foo");
		frame.handleClick(1, e2, xySeriesCollectionExtention);
		assertTrue(! xySeriesExtension.isVisible()); //not visible when left clicking on series legend
		assertTrue(xySeriesExtension2.isVisible()); //visible default

		frame.handleClick(2, e2, xySeriesCollectionExtention);
		assertTrue( xySeriesExtension.isVisible()); //visible when right clicking on series legend
		assertTrue(! xySeriesExtension2.isVisible()); //not visible hence right click of another series
		
	}
}
