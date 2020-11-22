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

import java.awt.Color;

import org.jfree.chart.ChartColor;

public enum CommonSeries {
	THROUGHPUT("Throughput (TPS)", ChartColor.BLACK, CommonYCalculators.THROUGHPUT),
	FAILS("Fails", ChartColor.RED, CommonYCalculators.FAILS);

	Color color;
	
	CommonYCalculator commonYCalculator;
	
	String name;

	public String getName() {
		return name;
	}

	public Color getColor() {
		return color;
	}

	public CommonYCalculator getCommonYCalculator() {
		return commonYCalculator;
	}

	CommonSeries(String name, Color color, CommonYCalculator commonYCalculator){
		this.name = name;
		this.color = color;
		this.commonYCalculator = commonYCalculator;
	}
}
