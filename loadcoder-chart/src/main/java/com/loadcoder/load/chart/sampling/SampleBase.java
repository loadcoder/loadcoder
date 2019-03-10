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
package com.loadcoder.load.chart.sampling;

import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;

public class SampleBase {
	public int created;
	
	protected XYDataItemExtension first;

	protected XYDataItemExtension last;
	
	protected long length;
	
	protected long firstTs;
	
	protected long lastTs;
	
	protected long y = -1;
	
	SampleBase previousSample;
	
	public long getLength() {
		return length;
	}
	
	public void updateDataItems(){
		first.setY(y);
		last.setY(y);
	}
	
	public long getFirstTs() {
		return firstTs;
	}

	public long getLastTs() {
		return lastTs;
	}
	
	public long getY() {
		return y;
	}

	public SampleBase getPreviousSample() {
		return previousSample;
	}

	public void setPreviousSample(SampleBase previousSample){
		this.previousSample = previousSample;
				
	}
	
	public void initDataItems(){
		first = new XYDataItemExtension(firstTs, y);
		last = new XYDataItemExtension(lastTs, y);
	}
	
	public XYDataItemExtension getFirst() {
		return first;
	}

	@Deprecated
	public XYDataItemExtension getLast() {
		return last;
	}
}
