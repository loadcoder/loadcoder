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
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.data.xy.XYSeries;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;
import com.loadcoder.load.chart.logic.ChartTest;
import com.loadcoder.load.chart.logic.RuntimeChart;
import com.loadcoder.load.chart.logic.RuntimeChartLogic;

public class ChartFrameTest {

	@Test
	public void testHandleClick() {
		RuntimeChartLogic logic = RuntimeChart.createNewRuntimeChartLogic();

		XYSeriesCollectionExtention xySeriesCollectionExtention = Mockito.mock(XYSeriesCollectionExtention.class);
		List<XYSeries> list = new ArrayList<XYSeries>();
		XYSeriesExtension seriesFoo = new XYSeriesExtension("foo", true, false, Color.BLACK);
		seriesFoo.setLegend(new LegendItem("foo"));
		XYSeriesExtension seriesBar = new XYSeriesExtension("bar", true, false, Color.WHITE);
		seriesBar.setLegend(new LegendItem("bar"));

		list.add(seriesFoo);
		list.add(seriesBar);

		PlotEntity graphArea = Mockito.mock(PlotEntity.class);
		LegendItemEntity legendArea = Mockito.mock(LegendItemEntity.class);

		int anotherButtonThanTheLeftOne = RuntimeChartLogic.MOUSE_LEFT_CLICK_CODE + 1;

		Mockito.when(xySeriesCollectionExtention.getSeries()).thenReturn(list);
		logic.handleClick(RuntimeChartLogic.MOUSE_LEFT_CLICK_CODE, graphArea, xySeriesCollectionExtention);
		logic.handleClick(anotherButtonThanTheLeftOne, graphArea, xySeriesCollectionExtention);

		/*
		 * If a visible legend is left clicked, it will become invisible. Other legends
		 * will be unaffected
		 */
		Mockito.when(legendArea.getSeriesKey()).thenReturn("foo");
		logic.handleClick(RuntimeChartLogic.MOUSE_LEFT_CLICK_CODE, legendArea, xySeriesCollectionExtention);
		assertTrue(!seriesFoo.isVisible());
		assertTrue(seriesBar.isVisible());

		/*
		 * If an invisible legend is right clicked, it will become visible. Other
		 * legends will become invisible
		 */
		logic.handleClick(anotherButtonThanTheLeftOne, legendArea, xySeriesCollectionExtention);
		assertTrue(seriesFoo.isVisible());
		assertTrue(!seriesBar.isVisible());

		/*
		 * if a visible legend is right clicked when at least on other legend is
		 * invisible, this will lead to that all legends will become visible
		 */
		logic.handleClick(anotherButtonThanTheLeftOne, legendArea, xySeriesCollectionExtention);
		assertTrue(seriesFoo.isVisible());
		assertTrue(seriesBar.isVisible());

		/*
		 * If a legend is right clicked when all legends are visible, the clicked legend
		 * will be the only one visible
		 */
		logic.handleClick(anotherButtonThanTheLeftOne, legendArea, xySeriesCollectionExtention);
		assertTrue(seriesFoo.isVisible());
		assertTrue(!seriesBar.isVisible());

	}

	@Test(groups = "manual")
	public void chartFrameExperiment() {
		ChartLogic logic = ChartTest.getNewLogic();
		ChartFrame frame = new ChartFrame(true, false, new HashMap<String, Color>(), logic);
		frame.setVisible(true);
		logic.initiateChart();

		XYSeriesExtension series = new XYSeriesExtension("foo", true, false, Color.GREEN);

		logic.getSeriesCollection().addSeries(series);
		series.add(new XYDataItemExtension(0, 0));
		series.add(new XYDataItemExtension(10, 10));

		XYDataItemExtension item = new XYDataItemExtension(12, 5);
		series.add(item);

		series.remove(item.getX());

		series.add(item);

		logic.getSeriesCollection().fireChange();

		series.remove(item.getX());
		logic.getSeriesCollection().fireChange();

		series.add(item);

		logic.getSeriesCollection().fireChange();

		LoadUtility.sleep(5_000);
		series.setColorInTheChart(Color.BLUE);
		logic.getSeriesCollection().fireChange();

		LoadUtility.sleep(600_000);
	}
}
