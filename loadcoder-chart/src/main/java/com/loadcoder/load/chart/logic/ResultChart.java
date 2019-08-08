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

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;

import javax.crypto.ExemptionMechanismSpi;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.common.YCalculator;
import com.loadcoder.load.chart.menu.DataSetUserType;
import com.loadcoder.load.chart.menu.settings.DetailsSettings;
import com.loadcoder.result.Result;

public class ResultChart extends Chart {

	private static final boolean defaultPointsMode = false;

	final ResultChartLogic resultChartLogic;

	Result[] results;

	public ResultChart(CommonSeries[] commonSeries, Result... results) {
		super(true, false, new ResultChartLogic(false, commonSeries, false, results));

		resultChartLogic = (ResultChartLogic) this.logic;
		long sampleLengthToUse = logic.getSampleLengthToUse();

		JMenu resultMenu = addResultMenu(sampleLengthToUse);
		JMenu settingsMenu = createSettingsMenu(logic);
		JMenu aboutMenu = createAboutMenu();

		chartFrame.setContentPane(resultChartLogic.panelForButtons);
		chartFrame.pack();
		chartFrame.setJMenuBar(resultChartLogic.getMenu());

		resultChartLogic.getMenu().add(resultMenu);
		resultChartLogic.getMenu().add(settingsMenu);
		resultChartLogic.getMenu().add(aboutMenu);

		chartFrame.setVisible(true);

		this.results = results;
	}

	public ResultChart(Result... results) {
		this(CommonSeries.values(), results);
		this.results = results;
	}

	protected ChartLogic getLogic() {
		return logic;
	}

	private void toggleRemoveFilterCheckBox(DataSetUserType dataSetUserType, boolean selected) {
		resultChartLogic.setFilteredData(null);
		resultChartLogic.setDottedSeries(null);
		resultChartLogic.clearChart();
		logic.createCommons();
		logic.addAllCommonSeriesToTheChart();
		if (selected) {
			logic.getRemovalFiltersInUse().add(dataSetUserType);
		} else {// checkbox has been deselected
			logic.getRemovalFiltersInUse().remove(dataSetUserType);
		}
		resultChartLogic.createHashesAndUpdate(false);
	}

	private JMenu addResultMenu(long initialSampleLength) {

		JMenu resultMenu = new JMenu("Result");

		JMenu removalFiltersMenu = new JMenu("Removal filters");
		resultMenu.add(removalFiltersMenu);
		for (DataSetUserType dataSetUserType : resultChartLogic.getRemovalFilters()) {
			JCheckBox checkbox = new JCheckBox(dataSetUserType.getName());

			checkbox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					boolean selected = e.getStateChange() == ItemEvent.SELECTED;
					toggleRemoveFilterCheckBox(dataSetUserType, selected);
				}

			});

			removalFiltersMenu.add(checkbox);
		}
		JMenu sampling = new JMenu("Sampling");

		JMenu graphType = new JMenu("Graph type");
		double keepFactor = resultChartLogic.getCurrentKeepFactor();
		String pointsRadioButtonText = "Points";
		if (keepFactor != 1.0) {
			pointsRadioButtonText = String.format("Points (%s)", DetailsSettings.keepFactorToProcentString(keepFactor));
		}

		JRadioButtonMenuItem pointsRadioButton = new JRadioButtonMenuItem(pointsRadioButtonText, defaultPointsMode);
		resultChartLogic.setPointsRadioButton(pointsRadioButton);

		JRadioButtonMenuItem samplesRadioButton = new JRadioButtonMenuItem("Samples", !defaultPointsMode);
		ButtonGroup graphTypeRadioButtonGroupForOneExclusiveSelection = new ButtonGroup();
		graphTypeRadioButtonGroupForOneExclusiveSelection.add(pointsRadioButton);
		graphTypeRadioButtonGroupForOneExclusiveSelection.add(samplesRadioButton);
		graphType.add(resultChartLogic.getPointsRadioButton());
		graphType.add(samplesRadioButton);
		resultMenu.add(graphType);

		resultChartLogic.getPointsRadioButton().addActionListener((a) -> {
			ajustDottedMode(true);
		});
		samplesRadioButton.addActionListener((a) -> {
			ajustDottedMode(false);
		});
		JMenu sampleMethod = new JMenu("Sample method");

		sampling.add(sampleMethod);
		resultMenu.add(sampling);
		ButtonGroup sampleMethodradioButtonGroupForOneExclusiveSelection = new ButtonGroup();
		List<YCalculator> yCalculators = logic.getyCalculators();
		for (YCalculator calc : yCalculators) {

			boolean selected = calc.equals(logic.getYCalculatorToUse());
			JRadioButtonMenuItem sampleMethodMenuIten = new JRadioButtonMenuItem(calc.getName(), selected);
			sampleMethodradioButtonGroupForOneExclusiveSelection.add(sampleMethodMenuIten);
			sampleMethod.add(sampleMethodMenuIten);

			sampleMethodMenuIten.addActionListener((a) -> {
				resultChartLogic.clearChart();
				logic.addAllCommonSeriesToTheChart();
				logic.yCalculatorToUse = calc;
				resultChartLogic.createHashesAndUpdate(false);
			});
		}
		return resultMenu;
	}

	void ajustDottedMode(boolean dottedMode) {
		resultChartLogic.useDottedModeValue(dottedMode);
	}

}
