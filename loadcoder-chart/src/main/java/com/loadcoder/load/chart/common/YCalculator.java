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
package com.loadcoder.load.chart.common;

import java.util.List;

import com.loadcoder.load.chart.data.Point;

public abstract class YCalculator {
	
	public abstract String getName();

	public abstract double calculateY(List<Point> points);

	public static final YCalculator avg = new YCalculator() {

		public double calculateY(List<Point> points) {
			double y = 0;
			int amountUsed = 0;
			for (Point point : points) {
				if (!point.isEnabled())
					continue;
				y += point.getY();
				amountUsed++;
			}
			if (amountUsed == 0)
				return -1;
			y = y / amountUsed;
			return y;
		}

		@Override
		public String getName() {
			return "Average";
		}
	};
	
	public static final YCalculator max = new YCalculator() {

		public double calculateY(List<Point> points) {
			long max = -1;

			for (Point point : points) {
				if (!point.isEnabled())
					continue;
				if (point.getY() > max)
					max = point.getY();
			}
			return max;
		}

		@Override
		public String getName() {
			return "Max";
		}
	};
}
