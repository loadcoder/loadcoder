package com.loadcoder.load.chart.data;

import java.util.ArrayList;
import java.util.List;

public class Ranges {

	private final List<Range> ranges = new ArrayList<Range>();

	public void clearRanges() {
		ranges.clear();
	}

	public void addRange(Range range) {
		ranges.add(range);
	}

	public Range getRange(int index) {
		return ranges.get(index);
	}

	public Range getLastRange() {
		return ranges.get(ranges.size() - 1);
	}

	public boolean isRangesEmpty() {
		return ranges.isEmpty();
	}

	// only used for test
	public Range lookupCorrectRange(long ts) {
		for (Range range : ranges) {
			if (range.isTimestampInThisRange(ts))
				return range;
		}
		return null;
	}

	public long findSampleLength(long timeStamp) {
		for (Range range : ranges) {
			if (range.getStart() <= timeStamp && range.getEnd() >= timeStamp) {
				return range.getSampleLength();
			}
		}
		throw new RuntimeException("no matching range found. This should never happend!");
	}
}
