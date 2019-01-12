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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.common.YCalculator;
import com.loadcoder.load.chart.data.Point;

public class SampleTest {

	@Test
	public void calculateYTest(){
		Sample s = new Sample(0, 1000, "Hello");
		s.addPoint(new Point(0, 0, true));
		s.addPoint(new Point(0, 10, true));
		s.calculateY(YCalculator.avg);
		long y = s.getY();
		Assert.assertEquals(5, y); //10/2=5
		
		s.addPoint(new Point(0, 1, true));
		s.calculateY(YCalculator.avg);
		y = s.getY();
		Assert.assertEquals(4, y); //11/3 = 3.67
		
		s.addPoint(new Point(0, 1, true));
		s.calculateY(YCalculator.avg);
		y = s.getY();
		Assert.assertEquals(3, y); //13/4 = 3.25
		
	}
}
