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

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;
import org.jfree.ui.ApplicationFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.chart.data.DataSet;
import com.loadcoder.load.chart.logic.ChartLogic;

public class ChartFrame extends ApplicationFrame {

	private static final long serialVersionUID = 1L;

	public static Logger log = LoggerFactory.getLogger(ChartFrame.class);

	private XYPlotExtension plot;

	private List<DataSetUser> dataSetUsers = new ArrayList<DataSetUser>();

	private final ChartLogic logic;

	public XYPlotExtension getPlot() {
		return plot;
	}

	public interface DataSetUser {
		void useDataSet(List<DataSet> dataSets);
	}

	public ChartFrame use(DataSetUser dataSetUser) {
		dataSetUsers.add(dataSetUser);
		return this;
	}

	public ChartFrame(boolean linesVisible, boolean shapesVisible, Map<String, Color> existingColors,
			ChartLogic logic) {
		super("");
		this.logic = logic;
		plot = logic.getPlot();
		Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/polarbear.png"));
		image.getScaledInstance(200, 200, Image.SCALE_FAST);
		setIconImage(image);

		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					logic.getChartPanel().doCopy();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
	}

	XYDataItem xgetDataItem(XYSeriesExtension series, long x) {
		int index = series.indexOf(x);

		XYDataItem existing = (XYDataItem) series.getItems().get(index);
		return existing;
	}

	public void handleClick(int button, Object clickedObject, XYSeriesCollectionExtention serieses) {
		logic.handleClick(button, clickedObject, serieses);
	}

	public void copy() {
		logic.getChartPanel().doCopy();
	}

	public void waitUntilClosed() {
		while (isDisplayable())
			LoadUtility.sleep(1000);
	}
}
