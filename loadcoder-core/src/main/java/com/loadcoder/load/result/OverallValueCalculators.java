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

import com.loadcoder.load.result.SummaryBuilder.OverallValueCalculator;
import com.loadcoder.utils.DateTimeUtil;

public class OverallValueCalculators {

	public OverallValueCalculator fails() {
		return (result, valueBuilder) -> {
			return valueBuilder.convert(context -> context.noDecimals()).build("Fails", result.getAmountOfFails());
		};
	}

	public OverallValueCalculator duration() {
		return (result, valueBuilder) -> {
			return valueBuilder.convert(context -> DateTimeUtil.getMillisAsHoursMinutesSecondsString((long)context.value())).build("Duration", result.getDuration());
		};
	}

	public OverallValueCalculator throughput() {
		return (result, valueBuilder) -> {
			long duration = result.getDuration();
			int sec = Summary.getDurationInSeconds(duration);
			double throughput = (double) result.getAmountOfTransactions() / sec;

			return valueBuilder.convert(context -> context.asDouble() + " tps").build("Throughput", throughput);
		};
	}

	public OverallValueCalculator amountOfTransactions() {
		return (result, valueBuilder) -> {

			int amountOfTransactions = result.getAmountOfTransactions();

			return valueBuilder.convert(context -> context.noDecimals()).build("Total transactions",
					amountOfTransactions);
		};
	}
}
