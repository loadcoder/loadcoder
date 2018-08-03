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

	final private Map<Thread, RateLimiter> limiters;
	final private RateLimiter limiter;

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
	
	public RateLimiter getRateLimiter(Thread t){
		if(limiter != null){
			return limiter;
		}else{
			RateLimiter limiterForThisThread = limiters.get(t);
			return limiterForThisThread;
		}
	}
	
	
	
}
