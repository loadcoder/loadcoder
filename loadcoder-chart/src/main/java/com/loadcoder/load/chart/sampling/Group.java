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
package com.loadcoder.load.chart.sampling;

import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;

public class Group {

	protected XYSeriesExtension series;

	public void setSeries(XYSeriesExtension series) {
		this.series = series;
	}

	public XYSeriesExtension getSeries() {
		return series;
	}

	public static long calculateFirstTs(long ts,  long sampleLengthToUse) {
		long rest = ts % sampleLengthToUse;
		long indexWithoutRest = (ts - (rest * 2) / 2);
		long first;
		if (ts < 0) {
			first = indexWithoutRest - (rest == 0 ? 0 : sampleLengthToUse);
		} else {
			first = indexWithoutRest;
		}
		return first;
	}
}
