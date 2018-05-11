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

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.testng.TestNGBase;

public class ThrottlerTest extends TestNGBase{
	
	@Test
	public void testRateLimiter2(){

		List<Thread> threads = new ArrayList<Thread>();
		threads.add(Thread.currentThread());
		new Throttler(new Intensity(1, PerTimeUnit.SECOND, ThrottleMode.SHARED), threads);
	}
	
	@Test(groups = "timeconsuming")
	public void testRateLimiter(){
		RateLimiter limiterForThisThread = RateLimiter.create(1);
		long start =System.currentTimeMillis();
		for(int i = 0; i<3; i++){
			limiterForThisThread.acquire();
		}
		long diff = System.currentTimeMillis() - start;
		Assert.assertTrue(diff> 1_500 && diff < 2_500);
	}
	
	@Test(groups = "timeconsuming")
	public void testManyThreadWithTheirOwnRateLimiter(){

		RateLimiter limiter = RateLimiter.create(1);
		RateLimiter limiter2 = RateLimiter.create(1);

		long start = System.currentTimeMillis();
		limiter.acquire();
		limiter2.acquire();
		long diff = System.currentTimeMillis() - start;
		Assert.assertTrue(diff < 200);

		for(int i =0; i<3; i++) {
			start = System.currentTimeMillis();
			limiter.acquire();
			limiter2.acquire();
			diff = System.currentTimeMillis() - start;
			Assert.assertTrue(diff < 1200 && diff > 800);
		}
	}
	
}
