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

import static com.loadcoder.load.chart.common.YCalculator.avg;
import static com.loadcoder.load.chart.common.YCalculator.max;
import static com.loadcoder.statics.Time.*;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.chart.LegendItem;

import com.loadcoder.load.chart.common.CommonSample;
import com.loadcoder.load.chart.common.CommonSampleGroup;
import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.common.CommonSeriesCalculator;
import com.loadcoder.load.chart.common.CommonYCalculator;
import com.loadcoder.load.chart.common.YCalculator;
import com.loadcoder.load.chart.data.DataSet;
import com.loadcoder.load.chart.data.FilteredData;
import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.jfreechart.ItemSeriesAdder;
import com.loadcoder.load.chart.jfreechart.XYDottedSeriesExtension;
import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.menu.DataSetUserType;
import com.loadcoder.load.chart.sampling.Sample;
import com.loadcoder.load.chart.sampling.SampleGroup;
import com.loadcoder.load.chart.utilities.ColorUtils;
import com.loadcoder.load.chart.utilities.SampleStatics;
import com.loadcoder.load.jfreechartfixes.XYLineAndShapeRendererExtention;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public abstract class ChartLogic {

	List<XYSeriesExtension> commonSeries = new ArrayList<XYSeriesExtension>();
	
	Map<Comparable, XYSeriesExtension> seriesCommonMap = new HashMap<Comparable, XYSeriesExtension>();
	
	CommonSeries[] commonsToBeUsed = CommonSeries.values();
	
	protected Long earliestX;
	
	protected long highestX = 0;

	protected long sampleLengthToUse;

	final boolean locked;
	
	FilteredData filteredData;

	List<DataSetUserType> removalFiltersInUse = new ArrayList<DataSetUserType>();

	protected final XYSeriesCollectionExtention seriesCollection;
	
	protected final XYPlotExtension plot;
	
	protected final XYLineAndShapeRendererExtention renderer;
	
	protected final Map<Comparable, Boolean> seriesVisible;
	
	protected List<YCalculator> yCalculators = new ArrayList<YCalculator>();

	public YCalculator yCalculatorToUse = avg;

	List<Color> existingColors = new ArrayList<Color>();
	
	Map<Comparable, Color> customizedColors;
	
	protected List<Comparable> seriesKeys = new ArrayList<Comparable>();
	
	protected Map<Comparable, LegendItem> legends = new HashMap<Comparable, LegendItem>();

	protected List<CommonSeriesCalculator> commonSeriesCalculators = new ArrayList<CommonSeriesCalculator>();

	protected List<Range> ranges = new ArrayList<Range>();
	
	protected boolean shallUpdate = true;

	protected abstract void getDataAndUpdate(HashSet<Long> hashesGettingUpdated, boolean updateSamples);

	protected abstract void update(List<List<TransactionExecutionResult>> listOfListOfList,
			HashSet<Long> hashesGettingUpdated, boolean updateSamples);

	protected abstract void doUpdate();

	protected long getXDiff() {
		if(earliestX == null)
			return 0;
		long xDiff = highestX - earliestX;
		return xDiff;
	}
	
	//only used for test
    protected Range lookupCorrectRange(long ts) {
        for (Range range : ranges) {
                if (range.isTimestampInThisRange(ts))
                        return range;
        }
        throw new RuntimeException("No range was found for timestamp " + ts);
    }
    
	public void doSafeUpdate(){
		synchronized (plot) {
			doUpdate();
			long xDiff = getXDiff();
			if(xDiff > 23 * HOUR) {
				plot.changeToMonthAndDayDateAxisFormat();
			}
		}
	}
	
	public List<DataSetUserType> getRemovalFiltersInUse() {
		return removalFiltersInUse;
	}

	public void setRemovalFiltersInUse(List<DataSetUserType> removalFiltersInUse) {
		this.removalFiltersInUse = removalFiltersInUse;
	}

	public void setFilteredData(FilteredData filteredData) {
		this.filteredData = filteredData;
	}

	public FilteredData getFilteredData() {
		return filteredData;
	}

	public long getSampleLengthToUse() {
		return sampleLengthToUse;
	}

	public void setSampleLengthToUse(long sampleLengthToUse) {
		this.sampleLengthToUse = sampleLengthToUse;
	}

	public List<YCalculator> getyCalculators() {
		return yCalculators;
	}

	public void setyCalculators(List<YCalculator> yCalculators) {
		this.yCalculators = yCalculators;
	}

	void addToSeriesKeys(FilteredData filteredData, List<Comparable> seriesKeys){
		for(DataSet dataSet : filteredData.getDataSets()){
			String s = dataSet.getName();
			if(!seriesKeys.contains(s))
				seriesKeys.add(s);
		}
	}

	public ChartLogic(XYSeriesCollectionExtention seriesCollection, XYPlotExtension plot,
			XYLineAndShapeRendererExtention renderer, Map<Comparable, Boolean> seriesVisible, boolean locked) {
		this.locked = locked;
		this.seriesCollection = seriesCollection;
		this.plot = plot;
		this.renderer = renderer;
		this.seriesVisible = seriesVisible;

		yCalculators.add(avg);
		yCalculators.add(max);
		
		populateColorArray();
	}

	public static void addSurroundingTimestampsAsUpdates(Set<Long> hashesGettingUpdated, long sampleStart, long earliest,
			long latest, List<Range> ranges, long currentSampleLength, 
			Set<Long> sampleTimestamps, Map<Long, Sample> aboutToBeUpdated) {

		// check backwards
		long iterator = sampleStart;
		while (earliest < iterator) {
			long lastTsInPrevious = iterator - 1;
			long sampleLength = Range.findSampleLength(lastTsInPrevious, ranges);
			long firstTsInPrevious = SampleGroup.calculateFirstTs(lastTsInPrevious, sampleLength);

			boolean exists = false;
			if(sampleTimestamps.contains(firstTsInPrevious) || aboutToBeUpdated.containsKey(firstTsInPrevious)){
				exists = true;
			}
			
			if (exists) {
				break;
			} else {
				if (!hashesGettingUpdated.contains(firstTsInPrevious))
					hashesGettingUpdated.add(firstTsInPrevious);
				iterator = firstTsInPrevious;
			}
		}

		// check forward
		iterator = sampleStart;
		while (latest > iterator) {
			long sampleLength = Range.findSampleLength(iterator, ranges);
			long firstTsInNext = iterator + sampleLength;
			boolean exists = false;
			if(sampleTimestamps.contains(firstTsInNext) || aboutToBeUpdated.containsKey(firstTsInNext)){
				exists = true;
			}
			
			if (exists) {
				break;
			} else {
				if (!hashesGettingUpdated.contains(firstTsInNext))
					hashesGettingUpdated.add(firstTsInNext);
				iterator = firstTsInNext;
			}
		}
	}

	public void createCommons() {

		commonSeries = new ArrayList<XYSeriesExtension>();
		commonSeriesCalculators.clear();
		commonSeries.clear();
		seriesCommonMap.clear();

		Arrays.stream(commonsToBeUsed).forEach((common) -> {
			XYSeriesExtension xySeries = new XYSeriesExtension(common.getName(), true, false, common.getColor());
			seriesCommonMap.put(common.getName(), xySeries);
			commonSeriesCalculators.add(new CommonSeriesCalculator(xySeries, common.getCommonYCalculator()));
			commonSeries.add(xySeries);
		} );
	}
		
	public void addSeries(XYSeriesExtension serie) {
		seriesCollection.addSeries(serie); //TODO. bug this fires render
		int indexOfSeries = seriesCollection.indexOf(serie);
		LegendItem legend = legends.get(serie.getKey());
		if (legend == null) {
			legend = plot.getRenderer().getLegendItem(0, indexOfSeries);
			legends.put(serie.getKey(), legend);
			plot.getLegendItems().add(legend);
		} else {
		}
		serie.setLegend(legend);
	}

	public void removeSeries(XYSeriesExtension serie) {
		int indexOfSeries = seriesCollection.indexOf(serie);
		seriesCollection.removeSeries(indexOfSeries);
	}

	/*
	 * iterate through all timestamp that are the first one in one of the
	 * updated samples. The series that are affected by the transaction series
	 * are going to be upated below
	 */
	void updateCommonsWithSamples(HashSet<Long> hashesGettingUpdated, Map<Comparable, SampleGroup> sampleGroups, Map<Comparable, CommonSampleGroup> samplesCommonMap, 		List<CommonSampleGroup> sampleGroupCommonList) {

		/*
		 * iterate through all timestamp that are the first one in one of the
		 * updated samples. The series that are affected by the transaction
		 * series are going to be upated below
		 */
		for (Long l : hashesGettingUpdated) {
			for (CommonSeriesCalculator calc : commonSeriesCalculators) {
				XYSeriesExtension series = calc.getSeries();

				CommonYCalculator calculator = calc.getCalculator();
				Range r = getSampleLength(l);
				double amount = calculator.calculateCommonY(seriesKeys, l, sampleGroups, r.getSampleLength());

				// get or create the samplegroup for the common series
				Comparable commonKey = series.getKey();
				CommonSampleGroup commonSampleGroup = samplesCommonMap.get(commonKey);
				if (commonSampleGroup == null) {
					commonSampleGroup = new CommonSampleGroup(series);
					samplesCommonMap.put(commonKey, commonSampleGroup);
					sampleGroupCommonList.add(commonSampleGroup);
				}

				CommonSample cs = commonSampleGroup.getAndCreateSample(l, (String) commonKey, r.getSampleLength());

				long longAmount = Sample.amountToYValue(amount);
				cs.setY(longAmount);
				if (cs.getFirst() == null) {
					cs.initDataItems();
					series.add(cs.getFirst(), false);
					if(SampleStatics.USE_TWO_SAMPLE_POINTS) {
						series.add(cs.getLast(), false);
					}
				} else {
					cs.updateDataItems();
				}
			}
		}
	}

	Paint getSeriesColor(Comparable dataSetName) {
		LegendItem legend = legends.get(dataSetName);
		Paint seriesColor = null;
		if (legend == null) {
			seriesColor = getNewColor(dataSetName);
		} else {
			seriesColor = legend.getFillPaint();
		}
		return seriesColor;
	}

	void getSerieses(List<DataSet> dataSets, boolean dottedMode, Map<Comparable, XYSeriesExtension> seriesMap) {
		for (DataSet dataSet : dataSets) {
			String dataSetName = dataSet.getName();

			Paint seriesColor = getSeriesColor(dataSetName);

			XYSeriesExtension seriesName = seriesMap.get(dataSetName);
			if(seriesName == null) {
				XYSeriesExtension serie = new XYSeriesExtension(dataSetName, true, false, seriesColor);
				seriesMap.put(dataSetName, serie);
			}
		}
	}

	void adjustVisibilityOfSeries(XYSeriesExtension serie) {
		int indexOfSeries = seriesCollection.indexOf(serie);
		renderer.setSeriesShape(indexOfSeries, XYDottedSeriesExtension.DOTTEDSHAPE);

		Boolean visible = seriesVisible.get(serie.getKey());

		if (visible == null) {
			visible = true;
			seriesVisible.put(serie.getKey(), visible);
		}

		serie.setVisible(visible);
		renderer.setSeriesLinesVisible(indexOfSeries, visible);
		// is sample mode, the shapes shall always be invisible.
		renderer.setSeriesShapesVisible(indexOfSeries, false);
	}

	void addPoints(List<DataSet> dataSets, Map<Comparable, SampleGroup> sampleGroups, Set<Long> sampleTimestamps) {
		long start = System.currentTimeMillis();
		for (DataSet dataSet : dataSets) {
			String dataSetName = dataSet.getName();
			SampleGroup sampleGroup = sampleGroups.get(dataSetName);
			XYSeriesExtension series = sampleGroup.getSeries();
			createSamplesAndAddPoints(dataSetName, sampleGroup.getSeries(), dataSet, sampleGroup, sampleTimestamps);
		}
	}

	void createSamplesGroups(Map<Comparable, XYSeriesExtension> seriesMap, Map<Comparable, SampleGroup> sampleGroups) {
		for(Comparable key : seriesKeys){
			XYSeriesExtension serie = seriesMap.get(key);
			SampleGroup sampleGroup = sampleGroups.get(key);
			if(sampleGroup == null){
				sampleGroup = new SampleGroup(sampleLengthToUse, serie, locked);
				sampleGroups.put(key, sampleGroup);
			}
		}
	}

	void createSamplesAndAddPoints(Comparable dataSetName, XYSeriesExtension serie, DataSet dataSet, SampleGroup sampleGroup, Set<Long> sampleTimestamps) {
		for (Point point : dataSet.getPoints()) {
			long x = point.getX();

			if (x > highestX)
				highestX = x;
			if (earliestX == null || x < earliestX) {
				earliestX = x;

				if (ranges.isEmpty()) {
					Sample s = sampleGroup.getAndCreateSample(point.getX(), dataSetName, sampleLengthToUse);
					ranges.add(new Range(s.getFirstTs(), Long.MAX_VALUE, sampleLengthToUse));

				} else {

					/*
					 * if there are more than one range, and a new
					 * earliestTimestamp is added we must pick up the last added
					 * range here.
					 */
					Range r = ranges.get(ranges.size() - 1);
					long sampleLengthOfTheLastAddedRange = r.getSampleLength();

					/*
					 * the last added range will get a new start equal to the
					 * firstTs for the Sample of this Point that has the
					 * earliest timestamp
					 */
					Sample s = sampleGroup.getAndCreateSample(x, dataSetName, sampleLengthOfTheLastAddedRange);
					r.setStart(s.getFirstTs());
				}
			}

			// fetch the correct Range for the point
			Range rangeToUse = getSampleLength(x);

			// here the Sample for the point is either fetched of created
			Sample s = sampleGroup.getAndCreateSample(point.getX(), dataSetName, rangeToUse.getSampleLength());
			
			if(! sampleTimestamps.contains(s.getFirstTs())){
				sampleTimestamps.add(s.getFirstTs());
			}
			
			s.addPoint(point);
			if (!point.isStatus()) {
				s.increaseFails();
			}
			
			// This sample needs to be recalculated and redrawn!
			if (!sampleGroup.getSamplesUnupdated().containsKey(s.getFirstTs())) {
				sampleGroup.getSamplesUnupdated().put(s.getFirstTs(), s);
			}
		}
	}

	public void addAllCommonSeriesToTheChart() {
		for (XYSeriesExtension series : commonSeries) {
			addSeries(series);
			int indexOfSeries = seriesCollection.indexOf(series);

			Boolean visible = seriesVisible.get(series.getKey());
			if (visible == null || visible) {
				renderer.setSeriesLinesVisible(indexOfSeries, true);
			} else {
				renderer.setSeriesLinesVisible(indexOfSeries, false);
			}
			renderer.setSeriesShapesVisible(indexOfSeries, false);
		}
	}

	protected void useResult(Result r, List<List<TransactionExecutionResult>> listOfListOfList) {
		List<List<TransactionExecutionResult>> transactionExecutionResults = r.getResultLists();
		synchronized (transactionExecutionResults) {

			for (List<TransactionExecutionResult> listToBeCopiedAndCleared : transactionExecutionResults) {
				List<TransactionExecutionResult> newList = new ArrayList<TransactionExecutionResult>();
				listOfListOfList.add(newList);
				newList.addAll(listToBeCopiedAndCleared);
			}
		}
	}

	public Range getSampleLength(long timestamp) {
		for (Range range : ranges) {
			if (timestamp >= range.getStart() && timestamp <= range.getEnd()) {
				return range;
			}
		}
		return null;
	}

	void populateColorArray(){
		if(customizedColors!= null){
			Iterator<Entry<Comparable, Color>> i = customizedColors.entrySet().iterator();
			while(i.hasNext()){
				Entry<Comparable, Color> e = i.next();
				existingColors.add(e.getValue());
			}
		}
		for(CommonSeries commonSerie : commonsToBeUsed){
			Color c = commonSerie.getColor();
			existingColors.add(c);
		}
	}
	
	public synchronized Color getNewColor(Comparable seriesKey) {

		if(customizedColors != null){
			Color color = customizedColors.get(seriesKey);
			return color;
		}
		Color newColor = ColorUtils.getNewContrastfulColor(existingColors);
		existingColors.add(newColor);
		return newColor;
	}

	void forceRerender() {
		seriesCollection.fireChange();
	}

	void updateSeriesWithSamples(ItemSeriesAdder itemSeriesAdder, Set<Long> hashesGettingUpdated,
	List<DataSet> dataSets, Map<Comparable, SampleGroup> sampleGroups, Set<Long> sampleTimestamps) {
		/*
		 * The new data has now been arranged into correct Samples. Time to
		 * calculate each of the affected Samples and update the points.
		 */
		for(Comparable key : seriesKeys){
			
			SampleGroup group = sampleGroups.get(key);
			XYSeriesExtension series = group.getSeries();

			Set<Long> aboutToBeUpdatedKeys = group.getSamplesUnupdated().keySet();
			for(Long unupdatedSampleKey : aboutToBeUpdatedKeys) {
				Sample sample = group.getSamplesUnupdated().get(unupdatedSampleKey);
				sample.calculateY(yCalculatorToUse);
				double y = sample.getY();
				
				// y will be -1 if there are no data in the sample. No items to add to the series
				if (y != -1) {
					itemSeriesAdder.add(series, sample);
				}
				long l = sample.getFirstTs();

				if (!hashesGettingUpdated.contains(l))
					hashesGettingUpdated.add(l);

				long[] minmaxPoints = filteredData.getMinmaxPoints();

				long earliest = minmaxPoints[0];
				long latest = minmaxPoints[1];

				addSurroundingTimestampsAsUpdates(hashesGettingUpdated, l, earliest, latest, ranges, sample.getLength(),sampleTimestamps, group.getSamplesUnupdated());
			}
			group.getSamplesUnupdated().clear();
		}
	}
}
