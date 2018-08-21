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

import java.awt.Color;

import org.testng.Assert;
import org.testng.annotations.Test;

public class XYDataItemExtensionTest {

	
	/**
	 * This test verifies that when adding a XYDataItemExtension to as series, it really is
	 * that specific item that is added, and not a clone.
	 * JFreeChart's XYDataItem are clone the item that is trying to be added. However, loadcoder
	 * needs the reference to that specific item, since we want to update already added items.
	 */
	@Test
	public void addToSeriesTest() {
		XYSeriesExtension series = new XYSeriesExtension("s", false, true, Color.BLACK);
		XYDataItemExtension addedItem = new XYDataItemExtension(0, 0);
		series.add(addedItem);
		addedItem.setY(1); // change added
		XYDataItemExtension itemInSeries = (XYDataItemExtension)series.getItems().get(0);
		Assert.assertEquals(itemInSeries, addedItem);
		Assert.assertEquals(itemInSeries.getYValue(), 1.0D);
	}
	
}
