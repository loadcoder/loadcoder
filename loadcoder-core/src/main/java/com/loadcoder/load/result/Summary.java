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
	public Summary(Result result){
		this.result = result;
	}

	private interface ResultAction{}

	public static String asString(List<ResultAction> resultActions, Result result) {
		
		String res = "";
		for(ResultAction resultAction : resultActions){
			if(resultAction instanceof ResultSummarizer){
				ResultSummarizer resultSummarizer = (ResultSummarizer)resultAction;
				res = res + resultSummarizer.summarize(result) + "\n";
			}else if(resultAction instanceof ResultUser){
				ResultUser resultUser = (ResultUser)resultAction;
				resultUser.summarize(result);
			}
		}
		return res;
	}
	
	/**
	 * 
	 */
	public interface ResultSummarizer extends ResultAction{
		String summarize(Result result);
	}

	public interface ResultUser extends ResultAction{
		void summarize(Result result);
	}

	public static int calculateDurationOfTest(long durationMillis){
		
		int sec = (int)durationMillis / 1000;
		sec = sec == 0 ? 1 : sec;
		return sec;
	}
	
	public final class SummaryWithResultActions{
		private List<ResultAction> resultActions = new ArrayList<ResultAction>();
		private List<SummaryWithTable> tables = new ArrayList<SummaryWithTable>();

		public SummaryWithResultActions log(ResultSummarizer resultSummarizer){
			resultActions.add(resultSummarizer);
			return this;
		}

		public void print(){
			String asString = asString(resultActions, result);
			log.info(asString);
		}
		
		public String getAsString(){
			String asString = asString(resultActions, result);
			return asString;
		}
		
		public Table table(){
			return new Table(this);
		}

		SummaryWithResultActions thenDo(ResultUser resultUser){
			resultActions.add(resultUser);
			return this;
		}

		public class Table{
			SummaryWithResultActions summaryWithResultActions;
			private Table(SummaryWithResultActions summaryWithResultActions){
				this.summaryWithResultActions = summaryWithResultActions; 

			}
			public SummaryWithTable column(String name, ValueCalculator valueCalculator){
				SummaryWithTable table = new SummaryWithTable().column(name, valueCalculator);
				tables.add(table);
				return table;
			}

			public class SummaryWithTable{
				List<Coloumn> columns = new ArrayList<Coloumn>();
				List<List<TableEntry>> stringTable = new ArrayList<List<TableEntry>>();

				public SummaryWithTable column(String name, ValueCalculator valueCalculator){
					columns.add(new Coloumn(name, valueCalculator));
					return this;
				}	

				private class TableEntry{
					Coloumn column;
					public Coloumn getColumn() {
						return column;
					}

					public String getEntry() {
						return entry;
					}

					String entry;

					private TableEntry(Coloumn c, String entry){
						this.column = c;
						this.entry = entry;
					}
				}

				private String tableAsString(){
					
					String res = "";
					List<TableEntry> firstStringRow = new ArrayList<TableEntry>();
					Map<Coloumn, Integer> maxWidthPerColumn = new HashMap<Coloumn, Integer>();
					stringTable.add(firstStringRow);
					for(Coloumn column : columns){
						firstStringRow.add(new TableEntry(column, column.getName()));
						maxWidthPerColumn.put(column, column.getName().length());
					}
					List<List<TransactionExecutionResult>> resultList = result.getResultLists();
					for(List<TransactionExecutionResult> r : resultList){

						List<TableEntry> transactionStringRow = new ArrayList<TableEntry>();
						stringTable.add(transactionStringRow);
						for(Coloumn column : columns){
							ValueCalculator calculator = column.getValueCalculator();
							String value = calculator.calculateValue(r);
							transactionStringRow.add(new TableEntry(column, value));
							if(maxWidthPerColumn.get(column) < value.length())
								maxWidthPerColumn.put(column, value.length());
						}
					}

					final int columnDistance = 2;
					for(List<TableEntry> str : stringTable){
						String line = ""; 
						for(TableEntry s : str){
							int maxWidth = maxWidthPerColumn.get(s.getColumn());
							String paddedValue = LoadUtility.rightpad(s.getEntry(), maxWidth + columnDistance);
							line += paddedValue;
						}

						res = res + line + "\n";
					}

					return res;
				}

				
				
				public void print(){
					String toLog = getAsString();
					log.info(toLog);
				}
				
				public String getAsString(){
					String res = summaryWithResultActions.getAsString();
					String tab = tableAsString();
					String total = res + tab;
					return total;
				}
				
			}

		}

		class Coloumn{
			String name;
			public String getName() {
				return name;
			}

			public ValueCalculator getValueCalculator() {
				return valueCalculator;
			}

			ValueCalculator valueCalculator;

			private Coloumn(String name, ValueCalculator valueCalculator){
				this.name = name;
				this.valueCalculator = valueCalculator;
			}
		}
	}

	public SummaryWithResultActions log(ResultSummarizer resultSummarizer){
		return new SummaryWithResultActions().log(resultSummarizer);
	}

	public SummaryWithResultActions firstDo(ResultUser resultUser){
		return new SummaryWithResultActions().thenDo(resultUser);
	}

	public interface ValueCalculator{
		String calculateValue(List<TransactionExecutionResult> resultList);
	}
}
