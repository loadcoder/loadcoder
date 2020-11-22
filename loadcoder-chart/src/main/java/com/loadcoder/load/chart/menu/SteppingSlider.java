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
package com.loadcoder.load.chart.menu;

import java.awt.Dimension;

import javax.swing.JSlider;

public class SteppingSlider extends JSlider
{
	private static final long serialVersionUID = 1L;

	private Integer[] values = null;
    
	public Integer[] getValues() {
		return values;
	}

    public SteppingSlider(Integer[] values, int defaultIndex)
    {
		super(0, values.length-1, defaultIndex);
		this.values = values;
		setPaintTicks(true);
		setPaintLabels(true);
        setSnapToTicks(true);
        setMajorTickSpacing(1);
        setPreferredSize(new Dimension(400, 40));
    }

}
