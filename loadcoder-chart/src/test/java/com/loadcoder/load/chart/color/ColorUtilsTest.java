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
package com.loadcoder.load.chart.color;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.utilities.ColorUtils;
import com.loadcoder.load.testng.TestNGBase;

public class ColorUtilsTest extends TestNGBase {

	Logger log = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testBlackListCloseToExtreme() {

		Color extremeColorToTheBlacklisted = new Color(0, 0, 0);
		Color colorToBeBlacklisted = new Color(1, 0, 0);

		// empty blacklist
		List<Color> colors = new ArrayList<Color>();
		List<Color> blacklisted = Arrays.asList(colorToBeBlacklisted);
		Set<Color> set = new HashSet<Color>(colors);
		ColorUtils.getNewContrastfulColor(set, blacklisted);

		boolean whiteExists = colors.stream().anyMatch((color) -> {
			return color.equals(extremeColorToTheBlacklisted);
		});
		assertFalse(whiteExists);
	}

	@Test
	public void testFirstColorsNotWhiteIfBlackList() {

		Color extremeColorToBeBlacklisted = Color.WHITE;

		// empty blacklist
		List<Color> colors = new ArrayList<Color>();
		List<Color> blacklisted = Arrays.asList();
		for (int i = 0; i < 10; i++) {
			Set<Color> set = new HashSet<Color>(colors);
			Color c = ColorUtils.getNewContrastfulColor(set, blacklisted);
			colors.add(c);
		}
		boolean whiteExists = colors.stream().anyMatch((color) -> {
			return color.equals(extremeColorToBeBlacklisted);
		});
		assertTrue(whiteExists);

		// now blacklist white
		colors = new ArrayList<Color>();
		blacklisted = Arrays.asList(extremeColorToBeBlacklisted);
		for (int i = 0; i < 10; i++) {
			Set<Color> set = new HashSet<Color>(colors);
			Color c = ColorUtils.getNewContrastfulColor(set, blacklisted);
			colors.add(c);
		}
		whiteExists = colors.stream().anyMatch((color) -> {
			return color.equals(extremeColorToBeBlacklisted);
		});
		assertFalse(whiteExists);
	}

	@Test
	public void testBlacklisting() {
		List<Color> colors = Arrays.asList();
		List<Color> blacklisted = Arrays.asList();

		Set<Color> set = new HashSet<Color>(colors);
		Color c = ColorUtils.getNewContrastfulColor(set, blacklisted);
		blacklisted = Arrays.asList(c);

		set = new HashSet<Color>(colors);
		Color c2 = ColorUtils.getNewContrastfulColor(set, blacklisted);
		assertNotSame(c, c2);
	}

	@Test
	public void testNewColor() {
		List<Color> colors = Arrays.asList();
		List<Color> blacklisted = Arrays.asList();
		Set<Color> set = new HashSet<Color>(colors);
		Color c = ColorUtils.getNewContrastfulColor(set, blacklisted);
		set = new HashSet<Color>(colors);
		Color c2 = ColorUtils.getNewContrastfulColor(set, blacklisted);
		assertSame(c, c2);

		colors = Arrays.asList(c2);
		set = new HashSet<Color>(colors);
		Color c3 = ColorUtils.getNewContrastfulColor(set, blacklisted);
		assertNotSame(c, c3);
	}

	/**
	 * The color should be possible to print. A thrown exception will fail this test
	 */
	@Test
	public void printFirstColors() {

		List<Color> colors = new ArrayList<Color>();
		for (int i = 0; i < 10; i++) {
			Set<Color> set = new HashSet<Color>(colors);
			Color c3 = ColorUtils.getNewContrastfulColor(set, ColorUtils.defaultBlacklistColors);
			colors.add(c3);
			log.info(c3.toString());
		}
	}

	@Test
	public void testDistanceCalculation() {

		double distanceMAX = ColorUtils.calculateDistance(Color.WHITE, Color.BLACK);
		double distance2 = ColorUtils.calculateDistance(Color.BLACK, Color.WHITE);
		double distance3 = ColorUtils.calculateDistance(Color.WHITE, Color.WHITE);
		double distance4 = ColorUtils.calculateDistance(Color.BLACK, Color.RED);
		double distance5 = ColorUtils.calculateDistance(Color.WHITE, new Color(0, 255, 255));
		double distance6 = ColorUtils.calculateDistance(Color.RED, Color.WHITE);

		assertEquals(distanceMAX, distance2);
		assertEquals(0.0, distance3);
		assertEquals(255.0, distance4);
		assertEquals(255.0, distance5);
		assertTrue(distanceMAX > distance6);
	}

	@Test
	public void findNewColorNoColorsExistingTest() {
		List<Color> colors = Arrays.asList();
		List<Color> blacklisted = Arrays.asList();
		Set<Color> set = new HashSet<Color>(colors);
		ColorUtils.getNewContrastfulColor(set, blacklisted);
		assertEquals(colors, blacklisted);
	}

	@Test
	public void getExtremeColorsAsPotentialsTest() {

		List<Color> l = Arrays.asList();
		List<Color> potentials = ColorUtils.getExtremeColorsAsPotentials(l);
		assertEquals(ColorUtils.extremeColors.size(), potentials.size());

		l = Arrays.asList(new Color(255, 0, 255));
		potentials = ColorUtils.getExtremeColorsAsPotentials(l);
		assertEquals(ColorUtils.extremeColors.size() - 1, potentials.size());

	}

	@Test
	public void getFirstColorsTest() {
		List<Color> colors = new ArrayList<Color>();
		List<Color> blacklisted = Arrays.asList();
		for (int i = 0; i < 10; i++) {
			Set<Color> set = new HashSet<Color>(colors);
			Color color = ColorUtils.getNewContrastfulColor(set, blacklisted);
			colors.add(color);
		}
		assertTrue(colors.contains(Color.RED));
		assertTrue(colors.contains(Color.GREEN));
		assertTrue(colors.contains(Color.BLUE));

	}

	@Test
	public void findNewColorTest() {
		List<Color> blacklisted = Arrays.asList();
		List<Color> colorList = Arrays.asList(Color.BLACK, Color.WHITE, new Color(255, 0, 0), new Color(255, 255, 0),
				new Color(255, 0, 255), new Color(0, 255, 0), new Color(0, 255, 255), new Color(0, 0, 255));

		Set<Color> set = new HashSet<Color>(colorList);
		Color newColor = ColorUtils.getNewContrastfulColor(set, blacklisted);

		assertFalse(set.contains(newColor));

	}

}
