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
package com.loadcoder.load.chart.utilities;

import java.util.List;

import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.jfreechart.ItemSeriesAdder;

public class ChartUtils {

	public final static ItemSeriesAdder itemSeriesAdderForSamples = (series, sample) -> {
		if (sample.getFirst() == null) {

			// create the points
			sample.initDataItems();
			series.add(sample.getFirst(), false);
			if(SampleStatics.USE_TWO_SAMPLE_POINTS) series.add(sample.getLast(), false);
		}else{
			sample.updateDataItems();
		}
	};

	public static final ItemSeriesAdder itemSeriesAdderForDots = (series, sample) -> {
		List<Point> points = sample.getPoints();
		for (Point point : points) {
			if (!point.isEnabled())
				continue;
			long x = point.getX();
			long y = point.getY();
			series.add(x, y, false);
		}
	};

	public static int calculateSampleLengthSliderMax(long initialSampleLength) {
		int result = 0;
		long maxMillis = (long) ((double) initialSampleLength * 2);
		int amountOf10Sec = (int)(maxMillis / 10_000);
		
		long diff = (amountOf10Sec + 1) * 10_000 - maxMillis;
		
		if(diff >2_000){
			result = (amountOf10Sec +1) *10;
		}else{
			result = (amountOf10Sec +2) *10;;
		}
			
		return result;
	}
}
