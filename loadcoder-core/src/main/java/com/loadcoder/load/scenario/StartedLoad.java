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

import java.lang.Thread.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.exceptions.NoResultOrFormatterException;
import com.loadcoder.load.measure.Result;

public class StartedLoad {

	Logger log = LoggerFactory.getLogger(this.getClass());
	
	Load l;

	public StartedLoad(Load l){
		this.l = l;
		l.getLoadStateThread().start();
	}

	public static Result getResult(StartedLoad startedScenario){
		return startedScenario.getRuntimeResult();
	}
	
	protected Result getRuntimeResult() throws NoResultOrFormatterException{
		Result r = new Result(l.getRuntimeResultList());
		return r;
	}		
	
	public boolean isScenarioTerminated(){
		return l.getLoadStateThread().getState() == State.TERMINATED;
	}
	
	public boolean isScenarioRunning(){
		
		State state = l.getLoadStateThread().getState();
		return ! (state == State.TERMINATED || state == State.NEW);
	}
	
	public FinishedScenario andWait(){
		long start = System.currentTimeMillis();
		try{
			l.getLoadStateThread().join();
		}catch(InterruptedException ie){
			log.error("Unexpected InterruptedException caught", ie);
		}
		
		if(l.getRuntimeResultUpdaterThread() != null) {
			l.getRuntimeResultUpdaterThread().interrupt();
		}
		log.debug("Load executed {} ms", (System.currentTimeMillis() - start));
		return new FinishedScenario(l);
	}
	
}

