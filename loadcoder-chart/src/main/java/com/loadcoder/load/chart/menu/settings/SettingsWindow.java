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
package com.loadcoder.load.chart.menu.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.data.xy.XYSeries;

import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;
import com.loadcoder.load.chart.logic.ResultChartLogic;
import com.loadcoder.load.chart.menu.DoubleSteppingSlider;
import com.loadcoder.load.chart.menu.MouseClickedListener;

public class SettingsWindow extends JDialog {

	private static final long serialVersionUID = 1L;

	JColorChooser colorChooser;
	Component clickedButton;
	private Map<Component, XYSeriesExtension> selectionMap = new HashMap<Component, XYSeriesExtension>();

	final SettingsLogic settingsLogic;

	JPanel bottomButtons = new JPanel();

	public class ColorSelectionSetting {
		public Component button;
		public XYSeriesExtension series;

		public ColorSelectionSetting(Component button, XYSeriesExtension series) {
			this.button = button;
			this.series = series;
		}
	}

	public SettingsWindow(ChartFrame parent, String title, ChartLogic chartLogic) {
		super(parent, title, true);
		setPreferredSize(new Dimension(1000, 500));
		settingsLogic = new SettingsLogic(chartLogic);

		JPanel base = new JPanel(new BorderLayout());
		getContentPane().add(base);

		JTabbedPane jtp = new JTabbedPane(JTabbedPane.LEFT);
		base.add(jtp);

		JPanel bottomButtonsBase = new JPanel();

		bottomButtonsBase.add(bottomButtons, BorderLayout.EAST);

		base.add(bottomButtonsBase, BorderLayout.SOUTH);

		List<XYSeries> list = parent.getSeriesCollection().getSeries();

		setTitle("Settings");

		JPanel colorsLeftArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jtp.addTab("Colors", colorsLeftArea);

		JPanel detailsLeftArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jtp.addTab("Details", detailsLeftArea);
		
		GridBagConstraints c = new GridBagConstraints();
		JPanel jp2 = new JPanel(new GridBagLayout());
		colorsLeftArea.add(jp2, BorderLayout.WEST);
		
		JLabel keepFactorSliderDescription = new JLabel("Drag the slider in order to change how many percent of the points that is going to be rendered");
		Font f = keepFactorSliderDescription.getFont();
		keepFactorSliderDescription.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
		
		keepFactorSliderDescription.setBackground(Color.LIGHT_GRAY);
		keepFactorSliderDescription.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		jp2.add(keepFactorSliderDescription, c);
		
		detailsLeftArea.add(jp2);
		
		Dictionary<Integer, Component> labelTable = new Hashtable<Integer, Component>();

		List<Double> keepFactorValues = new ArrayList<Double>();
		for (double i = 0.0001; i < 1; i = i * 10) {
			double roundMultiplicator = 1 / i;
			for (long j = 1; j < 10; j++) {
				long l = Math.round((double) j); // this is need for rounding issues with doubles
				double rounded = (double) l / roundMultiplicator;
				keepFactorValues.add(rounded);
			}
		}
		keepFactorValues.add(1D);
		labelTable.put(0, new JLabel("" + (keepFactorValues.get(0) * 100) + "%"));
		labelTable.put(keepFactorValues.size() - 1, new JLabel("100%"));


		
		JTextArea textField = new JTextArea();
		Double[] doubles = keepFactorValues.toArray(new Double[keepFactorValues.size()]);

		int startIndex = 0;
		double keepFactor;
		if (chartLogic instanceof ResultChartLogic) {
			ResultChartLogic resultChartLogic = (ResultChartLogic) chartLogic;

			keepFactor = resultChartLogic.getCurrentKeepFactor();
			startIndex = DoubleSteppingSlider.getIndexOf(doubles, keepFactor);

			textField.setText(SettingsLogic.keepFactorToProcentString(keepFactor));
		}
		DoubleSteppingSlider pointsKeepFactorSlider = new DoubleSteppingSlider(doubles, startIndex);
		pointsKeepFactorSlider.setLabelTable(labelTable);

		ChangeListener listener = new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				// update text field when the slider value changes
				DoubleSteppingSlider source = (DoubleSteppingSlider) event.getSource();
				int index = source.getValue();
				double value = source.getValues()[index];

				String valueAsProcent = SettingsLogic.keepFactorToProcentString(value);
				textField.setText(valueAsProcent);

				settingsLogic.setKeepFactorSelection(value);
			}
		};
		pointsKeepFactorSlider.addChangeListener(listener);

		c.gridx = 0;
		c.gridy = 1;
		jp2.add(pointsKeepFactorSlider, c);
		c.gridx = 1;
		c.gridy = 1;
		jp2.add(textField, c);

		
		JPanel jp1 = new JPanel(new GridBagLayout());
		colorsLeftArea.add(jp1, BorderLayout.NORTH);

		JPanel colorChooserPanel = new JPanel(new BorderLayout());
		colorsLeftArea.add(colorChooserPanel, BorderLayout.NORTH);

		addSeriesOptions(list, jp1);
		addColorChooser(colorChooserPanel);
		addBottomButtons(bottomButtons);

		pack();
		setVisible(true);

	}

	void addBottomButtons(JPanel parent) {
		JButton applyButton = new JButton("Apply");
		applyButton.addMouseListener(new MouseClickedListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				settingsLogic.applyColorSelections();
			}
		});

		JButton okButton = new JButton("Ok");
		okButton.addMouseListener(new MouseClickedListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				settingsLogic.applyColorSelections();
				dispose();
			}
		});

		JButton closeButton = new JButton("Close");
		closeButton.addMouseListener(new MouseClickedListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();
			}
		});

		parent.add(okButton);
		parent.add(applyButton);
		parent.add(closeButton);
	}

	public void addSeriesOptions(List<XYSeries> list, JPanel panelToAddOptionsTo) {
		GridBagConstraints c = new GridBagConstraints();
		for (int i = 0; i < list.size(); i++) {
			XYSeriesExtension series = (XYSeriesExtension) list.get(i);

			JButton button = new JButton("");
			selectionMap.put(button, series);

			Color seriesColor = (Color) series.getColorInTheChart();
			button.setBackground(seriesColor);
			button.setPreferredSize(new Dimension(25, 25));

			button.addMouseListener(new MouseClickedListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					clickedButton = e.getComponent();
					Color c = clickedButton.getBackground();

					settingsLogic.setChosenSeries(selectionMap.get(clickedButton));
					settingsLogic.setColorChooserVisible(true);

					if (settingsLogic.isColorChooserVisible()) {
						colorChooser.setVisible(settingsLogic.isColorChooserVisible());
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

	public void addColorChooser(JComponent parent) {

		// Set up color chooser for setting text color
		colorChooser = new JColorChooser(Color.GREEN);
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Color chosenColor = colorChooser.getColor();
				clickedButton.setBackground(chosenColor);
				settingsLogic.changeSeriesColorSelection(chosenColor);
			}
		});

		colorChooser.setBorder(BorderFactory.createTitledBorder("Choose new color"));

		AbstractColorChooserPanel[] ccPanels = colorChooser.getChooserPanels();
		for (AbstractColorChooserPanel ccPanel : ccPanels) {
			String name = ccPanel.getClass().getSimpleName();
			if (!ccPanel.getDisplayName().equals("RGB"))
				colorChooser.removeChooserPanel(ccPanel);
		}
		parent.add(colorChooser);
		colorChooser.setVisible(settingsLogic.isColorChooserVisible());
	}

}