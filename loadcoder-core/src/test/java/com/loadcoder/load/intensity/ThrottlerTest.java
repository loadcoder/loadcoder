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

import static com.loadcoder.statics.Statics.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.testng.TestNGBase;

public class ThrottlerTest extends TestNGBase{
	
	@Test
	public void sharedPerThreadModeTest(){

		List<Thread> threads = new ArrayList<Thread>();
		Thread t1 = Thread.currentThread();
		threads.add(t1);
		Thread t2 = new Thread();
		threads.add(t2);
		
		Throttler t = new Throttler(new Intensity(1, PER_SECOND, SHARED), threads);
		RateLimiter r = t.getRateLimiter(t1);	
		RateLimiter r2 = t.getRateLimiter(t2);	
		assertEquals(r, r2);
		t = new Throttler(new Intensity(1, PER_SECOND, PER_THREAD), threads);
		r = t.getRateLimiter(t1);	
		r2 = t.getRateLimiter(t2);	
		assertNotEquals(r, r2);
	
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
		assertTrue(diff < 200);

		for(int i =0; i<3; i++) {
			start = System.currentTimeMillis();
			limiter.acquire();
			limiter2.acquire();
			diff = System.currentTimeMillis() - start;
			assertTrue(diff < 1200 && diff > 800);
		}
	}
	
	@Test(groups = "timeconsuming")
	public void testIterationThrottler() {
		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {}
		};
		Load l = new LoadBuilder(ls)
				.stopDecision(iterations(3))
				.throttleIterations(1, PER_SECOND, SHARED)
				.amountOfThreads(1)
				.build();
		long start = System.currentTimeMillis();
		new ExecutionBuilder(l).build().execute().andWait();
		long diff = System.currentTimeMillis() - start;
		assertTrue(diff > 1500 && diff < 2500);
	}

	@Test(groups = "timeconsuming")
	public void testIterationThrottlerPerThread() {
		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {}
		};
		Load l = new LoadBuilder(ls)
				.stopDecision(iterations(9))
				.throttleIterations(1, PER_SECOND, PER_THREAD)
				.amountOfThreads(3)
				.build();
		long start = System.currentTimeMillis();
		new ExecutionBuilder(l).build().execute().andWait();
		long diff = System.currentTimeMillis() - start;
		assertTrue(diff > 1500 && diff < 2500);
	}

}
