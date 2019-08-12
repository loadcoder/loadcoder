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
package com.loadcoder.load.chart.data;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;

public class Range {

	Logger log = LoggerFactory.getLogger(Range.class);
	private XYSeriesExtension series;
	private XYDataItemExtension high;
	private XYDataItemExtension low;

	private long start;

	private long end;

	private long sampleLength;

	public Range(long start, long end, long sampleLength) {
		super();
		this.start = start;
		this.end = end;
		this.sampleLength = sampleLength;
		log.trace("created Range {}", this);
	}

	public boolean isTimestampInThisRange(long ts) {
		if (ts >= start && ts <= end)
			return true;
		return false;
	}

	public String toString() {
		return "{" + start + " - " + end + "sampleLength:" + sampleLength + "}";
	}

	public void setStart(long start) {
		this.start = start;
		log.trace("Range start was set {}", this);
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setSampleLength(long sampleLength) {
		this.sampleLength = sampleLength;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public long getSampleLength() {
		return sampleLength;
	}

	public XYSeriesExtension getSeries() {
		return series;
	}

	public void setSeries(XYSeriesExtension series) {
		this.series = series;
	}

	public XYDataItemExtension getHigh() {
		return high;
	}

	public void setHigh(XYDataItemExtension high) {
		this.high = high;
	}

	public XYDataItemExtension getLow() {
		return low;
	}

	public void setLow(XYDataItemExtension low) {
		this.low = low;
	}

}
