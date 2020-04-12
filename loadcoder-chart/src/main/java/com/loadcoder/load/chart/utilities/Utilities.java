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
package com.loadcoder.load.chart.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.loadcoder.load.chart.data.DataSet;
import com.loadcoder.load.chart.data.Point;
import com.loadcoder.result.TransactionExecutionResult;

public class Utilities {

	public static long[] findMinMaxTimestamp(Map<String, List<TransactionExecutionResult>> resultLists,
			List<String> keys) {
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;

		for (String key : keys) {
			List<TransactionExecutionResult> resultList = resultLists.get(key);
			if (resultList == null) {
				continue;
			}

			for (TransactionExecutionResult result : resultList) {
				if (result.getTs() < min) {
					min = result.getTs();
				}
				if (result.getTs() > max)
					max = result.getTs();
			}
		}
		return new long[] { min, max };
	}

	public static List<DataSet> convert(Map<String, List<TransactionExecutionResult>> resultLists, long earliestTs,
			boolean convertToRelativeTime, List<String> keys) {
		List<DataSet> dataSets = new ArrayList<DataSet>();
		if (resultLists.isEmpty())
			return dataSets;
		for (String key : keys) {
			List<TransactionExecutionResult> resultList = resultLists.get(key);
			if (resultList == null) {
				continue;
			}
			DataSet dataSet = new DataSet(key, new ArrayList<Point>());
			dataSets.add(dataSet);
			for (TransactionExecutionResult result : resultList) {
				if (convertToRelativeTime) {
					dataSet.getPoints()
							.add(new Point(result.getTs() - earliestTs, result.getValue(), result.isStatus()));
				} else {
					dataSet.getPoints().add(new Point(result.getTs(), result.getValue(), result.isStatus()));
				}
			}
		}
		return dataSets;
	}
}
