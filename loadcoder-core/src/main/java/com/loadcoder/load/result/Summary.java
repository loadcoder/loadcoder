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
import com.loadcoder.load.measure.Result;
import com.loadcoder.load.measure.TransactionExecutionResult;
import com.loadcoder.load.result.Summary.SummaryWithResultActions.Table.SummaryWithTable;

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
	
	public static ResultSummarizer throughput(){
		return (a)->{
			int seconds = calculateDurationOfTest(a.getDuration());
			double throughput = a.getNoOfTransactions() / seconds;
			return String.format("Throughput: %sTPS", throughput);
		};
	}

	public static ResultSummarizer duration(){
		return (a)->{

			return String.format("Duration: %s milliseconds", a.getDuration());
		};
	}
	
	public static ResultSummarizer amountOfTransactions(){
		return (a)->{
			return String.format("Amount of transactions: %s", a.getNoOfTransactions());
		};
	}
	
	public static ResultSummarizer amountOfFails(){
		return (a)->{
			return String.format("Amount of fails: %s", a.getNoOfFails());
		};
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

//						log.info(line);
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

	public static ValueCalculator avg(){
		ValueCalculator max = (rr)->{
			long totalSum = 0;
			for(TransactionExecutionResult transactionExecutionResult : rr){
				totalSum += transactionExecutionResult.getRt();
			}
			long avgValue = totalSum / rr.size();

			return "" + avgValue;
		};
		return max;
	}

	public static ValueCalculator percentile(int percentile){
		ValueCalculator max = (rr)->{

			List<Long> allResponseTimes = new ArrayList<Long>();
			for(TransactionExecutionResult r : rr){
				allResponseTimes.add(r.getRt());
			}
			allResponseTimes.sort((a,b)->{return (int)(a-b); });
			int percentileIndex = (int)(allResponseTimes.size() * (0.01 * percentile));
			Long responseTimePercentile = allResponseTimes.get(percentileIndex);
			return "" + responseTimePercentile;
		};
		return max;
	}

	public static ValueCalculator max(){
		ValueCalculator max = (rr)->{
			long maxValue = 0;
			for(TransactionExecutionResult transactionExecutionResult : rr){
				if(transactionExecutionResult.getRt() > maxValue)
					maxValue = transactionExecutionResult.getRt();
			}
			return "" +maxValue;
		};
		return max;
	}

	public static ValueCalculator transactions(){
		ValueCalculator max = (rr)->{
			return "" +rr.size();
		};
		return max;
	}
	public static ValueCalculator fails(){
		ValueCalculator max = (rr)->{
			long noOfFails = 0;
			for(TransactionExecutionResult transactionExecutionResult : rr){
				if(transactionExecutionResult.isStatus() == false)
					noOfFails++;
			}
			return "" +noOfFails;
		};
		return max;
	}

	public static ValueCalculator transactionNames(){
		ValueCalculator max = (rr)->{
			String name = rr.get(0).getName();
			return name;
		};
		return max;
	}

	public interface ValueCalculator{
		String calculateValue(List<TransactionExecutionResult> resultList);
	}
}
