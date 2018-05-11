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
package com.loadcoder.load.sut;


import static com.loadcoder.load.LoadUtility.sleep;
import static com.loadcoder.load.TestUtility.random;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.LoadUtility;

public class SUT {
	
	 Logger log = LoggerFactory.getLogger(this.getClass());
		
	long referenseResponseTime = 100;
	
	Map<Object, Long> lastResponseTimePerTracker = new HashMap<Object, Long>();
	void sleepWithDeviationFromLast(Object tracker){
		Long last = lastResponseTimePerTracker.get(tracker);
		if(last == null){
			last = referenseResponseTime;
		}else{
			last =last + random(-20, 20);
		}
		if(last < 3)
			last = 3L;
		lastResponseTimePerTracker.put(tracker, last);
		
		sleep( last); 
	}
	
	double cosCounter =0;
	long getCosRt(long xBase){
		cosCounter = cosCounter + 0.01;
		double modifier = 100*Math.cos(cosCounter);
		return (long)( xBase + modifier);
	}
	
	public void sleepCos(){
		sleepCos(200);
	}
	
	public void sleepCos(long xBase){
		LoadUtility.sleep(getCosRt(xBase));
	}
	
	Object trackerObject = new Object();
	public void methodWhereResponseTimeFollowSomeKindOfPattern(){
		sleepWithDeviationFromLast(trackerObject);
	}
	
	public void methodWhereResponseTimeFollowSomeKindOfPattern(Object o){
		sleepWithDeviationFromLast(o);
	}
	
	long trackerObject2 = 100;
	public void methodWhereResponseTimeIncreases(){
		LoadUtility.sleep(trackerObject2 += 50);
	}
	
	void sleepWithDeviation(long sleepTime){
		sleep( sleepTime + random(0, (int) sleepTime/4)); 
	}
	
	public void methodThatTakesLongerTime(){
		sleepWithDeviation(referenseResponseTime*2); 
	}
	
	
	public DomainDto getDomainDto(){
		return new DomainDto();
	}
	
	public void methodThatTakesBetweenTheseResponseTimes(long min, long max){
		sleep( random((int)min, (int)max)); 
	}
	
	public void methodThatTakesShorterTime(){
		sleepWithDeviation(referenseResponseTime /4); 
	}
	
	public void methodThatTakesNoTime(){
		sleep(1000);
		return;
	}
	
	public void loggingMethod(){
		log.info("logged by SUT in method loggingMethod");
		return;
	}
	
	Object methodThatSomeTimesThrowsRuntimeExceptionTrackerObject = new Object();
	public void methodThatSomeTimesThrowsRuntimeException(int percent){
		if(random(1, 100) <= percent )
			throw new RuntimeException("a RuntimeException occured in SUT");
		sleepWithDeviationFromLast(methodThatSomeTimesThrowsRuntimeExceptionTrackerObject);
		
	}
	
	public void methodThatSomeTimesThrowsCheckedException() throws IOException{
		sleep(random(40, 100));
		if(random(0, 100) == 5)
			throw new IOException("a RuntimeException occured in SUT");
	}
	
}
