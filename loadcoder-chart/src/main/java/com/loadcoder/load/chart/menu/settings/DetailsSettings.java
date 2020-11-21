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

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.loadcoder.load.chart.logic.ResultChartLogic;
import com.loadcoder.load.chart.menu.DoubleSteppingSlider;

public class DetailsSettings extends Settings {

	private double keepFactorSelection = -1;

	JRadioButtonMenuItem points;

	private final ResultChartLogic chartLogic;

	Dictionary<Integer, Component> labelTable = new Hashtable<Integer, Component>();

	public DetailsSettings(ResultChartLogic chartLogic) {
		super("Details");
		this.chartLogic = chartLogic;
	}

	public static String keepFactorToProcentString(double value) {
		long multiRounding = Math.round(value * 10000);
		double rounded = (double) multiRounding / 100;
		if (rounded >= 1)
			return "" + ((long) rounded) + "%";
		return "" + (rounded) + "%";
	}

	static final private Double[] doubles = generateKeepFactorValues();

	protected static Double[] generateKeepFactorValues() {

		List<Double> values = new ArrayList<Double>();
		for (double i = 0.0001; i < 1; i = i * 10) {
			double roundMultiplicator = 1 / i;
			for (long j = 1; j < 10; j++) {
				long l = Math.round((double) j); // this is need for rounding issues with doubles
				double rounded = (double) l / roundMultiplicator;
				values.add(rounded);
			}
		}
		values.add(1D);

		Double[] doubleArray = values.toArray(new Double[values.size()]);
		return doubleArray;
	}

	@Override
	public void apply(ChartSettingsActionsModel chartSettingsActionsModel) {

		if (getKeepFactorSelection() != -1) {
			ResultChartLogic resultChartLogic = (ResultChartLogic) chartLogic;

			resultChartLogic.setKeepFactorChosen(keepFactorSelection);
			String keepFactorAsProcentString = keepFactorToProcentString(keepFactorSelection);
			resultChartLogic.getPointsRadioButton().setText(String.format("Points (%s)", keepFactorAsProcentString));
			chartSettingsActionsModel.setRecreatePoints(true);
		}

		if (chartLogic.getNewSampleLengthSelection() != -1) {
			chartLogic.chartSliderAjustment(chartLogic.getNewSampleLengthSelection());
		}
	}

	public double getKeepFactorSelection() {
		return keepFactorSelection;
	}

	public void setKeepFactorSelection(double keepFactorSelection) {
		this.keepFactorSelection = keepFactorSelection;
	}

	JPanel getWindow() {

		labelTable.put(0, new JLabel("" + (doubles[0] * 100) + "%"));
		labelTable.put(doubles.length - 1, new JLabel("100%"));

		JPanel detailsLeftArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
		GridBagConstraints c = new GridBagConstraints();
		JPanel jp2 = new JPanel(new GridBagLayout());

		JTextArea textField = new JTextArea();

		int startIndex = 0;
		double keepFactor;
		if (chartLogic instanceof ResultChartLogic) {
			ResultChartLogic resultChartLogic = (ResultChartLogic) chartLogic;

			keepFactor = resultChartLogic.getCurrentKeepFactor();
			startIndex = DoubleSteppingSlider.getIndexOf(doubles, keepFactor);

			textField.setText(keepFactorToProcentString(keepFactor));
		}
		DoubleSteppingSlider pointsKeepFactorSlider = new DoubleSteppingSlider(doubles, startIndex);
		pointsKeepFactorSlider.setLabelTable(labelTable);

		ChangeListener listener = new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				// update text field when the slider value changes
				DoubleSteppingSlider source = (DoubleSteppingSlider) event.getSource();
				int index = source.getValue();
				double value = source.getValues()[index];

				String valueAsProcent = keepFactorToProcentString(value);
				textField.setText(valueAsProcent);

				setKeepFactorSelection(value);
			}
		};
		pointsKeepFactorSlider.addChangeListener(listener);

		JLabel keepFactorSliderDescription = new JLabel(
				"Drag the slider in order to change how many percent of the points that is going to be rendered");
		Font f = keepFactorSliderDescription.getFont();
		keepFactorSliderDescription.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		jp2.add(keepFactorSliderDescription, c);

		c.gridx = 0;
		c.gridy = 1;
		jp2.add(pointsKeepFactorSlider, c);
		c.gridx = 1;
		c.gridy = 1;
		jp2.add(textField, c);

		JLabel space = new JLabel(" ");
		c.gridx = 0;
		c.gridy = 2;
		jp2.add(space, c);

		JLabel keepFactorSliderDescription2 = new JLabel("Drag the slider to adjust the sample length in seconds");
		f = keepFactorSliderDescription2.getFont();
		keepFactorSliderDescription2.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
		c.gridx = 0;
		c.gridy = 3;
		jp2.add(keepFactorSliderDescription2, c);

		c.gridx = 0;
		c.gridy = 4;
		jp2.add(chartLogic.getSteppingSlider(), c);
		keepFactorSliderDescription.setBackground(Color.LIGHT_GRAY);
		keepFactorSliderDescription.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

		detailsLeftArea.add(jp2);

		return detailsLeftArea;
	}

}
