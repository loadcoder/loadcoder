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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.TestUtility;
import com.loadcoder.load.chart.logic.Chart;
import com.loadcoder.load.chart.logic.ResultChart;
import com.loadcoder.load.chart.logic.RuntimeChart;
import com.loadcoder.load.intensity.ThrottleMode;
import com.loadcoder.load.measure.Result;
import com.loadcoder.load.measure.TransactionExecutionResult;
import com.loadcoder.load.scenario.FinishedLoad;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.scenario.StartedLoad;
import com.loadcoder.load.scenario.Load.LoadBuilder;
import com.loadcoder.load.sut.DomainDto;
import com.loadcoder.load.sut.SUT;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.log.Logs;
import com.loadcoder.statics.SummaryUtils;

import static com.loadcoder.statics.Time.*;
public class FullTest extends TestNGBase{

	Logger log = LoggerFactory.getLogger(FullTest.class);
	
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

		Load l = new LoadBuilder(s).throttle(1, PerSecond, ThrottleMode.PER_THREAD).build();
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

		Load l = new LoadBuilder(s).throttle(1, PerSecond, ThrottleMode.PER_THREAD).build();
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
	public void benchmarkOnlyLog(Method method){
		
		setResultDestination(getNewLogDir(rootResultDir, method.getName()));
		
		Logger logsLog = LoggerFactory.getLogger(Logs.class);
		long start = System.currentTimeMillis();
		while(System.currentTimeMillis() < start + 20_000) {
			logsLog.info("<t name=\"fast\" ts=\"1527536832574\" rt=\"0\" status=\"true\"/>");
		}
	}
	
	@Test(groups = "manual")
	public void highestPossibleLoad(Method method){
		
		setResultDestination(getNewLogDir(rootResultDir, method.getName()));
		
		LoadScenario ls = new LoadScenario() {
			SUT sut = new SUT();
			@Override
			public void loadScenario() {
				load("fast", ()->{}).perform();
			}
		};
		
		RuntimeChart chart = new RuntimeChart(); 
		FinishedLoad finised = new LoadBuilder(ls)
				.continueCriteria(duration(20 * SECOND))
				.amountOfThreads(1)
				.resultUser(chart)
				.build().runLoad().andWait();
		
		SummaryUtils.printSimpleSummary(finised.getReportedResultFromResultFile(), method.getName());
		
		chart.waitUntilClosed();
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
				.throttle(23, PerMinute, ThrottleMode.SHARED)
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
				.throttle(20, PerMinute, ThrottleMode.SHARED)
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
				.throttle(2, PerMinute, ThrottleMode.SHARED)
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
	
		FinishedLoad finished = started.andWait();
		Result result = finished.getReportedResultFromResultFile();
		ResultChart resultChart = new ResultChart(result);
		
		
		
		SummaryUtils.printSimpleSummary(result, "simleTest");
		runtimeChart.waitUntilClosed();
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
	

	
	
}
