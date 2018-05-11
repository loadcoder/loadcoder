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
package com.loadcoder.load.chart.common;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.sampling.Group;
import com.loadcoder.load.chart.sampling.SampleConcaternator;
import com.loadcoder.load.chart.utilities.SampleStatics;

public class CommonSampleGroup extends Group {

	private static Logger log = LoggerFactory.getLogger(CommonSampleGroup.class);

	XYSeriesExtension series;

	private boolean internalErrorOccured = false;

	Map<Long, CommonSample> commonSamples = new HashMap<Long, CommonSample>();

	public CommonSampleGroup(XYSeriesExtension series) {
		this.series = series;
	}

	public CommonSample getExistingSample(long ts, long sampleLength) {
		long first = calculateFirstTs(ts, sampleLength);
		CommonSample s = commonSamples.get(first);
		return s;
	}

	public void remove(CommonSample s) {
		commonSamples.remove(s.getFirstTs());
	}

	public void concaternate(SampleConcaternator concater) {

		long start = concater.getOldRange().getStart();
		Range oldRange = concater.getOldRange();
		long oldSampleLength = oldRange.getSampleLength();
		int amountToConcaternate = concater.getAmountToConcaternate();
		long tsIterator = start;

		for (int i = 0; i < amountToConcaternate; i++) {
			CommonSample toBeConcaternated = getExistingSample(tsIterator, oldSampleLength);
			if (toBeConcaternated != null) {
				XYDataItemExtension first = toBeConcaternated.getFirst();

				/*
				 * for some unknown reason, XYDataItemExtension first can in some rare and
				 * special cases be null. The workaround here is to not remove the
				 * XYDataItemExtensions from the series.
				 */
				if (first == null) {
					if (!internalErrorOccured)
						log.warn("An internal problem occured. This has been solved with a workaround."
								+ "If this crashed your test, please report it to the LoadCoder project at http://loadcoder.com");
					internalErrorOccured = true;
				} else {
					series.remove(first.getX());
					if (SampleStatics.USE_TWO_SAMPLE_POINTS) {
						XYDataItemExtension last = toBeConcaternated.getLast();
						series.remove(last.getX());
					}
				}
				remove(toBeConcaternated);
			}
			tsIterator += oldSampleLength;
		}

		Range newRange = concater.getNewRange();
		long newSampleLength = newRange.getSampleLength();
		createCommonSample(start, newSampleLength, 1);

	}

	public CommonSample getAndCreateSample(long ts, Comparable name, long sampleLength) {
		long first = calculateFirstTs(ts, sampleLength);
		CommonSample s = getSample(first, name, sampleLength);
		if (s == null) {
			s = createCommonSample(first, sampleLength, 2);
		}
		return s;
	}

	public CommonSample getSample(long first, Comparable name, long sampleLength) {
		CommonSample s = fetch(first, sampleLength);
		return s;
	}

	protected CommonSample fetch(long firstTs, long sampleLength) {
		CommonSample s = commonSamples.get(firstTs);
		return s;
	}

	private CommonSample createCommonSample(long first, long sampleLength, int created) {
		CommonSample s = new CommonSample(first, sampleLength, created);
		commonSamples.put(first, s);
		return s;
	}
}
