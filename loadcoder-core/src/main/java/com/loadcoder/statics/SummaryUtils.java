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


	/**
	 * Logging a simple summary of the provided result
	 * @param result for the test to be summarized
	 * @param resultName
	 */
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

	/**
	 * The average throughput
	 * @return ResultSummarizer
	 */
	public static ResultSummarizer throughput(){
		return (a)->{
			int seconds = calculateDurationOfTest(a.getDuration());
			double throughput = a.getNoOfTransactions() / seconds;
			return String.format("Throughput: %sTPS", throughput);
		};
	}

	/**
	 * The duration
	 * @return ResultSummarizer
	 */
	public static ResultSummarizer duration(){
		return (a)->{

			return String.format("Duration: %s milliseconds", a.getDuration());
		};
	}

	/**
	 * The total amount of transactions
	 * @return ResultSummarizer
	 */
	public static ResultSummarizer amountOfTransactions(){
		return (a)->{
			return String.format("Amount of transactions: %s", a.getNoOfTransactions());
		};
	}

	/**
	 * The total amount of fails
	 * @return ResultSummarizer
	 */
	public static ResultSummarizer amountOfFails(){
		return (a)->{
			return String.format("Amount of fails: %s", a.getNoOfFails());
		};
	}
	
	/**
	 * ValueCalculator that calculates the average responsetime for a particular transaction
	 * @return ValueCalculator
	 */
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


	/**
	 * ValueCalculator that calculates the given responsetime percentile for a particular transaction
	 * @param percentile
	 * @return
	 */
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

	/**
	 * ValueCalculator that calculates the maximum responsetime for a particular transaction
	 * @return ValueCalculator
	 */
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

	/**
	 * ValueCalculator that calculates the amount of transactions done for a particular transaction
	 * @return ValueCalculator
	 */
	public static ValueCalculator transactions(){
		ValueCalculator max = (rr)->{
			return "" +rr.size();
		};
		return max;
	}
	
	/**
	 * ValueCalculator that calculates the amount of fails for a particular transaction
	 * @return ValueCalculator
	 */
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

	/**
	 * ValueCalculator that returns the name of the transaction
	 * @return ValueCalculator
	 */
	public static ValueCalculator transactionNames(){
		ValueCalculator max = (rr)->{
			if(rr.size() == 0) {
				return "empty";
			}
			String name = rr.get(0).getName();
			return name;
		};
		return max;
	}
}
