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
package com.loadcoder.load.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.result.SummaryBuilder.OverallValueCalculator;
import com.loadcoder.load.result.SummaryBuilder.SummaryValueHolder;
import com.loadcoder.load.result.SummaryBuilder.SummaryValueHolderBuilder;
import com.loadcoder.load.result.SummaryBuilder.TransactionValueCalculator;
import com.loadcoder.result.Result;
import com.loadcoder.result.Summarizable;
import com.loadcoder.result.TransactionExecutionResult;

public class Summary {

	final private static Logger log = LoggerFactory.getLogger(Summary.class);

	Map<String, ValueHolder> overAllSummary;
	List<SummaryValueHolder> overAllSummaryList;

	Map<String, Map<String, ValueHolder>> transactionsSummary;
	Map<String, ValueHolder> allTransactionsSummary;
	List<SummaryValueHolder> transactionsSummaryValueHolderList;

	Result loadTestResult;

	public List<SummaryValueHolder> getOverAllSummaryList() {
		return overAllSummaryList;
	}

	public Map<String, ValueHolder> getAllTransactionsSummary() {
		return allTransactionsSummary;
	}

	public Map<String, Map<String, ValueHolder>> getTransactionsSummary() {
		return transactionsSummary;
	}

	Map<String, ValueHolder> getOverAllSummary() {
		return overAllSummary;
	}

	public List<SummaryValueHolder> getTransactionsSummaryValueHolderList() {
		return transactionsSummaryValueHolderList;
	}

	void setTransactionsSummaryValueHolderList(List<SummaryValueHolder> transactionsSummaryValueHolderList) {
		this.transactionsSummaryValueHolderList = transactionsSummaryValueHolderList;
	}

	public Summary(List<SummaryValueHolder> overAllSummaryList,
			Map<String, List<SummaryValueHolder>> eachTransactionsSummary,
			Map<String, List<SummaryValueHolder>> allTransactions, Result loadTestResult) {
		this.loadTestResult = loadTestResult;
		this.overAllSummaryList = overAllSummaryList;
		this.overAllSummary = new HashMap<String, ValueHolder>();
		overAllSummaryList.forEach(valueHolder -> {
			overAllSummary.put(valueHolder.getName(), valueHolder.getValue());
		});

		eachTransactionsSummary.entrySet().stream().findFirst().ifPresent(first -> {
			setTransactionsSummaryValueHolderList(first.getValue());
		});

		this.transactionsSummary = new HashMap<String, Map<String, ValueHolder>>();
		eachTransactionsSummary.entrySet().forEach(entry -> {
			Map<String, ValueHolder> result = new HashMap<String, ValueHolder>();
			entry.getValue().stream().forEach(valueHolder -> {
				result.put(valueHolder.getName(), valueHolder.getValue());
			});
			transactionsSummary.put(entry.getKey(), result);
		});

		this.allTransactionsSummary = new HashMap<String, ValueHolder>();
		List<SummaryValueHolder> summaryValueHolder = allTransactions.get("TOTAL");
		if (summaryValueHolder != null) {
			summaryValueHolder.stream().forEach(entry -> {

				allTransactionsSummary.put(entry.getName(), entry.getValue());
			});
		}

	}

	/**
	 * Get the Result as a string formatted with the provided ResultActions
	 * 
	 * @param resultSummarizers is the summarizers that will be used to summarize
	 *                          the provided result
	 * @param result            is the Result to be summarized as a String
	 * @return the Result as a String
	 */
	public static String asString(List<ResultSummarizer> resultSummarizers, Result result) {

		String res = "";
		for (ResultSummarizer resultAction : resultSummarizers) {
			ResultSummarizer resultSummarizer = (ResultSummarizer) resultAction;
			res = res + resultSummarizer.summarize(result) + "\n";
		}
		return res;
	}

	public interface ResultSummarizer {

		/**
		 * Generate a summary based on the result
		 * 
		 * @param result that the result is going to be based on
		 * @return a summary of type String
		 */
		String summarize(Result result);
	}

	/**
	 * converts duration in milliseconds to seconds
	 * 
	 * @param durationMillis is the amount of milliseconds that will be converted to
	 *                       seconds
	 * @return the amount of seconds equivalent to the amount of milliseconds
	 *         provided in {@code durationMillis} Since the returned value is the
	 *         duration of the execution, this value will never be 0. The minimum
	 *         value should always be 1. Negative values shall is out of scope.
	 */
	public static int getDurationInSeconds(long durationMillis) {
		int sec = (int) durationMillis / 1000;
		sec = sec == 0 ? 1 : sec;
		return sec;
	}

	@FunctionalInterface
	public interface SummaryResultCalculator {

		/**
		 * Generate a double value based on the transaction key and its
		 * TransactionExecutionResults
		 * 
		 * @param key        is the name of the transaction
		 * @param resultList is a List of TransactionExecutionResult for the transaction
		 * @return a generated double value
		 */
		double calculateValue(String key, List<TransactionExecutionResult> resultList);
	}

	public Double overall(String name) {
		Double value = overAllSummary.get(name).value();
		return value;
	}

	public Double transaction(String name, String type) {
		Map<String, ValueHolder> transactionTypeMap = transactionsSummary.get(type);
		Double d = transactionTypeMap.get(name).value();
		return d;
	}

	public Double transaction(TransactionValueCalculator calc, String type) {

		Map<String, ValueHolder> ee = transactionsSummary.get(type);
		String name = getNameFromTransactionValueCalculator(calc);
		Double d = ee.get(name).value();
		return d;
	}

	public interface TransactionValueCalculatorFetchable {
		TransactionValueCalculator getTransactionValueCalculator(TransactionValueCalculators c);
	}

	public interface OverallValueCalculatorFetchable {
		OverallValueCalculator getOverallValueCalculator(OverallValueCalculators c);
	}

	public Double transaction(TransactionValueCalculatorFetchable calc, String type) {

		Map<String, ValueHolder> ee = transactionsSummary.get(type);
		TransactionValueCalculator t = calc.getTransactionValueCalculator(new TransactionValueCalculators());
		String name = getNameFromTransactionValueCalculator(t);
		Double d = ee.get(name).value();
		return d;
	}

	public Double overall(OverallValueCalculatorFetchable calc) {

		OverallValueCalculator t = calc.getOverallValueCalculator(new OverallValueCalculators());
		String name = getNameFromOverallValueCalculator(t);

		Double d = overAllSummary.get(name).value();
		return d;
	}

	private String getNameFromTransactionValueCalculator(TransactionValueCalculator calc) {
		List<TransactionExecutionResult> list = new ArrayList<TransactionExecutionResult>();
		list.add(new TransactionExecutionResult(0, 0, true, null));
		SummaryValueHolder holder = calc.getPerTransactionSummaryValue(list, new SummaryValueHolderBuilder(1));
		String name = holder.getName();
		return name;
	}

	private String getNameFromOverallValueCalculator(OverallValueCalculator calc) {
		SummaryValueHolder holder = calc.getOverallSummaryValue(loadTestResult, new SummaryValueHolderBuilder(1));
		String name = holder.getName();
		return name;
	}

	private Summarizable getResultTemplate() {
		return new Summarizable() {

			@Override
			public long getDuration() {
				return 60_000;
			}

			@Override
			public int getAmountOfTransactions() {
				return 2;
			}

			@Override
			public int getAmountOfFails() {
				return 0;
			}

			@Override
			public Map<String, List<TransactionExecutionResult>> getResultLists() {
				Map<String, List<TransactionExecutionResult>> map = new HashMap<String, List<TransactionExecutionResult>>();
				map.put("templateTransaction", new ArrayList<TransactionExecutionResult>());
				map.get("templateTransaction").add(new TransactionExecutionResult(0, 0, true, "message"));
				return map;
			}

		};
	}

	public Double allTransactions(String string) {
		Double d = allTransactionsSummary.get(string).value();
		return d;
	}

	public String asString(PrinterFormatBuilable printerFormatBuilable) {

		UserDefinedConverters userDefinedConverters = new UserDefinedConverters();
		printerFormatBuilable.populateConverters(userDefinedConverters, new ValueCalculators());

		String result = SummaryPrinter.tableAsString(this, userDefinedConverters);
		return result;
	}

	public void prettyPrint(PrinterFormatBuilable printerFormatBuilable) {
		String result = asString(printerFormatBuilable);
		log.info(result);
	}

	public void prettyPrint() {

		UserDefinedConverters userDefinedConverters = new UserDefinedConverters();
		SummaryPrinter.tableAsString(this, userDefinedConverters);
	}

	public interface PrinterFormatBuilable {
		void populateConverters(UserDefinedConverters userDefinedConverters, ValueCalculators valueCalculators);
	}

	public static class ValueCalculators {

		TransactionValueCalculators transaction;
		OverallValueCalculators overall;

		ValueCalculators() {
			this.transaction = new TransactionValueCalculators();
			this.overall = new OverallValueCalculators();
		}

		public TransactionValueCalculators transaction() {
			return transaction;
		}

		public OverallValueCalculators overall() {
			return overall;
		}

	}

	public interface DoubleToStringConvert {
		public String convert(ValueHolder b);
	}

	public class UserDefinedConverters {

		Map<String, DoubleToStringConvert> defaultConverters = new HashMap<String, DoubleToStringConvert>();

		DoubleToStringConvert defaultConverter;

		DoubleToStringConvert getDefaultConverter() {
			return defaultConverter;
		}

		public UserDefinedConverters convert(String name, DoubleToStringConvert defaultConverter) {
			defaultConverters.put(name, defaultConverter);
			return this;
		}

		public UserDefinedConverters convert(DoubleToStringConvert defaultConverter) {
			this.defaultConverter = defaultConverter;
			return this;
		}

		protected Map<String, DoubleToStringConvert> getMap() {
			return defaultConverters;
		}

		public UserDefinedConverters convert(TransactionValueCalculator calc, DoubleToStringConvert defaultConverter) {
			String name = getNameFromTransactionValueCalculator(calc);
			defaultConverters.put(name, defaultConverter);
			return this;
		}

		public UserDefinedConverters convert(OverallValueCalculator calc, DoubleToStringConvert defaultConverter) {
			String name = getNameFromOverallValueCalculator(calc);
			defaultConverters.put(name, defaultConverter);
			return this;
		}
	}
}
