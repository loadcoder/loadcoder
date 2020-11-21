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
package com.loadcoder.load.chart.menu.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.data.xy.XYSeries;

import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;
import com.loadcoder.load.chart.menu.MouseClickedListener;

public class ColorSettings extends Settings {

	JColorChooser colorChooser;

	Component clickedButton;

	private XYSeriesExtension chosenSeries;

	private Map<XYSeriesExtension, Color> selections = new HashMap<XYSeriesExtension, Color>();

	private final ChartLogic chartLogic;

	private boolean colorChooserVisible = false;

	private Map<Component, XYSeriesExtension> selectionMap = new HashMap<Component, XYSeriesExtension>();

	private List<XYSeries> list;

	public ColorSettings(ChartLogic chartLogic, List<XYSeries> list) {
		super("Color");
		this.chartLogic = chartLogic;
		this.list = list;
	}

	public void setChosenSeries(XYSeriesExtension chosenSeries) {
		this.chosenSeries = chosenSeries;
	}

	public void setColorChooserVisible(boolean colorChooserVisible) {
		this.colorChooserVisible = colorChooserVisible;
	}

	public void changeSeriesColorSelection(Color chosenColor) {
		selections.put(chosenSeries, chosenColor);
	}

	public Color getSeriesColorSelection() {
		return selections.get(chosenSeries);
	}

	public boolean isColorChooserVisible() {
		return colorChooserVisible;
	}

	public void addColorChooser(JComponent parent) {

		// Set up color chooser for setting text color
		colorChooser = new JColorChooser(Color.GREEN);
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Color chosenColor = colorChooser.getColor();
				clickedButton.setBackground(chosenColor);
				changeSeriesColorSelection(chosenColor);
			}
		});

		colorChooser.setBorder(BorderFactory.createTitledBorder("Choose new color"));

		AbstractColorChooserPanel[] ccPanels = colorChooser.getChooserPanels();
		for (AbstractColorChooserPanel ccPanel : ccPanels) {
			ccPanel.getClass().getSimpleName();
			if (!ccPanel.getDisplayName().equals("RGB"))
				colorChooser.removeChooserPanel(ccPanel);
		}
		parent.add(colorChooser);
		colorChooser.setVisible(isColorChooserVisible());

	}

	public void addSeriesOptions(List<XYSeries> list, JPanel panelToAddOptionsTo) {
		GridBagConstraints c = new GridBagConstraints();
		for (int i = 0; i < list.size(); i++) {
			XYSeriesExtension series = (XYSeriesExtension) list.get(i);

			JButton button = new JButton("");
			selectionMap.put(button, series);
//			Color seriesColor = (Color) series.getColorInTheChart();
			Color seriesColor = chartLogic.getExistingColors().get(series.getKey());
			button.setBackground(seriesColor);
			button.setPreferredSize(new Dimension(25, 25));

			button.addMouseListener(new MouseClickedListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					clickedButton = e.getComponent();
					Color c = clickedButton.getBackground();

					setChosenSeries(selectionMap.get(clickedButton));
					setColorChooserVisible(true);

					if (isColorChooserVisible()) {
						colorChooser.setVisible(isColorChooserVisible());
					}
					colorChooser.setColor(c);
				}
			});
			JLabel label = new JLabel("" + series.getKey());

			c.gridx = 0;
			c.gridy = i;

			panelToAddOptionsTo.add(button, c);
			c.gridx = 1;
			c.gridy = i;

			panelToAddOptionsTo.add(label, c);

		}

	}

	public void apply(ChartSettingsActionsModel chartSettingsActionsModel) {
		XYPlotExtension plot = chartLogic.getPlot();
		// boolean renderAtEnd = true;
		synchronized (plot) {
			Iterator<Entry<XYSeriesExtension, Color>> i = selections.entrySet().iterator();
			while (i.hasNext()) {
				chartSettingsActionsModel.setRender(true);
				Entry<XYSeriesExtension, Color> entry = i.next();
				Color newColor = entry.getValue();
				XYSeriesExtension series = entry.getKey();
				Color oldColor = chartLogic.getExistingColors().get(series.getKey());
//				series.setColorInTheChart(newColor);
				series.getLegend().setFillPaint(newColor);
				series.getLegend().setOutlinePaint(newColor);

				Map<String, Color> existingColors = chartLogic.getExistingColors();
//				existingColors.remove(oldColor);
//				existingColors.add(newColor);
				existingColors.put(series.getKey(), newColor);
			}

		}
	}

	@Override
	JPanel getWindow() {
		JPanel colorChooserPanel = new JPanel(new BorderLayout());
		JPanel jp1 = new JPanel(new GridBagLayout());
		colorChooserPanel.add(jp1, BorderLayout.WEST);

		addSeriesOptions(list, jp1);
		addColorChooser(colorChooserPanel);

		return colorChooserPanel;
	}
}
