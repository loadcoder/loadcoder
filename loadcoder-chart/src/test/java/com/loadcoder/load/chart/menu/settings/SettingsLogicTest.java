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
package com.loadcoder.load.chart.menu.settings;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.LegendItem;
import org.jfree.data.xy.XYSeries;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;


public class SettingsLogicTest {

	@Test
	public void create() {
		ChartLogic chartLogic = mock(ChartLogic.class);
		List<XYSeries> list = new ArrayList<XYSeries>();
		ColorSettings logic = new ColorSettings(chartLogic, list);
		assertEquals(logic.isColorChooserVisible(), false);
	}

	@Test
	public void selectColorTest() {
		ChartLogic chartLogic = mock(ChartLogic.class);

		List<XYSeries> list = new ArrayList<XYSeries>();
		ColorSettings logic = new ColorSettings(chartLogic, list);

		Color chosenColor = Color.PINK;
		logic.changeSeriesColorSelection(chosenColor);
		Color selectedColor = logic.getSeriesColorSelection();
		assertEquals(selectedColor, chosenColor);
	}

	@Test
	public void applyColorTest() {
		ChartLogic chartLogic = mock(ChartLogic.class);
		XYPlotExtension plot = mock(XYPlotExtension.class);
		XYSeriesExtension series = mock(XYSeriesExtension.class);
		LegendItem legend = mock(LegendItem.class);
		Map<String, Color> existingColors = new HashMap<String, Color>();
		when(chartLogic.getPlot()).thenReturn(plot);
		when(series.getLegend()).thenReturn(legend);
		when(series.getKey()).thenReturn("foo");
		when(chartLogic.getExistingColors()).thenReturn(existingColors);

		List<XYSeries> list = new ArrayList<XYSeries>();
		ColorSettings logic = new ColorSettings(chartLogic, list);
		logic.setChosenSeries(series);
		Color chosenColor = Color.PINK;
		logic.changeSeriesColorSelection(chosenColor);

		ChartSettingsActionsModel chartSettingsActionsModel = new ChartSettingsActionsModel();
		logic.apply(chartSettingsActionsModel);

		verify(series).setColorInTheChart(Color.PINK);
		verify(legend).setFillPaint(Color.PINK);
		verify(legend).setOutlinePaint(Color.PINK);
		assertEquals(existingColors.size(), 1);
		assertEquals(existingColors.get("foo"), Color.PINK);

	}
}
