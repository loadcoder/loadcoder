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

import static com.loadcoder.load.exceptions.ExceptionMessages.LoadAlreadyStarted;
import static com.loadcoder.load.exceptions.ExceptionMessages.ScenarioConnectedToOtherLoad;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.exceptions.InvalidLoadStateException;
import com.loadcoder.load.exceptions.NoResultOrFormatterException;
import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.result.Result;
import com.loadcoder.result.ResultFormatter;
import com.loadcoder.result.TransactionExecutionResult;
import com.loadcoder.statics.Formatter;

public class Execution {

	Logger log = LoggerFactory.getLogger(this.getClass());

	Thread runtimeResultUpdaterThread;

	final RuntimeResultUser user;
	final ResultFormatter resultFormatter;

	StartedExecution startedExecution;
	List<Load> loads;

	private long startTime;
	
	TransactionExecutionResultBuffer transactionExecutionResultBuffer = new TransactionExecutionResultBuffer();
	
	List<List<TransactionExecutionResult>> runtimeResultList = new ArrayList<List<TransactionExecutionResult>>();

	protected ResultFormatter getResultFormatter(){
		return resultFormatter;
	}
	
	public List<Load> getLoads() {
		return loads;
	}

	protected Thread getRuntimeResultUpdaterThread(){
		return runtimeResultUpdaterThread;
	}
	
	protected List<List<TransactionExecutionResult>> getRuntimeResultList() {
		return runtimeResultList;
	}
	
	protected TransactionExecutionResultBuffer getTransactionExecutionResultBuffer() {
		return transactionExecutionResultBuffer;
	}
	
	public Execution(ResultFormatter resultFormatter, RuntimeResultUser user, List<Load> loads) {
		this.resultFormatter = resultFormatter == null ? Formatter.SIMPLE_RESULT_FORMATTER : resultFormatter;
		this.user = user;
		this.loads = loads;
		loads.stream().forEach((load)->{load.setExecution(this);});
		if(user != null) {
			runtimeResultUpdaterThread = new Thread(new RuntimeResultUpdaterRunner(this, user));
		}	
	}
	
//	public StartedExecution execute() {
//		if(runtimeResultUpdaterThread != null) {
//			runtimeResultUpdaterThread.start();
//		}
//		
//	}
	
	
	/**
	 * Start the load
	 * 
	 * @return
	 * a StartedLoad instance
	 */
	public synchronized StartedExecution execute() {
		
		for(Load load : loads) {
			if (load.getStartedLoad() != null) {
				throw new InvalidLoadStateException(LoadAlreadyStarted.toString());
			}
			
			Load setLoad = load.getLoadScenario().getLoad();
			if(! setLoad.equals(load)) {
				throw new InvalidLoadStateException(ScenarioConnectedToOtherLoad.toString());
			}
		}

		start();

		if(runtimeResultUpdaterThread != null) {
			runtimeResultUpdaterThread.start();
		}
		
		for(Load l : loads) {
			l.runLoad();
		}

		startedExecution = new StartedExecution(this);
		return startedExecution;
	}
	
	protected void start(){
		this.startTime = System.currentTimeMillis();
	}
	
	protected Result getRuntimeResult() throws NoResultOrFormatterException{
		Result r = new Result(getRuntimeResultList());
		return r;
	}

	public long getStartTime() {
		return startTime;
	}
	
}
