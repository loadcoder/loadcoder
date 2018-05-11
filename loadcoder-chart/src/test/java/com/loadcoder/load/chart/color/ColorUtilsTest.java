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
package com.loadcoder.load.chart.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.loadcoder.load.chart.utilities.ColorUtils;
import com.loadcoder.load.testng.TestNGBase;

public class ColorUtilsTest extends TestNGBase{

	@Test
	public void testDistanceCalculation(){
		
		double distanceMAX = ColorUtils.calculateDistance(Color.WHITE, Color.BLACK);
		double distance2 = ColorUtils.calculateDistance(Color.BLACK, Color.WHITE);
		double distance3 = ColorUtils.calculateDistance(Color.WHITE, Color.WHITE);
		double distance4 = ColorUtils.calculateDistance(Color.BLACK, Color.RED);
		double distance5 = ColorUtils.calculateDistance(Color.WHITE, new Color(0, 255, 255));
		double distance6 = ColorUtils.calculateDistance(Color.RED, Color.WHITE);
		
		Assert.assertEquals(distanceMAX, distance2);
		Assert.assertEquals(0.0, distance3);
		Assert.assertEquals(255.0, distance4);
		Assert.assertEquals(255.0, distance5);
		Assert.assertTrue(distanceMAX > distance6);
	}
	
	@Test
	public void findNewColorNoColorsExistingTest(){
		List<Color> colors = Arrays.asList();
		ColorUtils.getNewContrastfulColor(colors);
	}
	
	@Test
	public void getExtremeColorsAsPotentialsTest(){
		
		List<Color> l = Arrays.asList();
		List<Color> potentials = ColorUtils.getExtremeColorsAsPotentials(l);
		Assert.assertEquals(ColorUtils.extremeColors.size(), potentials.size());
	
		l = Arrays.asList(new Color(255,0,255));
		potentials = ColorUtils.getExtremeColorsAsPotentials(l);
		Assert.assertEquals(ColorUtils.extremeColors.size()-1, potentials.size());

	}
	
	@Test
	public void getFirstColorTest(){
		List<Color> colors = new ArrayList<Color>();
		for(int i=0; i<10;i++){
			Color color = ColorUtils.getNewContrastfulColor(colors);
			colors.add(color);
		}
	}
	
	@Test
	public void findNewColorTest(){
		List<Color> colors = Arrays.asList(
				Color.BLACK, 
				Color.WHITE,
				new Color(255, 0,0),
				new Color(255, 255,0),
				new Color(255, 0,255),
				new Color(0, 255,0),
				new Color(0, 255, 255),
				new Color(0, 0, 255));
		ColorUtils.getNewContrastfulColor(colors);
	}
	
	
}
