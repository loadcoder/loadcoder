/*******************************************************************************
 * Copyright (C) 2019 Stefan Vahlgren at Loadcoder
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
package com.loadcoder.load;

import java.util.Random;

public class GeneralUtils {

	private static final Random r = new Random();

	/**
	 * Returns a random double between min and max
	 * 
	 * @param min is the minimum possible value that will be returned
	 * @param max is the maximum possible value that will be returned
	 * @return a random double value between min and max
	 */
	public static double randomDouble(double min, double max) {
		double randomValue = min + (max - min) * r.nextDouble();
		return randomValue;
	}
}
