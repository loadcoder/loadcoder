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

import com.loadcoder.load.result.SummaryBuilder.TransactionValueCalculator;
import com.loadcoder.result.TransactionExecutionResult;

public class TransactionValueCalculators {

	public TransactionValueCalculator amount() {
		return (transactionsList, valueBuilder) -> {

			return valueBuilder.convert(context -> context.noDecimals()).build("Amount", transactionsList.size());
		};
	}

	public TransactionValueCalculator fails() {
		return (transactionsList, valueBuilder) -> {
			int fails = 0;
			for (TransactionExecutionResult t : transactionsList) {
				if (!t.getStatus()) {
					fails++;
				}
			}
			return valueBuilder.convert(context -> context.noDecimals()).build("Fails", fails);
		};
	}

	public TransactionValueCalculator avg() {
		return (transactionsList, valueBuilder) -> {
			long totalSum = 0;
			for (TransactionExecutionResult t : transactionsList) {
				totalSum += t.getValue();
			}
			double result = (double) totalSum / transactionsList.size();
			return valueBuilder.convert(context -> context.asDecimalString()).build("Avg", result);
		};
	}

	public TransactionValueCalculator median() {
		return (transactionsList, valueBuilder) -> {
			transactionsList.sort((a, b) -> (int) (b.getValue() - a.getValue()));

			int medianIndex = transactionsList.size() / 2;
			TransactionExecutionResult t = transactionsList.get(medianIndex);
			return valueBuilder.convert(context -> context.noDecimals()).build("Median", t.getValue());
		};
	}

	public TransactionValueCalculator maximum() {
		return (transactionsList, valueBuilder) -> {
			transactionsList.sort((a, b) -> (int) (b.getValue() - a.getValue()));
			return valueBuilder.convert(context -> context.noDecimals()).build("Max",
					transactionsList.get(0).getValue());
		};
	}

	public TransactionValueCalculator minimum() {
		return (transactionsList, valueBuilder) -> {
			transactionsList.sort((a, b) -> (int) (a.getValue() - b.getValue()));
			return valueBuilder.convert(context -> context.noDecimals()).build("Min",
					transactionsList.get(0).getValue());
		};
	}

	public TransactionValueCalculator percentile(int percentile) {
		return (transactionsList, valueBuilder) -> {
			transactionsList.sort((a, b) -> (int) (a.getValue() - b.getValue()));

			int percentileIndex = (int) (transactionsList.size() * (0.01 * percentile));
			TransactionExecutionResult transaction = transactionsList.get(percentileIndex);
			return valueBuilder.convert(context -> context.noDecimals()).build("" + percentile + "%",
					transaction.getValue());
		};
	}

}
