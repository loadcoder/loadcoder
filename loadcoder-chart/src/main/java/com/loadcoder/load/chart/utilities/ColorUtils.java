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
package com.loadcoder.load.chart.utilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ColorUtils {
	
	public static final List<Color> extremeColors = Arrays.asList(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, new Color(255,255,0), new Color(255, 0, 255), new Color(0, 255, 255), Color.WHITE);

	public static final List<Color> defaultBlacklistColors = Arrays.asList(Color.YELLOW, Color.WHITE);
	
	public static Color getNewContrastfulColor(List<Color> alreadyExistingColors){
		return getNewContrastfulColor(alreadyExistingColors, new ArrayList<Color>());
	}
	
	public static Color getNewContrastfulColor(List<Color> alreadyExistingColors, List<Color> blacklistedColors){
		
		List<Color> colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible = new ArrayList<>();
		colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible.addAll(alreadyExistingColors);
		colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible.addAll(blacklistedColors);
		List<Color> newColors = getNewPotentialColors(colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible);
		
		Color newColor = findColorWithLongestDistanceToClosestNeighbour(newColors, colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible);
		return newColor;
	}

	protected static Color findColorWithLongestDistanceToClosestNeighbour(List<Color> newColors, List<Color> existingColors){
		
		Color result = null;
		double distanceOfTheClosestNeighbourOfAllColors = 0;
		for(int i=0; i<newColors.size(); i++){
			Color potentialColor = newColors.get(i);
			double distanceToTheClosestNeighbourForThisColor=500;
			
			for(int j= 0; j<existingColors.size();j++){
				Color toCompareWith = existingColors.get(j);
				double distance = calculateDistance(potentialColor, toCompareWith);
				if(distance < distanceToTheClosestNeighbourForThisColor){
					distanceToTheClosestNeighbourForThisColor = distance;
				}
			}
			
			if(distanceToTheClosestNeighbourForThisColor > distanceOfTheClosestNeighbourOfAllColors){
				distanceOfTheClosestNeighbourOfAllColors = distanceToTheClosestNeighbourForThisColor;
				result = potentialColor;
			}
		}
		return result;
	}
	
	public static List<Color> getExtremeColorsAsPotentials(List<Color> alreadyExistingColors){
		List<Color> result = new ArrayList<Color>();
		for(Color extremeColor : extremeColors){
			if(!alreadyExistingColors.contains(extremeColor)){
				result.add(extremeColor);
			}
		}
		return result;
	}
	
	protected static List<Color> getNewPotentialColors(List<Color> colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible){
		
		List<Color> result = getExtremeColorsAsPotentials(colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible);
		
		for(int i = 0; i<colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible.size(); i++){
			Color toBeCompared = colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible.get(i);
			for(int j = i+1; j<colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible.size(); j++){
				Color toMixWith = colorsThatShouldBeAsFarAwayAsTheNewColorAsPossible.get(j);
				Color mixed = mixColors(toBeCompared, toMixWith);
				result.add(mixed);
			}
		}
		return result;
	}
	
	public static Color mixColors(Color c1, Color c2){
		int r = (c1.getRed() + c2.getRed()) / 2;
		int g = (c1.getGreen() + c2.getGreen()) / 2;
		int b = (c1.getBlue() + c2.getBlue()) / 2;
		
		Color c = new Color(r, g, b);
		return c;
	}
	
	public static double calculateDistance(Color c1, Color c2){
		int r1 = c1.getRed();
		int r2 = c2.getRed();

		int g1 = c1.getGreen();
		int g2 = c2.getGreen();

		int b1 = c1.getBlue();
		int b2 = c2.getBlue();

		/*
		 * The 3D Pythagorean Theorem => 
		 * s2 = a^2 + b^2 + c^2 =>
		 * s = sqrt(a^2 + b^2 + c^2)
		 */
		double distance = Math.sqrt(
				Math.pow(r1 - r2, 2) +
				Math.pow(g1 - g2, 2) +
				Math.pow(b1 - b2, 2)
				);
		return distance;
	}
}
