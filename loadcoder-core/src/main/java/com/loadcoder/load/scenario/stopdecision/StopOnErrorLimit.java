/*******************************************************************************
 * Copyright (C) 2021 Team Loadcoder
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
package com.loadcoder.load.scenario.stopdecision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.scenario.RuntimeStatistics;
import com.loadcoder.load.scenario.StopDecision;

public class StopOnErrorLimit implements StopDecision {
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final RuntimeStatistics runtimeStatistics;
	double maxFailRate = -1;
	int maxAmountOfFails = -1;
	
	
	public StopOnErrorLimit(RuntimeStatistics runtimeStatistics) {
		this.runtimeStatistics = runtimeStatistics;
	}

	public boolean stopLoad(long startTime, long timesExecuted) {
		return stopDueToFailRate() || stopDueToAmountOfFails();
	}
	
	private boolean stopDueToFailRate() {
		if(maxFailRate < 0) {
			return false;
		}
		double failRate = runtimeStatistics.getFailRate();
		if (failRate > maxFailRate) {
			System.out.println("Fail rate too high:" + failRate);
			return true;
		}
		return false;
	}
	

	private boolean stopDueToAmountOfFails() {
		if(maxAmountOfFails < 0) {
			return false;
		}
		
		double amountOfFails = runtimeStatistics.getAmountOfFails();
		if (amountOfFails > maxAmountOfFails) {
			System.out.println("Too many fails:" + amountOfFails);
			return true;
		}
		return false;
	}
	
	
	public StopOnErrorLimit maxFailRate(double maxFailRate){
		this.maxFailRate = maxFailRate;
		return this;
	}
	
	public StopOnErrorLimit maxAmountOfFails(int maxAmountOfFails){
		this.maxAmountOfFails = maxAmountOfFails;
		return this;
	}
}
