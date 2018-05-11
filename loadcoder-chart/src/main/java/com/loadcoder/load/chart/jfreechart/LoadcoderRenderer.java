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

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.loadcoder.load.jfreechartfixes.XYLineAndShapeRendererExtention;

public class LoadcoderRenderer extends XYLineAndShapeRendererExtention {

	private static final long serialVersionUID = 1L;

	public LoadcoderRenderer(boolean lines, boolean shapes, XYSeriesCollection seriesCollection) {
		super(lines, shapes, seriesCollection);
	}
	
	/* 
	 * Visibility is redefined in loadcoder. A series can be invisible even though
	 * the legend is still visble (and clickable). This becomes important when visibility
	 * of a series is toggled (by clicking at corresponding legend)
	 */
	@Override
	public boolean isSeriesVisible(int series) {
		XYSeriesExtension serie = (XYSeriesExtension) seriesCollection.getSeries(series);
		return serie.isVisible();
	}
	
	/* 
	 * Overridden in order to get the same color for the same series name
	 */
	@Override
    public Paint getItemPaint(int row, int column) {
		return getLinePaint(row);
    }
    
	@Override
	public Paint getLinePaint(int seriesIndex){
		Paint result = null;
		XYSeries serie = seriesCollection.getSeries(seriesIndex);
		if(serie instanceof XYSeriesExtension){
			XYSeriesExtension seriesExtension = (XYSeriesExtension)serie;
			result = seriesExtension.getColorInTheChart();
		}
		if(result == null){
			result = lookupSeriesPaint(seriesIndex);
		}
		return result;
	}
}
