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
package com.loadcoder.load.chart.jfreechart;

import java.awt.Paint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.LegendItem;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;



public class XYSeriesExtension extends XYSeries {
	/**
	 * XYSeriesExtension extends XYSeries for two reasons:
	 * visible: boolean to state if the series is visible in the chart or not.
	 * color: what color does the chart have in the chart.
	 *
	 */

	private static final long serialVersionUID = 1L;
	
	private boolean visible = true;
	Paint colorInTheChart;

	public Paint getColorInTheChart() {
		return colorInTheChart;
	}

	LegendItem legend;
	
	public void add(XYDataItem item, boolean notify, boolean debug) {
		super.add(item, notify);
	}
	
	public void setLegend(LegendItem legend) {
		this.legend = legend;
	}

	public LegendItem getLegend() {
		return legend;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
    
	Map<Number, Integer> xIndex = new HashMap<Number, Integer>();

	
	public XYDataItem remove(Number ind) {
		int index = indexOf(ind);
        XYDataItem removed = (XYDataItem) this.data.remove(index);
        return removed;
    }
    
	public XYSeriesExtension(
			Comparable key, 
			boolean autoSort, 
			boolean allowDuplicateXValues, 
			Paint colorInTheChart) {
		super(key, autoSort, allowDuplicateXValues);
		setNotify(false);
		this.colorInTheChart = colorInTheChart;
	}

	public List<XYDataItemExtension> getXYDataItems() {
		return data;
	}
	
	@Override
	public String toString(){
		return "" + getKey() + " visible:" + isVisible();
	}
}
