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
package com.loadcoder.load.utils;

import java.util.Random;

import org.slf4j.Logger;

import com.loadcoder.load.measure.ResultFormatter;
import com.loadcoder.load.measure.TransactionExecutionResult;
import com.loadcoder.log.ResultLogger;

public class ResultGenerator {

	final static Logger log = ResultLogger.resultLogger;
	
	static Random rn = new Random();
	private static int random(int min, int max){
		int result = rn.nextInt(max - min + 1) + min;
		return result;
	}
	
	public static void generateResult(long duration, int amountOfTypes){
		
		ResultFormatter formatter = TransactionExecutionResult.resultStringFormatterDefault;
		
		long start = System.currentTimeMillis();
		long timeBetweenTransactionsWitinTypeStart = 100;
		long timeBetweenTransactionsWitinType = timeBetweenTransactionsWitinTypeStart;		

		long iterator = start;
		String logBuffer = "";
		int counter =1;
		
		while(iterator < start + duration){
			for(int i=0; i<amountOfTypes; i++){
				boolean status = true;
				if(i==0){
					status = ! ( random(1, 10) == 5); // false is match
				}
				long rt = i * 50 + random(1, 100);
				
				if(!status)
					rt = 0;
				
				TransactionExecutionResult t = new TransactionExecutionResult("a" +i, iterator, rt , status, "");
				
				String lineToLog = formatter.toString(t);
				logBuffer += lineToLog +"\n";
				iterator += timeBetweenTransactionsWitinType;
			}
			if(counter++ % 5 ==0){
				counter =1;
				
				log.info(logBuffer);
				logBuffer = "";
			}
			
		}
	}
}
