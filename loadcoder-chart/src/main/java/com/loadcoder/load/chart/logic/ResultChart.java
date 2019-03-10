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

	final ResultChartLogic logic;

	Result[] results;

	final CommonSeries[] commonSeries;

	private final boolean locked = false;

	public ResultChart(Map<String, Color> customizedColors, CommonSeries[] commonSeries, Result... results) {
		super(true, false);
		logic = new ResultChartLogic(chartFrame.getSeriesCollection(), chartFrame.getPlot(), chartFrame.getRenderer(),
				chartFrame.getSeriesVisible(), defaultPointsMode, commonSeries, customizedColors, locked, existingColors, results);

		long sampleLengthToUse = logic.getSampleLengthToUse();
		int defaultIndex = logic.getDefaultSliderIndex();

		JMenu resultMenu = addResultMenu(sampleLengthToUse, defaultIndex);
		JMenu settingsMenu = createSettingsMenu(logic);
		JMenu aboutMenu = createAboutMenu();

		chartFrame.getMenu().add(resultMenu);
		chartFrame.getMenu().add(settingsMenu);
		chartFrame.getMenu().add(aboutMenu);

		chartFrame.setVisible(true);

		this.results = results;
		this.commonSeries = commonSeries;
	}

	public ResultChart(CommonSeries[] commonSeries, Result... results) {
		this(null, commonSeries, results);
	}

	public ResultChart(Result... results) {
		this(CommonSeries.values(), results);
		this.results = results;
	}

	protected ChartLogic getLogic() {
		return logic;
	}

	private void toggleRemoveFilterCheckBox(DataSetUserType dataSetUserType, boolean selected) {
		logic.setFilteredData(null);
		logic.setDottedSeries(null);
		logic.clearChart();
		logic.createCommons();
		logic.addAllCommonSeriesToTheChart();
		if (selected) {
			logic.getRemovalFiltersInUse().add(dataSetUserType);
		} else {// checkbox has been deselected
			logic.getRemovalFiltersInUse().remove(dataSetUserType);
		}
		logic.createHashesAndUpdate(false);
	}

	private JMenu addResultMenu(long initialSampleLength, int defaultIndex) {

		JMenu resultMenu = new JMenu("Result");

		JMenu removalFiltersMenu = new JMenu("Removal filters");
		resultMenu.add(removalFiltersMenu);
		for (DataSetUserType dataSetUserType : logic.getRemovalFilters()) {
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
		double keepFactor = logic.getCurrentKeepFactor();
		String pointsRadioButtonText = "Points";
		if (keepFactor != 1.0) {
			pointsRadioButtonText = String.format("Points (%s)", DetailsSettings.keepFactorToProcentString(keepFactor));
		}
		logic.setPointsRadioButton(new JRadioButtonMenuItem(pointsRadioButtonText, defaultPointsMode));

		JRadioButtonMenuItem lines = new JRadioButtonMenuItem("Samples", !defaultPointsMode);
		ButtonGroup graphTypeGroup = new ButtonGroup();
		graphTypeGroup.add(logic.getPointsRadioButton());
		graphTypeGroup.add(lines);
		graphType.add(logic.getPointsRadioButton());
		graphType.add(lines);
		resultMenu.add(graphType);

		logic.getPointsRadioButton().addActionListener((a) -> {
			ajustDottedMode(true);
		});
		lines.addActionListener((a) -> {
			ajustDottedMode(false);
		});
		JMenu sampleMethod = new JMenu("Sample method");

		sampling.add(sampleMethod);
		resultMenu.add(sampling);

		List<YCalculator> yCalculators = logic.getyCalculators();
		for (YCalculator calc : yCalculators) {

			boolean selected = calc.equals(logic.getYCalculatorToUse());
			JRadioButtonMenuItem sampleMethodMenuIten = new JRadioButtonMenuItem(calc.getName(), selected);

			sampleMethod.add(sampleMethodMenuIten);

			sampleMethodMenuIten.addActionListener((a) -> {
				logic.clearChart();
				logic.addAllCommonSeriesToTheChart();
				logic.yCalculatorToUse = calc;
				logic.createHashesAndUpdate(false);
			});
		}
		return resultMenu;
	}

	void ajustDottedMode(boolean dottedMode) {
		logic.useDottedModeValue(dottedMode);
	}

}
