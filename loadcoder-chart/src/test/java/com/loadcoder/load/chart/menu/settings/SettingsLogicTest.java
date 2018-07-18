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
package com.loadcoder.load.chart.menu.settings;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;

import junit.framework.Assert;

public class SettingsLogicTest {

	@Test
	public void create() {
		ChartLogic chartLogic = mock(ChartLogic.class);
		SettingsLogic logic = new SettingsLogic(chartLogic);
		Assert.assertEquals(false, logic.isColorChooserVisible());
	}

	@Test
	public void selectColorTest() {
		ChartLogic chartLogic = mock(ChartLogic.class);
		SettingsLogic logic = new SettingsLogic(chartLogic);

		Color chosenColor = Color.PINK;
		logic.changeSeriesColorSelection(chosenColor);
		Color selectedColor = logic.getSeriesColorSelection();
		Assert.assertEquals(chosenColor, selectedColor);
	}

	@Test
	public void applyColorTest() {
		ChartLogic chartLogic = mock(ChartLogic.class);
		XYPlotExtension plot = mock(XYPlotExtension.class);
		XYSeriesExtension series = mock(XYSeriesExtension.class);
		LegendItem legend = mock(LegendItem.class);
		List<Color> existingColors = new ArrayList<Color>();
		when(chartLogic.getPlot()).thenReturn(plot);
		when(series.getLegend()).thenReturn(legend);
		when(chartLogic.getExistingColors()).thenReturn(existingColors);

		SettingsLogic logic = new SettingsLogic(chartLogic);
		logic.setChosenSeries(series);
		Color chosenColor = Color.PINK;
		logic.changeSeriesColorSelection(chosenColor);
		logic.applyColorSelections();

		verify(series).setColorInTheChart(Color.PINK);
		verify(legend).setFillPaint(Color.PINK);
		verify(legend).setOutlinePaint(Color.PINK);
		Assert.assertEquals(1, existingColors.size());
		Assert.assertEquals(Color.PINK, existingColors.get(0));
		
	}
}
