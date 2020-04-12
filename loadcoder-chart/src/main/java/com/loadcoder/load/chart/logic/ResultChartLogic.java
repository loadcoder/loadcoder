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

import static com.loadcoder.statics.Statics.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.chart.common.CommonSampleGroup;
import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.data.DataSet;
import com.loadcoder.load.chart.data.FilteredData;
import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.jfreechart.ChartFrame.DataSetUser;
import com.loadcoder.load.chart.jfreechart.XYDottedSeriesExtension;
import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.menu.DataSetUserType;
import com.loadcoder.load.chart.menu.SteppingSlider;
import com.loadcoder.load.chart.sampling.SampleGroup;
import com.loadcoder.load.chart.utilities.ChartUtils;
import com.loadcoder.load.chart.utilities.Utilities;
import com.loadcoder.load.jfreechartfixes.XYLineAndShapeRendererExtention;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class ResultChartLogic extends ChartLogic {

	private static Logger logger = LoggerFactory.getLogger(ResultChartLogic.class);

	private static final boolean ALLOW_DUPLICATE_X_VALUES_FOR_POINT_SERIES = true;

	private List<DataSetUserType> removalFilters = new ArrayList<DataSetUserType>();

	private boolean dottedMode;

	private final Map<String, List<TransactionExecutionResult>> originalFromFile;

	private Map<String, XYSeriesExtension> dottedSeries = null;

	private double keepFactorChosen = -1;

	private JRadioButtonMenuItem pointsRadioButton;

	private long newSampleLengthSelection = -1;

	private SteppingSlider sampleLengthSlider;

	public void setNewSampleLengthSelection(long newSampleLengthSelection) {
		this.newSampleLengthSelection = newSampleLengthSelection;
	}

	public long getNewSampleLengthSelection() {
		return newSampleLengthSelection;
	}

	public SteppingSlider getSteppingSlider() {
		return sampleLengthSlider;
	}

	protected static List<Integer> getValuesForSliderAsList(long currentSampleLength, int tickPacing) {
		int valueWith4TicksBiggerThanCurrentSampleLengthTickValue = ((int) currentSampleLength / 1000) + tickPacing * 4;
		List<Integer> valuesList = new ArrayList<Integer>();
		valuesList.add(1);
		for (int i = tickPacing; i <= valueWith4TicksBiggerThanCurrentSampleLengthTickValue; i = i + tickPacing) {
			if (!valuesList.contains(i))
				valuesList.add(i);
		}
		return valuesList;
	}

	protected SteppingSlider createSlider(long initialSampleLength, int minorTickPacing, int defaultIndex) {

		Dictionary<Integer, Component> labelTable = new Hashtable<Integer, Component>();

		List<Integer> valuesList = getValuesForSliderAsList(initialSampleLength, minorTickPacing);

		for (int i = 0; i < valuesList.size(); i++) {
			labelTable.put(i, new JLabel("" + valuesList.get(i)));
		}

		Integer[] values = valuesList.toArray(new Integer[valuesList.size()]);

		SteppingSlider slider = new SteppingSlider(values, defaultIndex);
		slider.setLabelTable(labelTable);

		slider.addChangeListener((e) -> {
			JSlider source = (JSlider) e.getSource();
			if (!source.getValueIsAdjusting()) {
				int indexOfSlider = (int) source.getValue();

				long newSampleLength = calculateSampleLengthWith(indexOfSlider);
				setNewSampleLengthSelection(newSampleLength);
			}
		});
		return slider;
	}

	public double getCurrentKeepFactor() {
		if (getKeepFactorChosen() != -1) {
			return getKeepFactorChosen();
		} else {
			return getKeepFactorDefault();
		}
	}

	public JRadioButtonMenuItem getPointsRadioButton() {
		return pointsRadioButton;
	}

	public void setPointsRadioButton(JRadioButtonMenuItem pointsRadioButton) {
		this.pointsRadioButton = pointsRadioButton;
	}

	public double getKeepFactorChosen() {
		return keepFactorChosen;
	}

	double keepFactorDefault = -1;

	public double getKeepFactorDefault() {
		return keepFactorDefault;
	}

	// slider
	final int minorTickLength;
	int defaultIndex;
	int sliderCompensation;

	public void setKeepFactorChosen(double keepFactorChosen) {
		this.keepFactorChosen = keepFactorChosen;
	}

	public List<DataSetUserType> getRemovalFilters() {
		return removalFilters;
	}

	public void setDottedSeries(Map<String, XYSeriesExtension> dottedSeries) {
		this.dottedSeries = dottedSeries;
	}

	public Map<String, XYSeriesExtension> getDottedSeries() {
		return dottedSeries;
	}

	public ResultChartLogic(boolean defaultDottedMode, CommonSeries[] commonSeries, boolean locked, Result... results) {
		super(commonSeries, locked);

		this.dottedMode = defaultDottedMode;
		populateRemovalFilters();

		originalFromFile = populateResultLists(results);
		originalFromFile.keySet().stream().forEach(key -> addSeriesKey(key));

		setFilteredData(generateDataSets(originalFromFile));

		int totalAmountOfPoints = 0;
		if (keepFactorDefault == -1) {
			for (DataSet set : getFilteredData().getDataSets()) {
				totalAmountOfPoints = totalAmountOfPoints + set.getPoints().size();
			}

			keepFactorDefault = ChartUtils.calculateKeepFactor(totalAmountOfPoints, TARGET_AMOUNT_OF_POINTS_DEFAULT);
		}

		long tickSize = calculateSliderTickSize(getFilteredData());
		minorTickLength = (int) tickSize;

		calculateSliderValueCompensation(minorTickLength);

		defaultIndex = 4;
		int minorTickLengthInAmountOfSeconds = getMinorTickLength();
		if (minorTickLengthInAmountOfSeconds <= 4) {
			defaultIndex = minorTickLengthInAmountOfSeconds - 1;
		}
		this.sampleLengthSlider = createSlider(getSampleLengthToUse(), getMinorTickLength(), getDefaultSliderIndex());

		long sampleLength = calculateSampleLengthWith(defaultIndex);
		setSampleLengthToUse(sampleLength);

		initiateChart();
		doSafeUpdate();
	}

	@Override
	protected void update(Map<String, List<TransactionExecutionResult>> listOfListOfList,
			HashSet<Long> hashesGettingUpdated) {

		seriesCollection.removeAllSeries();
		ranges.clearRanges();
		commonSeriesCalculators.clear();
		initCommonSeries();
		handleData(listOfListOfList, hashesGettingUpdated);
	}
	
	private void handleData(Map<String, List<TransactionExecutionResult>> listOfListOfList,
			HashSet<Long> hashesGettingUpdated) {
		earliestX = null;

		FilteredData filteredData = getFilteredData();
		if (filteredData == null) {
			filteredData = generateDataSets(listOfListOfList);
			setFilteredData(filteredData);
		}

		HashSet<Long> sampleTimestamps = new HashSet<Long>();

		Map<String, CommonSampleGroup> samplesCommonMap = new HashMap<String, CommonSampleGroup>();
		List<CommonSampleGroup> sampleGroupCommonList = new ArrayList<CommonSampleGroup>();

		Map<String, XYSeriesExtension> seriesMap = new HashMap<String, XYSeriesExtension>();
		if (dottedMode) {
			if (dottedSeries == null) {
				setDottedSeries(createDottedSeries(filteredData.getDataSets()));
			}
			seriesMap = dottedSeries;
		} else {
			getSerieses(filteredData.getDataSets(), seriesMap);
		}

		Map<String, SampleGroup> sampleGroups = new HashMap<String, SampleGroup>();
		createSamplesGroups(seriesMap, sampleGroups);

		addSerieseToChart(seriesMap);

		adjustVisibility(seriesMap);

		for (XYSeriesExtension commonSerie : getCommonSeriesMap().values()) {
			adjustVisibilityOfSeries(commonSerie);
		}

		addPoints(filteredData.getDataSets(), sampleGroups, sampleTimestamps);

		if (dottedMode) {

			if (dottedSeries == null) {
				createDottedSeries(filteredData.getDataSets());

			} else {
				populateChartWithSavedDottedSeries();
			}

		}

		double keepFactor;
		if (dottedMode) {

			if (keepFactorChosen == -1) {
				keepFactor = keepFactorDefault;
			} else {
				keepFactor = keepFactorChosen;
			}

			for (DataSet set : filteredData.getDataSets()) {
				SampleGroup group = sampleGroups.get(set.getName());
				XYSeriesExtension series = group.getSeries();

				ChartUtils.populateSeriesWithPoints(set.getPoints(), series, keepFactor);
			}
		}

		updateSeriesWithSamples(hashesGettingUpdated, filteredData.getDataSets(), sampleGroups, sampleTimestamps,
				dottedMode);
		updateCommonsWithSamples(hashesGettingUpdated, sampleGroups, samplesCommonMap, sampleGroupCommonList);

	}

	public long calculateSampleLengthWith(int indexOfSlider) {
		long newSampleLength = 1000;
		if (indexOfSlider != 0) {
			long valueOfSlider = indexOfSlider * minorTickLength + sliderCompensation;
			newSampleLength = valueOfSlider * 1000;
		}
		return newSampleLength;
	}

	public int getDefaultSliderIndex() {
		return defaultIndex;
	}

	public int getsliderCompensation() {
		return sliderCompensation;
	}

	void calculateSliderValueCompensation(int minorTickPacing) {
		int sliderCompensation = 0;
		if (minorTickPacing == 1)
			sliderCompensation = 1;

		this.sliderCompensation = sliderCompensation;
	}

	public void chartSliderAjustment(long newSampleLength) {
		long sampleLengthToUse = newSampleLength;
		setSampleLengthToUse(sampleLengthToUse);

		clearChart();
		createHashesAndUpdate(true);
	}

	public int getMinorTickLength() {
		return minorTickLength;
	}

	protected void doUpdate() {
		createHashesAndUpdate(true);
	}

	public void recreateDottedSeries() {
		setFilteredData(null);
		setDottedSeries(null);
		clearChart();
		getCommonSeriesMap().clear();
		createCommons();
		addAllCommonSeriesToTheChart();
		createHashesAndUpdate(false);
	}

	public void createHashesAndUpdate(boolean updateSamples) {
		HashSet<Long> hashesGettingUpdated = new HashSet<Long>();
		long start = System.currentTimeMillis();
		update(originalFromFile, hashesGettingUpdated);
		long diff = System.currentTimeMillis() - start;
		logger.debug("update time: {}", diff);
		forceRerender();
	}

	public void addSerieseToChart(Map<String, XYSeriesExtension> seriesMap) {
		for (String key : getSeriesKeys()) {
			XYSeriesExtension series = seriesMap.get(key);
			addSeries(series);
		}
	}

	public void adjustVisibility(Map<String, XYSeriesExtension> seriesMap) {
		for (String key : getSeriesKeys()) {
			XYSeriesExtension series = seriesMap.get(key);
			adjustVisibilityOfSeries(series);
		}
	}

	void setCorrectColorsForDottedSerieses(Map<String, XYSeriesExtension> dottedSerieses) {
		dottedSerieses.entrySet().stream().forEach((entry) -> {
			XYSeriesExtension dottedSeries = entry.getValue();
			String dataSetName = dottedSeries.getKey();
			Paint seriesColor = getSeriesColor(dataSetName);
			dottedSeries.setColorInTheChart(seriesColor);
		});
	}

	/**
	 * takes the list of resultlists and generates a list of DataSets from it along
	 * with some metadata such as min and max timestamp.
	 * 
	 * @param src the Map of lists of results that will be used to generate a
	 *            FilteredData instance from
	 * @return the generated FilteredData
	 */
	protected FilteredData generateDataSets(Map<String, List<TransactionExecutionResult>> src) {

		Map<String, List<TransactionExecutionResult>> inputToChart = cloneTheOriginalResultList(src);

		long[] minmax = Utilities.findMinMaxTimestamp(inputToChart, getSeriesKeys());

		long[] minmaxPoints = { 0, minmax[1] - minmax[0] };
		List<DataSet> generated = Utilities.convert(inputToChart, minmax[0], true, getSeriesKeys());

		for (DataSetUserType type : getRemovalFiltersInUse()) {
			type.getDataSetUser().useDataSet(generated);
		}

		return new FilteredData(generated, minmax, minmaxPoints);
	}

	public void populateRemovalFilters() {
		removalFilters.add(DataSetUserType.PERCENTREMOVALFILTER);
		removalFilters.add(DataSetUserType.FAILSREMOVALFILTER);
	}

	public void useDottedModeValue(boolean dottedMode) {
		if (this.dottedMode == dottedMode)
			return;
		this.dottedMode = dottedMode;
		createHashesAndUpdate(false);
	}

	protected Map<String, List<TransactionExecutionResult>> populateResultLists(Result... results) {
		Map<String, List<TransactionExecutionResult>> listOfListOfTransactionResults = new HashMap<String, List<TransactionExecutionResult>>();
		for (Result r : results) {
			useResult(r, listOfListOfTransactionResults);
		}

		return listOfListOfTransactionResults;
	}

	protected void useResult(Result r, Map<String, List<TransactionExecutionResult>> listOfListOfTransactionResults) {
		Map<String, List<TransactionExecutionResult>> transactionExecutionResults = r.getResultLists();

		for (String key : transactionExecutionResults.keySet()) {
			List<TransactionExecutionResult> listToBeCopiedAndCleared = transactionExecutionResults.get(key);
			List<TransactionExecutionResult> newList = listOfListOfTransactionResults.get(key);
			if (newList == null) {
				newList = new ArrayList<TransactionExecutionResult>();
				listOfListOfTransactionResults.put(key, newList);
			}
			newList.addAll(listToBeCopiedAndCleared);
		}
	}

	public static DataSetUser removeFails() {
		DataSetUser percentile = (a) -> {
			for (DataSet dataSet : a) {
				for (Point p : dataSet.getPoints()) {
					if (!p.isStatus())
						p.setEnabled(false);
				}
			}
		};
		return percentile;
	}

	public static DataSetUser removePercentile(int percentilToRemove) {
		DataSetUser percentile = (a) -> {
			for (DataSet dataSet : a) {
				dataSet.getPoints().sort((b, c) -> {
					long diff = c.getY() - b.getY();
					return (int) diff;
				});

				// 10% highest of 1000: remove 100 of the highest points
				double factor = (double) percentilToRemove / 100; // 0.1

				// 100 * 0.1 = 100
				int index = (int) (dataSet.getPoints().size() * factor);

				for (int i = 0; i < index; i++) {
					dataSet.getPoints().get(i).setEnabled(false);
				}
			}
		};
		return percentile;
	}

	protected static double amountOfSeriesesFactor(int amountOfThreads) {
		double factor = 1 + Math.log(1 + amountOfThreads * 0.1);
		return factor;
	}

	public static int calculateDefaultIndex(long sampleLengthToUse, int minorTickLength) {
		int initalSlideValue = (int) (sampleLengthToUse / 1000);
		int defaultIndex = initalSlideValue / minorTickLength;
		return defaultIndex;
	}

	static long[][] ticks = new long[][] { { 10 * MINUTE, 1 }, { 30 * MINUTE, 2 }, { 2 * HOUR, 5 }, { 4 * HOUR, 10 },
			{ 10 * HOUR, 30 }, { 1 * DAY, 60 }, { 10 * DAY, 600 }, { Long.MAX_VALUE, 3600 } };

	public static long calculateSliderTickSize(FilteredData filteredData) {
		long[] minmax = filteredData.getMinmax();
		long diff = minmax[1] - minmax[0];

		for (long[] tick : ticks) {
			if (diff < tick[0]) {
				return tick[1];
			}
		}

		logger.error("Internal error in class {} when calculating tick size for samplelength slider",
				ResultChartLogic.class);
		return 3600;
	}

	public static long calculateSampleLengthDefault(FilteredData filteredData) {
		long[] minmax = filteredData.getMinmax();
		int amoutOfTransactionTypes = filteredData.getDataSets().size();
		return calculateSampleLengthDefault(minmax[0], minmax[1], amoutOfTransactionTypes);
	}

	public static long calculateSampleLengthDefault(long min, long max, int amountOfTransactionTypes) {
		long diff = (max - min); // 3600_000 / 1000 = 3600

		if (diff < 0) {
			throw new RuntimeException(
					"Internal error. The earliest timestamp must be lower than the lastest timestamp");
		}

		int targetAmountOfSamplesForOneSeries = 1000;

		long diffPerSample = diff / targetAmountOfSamplesForOneSeries;

		/*
		 * the more serieses there are, the longer the sampleLength needs to be. There
		 * are a logaritmic relation between the amount of serieses and the sampleLength
		 * which means that increasing from 1 to 2 serieses will affect the sampleLength
		 * more than it will be affected if increasing from 10 to 11 serieses
		 */
		double amountOfSeriesesFactor = amountOfSeriesesFactor(amountOfTransactionTypes);
		long diffPerSampleForAllSeries = (long) (diffPerSample * amountOfSeriesesFactor);

		long sampleLengthInSeconds = diffPerSampleForAllSeries / 1000;
		long sampleLengthInMillis = sampleLengthInSeconds * 1000;

		if (sampleLengthInMillis < 1000)
			sampleLengthInMillis = 1000;

		return sampleLengthInMillis;

	}

	public Map<String, List<TransactionExecutionResult>> cloneTheOriginalResultList(
			Map<String, List<TransactionExecutionResult>> original) {
		Map<String, List<TransactionExecutionResult>> dst = new HashMap<String, List<TransactionExecutionResult>>();
		for (String key : getSeriesKeys()) {
			List<TransactionExecutionResult> list = original.get(key);
			ArrayList<TransactionExecutionResult> casted = (ArrayList<TransactionExecutionResult>) list;
			ArrayList<TransactionExecutionResult> clone = (ArrayList<TransactionExecutionResult>) casted.clone();
			dst.put(key, clone);
		}
		return dst;
	}

	public void clearChart() {
		commonSeriesCalculators.clear();
		seriesCollection.removeAllSeries();
		ranges.clearRanges();
		earliestX = null;
	}

	void populateChartWithSavedDottedSeries() {
		for (String key : getSeriesKeys()) {
			XYSeriesExtension series = dottedSeries.get(key);
			int indexOfSeries = seriesCollection.indexOf(series);
			Boolean visible = seriesVisible.get(series.getKey());
			if (visible == null || visible) {
				renderer.setSeriesShapesVisible(indexOfSeries, true);
			} else {
				renderer.setSeriesShapesVisible(indexOfSeries, false);
			}
			renderer.setSeriesLinesVisible(indexOfSeries, false);
		}
	}

	Map<String, XYSeriesExtension> createDottedSeries(List<DataSet> dataSets) {
		Map<String, XYSeriesExtension> result = new HashMap<String, XYSeriesExtension>();
		for (DataSet dataSet : dataSets) {
			String dataSetName = dataSet.getName();
			Paint seriesColor = getSeriesColor(dataSetName);
			XYSeriesExtension serie = new XYDottedSeriesExtension(dataSetName, false,
					ALLOW_DUPLICATE_X_VALUES_FOR_POINT_SERIES, seriesColor);
			result.put(dataSetName, serie);
		}
		return result;
	}
}
