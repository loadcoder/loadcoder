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
package com.loadcoder.load.chart.logic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;

import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.util.ParamChecks;

import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.ChartFrame.DataSetUser;
import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.chart.menu.AboutPopup;
import com.loadcoder.load.chart.menu.MouseClickedListener;
import com.loadcoder.load.chart.menu.settings.SettingsWindow;
import com.loadcoder.load.jfreechartfixes.XYLineAndShapeRendererExtention;

public abstract class Chart {

	ChartFrame chartFrame;

	protected final Map<String, Color> existingColors = new HashMap<String, Color>();

	final ChartLogic logic;

	public Chart(boolean linesVisible, boolean shapesVisible, ChartLogic logic) {

		this.logic = logic;

		/*
		 * This is done to inactivate automatic rerendering of the chart when resizing
		 * it. Without this, the whole chart will be rerendered when the size changes
		 * just one pixel
		 */
		Toolkit.getDefaultToolkit().setDynamicLayout(false);

		this.chartFrame = new ChartFrame(linesVisible, shapesVisible, existingColors, logic);

		Stroke[] strokes = new Stroke[] { new BasicStroke() };

		// this is done in order to set the size of the dots shown in dotted mode.
		DrawingSupplier newSup = new DefaultDrawingSupplier(null, new Paint[] {}, strokes, strokes,
				new Shape[] { new Rectangle(new Dimension(8, 8)) });
		logic.getPlot().setDrawingSupplier(newSup);
	}

	/**
	 * Wait here until the chart is closed
	 */
	public void waitUntilClosed() {
		chartFrame.waitUntilClosed();
	}

	JMenu createAboutMenu() {

		JMenu about = new JMenu("About");
		about.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				AboutPopup.showAboutPopup(chartFrame);
			}
		});

		return about;
	}

	JMenu createSettingsMenu(ChartLogic logic) {
		JMenu settings = new JMenu("Settings");
		settings.addMouseListener(new MouseClickedListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				new SettingsWindow(chartFrame, "Settings", logic);
			}
		});
		return settings;
	}

	protected Chart use(DataSetUser dataSetUser) {
		chartFrame.use(dataSetUser);
		return this;
	}

	protected XYPlotExtension getPlot() {
		return chartFrame.getPlot();
	}
}
