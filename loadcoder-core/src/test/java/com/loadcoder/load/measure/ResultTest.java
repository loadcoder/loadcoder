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
package com.loadcoder.load.measure;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.loadcoder.load.testng.TestNGBase;

import junit.framework.Assert;

public class ResultTest extends TestNGBase{

	@Test
	public void testToMerge(){
		List<List<TransactionExecutionResult>> resultList = new ArrayList<List<TransactionExecutionResult>>();

		List<TransactionExecutionResult> result = new ArrayList<TransactionExecutionResult>();
		resultList.add(result);
		for(int i =0; i<10; i++)
			result.add(new TransactionExecutionResult("a", 10 + i *1000, 10, i==2 ? false : true, ""));
		
		List<TransactionExecutionResult> result2 = new ArrayList<TransactionExecutionResult>();
		resultList.add(result2);
		for(int i =0; i<10; i++)
			result2.add(new TransactionExecutionResult("b",70 +  i *1000, 20, i==2 ? false : true, ""));
		
		Result r = new Result(resultList);
		
		List<List<TransactionExecutionResult>> resultList2 = new ArrayList<List<TransactionExecutionResult>>();

		List<TransactionExecutionResult> result3 = new ArrayList<TransactionExecutionResult>();
		resultList2.add(result3);
		for(int i =0; i<10; i++)
			result3.add(new TransactionExecutionResult("a",20 +  i *1000, 30, i==2 ? false : true, ""));
		
		List<TransactionExecutionResult> result4 = new ArrayList<TransactionExecutionResult>();
		resultList2.add(result4);
		for(int i =0; i<10; i++)
			result4.add(new TransactionExecutionResult("b",50 +  i *1000, 40, i==2 ? false : true, ""));
		
		Result r2 = new Result(resultList2);
	
		r.mergeResult(r2);
	
		Assert.assertEquals(40, r.getNoOfTransactions());
		Assert.assertEquals(4, r.getNoOfFails());
		Assert.assertEquals(10, r.getStart());
		Assert.assertEquals(9070, r.getEnd());
		
		Assert.assertEquals(9060, r.getDuration());
		
	}
}
