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
package com.loadcoder.load.chart.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.loadcoder.load.chart.common.YCalculator;
import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.utilities.SampleStatics;

public class SampleGroup extends Group{

	boolean locked = true;

	long startOfTheGroup = 0;
	
	long sampleLengthBase = 0;

	Sample earliestSample;
	
	private Map<Long, Sample> samples = new HashMap<Long, Sample>();

	private Map<Long, Sample> samplesUnupdated = new HashMap<Long, Sample>();

	YCalculator yCalculatorToUse = YCalculator.avg;

	public SampleGroup(long sampleLength, XYSeriesExtension series, boolean locked) {
		this.locked = locked;
		this.sampleLengthBase = sampleLength;
		this.series = series;
	}
	
	public String toString() {
		return String.format("{samples:%s, samplesUnupdated:%s}", samples.size(), samplesUnupdated.size());
	}

	public Sample getEarliestSample() {
		return earliestSample;
	}

	protected Map<Long, Sample> getSamples() {
		return samples;
	}

	public Map<Long, Sample> getSamplesUnupdated() {
		return samplesUnupdated;
	}

	public void remove(Sample s) {
		samples.remove(s.getFirstTs());
	}
	
	public static class ConcaternationResult{
		List<Sample> concaternated;
		Sample newSample;
		ConcaternationResult(List<Sample> concaternated, Sample newSample){
			this.concaternated = concaternated;
			this.newSample = newSample;
		}
		
		public List<Sample> getConcatenated(){
			return concaternated;
		}
		
		public void fixPointsForSeries(XYSeriesExtension series){
			
			for(Sample toBeConcaternated : concaternated){
				XYDataItemExtension first = toBeConcaternated.getFirst();

				if(first != null){
					series.remove(first.getX());
				}
				if(SampleStatics.USE_TWO_SAMPLE_POINTS){
					XYDataItemExtension last = toBeConcaternated.getLast();
					if(last != null){
						series.remove(last.getX());
					}
				}
			}	
			
			if (!newSample.isEmpty()) {
				newSample.initDataItems();
				XYDataItemExtension first = newSample.getFirst();
				series.add(first, false, true);
				if(SampleStatics.USE_TWO_SAMPLE_POINTS) {
					XYDataItemExtension last = newSample.getLast();
					series.add(last, false, true);
				}
			}
		}
	}
	
	public ConcaternationResult concaternate(SampleConcaternator concater) {

		long start = concater.getOldRange().getStart();
		Range oldRange = concater.getOldRange();
		long oldSampleLength = oldRange.getSampleLength();
		int amountToConcaternate = concater.getAmountToConcaternate();
		
		long tsIterator = start;
		List<Point> newPoints = new ArrayList<Point>();
		int newAmountOfFails = 0;
		long pointsSum =0;
		long pointsAmount =0;
		List<Sample> concaternated = new ArrayList<Sample>();
		for (int i = 0; i < amountToConcaternate; i++) {

			Sample toBeConcaternated = getExistingSample(tsIterator, oldSampleLength);
			if (toBeConcaternated != null) {
				concaternated.add(toBeConcaternated);
				newAmountOfFails += toBeConcaternated.getAmountOfFails();
				remove(toBeConcaternated);

				pointsSum = pointsSum + toBeConcaternated.getPointsSum();
				pointsAmount = pointsAmount + toBeConcaternated.getAmountOfPoints();
				newPoints.addAll(toBeConcaternated.scrapThisSampleAndGetPoints());
			}
			tsIterator += oldSampleLength;
		}

		Range newRange = concater.getNewRange();
		long newSampleLength = newRange.getSampleLength();

		Sample newSample = createSample(start, "", newSampleLength);
		newSample.setFails(newAmountOfFails);
		newSample.setPoints(newPoints);
		newSample.setPointsSum(pointsSum);
		newSample.setPointsAmount(pointsAmount);
		
		if (! newSample.isEmpty()) {
			newSample.calculateY(yCalculatorToUse);
		}
		ConcaternationResult concaternationResult = new ConcaternationResult(concaternated, newSample); 
		return concaternationResult;

	}

	private Sample createSample(long first, String name) {
		return createSample(first, name, sampleLengthBase);
	}

	private Sample createSample(long first, String name, long sampleLength) {

		Sample s = new Sample(first, sampleLength, name, locked);
		samples.put(first, s);

		long previousFirst = first - sampleLength;
		Sample previousSample = samples.get(previousFirst);
		if (previousSample != null)
			s.setPreviousSample(previousSample);

		long nextFirst = first + sampleLength;
		Sample nextSample = samples.get(nextFirst);
		if (nextSample != null)
			nextSample.setPreviousSample(s);
		return s;
	}

	public Sample getAndCreateSample_old(long ts, String name, long sampleLength) {
		long first = calculateFirstTs(ts, sampleLength);
		Sample s = samples.get(first);
		if (s == null) {
			s = createSample(first, name);
		}
		return s;
	}
	
	public Sample getAndCreateSample(long ts, String name, long sampleLength) {
		long first = calculateFirstTs(ts, sampleLength);
		Sample s = samples.get(first);
		if (s == null) {
			s = createSample(first, name);
		}
		return s;
	}

	public Sample getExistingSample(long ts, long sampleLength) {
		long first = calculateFirstTs(ts, sampleLength);
		Sample s = samples.get(first);
		return s;
	}
	
	public Sample getExistingAndRemoveSample(long ts, long sampleLength) {
		long first = calculateFirstTs(ts, sampleLength);
		Sample s = samples.remove(first);
		return s;
	}
	
}
