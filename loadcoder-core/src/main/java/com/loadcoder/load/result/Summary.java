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

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.result.Summary.SummaryWithResultActions.Table.SummaryWithTable;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class Summary {

	final private static Logger log = LoggerFactory.getLogger(Summary.class);

	private Result result;

	public Summary(Result result) {
		this.result = result;
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

	public final class SummaryWithResultActions {
		private List<ResultSummarizer> resultActions = new ArrayList<ResultSummarizer>();
		private List<SummaryWithTable> tables = new ArrayList<SummaryWithTable>();

		public SummaryWithResultActions log(ResultSummarizer resultSummarizer) {
			resultActions.add(resultSummarizer);
			return this;
		}

		/**
		 * print the Summary to the logger of
		 * <code>com.loadcoder.load.result.Summary</code>
		 */
		public void print() {
			String asString = asString(resultActions, result);
			log.info(asString);
		}

		/**
		 * @return the Summary as a String
		 */
		public String getAsString() {
			String asString = asString(resultActions, result);
			return asString;
		}

		public Table table() {
			return new Table(this);
		}

		public class Table {
			SummaryWithResultActions summaryWithResultActions;

			private Table(SummaryWithResultActions summaryWithResultActions) {
				this.summaryWithResultActions = summaryWithResultActions;
			}

			/**
			 * Define a column for the table
			 * 
			 * @param name            is the name of the column
			 * @param valueCalculator is the implementation of the way the value for the
			 *                        column will be calculated for each of the lines
			 *                        (transactions)
			 * @return a SummaryWithTable possible to extend with additional columns
			 */
			public SummaryWithTable column(String name, ValueCalculator valueCalculator) {
				SummaryWithTable table = new SummaryWithTable().column(name, valueCalculator);
				tables.add(table);
				return table;
			}

			public class SummaryWithTable {
				List<Coloumn> columns = new ArrayList<Coloumn>();
				List<List<TableEntry>> stringTable = new ArrayList<List<TableEntry>>();

				public SummaryWithTable column(String name, ValueCalculator valueCalculator) {
					columns.add(new Coloumn(name, valueCalculator));
					return this;
				}

				private class TableEntry {
					Coloumn column;

					public Coloumn getColumn() {
						return column;
					}

					public String getEntry() {
						return entry;
					}

					String entry;

					private TableEntry(Coloumn c, String entry) {
						this.column = c;
						this.entry = entry;
					}
				}

				private String tableAsString() {

					String res = "";
					List<TableEntry> firstStringRow = new ArrayList<TableEntry>();
					Map<Coloumn, Integer> maxWidthPerColumn = new HashMap<Coloumn, Integer>();
					stringTable.add(firstStringRow);
					for (Coloumn column : columns) {
						firstStringRow.add(new TableEntry(column, column.getName()));
						maxWidthPerColumn.put(column, column.getName().length());
					}
					Map<String, List<TransactionExecutionResult>> resultList = result.getResultLists();
					for (String key : resultList.keySet()) {
						List<TransactionExecutionResult> transactions = resultList.get(key);
						List<TableEntry> transactionStringRow = new ArrayList<TableEntry>();
						stringTable.add(transactionStringRow);
						for (Coloumn column : columns) {
							ValueCalculator calculator = column.getValueCalculator();
							String value = calculator.calculateValue(key, transactions);
							transactionStringRow.add(new TableEntry(column, value));
							if (maxWidthPerColumn.get(column) < value.length())
								maxWidthPerColumn.put(column, value.length());
						}
					}

					final int columnDistance = 2;
					for (List<TableEntry> str : stringTable) {
						String line = "";
						for (TableEntry s : str) {
							int maxWidth = maxWidthPerColumn.get(s.getColumn());
							String paddedValue = LoadUtility.rightpad(s.getEntry(), maxWidth + columnDistance);
							line += paddedValue;
						}

						res = res + line + "\n";
					}

					return res;
				}

				public void print() {
					String toLog = getAsString();
					log.info(toLog);
				}

				public String getAsString() {
					String res = summaryWithResultActions.getAsString();
					String tab = tableAsString();
					String total = res + tab;
					return total;
				}
			}
		}

		class Coloumn {
			String name;

			public String getName() {
				return name;
			}

			public ValueCalculator getValueCalculator() {
				return valueCalculator;
			}

			ValueCalculator valueCalculator;

			private Coloumn(String name, ValueCalculator valueCalculator) {
				this.name = name;
				this.valueCalculator = valueCalculator;
			}
		}
	}

	public SummaryWithResultActions log(ResultSummarizer resultSummarizer) {
		return new SummaryWithResultActions().log(resultSummarizer);
	}

	@FunctionalInterface
	public interface ValueCalculator {

		/**
		 * Generate a String value based on the transaction key and its
		 * TransactionExecutionResults
		 * 
		 * @param key        is the name of the transaction
		 * @param resultList is a List of TransactionExecutionResult for the transaction
		 * @return a generated String value
		 */
		String calculateValue(String key, List<TransactionExecutionResult> resultList);
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
}
