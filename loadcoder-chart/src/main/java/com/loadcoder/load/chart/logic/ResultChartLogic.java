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
import static com.loadcoder.statics.Milliseconds.*;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.chart.common.CommonSampleGroup;
import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.data.DataSet;
import com.loadcoder.load.chart.data.FilteredData;
import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.jfreechart.ChartFrame.DataSetUser;
import com.loadcoder.load.chart.jfreechart.ItemSeriesAdder;
import com.loadcoder.load.chart.jfreechart.XYDottedSeriesExtension;
import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.menu.DataSetUserType;
import com.loadcoder.load.chart.sampling.SampleGroup;
import com.loadcoder.load.chart.utilities.ChartUtils;
import com.loadcoder.load.chart.utilities.Utilities;
import com.loadcoder.load.jfreechartfixes.XYLineAndShapeRendererExtention;
import com.loadcoder.load.measure.Result;
import com.loadcoder.load.measure.TransactionExecutionResult;

public class ResultChartLogic extends ChartLogic {

	static Logger log = LoggerFactory.getLogger(ResultChartLogic.class);
	
	private static final boolean allowDuplicateXValues_forDottedSeries = true;

	Result[] results;

	List<DataSetUserType> removalFilters = new ArrayList<DataSetUserType>();

	boolean dottedMode;

	List<List<TransactionExecutionResult>> originalFromFile = new ArrayList<List<TransactionExecutionResult>>();

	Map<Comparable, XYSeriesExtension> dottedSeries = null;

	Map<Comparable, XYSeriesExtension> series;

	//slider
	final int minorTickLength;
	int defaultIndex;
	int sliderCompensation;
	
	protected static final int[][] sampleLengtSteps = {{10, 1}, {10, 2}, {30, 5}, {100, 10}, {1000, 100}, {10000, 1000}, {100000, 10000}};

	public List<DataSetUserType> getRemovalFilters() {
		return removalFilters;
	}
	
	public void setDottedSeries(Map<Comparable, XYSeriesExtension> dottedSeries) {
		this.dottedSeries = dottedSeries;
	}

	public Map<Comparable, XYSeriesExtension> getDottedSeries() {
		return dottedSeries;
	}

	public ResultChartLogic(
			XYSeriesCollectionExtention seriesCollection,
			XYPlotExtension plot,
			XYLineAndShapeRendererExtention renderer,
			Map<Comparable, Boolean> seriesVisible,
			boolean defaultDottedMode,
			CommonSeries[] commonSeries,
			Map<Comparable, Color> customizedColors,
			boolean locked,
			Result... results
			) {
		super(seriesCollection, plot, renderer, seriesVisible, locked);

		this.customizedColors = customizedColors;
		this.dottedMode = defaultDottedMode;
		this.results = results;
		commonsToBeUsed = commonSeries;
		populateRemovalFilters();

		populateResultLists(originalFromFile);
		originalFromFile = TransactionExecutionResult.mergeList(originalFromFile);

		filteredData = generateDataSets(originalFromFile);

		long tickSize = calculateSliderTickSize(filteredData);
		minorTickLength = (int)tickSize;
		calculateSliderValueCompensation(minorTickLength);
		
		defaultIndex = 4;
		sampleLengthToUse = calculateSampleLengthWith(defaultIndex);
		log.info("sampleLengthToUse:{}", sampleLengthToUse);

				
		calculateDefaultIndex(sampleLengthToUse, minorTickLength);


		doSafeUpdate();
	}
	
	public long calculateSampleLengthWith(int indexOfSlider){
		long newSampleLength = 1000;
		if(indexOfSlider != 0){
			long valueOfSlider = indexOfSlider * getMinorTickLength() + getsliderCompensation();
			newSampleLength = valueOfSlider * 1000;
		}
		return newSampleLength;
	}
	public int getDefaultSliderIndex(){
		return defaultIndex;
	}
	
	public int getsliderCompensation(){
		return sliderCompensation;
	}
	
	void calculateSliderValueCompensation(int minorTickPacing){
		int sliderCompensation = 0;
		if(minorTickPacing ==1)
			sliderCompensation =1;
		
		this.sliderCompensation = sliderCompensation;
	}
	
	public int getMinorTickLength(){
		return minorTickLength;
	}
	protected void doUpdate(){
		createHashesAndUpdate(true);
	}
	
	public void createHashesAndUpdate(boolean updateSamples) {
		HashSet<Long> hashesGettingUpdated = new HashSet<Long>();
		getDataAndUpdate(hashesGettingUpdated, updateSamples);
		forceRerender();
	}

	protected void getDataAndUpdate(HashSet<Long> hashesGettingUpdated, boolean updateSamples) {
		update(originalFromFile, hashesGettingUpdated, updateSamples);
	}

	public void addSerieseToChart(Map<Comparable, XYSeriesExtension> seriesMap) {
		for (Comparable key : seriesKeys) {
			XYSeriesExtension series = seriesMap.get(key);
			sleep();
			addSeries(series);
		}
	}

	public void adjustVisibility(Map<Comparable, XYSeriesExtension> seriesMap) {
		for (Comparable key : seriesKeys) {
			XYSeriesExtension series = seriesMap.get(key);
			adjustVisibilityOfSeries(series);
		}
	}

    private void sleep(){
    	try{
    		Thread.sleep(150);
    	}catch(Exception e){}
    }
    
	@Override
	protected void update(List<List<TransactionExecutionResult>> listOfListOfList, HashSet<Long> hashesGettingUpdated,
			boolean updateSamples) {

		seriesCollection.removeAllSeries();
		ranges.clear();

		earliestX = null;

		if (filteredData == null) {
			filteredData = generateDataSets(listOfListOfList);
		}

		addToSeriesKeys(filteredData, seriesKeys);
		
		HashSet<Long> sampleTimestamps = new HashSet<Long>();
		
		Map<Comparable, CommonSampleGroup> samplesCommonMap = new HashMap<Comparable, CommonSampleGroup>();
		List<CommonSampleGroup> sampleGroupCommonList = new ArrayList<CommonSampleGroup>();
		
		Map<Comparable, XYSeriesExtension> seriesMap = new HashMap<Comparable, XYSeriesExtension>();
		if (dottedMode) {
			if (dottedSeries == null) {
				dottedSeries = createDottedSeries(filteredData.getDataSets());
			}
			seriesMap = dottedSeries;
		} else {
			getSerieses(filteredData.getDataSets(), dottedMode, seriesMap);
		}

		Map<Comparable, SampleGroup> sampleGroups = new HashMap<Comparable, SampleGroup>();
		createSamplesGroups(seriesMap, sampleGroups);

		addSerieseToChart(seriesMap);

		adjustVisibility(seriesMap);

		createCommons();
		addAllCommonSeriesToTheChart();
		
		for (XYSeriesExtension commonSerie : commonSeries) {
			adjustVisibilityOfSeries(commonSerie);
		}

		addPoints(filteredData.getDataSets(), sampleGroups, sampleTimestamps);
		ItemSeriesAdder itemSeriesAdderToUse = ChartUtils.itemSeriesAdderForSamples;

		if (dottedMode) {
			itemSeriesAdderToUse = ChartUtils.itemSeriesAdderForDots;

			if (dottedSeries == null) {
				createDottedSeries(filteredData.getDataSets());

			} else {
				populateChartWithSavedDottedSeries();
			}

		}

		updateSeriesWithSamples(itemSeriesAdderToUse, hashesGettingUpdated, filteredData.getDataSets(), sampleGroups, sampleTimestamps);
		updateCommonsWithSamples(hashesGettingUpdated, sampleGroups, samplesCommonMap, sampleGroupCommonList);
	}

	/**
	 * takes the list of resultlists and generates a list of DataSets from it
	 * along with some metadata such as min and max timestamp.
	 */
	protected FilteredData generateDataSets(List<List<TransactionExecutionResult>> src) {

		List<List<TransactionExecutionResult>> inputToChart = new ArrayList<List<TransactionExecutionResult>>();
		cloneTheOriginalResultList(inputToChart, src);

		long[] minmax = Utilities.findMinMaxTimestamp(inputToChart);

		long[] minmaxPoints = { 0, minmax[1] - minmax[0] };
		List<DataSet> generated = Utilities.convert(inputToChart, minmax[0], true);

		for (DataSetUserType type : removalFiltersInUse) {
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

	protected void populateResultLists(List<List<TransactionExecutionResult>> listOfListOfList) {
		for (Result r : results) {
			useResult(r, listOfListOfList);
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
				} );

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

	public static double amountOfSeriesesFactor(int amountOfThreads) {
		double factor = 1 + Math.log(1 + amountOfThreads * 0.1);
		return factor;
	}
	
	public static int calculateDefaultIndex(long sampleLengthToUse, int minorTickLength){
		int initalSlideValue = (int) (sampleLengthToUse / 1000);
		int defaultIndex = initalSlideValue / minorTickLength;
		return defaultIndex;
	}

	
	static long[][] ticks = new long[][] {
		{10 *MINUTE, 1}, 
		{30 *MINUTE, 2}, 
		{2 * HOUR, 5}, 
		{4 * HOUR, 10}, 
		{10 * HOUR, 30},
		{1 * DAY, 60},
		{10 * DAY, 600},
		{Long.MAX_VALUE, 3600}};
	
	public static long calculateSliderTickSize(FilteredData filteredData) {
		long[] minmax = filteredData.getMinmax();
		long diff = minmax[1] - minmax[0];
		
		for(long[] tick : ticks){
			if(diff < tick[0]){
				return tick[1];
			}
		}
		
		log.error("Internal error in class {} when calculating tick size for samplelength slider", ResultChartLogic.class);
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
		 * the more serieses there are, the longer the sampleLength needs to be.
		 * There are a logaritmic relation betwwen the amount of serieses and the sampleLength
		 * which means that increasing from 1 to 2 serieses will affect the sampleLength more
		 * than it will be affected if increasing from 10 to 11 serieses
		 */
		double amountOfSeriesesFactor = amountOfSeriesesFactor(amountOfTransactionTypes);
		long diffPerSampleForAllSeries = (long) (diffPerSample * amountOfSeriesesFactor);

		long sampleLengthInSeconds = diffPerSampleForAllSeries / 1000;
		long sampleLengthInMillis = sampleLengthInSeconds * 1000;

		if (sampleLengthInMillis < 1000)
			sampleLengthInMillis = 1000;
		
		return sampleLengthInMillis;

	}

	public void cloneTheOriginalResultList(List<List<TransactionExecutionResult>> dst,
			List<List<TransactionExecutionResult>> original) {
		for (List<TransactionExecutionResult> list : original) {
			ArrayList<TransactionExecutionResult> casted = (ArrayList<TransactionExecutionResult>) list;
			ArrayList<TransactionExecutionResult> clone = (ArrayList<TransactionExecutionResult>) casted.clone();
			dst.add(clone);
		}
	}

	public void clearChart() {
		commonSeriesCalculators.clear();
		seriesCollection.removeAllSeries();
		plot.getLegends();
		ranges.clear();
		earliestX = null;
	}

	void populateChartWithSavedDottedSeries() {
		for (Comparable key : seriesKeys) {
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

	Map<Comparable, XYSeriesExtension> createDottedSeries(List<DataSet> dataSets) {
		Map<Comparable, XYSeriesExtension> result = new HashMap<Comparable, XYSeriesExtension>();
		for (DataSet dataSet : dataSets) {
			String dataSetName = dataSet.getName();
			Paint seriesColor = getSeriesColor(dataSetName);
			XYSeriesExtension serie = new XYDottedSeriesExtension(dataSetName, true,
					allowDuplicateXValues_forDottedSeries, seriesColor);
			result.put(dataSetName, serie);
		}
		return result;
	}
}
