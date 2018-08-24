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

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.List;

public class XYDottedSeriesExtension extends XYSeriesExtension {

	private static final long serialVersionUID = 1L;

	private boolean visible = true;

	private boolean dotted = true;

	public static final Shape DOTTEDSHAPE = setupShape();

	public boolean isDotted() {
		return dotted;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public XYDottedSeriesExtension(Comparable key, boolean autoSort, boolean allowDuplicateXValues, Paint color) {
		super(key, autoSort, allowDuplicateXValues, color);
	}

	public List<XYDataItemExtension> getXYDataItems() {
		return data;
	}

	private static Shape setupShape() {
		float f = 2.7F;
		GeneralPath p0 = new GeneralPath();
		p0.moveTo(0, 0);
		p0.lineTo(0, -f);
		p0.closePath();

		return p0;
	}

}
