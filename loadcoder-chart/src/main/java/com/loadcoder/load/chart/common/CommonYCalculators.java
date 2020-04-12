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

import java.util.HashSet;
import java.util.Set;

import com.loadcoder.load.chart.sampling.Sample;
import com.loadcoder.load.chart.sampling.SampleGroup;

public class CommonYCalculators {

	public static final CommonYCalculator THROUGHPUT = (keys, timestamp, sampleGroups, sampleLength) -> {
		double amount = 0;

		for(String key : keys){
			SampleGroup group = sampleGroups.get(key);
			Sample s = group.getExistingSample(timestamp, sampleLength);

			/*
			 * it is likely that the sample doesn't exist for some of the
			 * series. Then just continue with next one
			 */
			amount += s == null ? 0 : s.getAmountOfPoints();
		}
		
		double pointsPerSecond = amount / (sampleLength / 1000);
		return pointsPerSecond;
	};
	
	public static final CommonYCalculator FAILS = (keys, timestamp, sampleGroups, sampleLength) -> {
		double amount = 0;

		for(String key : keys){
			SampleGroup group = sampleGroups.get(key);

			Sample s = group.getExistingSample(timestamp, sampleLength);

			/*
			 * it is likely that the sample doesn't exist for some of the
			 * series. Then just continue with next one
			 */
			if (s == null)
				continue;

			int amountOfPoints = s.getAmountOfFails();
			amount += amountOfPoints;
		}

		//20 fails over 10 sec: 20 / (10_000 / 1000) = 20 / 10 = 2
		//3 fails over 2 sec: 3 / (3000 / 1000) = 3 / 2 = 1.5
		amount = amount / (sampleLength / 1000);
		return amount;
	};
}
