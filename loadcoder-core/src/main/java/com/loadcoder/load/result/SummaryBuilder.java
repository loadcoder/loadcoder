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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.loadcoder.load.result.Summary.DoubleToStringConvert;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class SummaryBuilder {

	Result result;

	List<OverallSummaryValueBuildable> overallSummaryValueBuildable = new ArrayList<OverallSummaryValueBuildable>();
	OverallSummaryValueBuilder overallSummaryValueBuilder = new OverallSummaryValueBuilder();

	List<PerTransactionSummaryValueBuildable> perTransactionSummaryValueBuildable = new ArrayList<PerTransactionSummaryValueBuildable>();
	PerTransactionSummaryValueBuilder perTransactionSummaryValueBuilder = new PerTransactionSummaryValueBuilder();

	int maxAmountOfDecimals = -1;

	public SummaryBuilder(Result result) {
		this.result = result;

	}

	public SummaryBuilder overall(OverallSummaryValueBuildable overallSummaryValueBuildable) {
		this.overallSummaryValueBuildable.add(overallSummaryValueBuildable);
		return this;
	}

	public SummaryBuilder perTransaction(PerTransactionSummaryValueBuildable perTransactionSummaryValueBuildable) {
		this.perTransactionSummaryValueBuildable.add(perTransactionSummaryValueBuildable);
		return this;
	}

	public interface OverallSummaryValueBuildable {
		OverallSummaryValueBuilder builder(OverallSummaryValueBuilder overallSummaryValueBuilder,
				OverallValueCalculators c);
	}

	public interface PerTransactionSummaryValueBuildable {
		PerTransactionSummaryValueBuilder builder(PerTransactionSummaryValueBuilder perTransactionSummaryValueBuilder,
				TransactionValueCalculators c);
	}

	public class PerTransactionSummaryValueBuilder {
		List<TransactionValueCalculator> list = new ArrayList<>();
		int maxAmountOfDecimals = -1;

		public PerTransactionSummaryValueBuilder use(TransactionValueCalculator summaryValueCalculator) {
			list.add(summaryValueCalculator);
			return this;
		}

		public PerTransactionSummaryValueBuilder use(String name, TransactionValueCalculator summaryValueCalculator) {

			TransactionValueCalculator calculator = (result, valueHolder) -> {
				SummaryValueHolder holder = summaryValueCalculator.getPerTransactionSummaryValue(result, valueHolder);
				holder.updateName(name);
				return holder;
			};
			list.add(calculator);
			return this;
		}

		protected Map<String, List<SummaryValueHolder>> build(
				Map<String, List<TransactionExecutionResult>> toBeSummarized) {

			Map<String, List<SummaryValueHolder>> summaryValues = new HashMap<String, List<SummaryValueHolder>>();
			list.forEach(perTransactionCalculator -> {
				toBeSummarized.entrySet().forEach(transactions -> {
					List<TransactionExecutionResult> t = transactions.getValue();
					String transactionName = transactions.getKey();
					SummaryValueHolder value = perTransactionCalculator.getPerTransactionSummaryValue(t,
							new SummaryValueHolderBuilder(maxAmountOfDecimals));
					List<SummaryValueHolder> summaryValueHolders = summaryValues.get(transactionName);
					if (summaryValueHolders == null) {
						summaryValueHolders = new ArrayList<SummaryValueHolder>();
						summaryValues.put(transactionName, summaryValueHolders);
					}
					summaryValueHolders.add(value);
				}

				);

			});
			return summaryValues;
		}

		public void setMaxAmountOfDecimals(int maxAmountOfDecimals) {
			this.maxAmountOfDecimals = maxAmountOfDecimals;
		}
	}

	public class OverallSummaryValueBuilder {

		List<OverallValueCalculator> list = new ArrayList<>();
		int maxAmountOfDecimals = -1;

		public OverallSummaryValueBuilder use(OverallValueCalculator summaryValueCalculator) {
			list.add(summaryValueCalculator);
			return this;
		}

		public OverallSummaryValueBuilder use(String name, OverallValueCalculator summaryValueCalculator) {

			OverallValueCalculator calculator = (result, valueHolder) -> {
				SummaryValueHolder holder = summaryValueCalculator.getOverallSummaryValue(result, valueHolder);
				holder.updateName(name);
				return holder;
			};
			list.add(calculator);
			return this;
		}

		protected List<SummaryValueHolder> build() {
			List<SummaryValueHolder> summaryValues = new ArrayList<SummaryValueHolder>();
			list.forEach(overallCalculator -> {
				SummaryValueHolder value = overallCalculator.getOverallSummaryValue(result,
						new SummaryValueHolderBuilder(maxAmountOfDecimals));
				summaryValues.add(value);
			});
			return summaryValues;
		}

		protected void setMaxAmountOfDecimals(int maxAmountOfDecimals) {
			this.maxAmountOfDecimals = maxAmountOfDecimals;
		}
	}

	public static class SummaryValueHolderBuilder {

		DoubleToStringConvert converter;
		private final int maxAmountOfDecimals;

		SummaryValueHolderBuilder(int maxAmountOfDecimals) {
			this.maxAmountOfDecimals = maxAmountOfDecimals;
		}

		public SummaryValueHolderBuilder convert(DoubleToStringConvert converter) {
			this.converter = converter;
			return this;
		}

		public SummaryValueHolder build(String name, Double value) {
			ValueHolder v = new ValueHolder(value, converter);
			if (maxAmountOfDecimals != -1) {
				v.useRoundedValue(maxAmountOfDecimals);
			}
			return new SummaryValueHolder(name, v);
		}

		public SummaryValueHolder build(String name, Integer value) {
			ValueHolder v = new ValueHolder(value, converter);
			if (maxAmountOfDecimals != -1) {
				v.useRoundedValue(maxAmountOfDecimals);
			}
			return new SummaryValueHolder(name, v);
		}

		public SummaryValueHolder build(String name, Long value) {
			ValueHolder v = new ValueHolder(value, converter);
			if (maxAmountOfDecimals != -1) {
				v.useRoundedValue(maxAmountOfDecimals);
			}
			return new SummaryValueHolder(name, v);
		}

	}

	public static class SummaryValueHolder {

		String name;
		final ValueHolder value;

		protected SummaryValueHolder(String name, ValueHolder value) {
			this.name = name;
			this.value = value;
		}

		protected void updateName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public ValueHolder getValue() {
			return value;
		}
	}

	public interface OverallValueCalculator {
		public SummaryValueHolder getOverallSummaryValue(Result result, SummaryValueHolderBuilder value);
	}

	public interface TransactionValueCalculator {
		public SummaryValueHolder getPerTransactionSummaryValue(List<TransactionExecutionResult> result,
				SummaryValueHolderBuilder value);
	}

	public Summary build() {
		for (OverallSummaryValueBuildable f : overallSummaryValueBuildable) {
			overallSummaryValueBuilder.setMaxAmountOfDecimals(maxAmountOfDecimals);
			f.builder(overallSummaryValueBuilder, new OverallValueCalculators());
		}

		for (PerTransactionSummaryValueBuildable f : perTransactionSummaryValueBuildable) {
			perTransactionSummaryValueBuilder.setMaxAmountOfDecimals(maxAmountOfDecimals);
			f.builder(perTransactionSummaryValueBuilder, new TransactionValueCalculators());
		}
		List<SummaryValueHolder> overallSummary = overallSummaryValueBuilder.build();
		Map<String, List<SummaryValueHolder>> perTransactionSummary = perTransactionSummaryValueBuilder
				.build(result.getResultLists());

		List<TransactionExecutionResult> allTransactions = new ArrayList<TransactionExecutionResult>();

		result.getResultLists().entrySet().forEach(transactions -> {

			allTransactions.addAll(transactions.getValue());
		});
		Map<String, List<TransactionExecutionResult>> totalTransactions = new HashMap<String, List<TransactionExecutionResult>>();
		totalTransactions.put("TOTAL", allTransactions);
		Map<String, List<SummaryValueHolder>> allTransactionsSummary = perTransactionSummaryValueBuilder
				.build(totalTransactions);

		Summary summary = new Summary(overallSummary, perTransactionSummary, allTransactionsSummary, result);
		return summary;
	}

	public SummaryBuilder roundValues(int maxAmountOfDecimals) {
		this.maxAmountOfDecimals = maxAmountOfDecimals;
		return this;
	}
}
