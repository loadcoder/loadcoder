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

import static com.loadcoder.statics.SummaryUtils.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.loadcoder.load.result.Summary.SummaryWithResultActions.Table.SummaryWithTable;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class SummaryTest extends TestNGBase{

	@Test
	public List<List<TransactionExecutionResult>> getResultList(){

		List<List<TransactionExecutionResult>> resultLists = new ArrayList<List<TransactionExecutionResult>>();

		List<TransactionExecutionResult> incrementalResponseTimes = new ArrayList<TransactionExecutionResult>();
		resultLists.add(incrementalResponseTimes);

		List<TransactionExecutionResult> someErrors = new ArrayList<TransactionExecutionResult>();
		resultLists.add(someErrors);

		for(int i = 0; i<100; i++){
			TransactionExecutionResult result = new TransactionExecutionResult("0-100", (long)i * 1000, i, true, "" );
			incrementalResponseTimes.add(result);
		
			if(i == 0 || i == 50 || i ==90)
				result = new TransactionExecutionResult("sometimesError", (long)i * 1000, i, false, "");
			else
				result = new TransactionExecutionResult("sometimesError", (long)i * 1000, i, true, "");
			someErrors.add(result);
		}
		
		return resultLists;

	}
	
	public SummaryWithTable fullSummary(Result result, Method method){
		Summary resultSummarizer = new Summary(result);
		SummaryWithTable summaryWithTable = resultSummarizer.
		firstDo((a)->{}).
		log((a)->{return String.format("Summary for %s:%s", this.getClass().getSimpleName(), method.getName());}).
		log(throughput()).
		log(amountOfTransactions()).
		log(amountOfFails()).
		table().
		column("Transaction", transactionNames()).
		column("Amount", transactions()).
		column("MAX", max()).
		column("AVG", avg()).
		column("80%", percentile(80)).
		column("FAILS", fails());

		return summaryWithTable;
	}

	@Test
	public void testLogSummaryRow() {
		Result result = new Result(getResultList());
		Summary summary = new Summary(result);
		summary
		.log(a -> "Foo")
		.log(a -> "Bar")
		.print();
	}
	
	@Test
	public void test(Method m) {
		Result result = new Result(getResultList());
		SummaryWithTable summaryWithTable = fullSummary(result, m);
		Assert.assertTrue(summaryWithTable.getAsString().contains("Throughput: 2.0TPS"));
		
	}
}
