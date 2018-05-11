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
package com.loadcoder.load;

import static com.loadcoder.statics.ContinueDesisions.*;
import static com.loadcoder.statics.Milliseconds.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.intensity.PerTimeUnit;
import com.loadcoder.load.intensity.ThrottleMode;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.scenario.StartedLoad;
import com.loadcoder.load.scenario.Load.ContinueDecision;
import com.loadcoder.load.scenario.Load.LoadBuilder;
import com.loadcoder.load.testng.TestNGBase;

public class LoadTest extends TestNGBase{

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void testDefault(Method m){
		
		LoadScenario ls = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				load("t1", () -> {return "";}).perform();
			}
		};
		
		Load l = new LoadBuilder(ls)
				.build();
		l.runLoad().andWait();
	}
	
	@Test(groups = "timeconsuming")
	public void testDuration(){
		long duration = 3 * 1000;
		long startTime = System.currentTimeMillis();
		ContinueDecision continueToExecute = duration(duration);
		
		Assert.assertTrue(continueToExecute.continueToExecute(startTime, 0));
		LoadUtility.sleep(2000);
		Assert.assertTrue(continueToExecute.continueToExecute(startTime, 0));
		LoadUtility.sleep(1500);
		Assert.assertFalse(continueToExecute.continueToExecute(startTime, 0));
		
		LoadScenario ls = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				LoadUtility.sleep(100);
			}
		};

		Load l = new LoadBuilder(ls).continueCriteria(duration(duration)).build();
		long start = System.currentTimeMillis();
		l.runLoad().andWait();
		long end = System.currentTimeMillis();
		long diff = end - start;
		long faultMargin = 1000;

		String message = "Duration was " + duration + ". Diff was " +diff + " ms";
		Assert.assertTrue(diff > duration - faultMargin, message);
		Assert.assertTrue(diff < duration + faultMargin, message);
	}

	@Test
	public void testThreadsAndIterations(){
		
		List<Thread> list = new ArrayList<Thread>();
		List<Object> o = new ArrayList<Object>();
		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
					LoadUtility.sleep(1);
					synchronized (list) {
						if(!list.contains(Thread.currentThread()))
							list.add(Thread.currentThread());
					}
					synchronized (o) {
						o.add(new Object());
					}
				}
		};

		Load l = new LoadBuilder(ls)
				.amountOfThreads(10)
				.continueCriteria(iterations(1000))
				.build();
		
		l.runLoad().andWait();
		Assert.assertEquals(list.size(), 10);
		Assert.assertEquals(o.size(), 1000);
		
	}
	
	@Test(groups = "timeconsuming")
	public void testRampup(){
		List<Thread> list = new ArrayList<Thread>();
		
		LoadScenario ls = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				LoadUtility.sleep(100);
				synchronized (list) {
					if(!list.contains(Thread.currentThread())){
						list.add(Thread.currentThread());
					}
				}

			}
		};

		Load l = new LoadBuilder(ls)
				.continueCriteria(duration(12_000))
				.rampup(10 * SECOND)
				.amountOfThreads(3)
				.build();
		
		StartedLoad started = l.runLoad();
		LoadUtility.sleep(1000);
		Assert.assertEquals(list.size(), 1);
		LoadUtility.sleep(5000); // 6sec past
		Assert.assertEquals(list.size(), 2);
		LoadUtility.sleep(3000); //9sec past
		Assert.assertEquals(list.size(), 2);
		LoadUtility.sleep(2000); //11sec past
		Assert.assertEquals(list.size(), 3);
		started.andWait();
	}
	
	@Test(groups = "timeconsuming")
	public void testOneThrottle(Method m){

		int iterationsPerThread = 5;
		LoadScenario ls = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				load("t1", () -> {return "";}).perform();
			}
		};
		
		Load l = new LoadBuilder(ls)
				.intensity(1, PerTimeUnit.SECOND, ThrottleMode.PER_THREAD)
				.continueCriteria(iterations(iterationsPerThread))
				.build();
		
		
		long start = System.currentTimeMillis();
		l.runLoad().andWait();
		long end = System.currentTimeMillis();
		
		//set to a high value since it's affected by the asyncronous wait in Scenarion andWait
		long faultMargin = 1000;

		long target = (5-1 )*1000;
		long diff = end - start;
		//assert that the iterations divided at multiple threads don't take too less or too long time
		Assert.assertTrue(diff > target - faultMargin && diff < target + faultMargin,
				"diff was:" +diff + " ms. Target is " + target);
	}
	
	@Test(groups = "timeconsuming")
	public void testMultipleThreadsWithThrottleModePerThread(Method m){

		List<Object> list = new ArrayList<Object>();
		int threads = 4;
		int iterationsPerThread = 4;
		
		LoadScenario ls = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				LoadUtility.sleep(100);

				synchronized (list) {
						list.add(new Object());
				}
				load("t1", () -> {return "";}).perform();
			}
		};

		Load l = new LoadBuilder(ls)
				.continueCriteria(iterations(threads * iterationsPerThread))
				.intensity(1, PerTimeUnit.SECOND, ThrottleMode.PER_THREAD)
				.amountOfThreads(threads)
				.build();

		long start = System.currentTimeMillis();
		l.runLoad().andWait();
		long end = System.currentTimeMillis();
		long diff = end - start;

		//assert that the test is correct
		Assert.assertEquals(list.size(), threads * iterationsPerThread);

		//set to a high value since it's affected by the asyncronous wait in Scenarion andWait
		long faultMargin = 1000;

		long target = (iterationsPerThread-1 )*1000;
		//assert that the iterations divided at multiple threads don't take too less or too long time
		Assert.assertTrue(diff > target - faultMargin && diff < target + faultMargin,
				"diff was:" +diff + " ms. Target is " + target);
	}
}
