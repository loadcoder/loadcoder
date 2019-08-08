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
package com.loadcoder.load.chart.jfreechart;

import java.io.Serializable;

import org.jfree.data.xy.XYDataItem;

public class XYDataItemExtension extends XYDataItem implements Cloneable, Comparable, Serializable {

	private static final long serialVersionUID = 1L;

	public XYDataItemExtension(double x, double y) {
		super(x, y);
	}

	/* 
	 * cloning is disabled for this class with following implementation of clone method.
	 * Without this, the common serieses won't be drawn correctly. For example, 
	 * the common series won't show correctly in a RuntimeChart
	 * 
	 */
	public Object clone() {

		/*
		 * jfreechart uses clone when adding new XYDataItem's
		 * (see XYSeries:add) which makes the reference unusable when update is
		 * done which will have no effect for the chart.
		 * 
		 * This is risky business though. It may introduce future bugs so keep
		 * this in mind. There are less good ways of achieve the reference
		 * keeping.
		 */
		return this;
	}

	public String toString() {
		return "[" + getXValue() + ", " + getYValue() + "]";
	}
}
