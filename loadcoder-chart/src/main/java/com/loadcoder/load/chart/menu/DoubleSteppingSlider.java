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
package com.loadcoder.load.chart.menu;

import java.awt.Dimension;

import javax.swing.JSlider;

public class DoubleSteppingSlider extends JSlider {
	private static final long serialVersionUID = 1L;

	private Double[] values = null;

	public Double[] getValues() {
		return values;
	}

	public static int getIndexOf(Double[] values, Double value) {
		for (int i = 0; i < values.length; i++) {
			if ((long) (values[i] * 1_000_000) == (long) (value * 1_000_000)) {
				return i;
			}
		}
		throw new RuntimeException(
				"An internal error occured in Loadcoder when fetching index of the slider for points reduction.");
	}

	public DoubleSteppingSlider(Double[] values, int defaultIndex) {
		super(0, values.length - 1, defaultIndex);
		this.values = values;
		setPaintTicks(true);
		setPaintLabels(true);
		setSnapToTicks(true);
		setMajorTickSpacing(1);
		setPreferredSize(new Dimension(400, 40));
	}
}
