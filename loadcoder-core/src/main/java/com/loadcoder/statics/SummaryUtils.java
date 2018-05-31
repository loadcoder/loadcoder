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
package com.loadcoder.statics;

import static com.loadcoder.load.result.Summary.*;

import java.util.ArrayList;
import java.util.List;

import com.loadcoder.load.measure.Result;
import com.loadcoder.load.measure.TransactionExecutionResult;
import com.loadcoder.load.result.Summary;
import com.loadcoder.load.result.Summary.ResultSummarizer;
import com.loadcoder.load.result.Summary.ValueCalculator;

public class SummaryUtils {

	public static void printSimpleSummary(Result result, String resultName) {

		Summary resultSummarizer = new Summary(result);
		resultSummarizer
		.log((a)->{return String.format("Summary for %s", resultName);})
		.log(duration())
		.log(throughput())
		.table()
		.column("Transaction", transactionNames())
		.column("MAX", max())
		.column("AVG", avg())
		.column("Fails", fails())
		.column("90%", percentile(90))
		
		
		.print();
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
}
