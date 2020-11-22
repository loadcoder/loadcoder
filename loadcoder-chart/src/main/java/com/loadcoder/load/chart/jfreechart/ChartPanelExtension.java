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
package com.loadcoder.load.chart.jfreechart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public class ChartPanelExtension extends ChartPanel{

	private static final long serialVersionUID = 1L;

	public ChartPanelExtension(JFreeChart chart, int width, int height,
	           int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
	           int maximumDrawHeight, boolean useBuffer, boolean properties,
	           boolean copy, boolean save, boolean print, boolean zoom,
	           boolean tooltips) {
		super(chart, width, height,
		           minimumDrawWidth, minimumDrawHeight, maximumDrawWidth,
		           maximumDrawHeight, useBuffer, properties,
		           copy, save, print, zoom,
		           tooltips);
	}
	
	@Override
	public void paintImmediately(int x,int y,int w, int h) {
		super.paintImmediately(x, y, w, h);
	}
}
