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

import static com.loadcoder.statics.Time.DAY;
import static com.loadcoder.statics.Time.HOUR;
import static com.loadcoder.statics.Time.MINUTE;
import static com.loadcoder.statics.Time.SECOND;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.chart.common.CommonSampleGroup;
import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.data.DataSet;
import com.loadcoder.load.chart.data.FilteredData;
import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.data.Ranges;
import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.menu.DataSetUserType;
import com.loadcoder.load.chart.sampling.SampleConcaternator;
import com.loadcoder.load.chart.sampling.SampleConcaternatorRunDecider;
import com.loadcoder.load.chart.sampling.SampleConcaternatorSpec;
import com.loadcoder.load.chart.sampling.SampleGroup;
import com.loadcoder.load.chart.sampling.SampleGroup.ConcaternationResult;
import com.loadcoder.load.chart.utilities.Utilities;
import com.loadcoder.load.scenario.RuntimeResultUser;
import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeChartLogic extends ChartLogic implements RuntimeResultUser {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<String, XYSeriesExtension> seriesMap = new HashMap<String, XYSeriesExtension>();

	private long[] minmax = { Long.MAX_VALUE, Long.MIN_VALUE };

	private long firstTsToBeReceived;

	private long tsForFirstUpdateContainingData = Long.MAX_VALUE;

	protected List<SampleConcaternator> sampleConcaternatorList = new ArrayList<SampleConcaternator>();

	private Set<Long> sampleTimestamps = new HashSet<Long>();

	private Map<String, SampleGroup> sampleGroups = new HashMap<String, SampleGroup>();

	private final List<SampleConcaternatorSpec> concaterSpecList;// = new ArrayList<SampleConcaternatorSpec>();

	private Map<String, CommonSampleGroup> samplesCommonMap = new HashMap<String, CommonSampleGroup>();

	private List<CommonSampleGroup> sampleGroupCommonList = new ArrayList<CommonSampleGroup>();

	private Map<String, List<TransactionExecutionResult>> incomingData;

	Ranges ranges = new Ranges();
	
	public List<SampleConcaternator> getSampleConcaternatorList() {
		return sampleConcaternatorList;
	}

	public void setIncomingData(Map<String, List<TransactionExecutionResult>> incomingData) {
		this.incomingData = incomingData;
	}

	public Map<String, SampleGroup> getSampleGroups() {
		return sampleGroups;
	}

	public RuntimeChartLogic(CommonSeries[] commonSeries, boolean locked) {
		super(commonSeries, locked);

		setSampleLengthToUse(SAMPLELENGTH_DEFAULT);
		concaterSpecList = getSampleConcaternatorSpecs();

		// Init the first ranges
		ranges.addRange(new Range(Long.MIN_VALUE, -1, getSampleLengthToUse()));
		ranges.addRange(new Range(0, Long.MAX_VALUE, getSampleLengthToUse()));

		initiateChart();
	}

	public int getIncomingSize(Map<String, List<TransactionExecutionResult>> listOfListOfList,
			List<String> seriesKeys) {
		int size = 0;
		if (listOfListOfList == null) {
			return 0;
		}
		for (String key : seriesKeys) {
			List<TransactionExecutionResult> list = listOfListOfList.get(key);
			if (list == null) {
				continue;
			}
			size += list.size();
		}
		return size;
	}

	public void updateRangesForSampleConcaternatorAfterConcaternation(SampleConcaternator concater) {
		Range oldRange = concater.getOldRange();
		Range newRange = concater.getNewRange();
		long newSampleLength = oldRange.getSampleLength() * concater.getAmountToConcaternate();
		long start = oldRange.getStart();
		long newStart = start + newSampleLength;

		oldRange.setStart(newStart);
		newRange.setEnd(newStart - 1);
		if (false) {
			drawRangeDebugStartLines(oldRange, newStart);
		}
	}

	/**
	 * This method is only for debugging purposes. It is invoked after the Ranges
	 * has been adjusted for the concatenation of Samples. It will draw the active
	 * Ranges start points as lines.
	 * 
	 * @param oldRange
	 * @param newStart
	 */
	private void drawRangeDebugStartLines(Range oldRange, long newStart) {
		if (oldRange.getSeries() == null) {
			Color c = getNewColor("" + oldRange.getSampleLength());
			oldRange.setSeries(new XYSeriesExtension("" + oldRange.getSampleLength(), true, false, c));
			addSeries(oldRange.getSeries());
		} else {
			oldRange.getSeries().clear();
		}
		oldRange.setHigh(new XYDataItemExtension(newStart + 1, 5000));
		oldRange.setLow(new XYDataItemExtension(newStart, 0));
		oldRange.getSeries().add(oldRange.getHigh());
		oldRange.getSeries().add(oldRange.getLow());
	}

	public void removeFromSampleTimestamps(SampleConcaternator concater, Set<Long> sampleTs) {
		long start = concater.getOldRange().getStart();
		long oldSampleLength = concater.getOldRange().getSampleLength();
		long amount = concater.getAmountToConcaternate();
		long tsToRemove = start;
		for (int i = 1; i < amount; i++) {
			tsToRemove = tsToRemove + oldSampleLength;

			/*
			 * it is not certain that tsToRemove exists in the Set! this can happen if the
			 * intensity for the particular transaction is low and there are entire
			 * samplelength's where the transaction wasn't made. In this case there won't be
			 * a Sample to be removed and neither an entry in this Set.
			 */
			sampleTs.remove(tsToRemove);
		}
	}

	public void concatAndAdjustRanges(SampleConcaternator concater, Set<Long> hashesGettingUpdated) {
		for (String key : getSeriesKeys()) {
			SampleGroup sampleGroup = sampleGroups.get(key);
			ConcaternationResult concaternationResult = sampleGroup.concaternate(concater);
			concaternationResult.fixPointsForSeries(sampleGroup.getSeries());
		}

		log.trace("old range:{}", concater.getOldRange());
		log.trace("new range:{}", concater.getNewRange());
		for (CommonSampleGroup sampleGroup : sampleGroupCommonList) {
			sampleGroup.concaternate(concater);
		}

		removeFromSampleTimestamps(concater, sampleTimestamps);

		hashesGettingUpdated.add(concater.getOldRange().getStart());

		updateRangesForSampleConcaternatorAfterConcaternation(concater);
	}

	protected void concat(HashSet<Long> hashesGettingUpdated) {
		long start = System.currentTimeMillis();
		int mostConcatsAtOnce = 1000;

		for (SampleConcaternator concater : getSampleConcaternatorList()) {
			int i = 0;
			while (concater.getSampleConcaternatorRunDecider().timeForConcaternation(concater)) {
				concatAndAdjustRanges(concater, hashesGettingUpdated);
				if (i++ > mostConcatsAtOnce) {
					break;
				}
			}
		}
		LoadUtility.logExecutionTime("concatenation", start);
	}

	void performUpdate() {
		HashSet<Long> hashesGettingUpdated = new HashSet<Long>();
		concat(hashesGettingUpdated);
		log.trace("hashes after concat removed samples {}", hashesGettingUpdated);
		update(incomingData, hashesGettingUpdated);
		addNewSampleConcaternaterIfItsTime();
	}

	public void update(Map<String, List<TransactionExecutionResult>> incomingData, HashSet<Long> hashesGettingUpdated) {

		List<String> seriesKeys = getSeriesKeys();
		incomingData.keySet().stream().forEach(key -> addSeriesKey(key));
		int incomingSize = getIncomingSize(incomingData, seriesKeys);
		if (incomingSize == 0) {
			updateCommonsWithSamples(hashesGettingUpdated, sampleGroups, samplesCommonMap, sampleGroupCommonList);
			return;
		}
		long[] minmaxNew = Utilities.findMinMaxTimestamp(incomingData, getSeriesKeys());

		/*
		 * tsForFirstUpdateContainingData is needed in order to determine when to add
		 * new concatenators as samples. this variable is only set once, which is the
		 * first time data arrives to the chart.
		 */
		if (tsForFirstUpdateContainingData == Long.MAX_VALUE) {
			tsForFirstUpdateContainingData = minmaxNew[0];

		}
		if (minmaxNew[0] < minmax[0]) {
			minmax[0] = minmaxNew[0];
		}
		if (minmaxNew[1] > minmax[1]) {
			minmax[1] = minmaxNew[1];
		}

		if (firstTsToBeReceived == 0) {
			firstTsToBeReceived = minmax[0];
		}

		long[] minmaxPoints = { 0, minmax[1] - minmax[0] };

		List<DataSet> dataSets = Utilities.convert(incomingData, firstTsToBeReceived, true, getSeriesKeys());
		setFilteredData(new FilteredData(dataSets, minmax, minmaxPoints));

		addToSeriesKeys(getFilteredData(), getSeriesKeys());

		for (DataSetUserType type : getRemovalFiltersInUse()) {
			type.getDataSetUser().useDataSet(dataSets);
		}

		getSerieses(getFilteredData().getDataSets(), seriesMap);
		addSeriesNotAdded(seriesMap);
		createSamplesGroups(seriesMap, sampleGroups);
		addPoints(getFilteredData().getDataSets(), sampleGroups, sampleTimestamps);

		updateSeriesWithSamples(hashesGettingUpdated, getFilteredData().getDataSets(), sampleGroups, sampleTimestamps,
				false);

		updateCommonsWithSamples(hashesGettingUpdated, sampleGroups, samplesCommonMap, sampleGroupCommonList);

		forceRerender();
	}

	private void addSeriesNotAdded(Map<String, XYSeriesExtension> seriesMap) {
		for (String key : getSeriesKeys()) {
			if (sampleGroups.get(key) == null) {
				XYSeriesExtension series = seriesMap.get(key);
				addSeries(series);
			}
		}
	}

	public void addNewConcater(int amountOfSamplesToConcaternate,
			SampleConcaternatorRunDecider sampleConcaternatorRunDecider) {

		Range oldRange = ranges.getLastRange();
		long newSampleLength = oldRange.getSampleLength() * amountOfSamplesToConcaternate;
		Range newRange = new Range(0, -1, newSampleLength);
		ranges.addRange(newRange);
		SampleConcaternator sampleConcaternator = new SampleConcaternator(oldRange, newRange,
				amountOfSamplesToConcaternate, sampleConcaternatorRunDecider);
		getSampleConcaternatorList().add(sampleConcaternator);
	}

	public void addNewSampleConcaternaterIfItsTime() {
		if (!concaterSpecList.isEmpty() && ! ranges.isRangesEmpty()) {
			SampleConcaternatorSpec s = concaterSpecList.get(0);
			long howLongAfterStart = s.getHowLongAfterStartShouldThisBeAdded();

			/*
			 * if the updateTimestamp is greater than the timestamp for the first update
			 * with data + diff (howLongAfterStart) stated in the SampleConcaternatorSpec
			 */
			if (minmax[1] - howLongAfterStart > tsForFirstUpdateContainingData) {

				logger.debug("Starting a new concater: {}", s);
				concaterSpecList.remove(0);
				addNewConcater(s.getAmoutThatShouldBeConcaternated(), s.getSampleConcaternatorRunDecider());
			}
		}
	}

	protected static SampleConcaternatorSpec getNewSpec(ConcatenationDefinition def) {
		int amountToConcat = def.amountToConcatenate;
		long minSizeOfOldRane = def.width;
		/*
		 * this concater starts whenToBeAdded sec into the test will concat if length of
		 * old range is greater than minSizeOfOldRange
		 */
		return new SampleConcaternatorSpec(minSizeOfOldRane, amountToConcat, (a) -> {
			long minSizeOfOldRange2 = minSizeOfOldRane;
			Range newRange = a.getNewRange();
			Range oldRange = a.getOldRange();
			long endOfTheNewRange = newRange.getEnd();
			long endOfTheOldRange = oldRange.getEnd();

			long diff = endOfTheOldRange - endOfTheNewRange;
			if (diff > minSizeOfOldRange2)
				return true;
			return false;
		});
	}

	SampleConcaternatorRunDecider getFirstConcaterRunDecider(long widthOfUnconcatenatedRange) {
		SampleConcaternatorRunDecider firstConcater = (a) -> {
			Long higest = highestX;
			long startOfOldRange = a.getOldRange().getStart();
			if (higest > startOfOldRange + widthOfUnconcatenatedRange)
				return true;
			return false;
		};
		return firstConcater;
	}

	protected static class ConcatenationDefinition {
		long width;
		int amountToConcatenate;

		public ConcatenationDefinition(long widthOfPreviousRangeBeforeConcatenation, int amountToConcatenate) {
			this.width = widthOfPreviousRangeBeforeConcatenation;
			this.amountToConcatenate = amountToConcatenate;
		}
	}

	public List<SampleConcaternatorSpec> getSampleConcaternatorSpecs() {

		List<SampleConcaternatorSpec> concaterSpecs = new ArrayList<SampleConcaternatorSpec>();
		/*
		 * this concater starts 10 sec into the test will concat if diff from first
		 * range start to highest x value is over 20 sec
		 */
		ConcatenationDefinition firstConcatenationDefinition = new ConcatenationDefinition(30 * SECOND, 4); // 4
//		ConcatenationDefinition firstConcatenationDefinition = new ConcatenationDefinition(2 * MINUTE, 4); // 4
		concaterSpecs.add(new SampleConcaternatorSpec(firstConcatenationDefinition.width,
				firstConcatenationDefinition.amountToConcatenate,
				getFirstConcaterRunDecider(firstConcatenationDefinition.width)));

		concaterSpecs.add(getNewSpec(new ConcatenationDefinition(6000 * SECOND, 4))); // 16
		concaterSpecs.add(getNewSpec(new ConcatenationDefinition(40 * MINUTE, 8))); // 128
		concaterSpecs.add(getNewSpec(new ConcatenationDefinition(5 * HOUR, 8))); // 1024
		concaterSpecs.add(getNewSpec(new ConcatenationDefinition(2 * DAY, 8))); // 8192

		return concaterSpecs;
	}

	@Override
	protected void doUpdate() {
		performUpdate();
	}

	@Override
	public void useData(Map<String, List<TransactionExecutionResult>> transactionsMap) {

		getChart().setNotify(false);
		setIncomingData(transactionsMap);
		long start = System.currentTimeMillis();
		doSafeUpdate();
		long diff = System.currentTimeMillis() - start;
		logger.debug("update time: {}", diff);
		getChart().setNotify(true);
		logger.trace("Total Points in chart: {}", getTotalSize());
	}

}
