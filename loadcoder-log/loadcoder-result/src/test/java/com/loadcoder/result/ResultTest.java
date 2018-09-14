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
package com.loadcoder.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

import static junit.framework.Assert.*;

public class ResultTest extends TestNGBase{

	@Test
	public void testToMerge(){
		Map<String, List<TransactionExecutionResult>> resultList = new HashMap<String, List<TransactionExecutionResult>>();

		List<TransactionExecutionResult> result = new ArrayList<TransactionExecutionResult>();
		resultList.put("a", result);
		for(int i =0; i<10; i++)
			result.add(new TransactionExecutionResult(10 + i *1000, 10, i==2 ? false : true, ""));
		
		List<TransactionExecutionResult> result2 = new ArrayList<TransactionExecutionResult>();
		resultList.put("b", result2);
		for(int i =0; i<10; i++)
			result2.add(new TransactionExecutionResult(70 +  i *1000, 20, i==2 ? false : true, ""));
		
		Result r = new Result(resultList);
		
		Map<String, List<TransactionExecutionResult>> resultList2 = new HashMap<String, List<TransactionExecutionResult>>();

		List<TransactionExecutionResult> result3 = new ArrayList<TransactionExecutionResult>();
		resultList2.put("a", result3);
		for(int i =0; i<10; i++)
			result3.add(new TransactionExecutionResult("a",20 +  i *1000, 30, i==2 ? false : true, ""));
		
		List<TransactionExecutionResult> result4 = new ArrayList<TransactionExecutionResult>();
		resultList2.put("b", result4);
		for(int i =0; i<10; i++)
			result4.add(new TransactionExecutionResult("b",50 +  i *1000, 40, i==2 ? false : true, ""));
		
		Result r2 = new Result(resultList2);
	
		r.mergeResult(r2);
	
		assertEquals(40, r.getAmountOfTransactions());
		assertEquals(4, r.getAmountOfFails());
		assertEquals(10, r.getStart());
		assertEquals(9070, r.getEnd());
		assertEquals(9060, r.getDuration());
		
	}
}
