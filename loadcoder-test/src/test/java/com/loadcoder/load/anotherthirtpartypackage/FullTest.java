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
package com.loadcoder.load.anotherthirtpartypackage;

import static com.loadcoder.statics.ContinueDesisions.*;
import static com.loadcoder.statics.Logging.*;
import static com.loadcoder.statics.Milliseconds.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.TestUtility;
import com.loadcoder.load.chart.logic.Chart;
import com.loadcoder.load.chart.logic.ResultChart;
import com.loadcoder.load.chart.logic.RuntimeChart;
import com.loadcoder.load.intensity.PerTimeUnit;
import com.loadcoder.load.intensity.ThrottleMode;
import com.loadcoder.load.measure.Result;
import com.loadcoder.load.measure.TransactionExecutionResult;
import com.loadcoder.load.result.SummaryUtils;
import com.loadcoder.load.scenario.FinishedScenario;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.scenario.StartedLoad;
import com.loadcoder.load.scenario.Load.LoadBuilder;
import com.loadcoder.load.sut.DomainDto;
import com.loadcoder.load.sut.SUT;
import com.loadcoder.load.testng.TestNGBase;

public class FullTest extends TestNGBase{

	@Test(groups = "manual")
	public void create(Method method){

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));
		List<?> list = new ArrayList<Exception>();

		LoadScenario s = new LoadScenario() {

			@Override
			public void loadScenario() {
				
				ThreadLocal<Exception> tl = new ThreadLocal<Exception>();
				load("t1", ()->{return new NullPointerException();})
				.handleResult((a)->{
					//getters
					Exception e = a.getException();
					long rt = a.getResponseTime();
					NullPointerException npe = a.getResponse();
					
					//setters
					a.changeTransactionName("newTransactionName");
					a.setStatus(false);
					a.reportTransaction(true);
					a.setMessage("message for the report");
				})
				.perform();
				
				
				load("t1", ()->{/*nothing to return*/})
					.handleResult((a)->{
						//getters
						Exception e = a.getException();
						long rt = a.getResponseTime();
						
						//setters
						a.changeTransactionName("newTransactionName");
						a.setStatus(false);
						a.reportTransaction(true);
						a.setMessage("message for the report");
					})
					.perform();
			}
		};

		Load l = new LoadBuilder(s).intensity(1, PerTimeUnit.SECOND, ThrottleMode.PER_THREAD).build();
		l.runLoad().andWait();
		
	}
	
	@Test(groups = "manual")
	public void createLoadScenarioPreAndPost(Method method){

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));
		List<?> list = new ArrayList<Exception>();

		ThreadLocal<Exception> threadLocal = new ThreadLocal<Exception>(); 
		LoadScenario s = new LoadScenario() {

			@Override
			public void pre(){
				threadLocal.set(new Exception());
			}
			
			@Override
			public void post(){
			}
			
			@Override
			public void loadScenario() {
				
				threadLocal.get();
				load("t1", ()->{return new NullPointerException();})
				.handleResult((a)->{
					//getters
					Exception e = a.getException();
					long rt = a.getResponseTime();
					NullPointerException npe = a.getResponse();
					
					//setters
					a.changeTransactionName("newTransactionName");
					a.setStatus(false);
					a.reportTransaction(true);
					a.setMessage("message for the report");
				})
				.perform();
				
				
				load("t1", ()->{/*nothing to return*/})
					.handleResult((a)->{
						//getters
						Exception e = a.getException();
						long rt = a.getResponseTime();
						
						//setters
						a.changeTransactionName("newTransactionName");
						a.setStatus(false);
						a.reportTransaction(true);
						a.setMessage("message for the report");
					})
					.perform();
			}
		};

		Load l = new LoadBuilder(s).intensity(1, PerTimeUnit.SECOND, ThrottleMode.PER_THREAD).build();
		l.runLoad().andWait();
	}
	
	@Test(groups = "manual")
	public void oneTransaction(Method method){
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();
			@Override
			public void loadScenario() {
				load("t2", ()->{sut.sleepCos();}).perform();
			}
		};
		
		Load l = new LoadBuilder(ls)
				.resultUser(new RuntimeChart())
				.build();
		
	}
	
	@Test(groups = "manual")
	public void testRuntimeChart2(Method method){
		RuntimeChart chart = new RuntimeChart();
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();
			@Override
			public void loadScenario() {
				load("t2", ()->{LoadUtility.sleep(TestUtility.random(300, 400));}).perform();
			}
		};
		
		new LoadBuilder(ls)
				.continueCriteria(duration(300_000))
				.intensity(23, PerTimeUnit.MINUTE, ThrottleMode.SHARED)
				.resultUser(chart)
				.build().runLoad().andWait();
		chart.waitUntilClosed();
		
	}
	
	@Test(groups = "manual")
	public void testSurrounding(Method method){
		RuntimeChart chart = new RuntimeChart();
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();
			@Override
			public void loadScenario() {
				load("t2", ()->{LoadUtility.sleep(TestUtility.random(2, 10));}).perform();
			}
		};
		
		new LoadBuilder(ls)
				.intensity(20, PerTimeUnit.MINUTE, ThrottleMode.SHARED)
				.continueCriteria(duration(300_000))
				.resultUser(chart)
				.build().runLoad().andWait();
		chart.waitUntilClosed();
		
	}
	
	@Test(groups = "manual")
	public void testRuntimeChart(Method method){
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();
			@Override
			public void loadScenario() {
				load("t2", ()->{sut.sleepCos(130);}).perform();
			}
		};
		
		Load l = new LoadBuilder(ls)
				.continueCriteria(duration(300_000))
				.resultUser(new RuntimeChart())
				.amountOfThreads(2)
				.build();

		l.runLoad().andWait();
	}
	
	@Test(groups = "manual")
	public void twoScenariosTest(Method method){
		
		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("t1", () -> {
					LoadUtility.sleep(20);
				}).perform();
			}
		};
		LoadScenario ls2 = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				load("t2", ()->{LoadUtility.sleep(20);}).perform();
			}
		};
		
		Load l = new LoadBuilder(ls)
				.intensity(2, PerTimeUnit.SECOND, ThrottleMode.SHARED)
				.continueCriteria(duration(60_000))
				.resultUser(new RuntimeChart())
				.build();
	}
	
	@Test(groups = "manual")
	public void testDynamicChart(Method method){

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));

		SUT sut = new SUT();
		LoadScenario s = new LoadScenario() {

			@Override
			public void loadScenario() {

				load("t1", ()->{ sut.sleepCos(); })
				.handleResult((a)->{
				}).perform();
				load("t2", ()->{sut.methodThatTakesBetweenTheseResponseTimes(200, 220); return "";})
				.handleResult((a)->{
				}).perform();

				load("t3", ()-> sut.methodThatTakesBetweenTheseResponseTimes(300, 320))
				.handleResult((a)->{
				})
				.perform();
				
				load("sometimesFails", ()->{sut.methodThatSomeTimesThrowsCheckedException();})
				.handleResult((a)->{
				})
				.perform();
				
				DomainDto dto = load("getDomain", ()->sut.getDomainDto())
				.handleResult((a)->{
				})
				.perform();
			}
		};
		
		RuntimeChart runtimeChart = new RuntimeChart();
		Load l = new LoadBuilder(s)
		.continueCriteria(duration(1 * MINUTE))
		.amountOfThreads(10)
		.rampup(30 * SECOND)
		.resultUser(runtimeChart)
		.build();
		
		StartedLoad started = l.runLoad();
	
		FinishedScenario finished = started.andWait();
		Result result = finished.getReportedResultFromResultFile();
		ResultChart resultChart = new ResultChart(result);
		
		SummaryUtils.printSimpleSummary(result, "simleTest");
		runtimeChart.waitUntilClosed();
	}
	
	@Test(groups = "manual")
	public void sendInPreHistoricResultsIntoRuntimeChart_NPE(Method method){

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));
		LoadScenario s = new LoadScenario() {
			@Override
			public void loadScenario() {
				
				SUT sut = new SUT();
				load("t1", ()->{sut.methodThatTakesBetweenTheseResponseTimes(100, 120); return "";})
				.handleResult((a)->{
				})
				.perform();
			}
		};
		Load l = new LoadBuilder(s)
				.continueCriteria(duration(500_000))
				.resultUser(new RuntimeChart())
				.build();

		add(l.getTransactionExecutionResultBuffer().getBuffer(), 50000_000, 1);
		
		StartedLoad started = l.runLoad();
		started.andWait();
		
	}
	
	@Test(groups = "manual")
	public void sendInPreHistoricResultsIntoRuntimeChart(Method method){

		setResultDestination(getNewLogDir(rootResultDir, method.getName()));
		LoadScenario s = new LoadScenario() {
			@Override
			public void loadScenario() {
				SUT sut = new SUT();
				load("t1", ()->{sut.methodThatTakesBetweenTheseResponseTimes(100, 120); return "";})
				.handleResult((a)->{
				})
				.perform();
			}
		};
		Load l = new LoadBuilder(s)
				.continueCriteria(duration(300_000))
				.resultUser(new RuntimeChart())
				.build();

		add(l.getTransactionExecutionResultBuffer().getBuffer(), 20000_000, 2);		
		
		StartedLoad started = l.runLoad();
		started.andWait();
		
	}
	
	@Test(groups = "manual")
	public void testResultChart(Method method) {
		setResultDestination(getNewLogDir(rootResultDir, method.getName()));
		LoadScenario s = new LoadScenario() {
			@Override
			public void loadScenario() {
				SUT sut = new SUT();
				load("t1", () -> {
					sut.methodThatTakesBetweenTheseResponseTimes(100, 120);
					return "";
				}).handleResult((a) -> {
				}).perform();
				
				load("t2", () -> {
					sut.methodThatTakesBetweenTheseResponseTimes(200, 250);
					return "";
				}).handleResult((a) -> {}).perform();
			}
		};
		Load l = new LoadBuilder(s)
				.continueCriteria(duration(30_000))
				.resultUser(new RuntimeChart())
				.build();
		
		
		StartedLoad started = l.runLoad();
		
		Result result = started.andWait().getReportedResultFromResultFile();

		Chart c = new ResultChart(result);
		c.waitUntilClosed();

	}

	@Test(groups = "manual")
	public void readResultForResultChartTest() {
		Result r = new Result(new File("src/test/resources/testresults/2min.log"));
		Chart c = new ResultChart(r);
		c.waitUntilClosed();
	}
	
	@Test(groups = "manual")
	public void readResult8HBeforeGMTForResultChartTest() {
		Result r = new Result(new File("src/test/resources/testresults/result_8H_before_GMT.log"));
		Chart c = new ResultChart(r);
		c.waitUntilClosed();
	}
	public void add(List<TransactionExecutionResult> buffer, long timeBackInHistory, int amountOfTransactions){
		long start = System.currentTimeMillis();
		long end = start - timeBackInHistory;
		long iterator = start;
		double cosCounter = 0;
		for(; iterator > end; iterator = iterator - 500){
			cosCounter = cosCounter + 0.03;
			
			for(int i=0; i<amountOfTransactions; i++){
			TransactionExecutionResult result = 
					new TransactionExecutionResult("prehistoric" +i, iterator, (long)(100+ i*100 + 20*Math.cos(cosCounter* (i+1))), true, "");
			buffer.add(result);
			}
	
		}
	}
	
	
}
