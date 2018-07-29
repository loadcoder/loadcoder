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
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;

import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.common.YCalculator;
import com.loadcoder.load.chart.menu.DataSetUserType;
import com.loadcoder.load.chart.menu.SteppingSlider;
import com.loadcoder.load.chart.utilities.ChartUtils;
import com.loadcoder.result.Result;

public class ResultChart extends Chart {

	private static final boolean defaultPointsMode = false;

	final ResultChartLogic logic;

	Result[] results;

	final CommonSeries[] commonSeries;

	public ResultChart(Map<Comparable, Color> customizedColors, CommonSeries[] commonSeries, Result... results) {
		super(true, false);
		logic = new ResultChartLogic(chartFrame.getSeriesCollection(), chartFrame.getPlot(), chartFrame.getRenderer(),
				chartFrame.getSeriesVisible(), defaultPointsMode, commonSeries, customizedColors, false, results);

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

	protected void recreateDottedPoints() {
		logic.setFilteredData(null);
		logic.setDottedSeries(null);
		logic.clearChart();
		logic.createCommons();
		logic.addAllCommonSeriesToTheChart();
		logic.createHashesAndUpdate(false);
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

		JRadioButtonMenuItem points = new JRadioButtonMenuItem("Points", defaultPointsMode);
		JRadioButtonMenuItem lines = new JRadioButtonMenuItem("Samples", !defaultPointsMode);
		ButtonGroup graphTypeGroup = new ButtonGroup();
		graphTypeGroup.add(points);
		graphTypeGroup.add(lines);
		graphType.add(points);
		graphType.add(lines);
		resultMenu.add(graphType);

		points.addActionListener((a) -> {
			ajustDottedMode(true);
		});
		lines.addActionListener((a) -> {
			ajustDottedMode(false);
		});
		JMenu sampleMethod = new JMenu("Sample method");

		JMenu sampleLengthMenu = new JMenu("Sample length");

		SteppingSlider slider = createSlider(initialSampleLength, logic.getMinorTickLength(), defaultIndex);
		slider.addChangeListener((e) -> {
			JSlider source = (JSlider) e.getSource();
			if (!source.getValueIsAdjusting()) {
				int indexOfSlider = (int) source.getValue();

				long newSampleLength = logic.calculateSampleLengthWith(indexOfSlider);
				chartSliderAjustment(newSampleLength);
			}

		});

		sampleLengthMenu.add(slider);
		sampling.add(sampleMethod);
		resultMenu.add(sampling);
		sampling.add(sampleLengthMenu);

		ButtonGroup group = new ButtonGroup();
		List<YCalculator> yCalculators = logic.getyCalculators();
		for (YCalculator calc : yCalculators) {

			JRadioButtonMenuItem sampleMethodMenuIten = new JRadioButtonMenuItem(calc.getName());
			group.add(sampleMethodMenuIten);

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

	protected static SteppingSlider createSlider(long initialSampleLength, int minorTickPacing, int defaultIndex) {

		Dictionary<Integer, Component> labelTable = new Hashtable<Integer, Component>();
		labelTable.put(1, new JLabel("1"));

		int max = ChartUtils.calculateSampleLengthSliderMax(initialSampleLength);
		max = ((int) initialSampleLength / 1000) + minorTickPacing * 4;
		List<Integer> valuesList = new ArrayList<Integer>();
		valuesList.add(1);
		for (int i = minorTickPacing; i <= max; i = i + minorTickPacing) {
			if (!valuesList.contains(i))
				valuesList.add(i);
		}

		for (int i = 0; i < valuesList.size(); i++) {
			labelTable.put(i, new JLabel("" + valuesList.get(i)));
		}

		Integer[] values = valuesList.toArray(new Integer[valuesList.size()]);

		SteppingSlider slider = new SteppingSlider(values, defaultIndex);
		slider.setLabelTable(labelTable);

		return slider;
	}

	void ajustDottedMode(boolean dottedMode) {
		logic.useDottedModeValue(dottedMode);
	}

	void chartSliderAjustment(long newSampleLength) {
		long sampleLengthToUse = newSampleLength;
		logic.setSampleLengthToUse(sampleLengthToUse);
		logic.createHashesAndUpdate(true);
	}
}
