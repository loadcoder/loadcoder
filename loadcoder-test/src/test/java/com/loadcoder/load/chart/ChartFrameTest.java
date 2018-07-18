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

import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;

public class ChartFrameTest {

	@Test(groups = "manual")
	public void chartFrameExperiment() {
		ChartFrame frame = new ChartFrame(true, false);
		frame.setVisible(true);
		frame.showChart();
		
		XYSeriesExtension series = new XYSeriesExtension("test", true, false, Color.GREEN);

		frame.getSeriesCollection().addSeries(series);
		series.add(new XYDataItemExtension(0, 0));
		series.add(new XYDataItemExtension(10, 10));

		XYDataItemExtension item = new XYDataItemExtension(12, 5);
		series.add(item);
		
		series.remove(item.getX());
		
		series.add(item);
		
		frame.getSeriesCollection().fireChange();
		
		series.remove(item.getX());
		frame.getSeriesCollection().fireChange();
				
		series.add(item);
		
		frame.getSeriesCollection().fireChange();

		LoadUtility.sleep(5_000);
		series.setColorInTheChart(Color.BLUE);
		frame.getSeriesCollection().fireChange();

		LoadUtility.sleep(600_000);
	}
}
