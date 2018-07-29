package com.loadcoder.load.chart.menu.settings;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;
import com.loadcoder.load.chart.logic.ResultChartLogic;

public class SettingsLogic {

	private XYSeriesExtension chosenSeries;

	private Map<XYSeriesExtension, Color> selections = new HashMap<XYSeriesExtension, Color>();

	private double keepFactorSelection = -1;

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
					renderAtEnd = false; // TODO: very ugly. improve this
				}
			}

			if (renderAtEnd) {
				chartLogic.forceRerender();
			}
		}
	}

	public void changeSeriesColorSelection(Color chosenColor) {
		selections.put(chosenSeries, chosenColor);
	}

	public Color getSeriesColorSelection() {
		return selections.get(chosenSeries);
	}
}
