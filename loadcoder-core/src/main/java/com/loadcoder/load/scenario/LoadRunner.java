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
package com.loadcoder.load.scenario;

import static com.loadcoder.load.LoadUtility.sleep;

public class LoadRunner implements Runnable{
	
	private Load load;
	public LoadRunner(Load load){
		this.load = load;
	}
	public void run(){

		long rampUpSleepTime = calculateRampUpSleepTime(load.getRampup(), load.getAmountOfThreads());
		
		for (int i = 0; i < load.getAmountOfThreads(); i++) {

			Thread thread = load.getThreads().get(i);
			thread.start();

			// dont sleep after last thread has been started
			if (i + 1 != load.getAmountOfThreads()) {
				sleep(rampUpSleepTime);
			}
		}
	}
	
	private long calculateRampUpSleepTime(long rampup, int amountOfThreads) {
		long rampUpSleepTime = 0;
		if (amountOfThreads > 1)
			rampUpSleepTime = rampup / (amountOfThreads - 1);
		return rampUpSleepTime;
	}
}