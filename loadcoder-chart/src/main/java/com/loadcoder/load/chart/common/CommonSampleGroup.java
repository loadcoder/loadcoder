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
package com.loadcoder.load.chart.common;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;
import com.loadcoder.load.chart.sampling.Group;
import com.loadcoder.load.chart.sampling.SampleConcaternator;

public class CommonSampleGroup extends Group {

	Logger log = LoggerFactory.getLogger(ChartLogic.class);
	
	XYSeriesExtension series;

	Map<Long, CommonSample> commonSamples = new HashMap<Long, CommonSample>();

	public CommonSampleGroup(XYSeriesExtension series) {
		this.series = series;
	}

	public void removeAllOldSamples(long start, int amountToConcaternate, long oldSampleLength) {
		long tsIterator = start;

		for (int i = 0; i < amountToConcaternate; i++) {
			long firstTs = calculateFirstTs(tsIterator, oldSampleLength);
			CommonSample toBeConcaternated = commonSamples.get(firstTs);

			if (toBeConcaternated != null) {
				XYDataItemExtension first = toBeConcaternated.getFirst();
				/*
				 * for some unknown reason, XYDataItemExtension first can in some rare and
				 * special cases be null. The workaround here is to not remove the
				 * XYDataItemExtensions from the series.
				 */
				if (first != null) {
					series.remove(first.getX());
					if(series.getKey().contains("Throughput")) {
						log.trace("removing Throughput at x:{} y:{}", first.getX().intValue(), first.getY().intValue());
					}
				}
				commonSamples.remove(toBeConcaternated.getFirstTs());
			}
			tsIterator += oldSampleLength;
		}
	}

	public void concaternate(SampleConcaternator concater) {

		Range oldRange = concater.getOldRange();
		long start = oldRange.getStart();

		long oldSampleLength = oldRange.getSampleLength();
		int amountToConcaternate = concater.getAmountToConcaternate();

		removeAllOldSamples(start, amountToConcaternate, oldSampleLength);

		Range newRange = concater.getNewRange();
		long newSampleLength = newRange.getSampleLength();
		createCommonSampleAndPutInMap(start, newSampleLength);
	}

	public void putCommonSample(Long key, CommonSample commonSample) {
		commonSamples.put(key, commonSample);
	}

	public CommonSample getAndCreateSampleAndPutInMap(long ts, long sampleLength) {
		long first = calculateFirstTs(ts, sampleLength);
		CommonSample s = getSample(first, sampleLength);
		if (s == null) {
			s = createCommonSampleAndPutInMap(first, sampleLength);
		}
		return s;
	}

	public CommonSample getSample(long first, long sampleLength) {
		CommonSample s = fetch(first, sampleLength);
		return s;
	}

	protected CommonSample fetch(long firstTs, long sampleLength) {
		CommonSample s = commonSamples.get(firstTs);
		return s;
	}

	private CommonSample createCommonSampleAndPutInMap(long first, long sampleLength) {
		CommonSample s = new CommonSample(first, sampleLength);
		commonSamples.put(first, s);
		return s;
	}
}
