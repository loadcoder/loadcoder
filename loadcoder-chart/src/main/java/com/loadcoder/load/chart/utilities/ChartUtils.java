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

		// all responsetimes ordered by the size of the responsetime group
		List<Long> responeTimeGroup = responseTimePointsMap.entrySet().stream().map((a) -> {
			return a.getKey();
		}).collect(Collectors.toList());
		responeTimeGroup.sort((a, b) -> {
			int diff = responseTimePointsMap.get(a).size() - responseTimePointsMap.get(b).size();
			return diff;
		});

		/*
		 * the for loop below can potentially be faster. if many point are to be
		 * processed, and the keepFactor is reached early, we could possibly iterate
		 * more than one step at a time, which will automatically skip points. This
		 * should only be done if target keepFactor is low
		 */
		int added = 0;
		int processed = 0;
		for (Long rts : responeTimeGroup) {
			List<Point> p = responseTimePointsMap.get(rts);
			int pSize = p.size();

			for (int i = 0; i < pSize; i++) {
				processed++;
				Point point = p.get(i);
				if (!point.isEnabled())
					continue;
				if (pSize == 1) {
					long x = point.getX();
					long y = point.getY();
					series.add(x, y, false);
					added++;
					continue;
				}

				double factorIfAdded = (double) (added + 1) / processed;
				double factorIfNotAdded = (double) (added) / processed;

				double distanceIfAdded = keepFactor - factorIfAdded;
				double distanceIfNotAdded = keepFactor - factorIfNotAdded;

				if (distanceIfAdded < 0) {
					distanceIfAdded = distanceIfAdded * -1;
				}
				if (distanceIfNotAdded < 0) {
					distanceIfNotAdded = distanceIfNotAdded * -1;
				}

				if (distanceIfAdded < distanceIfNotAdded) {
					long x = point.getX();
					long y = point.getY();
					series.add(x, y, false);
					added++;
				}
			}
		}
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

}
