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
package com.loadcoder.load;

import java.util.List;

import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.result.TransactionExecutionResult;

public class TestUtils extends BaseTest {

	public static LoadScenario s = new LoadScenario() {
		@Override
		public void loadScenario() {
			load("t1", ()->{return new Exception();})
			.handleResult((a)->{
				})
			.perform();
		}
	};
	
	public static void add(List<TransactionExecutionResult> buffer, long timeBackInHistory, int amountOfTransactions){
		long start = System.currentTimeMillis();
		long end = start - timeBackInHistory;
		long iterator = start;
		double cosCounter = 0;
		for(; iterator > end; iterator = iterator - 500){
			cosCounter = cosCounter + 0.03;
			
			for(int i=0; i<amountOfTransactions; i++){
			TransactionExecutionResult result = 
					new TransactionExecutionResult("prehistoric" +i, iterator, (long)(100+ i*100 + 20*Math.cos(cosCounter* (i+1))), true, "");
			buffer.add(result);
			}
	
		}
	}
}
