/*******************************************************************************
 * Copyright (C) 2020 Team Loadcoder
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
package com.loadcoder.load.result;

import java.text.DecimalFormat;

import com.loadcoder.load.result.Summary.DoubleToStringConvert;

public class ValueHolder {

	double originalValue;
	double presentedValue;
	DoubleToStringConvert converter = d -> d.asDouble(2);

	public ValueHolder(double d, DoubleToStringConvert converter) {
		this.originalValue = d;
		this.presentedValue = d;
		if (converter != null) {
			this.converter = converter;
		}
	}

	public void useRoundedValue(int maxNumberOfDecimals) {
		String roundedValueString = new DecimalFormat(getFormatString(maxNumberOfDecimals)).format(originalValue);
		this.presentedValue = Double.valueOf(roundedValueString);
	}
	protected DoubleToStringConvert getConverter() {
		return converter;
	}

	public String asDouble() {
		return ""+ presentedValue;
	}
	
	private String asDouble(int maxNumberOfDecimals) {
		return new DecimalFormat(getFormatString(maxNumberOfDecimals)).format(presentedValue);
	}

	public String noDecimals() {
		return asDouble(0);
	}

	public double value() {
		return presentedValue;
	}

	public double originalValue() {
		return originalValue;
	}
	
	private String getFormatString(int noOfDecimals) {
		String decimals = "";
		String result;
		for (int i = 0; i < noOfDecimals; i++) {
			decimals += "#";
		}
		if (decimals.length() > 0) {
			result = "#." + decimals;
		} else {
			result = "#";
		}
		return result;
	}
}