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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XYPlotExtension extends XYPlot {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	XYDataset dataset;

	private final SimpleDateFormat monthDayDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");

	public XYPlotExtension(XYDataset dataset, ValueAxis domainAxis, ValueAxis rangeAxis, XYItemRenderer renderer) {
		super(dataset, domainAxis, rangeAxis, renderer);
		this.dataset = dataset;
	}

	public long getXRange() {
		ValueAxis xAxis = getDomainAxisForDataset(0);
		double diff = xAxis.getUpperBound() - xAxis.getLowerBound();
		return (long) diff;
	}

	public void changeToMonthAndDayDateAxisFormat() {
		((DateAxis) getDomainAxis()).setDateFormatOverride(monthDayDateFormat);
	}

	@Override
	public boolean render(Graphics2D g2, Rectangle2D dataArea, int index, PlotRenderingInfo info,
			CrosshairState crosshairState) {

		boolean result = false;
		synchronized (this) {
			long start = System.currentTimeMillis();
			result = super.render(g2, dataArea, index, info, crosshairState);
			logger.info("Render time: {} ms", System.currentTimeMillis() - start);
		}
		return result;

	}

	/*
	 * This is invoked when zooming out in the chart using right button mouse click
	 */
	@Override
	public void zoomDomainAxes(double factor, PlotRenderingInfo info, Point2D source) {
		synchronized (this) {
			super.zoomDomainAxes(factor, info, source);
		}
	}

	XYItemRenderer renderer = getRenderer(0);
	LegendItemCollection legends = new LegendItemCollection();

	public LegendItemCollection getLegends() {
		return legends;
	}

	/*
	 * Overriding this method since the legends dissapears if clicking on them
	 * making them in order to make the series invisible.
	 */
	public LegendItemCollection getLegendItems() {
		return getLegends();
	}
}
