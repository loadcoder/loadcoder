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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadRunner implements Runnable{

	private static Logger log = LoggerFactory.getLogger(ThreadRunner.class);
	
	private final Load load;
	private long loadStartTime;
	ThreadRunner(Load load){
		this.load = load;
	}
	ThreadLocal<?> ls = new ThreadLocal<Object>();
	public void run(){
		log.debug("Thread {} started" + Thread.currentThread());
		LoadScenario ls = load.getLoadScenario();
		
		loadStartTime = load.getStartTime();
		ls.pre();
		while (decideIfContinue()) {
			try {
				ls.loadScenario();
			} catch (RuntimeException rte) {
				log.info("LoadScenario threw exception {}. Test thread will continue!", rte);
				continue;
			}
		}
		ls.post();
	}
	
	protected boolean decideIfContinue() {
		boolean result = false;
		synchronized (load) {
			result = load.getContinueToExecute().continueToExecute(loadStartTime, load.getTimesExecuted());
			if (result)
				load.increaseTimesExecuted();
		}
		return result;
	}
	
}
