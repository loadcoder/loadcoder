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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.sampling.Sample;

public class ChartUtils {

	public static void populateSeriesWithSamples(Sample sample, XYSeriesExtension series) {
		if (sample.getFirst() == null) {

			// create the points
			sample.initDataItems();
			series.add(sample.getFirst(), false);
			if (SampleStatics.USE_TWO_SAMPLE_POINTS) {
				series.add(sample.getLast(), false);
			}
		} else {
			sample.updateDataItems();
		}
	}

	public static void populateSeriesWithPoints(List<Point> points, XYSeriesExtension series, double keepFactor) {
		Map<Long, List<Point>> responseTimePointsMap = new HashMap<Long, List<Point>>();
		for (int i = 0; i < points.size(); i++) {
			Point point = points.get(i);
			List<Point> pointsForResponseTime = responseTimePointsMap.get(point.getY());
			if (pointsForResponseTime == null) {
				pointsForResponseTime = new ArrayList<Point>();
				responseTimePointsMap.put(point.getY(), pointsForResponseTime);
			}
			pointsForResponseTime.add(point);
		}
		int step = calculateStepping(keepFactor);

		// all responsetimes ordered by the size of the responsetime group
		List<Long> responeTimeGroup = responseTimePointsMap.entrySet().stream().map((a) -> {
			return a.getKey();
		}).collect(Collectors.toList());
		responeTimeGroup.sort((a, b) -> {
			int diff = responseTimePointsMap.get(a).size() - responseTimePointsMap.get(b).size();
			return diff;
		});

		int rest = 0;
		for (Long rts : responeTimeGroup) {
			List<Point> p = responseTimePointsMap.get(rts);
			int pSize = p.size();

			int start = rest;
			if (start > pSize) {
				rest = rest - pSize;
				start = 0;
			}

			for (int i = start; i < pSize; i = i + step) {
				Point point = p.get(i);
				if (!point.isEnabled())
					continue;
				long x = point.getX();
				long y = point.getY();
				series.add(x, y, false);

				start = 0;

				if (i + step > pSize) {
					rest = i + step - p.size();
					continue;
				}
			}
		}
	}

	/**
	 * calculates and returns the amount of points to skip. factorToKeep: 0.1 gives
	 * 10, which means that 10 points will be skipped when one is added
	 * 
	 * @param factorToKeep
	 *            shall be a value between 0 and 1.
	 * @return an integer equal to the amount of points to skip in order to have the
	 *         desired factor of points to keep
	 */
	protected static int calculateStepping(double factorToKeep) {
		double step = 1 / factorToKeep;
		if (step < 1)
			step = 1;
		return (int) step;
	}

	public static double calculateKeepFactor(int totalAmountOfPoints, int targetItemsInChart) {
		if (totalAmountOfPoints <= targetItemsInChart) {
			return 1;
		}

		// 200, 100: 0.5
		double keepFactor = (double) targetItemsInChart / (double) totalAmountOfPoints;

		int multiplier = 1;
		while (keepFactor < 1) {
			multiplier = multiplier * 10;
			keepFactor = keepFactor * 10;
		}
		long roundedMultiplied = Math.round(keepFactor);
		double rounded = (double) roundedMultiplied / multiplier;
		if (rounded < 0.0001) {
			rounded = 0.0001;
		}

		return rounded;
	}

	public static int calculateSampleLengthSliderMax(long initialSampleLength) {
		int result = 0;
		long maxMillis = (long) ((double) initialSampleLength * 2);
		int amountOf10Sec = (int) (maxMillis / 10_000);

		long diff = (amountOf10Sec + 1) * 10_000 - maxMillis;

		if (diff > 2_000) {
			result = (amountOf10Sec + 1) * 10;
		} else {
			result = (amountOf10Sec + 2) * 10;
			;
		}

		return result;
	}
}
