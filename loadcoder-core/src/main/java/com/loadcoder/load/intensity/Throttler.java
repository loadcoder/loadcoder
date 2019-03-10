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
package com.loadcoder.load.intensity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.statics.ThrottleMode;

public class Throttler {

	private final Map<Thread, RateLimiter> limiters;
	private final RateLimiter limiter;

	/**
	 * Constructor for Throttler
	 * @param intensity is the limit for the maximum throughput through this Throttler
	 * @param threads is the Threads that are going to be throttled
	 */
	public Throttler(Intensity intensity, List<Thread> threads){
		if(intensity.getThrottleMode().equals(ThrottleMode.PER_THREAD)){
			Map<Thread, RateLimiter> temporaryMap = new HashMap<Thread, RateLimiter>();
			limiter = null;
			double amountPerSecond = LoadScenario.getAmountPerSecond(intensity);
			for(Thread t : threads){
				RateLimiter limiterForThisThread = RateLimiter.create(amountPerSecond);
				temporaryMap.put(t, limiterForThisThread);
			}
			limiters = Collections.unmodifiableMap(temporaryMap);
		}else{
			double amountPerSecond = LoadScenario.getAmountPerSecond(intensity);
			limiter = RateLimiter.create(amountPerSecond);
			limiters = null;
		}
	}
	
	/**
	 * @param t is the thread that is going to be throttled
	 * @return the RateLimiter instance that does the actual limitation of the throughput
	 */
	public RateLimiter getRateLimiter(Thread t){
		if(limiter != null){
			return limiter;
		}else{
			RateLimiter limiterForThisThread = limiters.get(t);
			return limiterForThisThread;
		}
	}
	
	
	
}
