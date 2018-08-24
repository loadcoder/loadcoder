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

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JRadioButtonMenuItem;

import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;
import com.loadcoder.load.chart.logic.ResultChartLogic;

public class SettingsLogic {

	private XYSeriesExtension chosenSeries;

	private Map<XYSeriesExtension, Color> selections = new HashMap<XYSeriesExtension, Color>();

	private double keepFactorSelection = -1;

	JRadioButtonMenuItem points;

	public double getKeepFactorSelection() {
		return keepFactorSelection;
	}

	public void setKeepFactorSelection(double keepFactorSelection) {
		this.keepFactorSelection = keepFactorSelection;
	}

	private final ChartLogic chartLogic;

	private boolean colorChooserVisible = false;

	public void setChosenSeries(XYSeriesExtension chosenSeries) {
		this.chosenSeries = chosenSeries;
	}

	public boolean isColorChooserVisible() {
		return colorChooserVisible;
	}

	public void setColorChooserVisible(boolean colorChooserVisible) {
		this.colorChooserVisible = colorChooserVisible;
	}

	public SettingsLogic(ChartLogic chartLogic) {
		this.chartLogic = chartLogic;
	}

	void applyColorSelections() {
		XYPlotExtension plot = chartLogic.getPlot();
		boolean renderAtEnd = true;
		synchronized (plot) {
			Iterator<Entry<XYSeriesExtension, Color>> i = selections.entrySet().iterator();
			while (i.hasNext()) {
				Entry<XYSeriesExtension, Color> entry = i.next();
				Color newColor = entry.getValue();
				XYSeriesExtension series = entry.getKey();
				Color oldColor = (Color) series.getColorInTheChart();
				series.setColorInTheChart(newColor);
				series.getLegend().setFillPaint(newColor);
				series.getLegend().setOutlinePaint(newColor);

				List<Color> existingColors = chartLogic.getExistingColors();
				existingColors.remove(oldColor);
				existingColors.add(newColor);
			}

			if (keepFactorSelection != -1) {
				if (chartLogic instanceof ResultChartLogic) {
					ResultChartLogic resultChartLogic = (ResultChartLogic) chartLogic;

					resultChartLogic.setKeepFactorChosen(keepFactorSelection);
					resultChartLogic.recreateDottedSeries();
					String keepFactorAsProcentString = keepFactorToProcentString(keepFactorSelection);
					resultChartLogic.getPointsRadioButton().setText(String.format("Points (%s)", keepFactorAsProcentString));
					renderAtEnd = false; // TODO: very ugly. improve this
				}
			}

			if (renderAtEnd) {
				chartLogic.forceRerender();
			}
		}
	}

	public static String keepFactorToProcentString(double value) {
		long multiRounding = Math.round(value * 10000);
		double rounded = (double) multiRounding / 100;
		if(rounded >= 1)
			return "" + ((long)rounded) + "%";
		return "" + (rounded) + "%";

	}
	public void changeSeriesColorSelection(Color chosenColor) {
		selections.put(chosenSeries, chosenColor);
	}

	public Color getSeriesColorSelection() {
		return selections.get(chosenSeries);
	}
}
