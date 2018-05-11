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
package com.loadcoder.load.chart.logic;

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ShapeUtilities;

import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.ChartFrame.DataSetUser;
import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.jfreechartfixes.XYLineAndShapeRendererExtention;

public abstract class Chart {

	ChartFrame chartFrame;
	
	//only for test purposes, in order to create a chart, without the actual ChartFrame
	protected Chart() {}
	
	public Chart(boolean linesVisible, boolean shapesVisible) {
		
		/*
		 * This is done to inactivate automatic rerendering of the chart when resizing it.
		 * Without this, the whole chart will be rerendered when the size changes just one pixel
		 */
		Toolkit.getDefaultToolkit().setDynamicLayout(false);
		
		this.chartFrame = new ChartFrame(linesVisible, shapesVisible);
		
		Stroke[] strokes = new Stroke[] { new BasicStroke() };
		
		//this is done in order to set the size of the dots shown in dotted mode.
		DrawingSupplier newSup = new DefaultDrawingSupplier(null, new Paint[] { }, strokes,
				strokes, new Shape[] { ShapeUtilities.createDiamond(3) });
		chartFrame.getPlot().setDrawingSupplier(newSup);
	}
	
	public void waitUntilClosed() {
		chartFrame.waitUntilClosed();
	}

	public Chart use(DataSetUser dataSetUser) {
		chartFrame.use(dataSetUser);
		return this;
	}
	
	public XYSeriesCollectionExtention getSeriesCollection(){
		return chartFrame.getSeriesCollection();
	}
	
	public XYPlotExtension getPlot(){
		return chartFrame.getPlot();
	}
	
	public XYLineAndShapeRendererExtention getRenderer(){
		return chartFrame.getRenderer();
	}

	public static XYPlotExtension createXYPlotExtension(String yAxisLabel, String xAxisLabel, XYDataset dataset, XYLineAndShapeRendererExtention renderer) {
		NumberAxis yAxis = new NumberAxis(yAxisLabel);
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setAutoRangeIncludesZero(false);
		XYPlotExtension plot = new XYPlotExtension(dataset, xAxis, yAxis, renderer);
		return plot;
	}
}
