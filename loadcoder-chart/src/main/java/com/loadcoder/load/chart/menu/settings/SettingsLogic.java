package com.loadcoder.load.chart.menu.settings;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;

public class SettingsLogic {

    private XYSeriesExtension chosenSeries;

    private Map<XYSeriesExtension, Color> selections = new HashMap<XYSeriesExtension, Color>();
    
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

	public SettingsLogic(ChartLogic chartLogic){
    	this.chartLogic = chartLogic;
    }
    
	void applyColorSelections(){
		XYPlotExtension plot = chartLogic.getPlot();
		synchronized (plot) {
			Iterator<Entry<XYSeriesExtension, Color>> i = selections.entrySet().iterator();
			while(i.hasNext()) {
				Entry<XYSeriesExtension, Color> entry = i.next();
				Color newColor = entry.getValue();
				XYSeriesExtension series = entry.getKey();
				Color oldColor = (Color)series.getColorInTheChart();
				series.setColorInTheChart(newColor);
				series.getLegend().setFillPaint(newColor);
				series.getLegend().setOutlinePaint(newColor);
				
				List<Color> existingColors = chartLogic.getExistingColors();
				existingColors.remove(oldColor);
				existingColors.add(newColor);
				
			}
			chartLogic.forceRerender();
		}
	}

	public void changeSeriesColorSelection(Color chosenColor) {
		selections.put(chosenSeries, chosenColor);
	}
	
	public Color getSeriesColorSelection() {
		return selections.get(chosenSeries);
	}
}
