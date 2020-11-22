/*******************************************************************************
 * Copyright (C) 2019 Team Loadcoder
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

import static com.loadcoder.statics.Statics.iterations;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.load.TestUtility;
import com.loadcoder.load.testng.TestNGBase;

public class TypedLoadScenarioTest extends TestNGBase {

	Logger log = LoggerFactory.getLogger(this.getClass());

	public class ThreadInstance {
		private LoadScenario ls;

		public LoadScenario getLs() {
			return ls;
		}

		public ThreadInstance(LoadScenario ls) {
			log.info("created a new ThreadInstance");
			this.ls = ls;
		}
	}

	@Test
	public void testThreadInstance() {
		List<ThreadInstance> instances = new ArrayList<ThreadInstance>();
		LoadScenarioTyped<ThreadInstance> ls = new LoadScenarioTyped<ThreadInstance>() {

			public void loadScenario(ThreadInstance t) {
				testLogic(t);
			}

			@Override
			public ThreadInstance createInstance() {
				ThreadInstance t = new ThreadInstance(this);
				TestUtility.addValueToList(instances, t);
				return t;
			}

			@Override
			public void preThreadExecution(ThreadInstance t) {
			}

			@Override
			public void postThreadExecution(ThreadInstance t) {
			}

		};

		Load l = new LoadBuilder(ls).amountOfThreads(10).stopDecision(iterations(10)).build();

		new ExecutionBuilder(l).resultFormatter(null).storeResultRuntime().build().execute().andWait();
		assertEquals(instances.size(), 10);

	}

	void testLogic(ThreadInstance t) {
		t.getLs().load("foo", () -> {
			log.info("foo" + Thread.currentThread());
		}).perform();
	}

	class CustomStopDecision implements StopDecision {

		boolean stop = false;

		@Override
		public boolean stopLoad(long startTime, long timesExecuted) {
			// TODO Auto-generated method stub
			return stop;
		}

		public void stop() {
			this.stop = true;
		}

	}

	class ThreadInstanceStopper {

		final CustomStopDecision customStopDecision;
		LoadScenario ls;

		ThreadInstanceStopper(LoadScenario ls, CustomStopDecision stop) {
			this.ls = ls;
			this.customStopDecision = stop;
		}

		public CustomStopDecision getCustomStopDecision() {
			return customStopDecision;
		}

		public LoadScenario getLs() {
			return ls;
		}
	}

	@Test
	public void testToPassTheLoadInstanceThroughTheThreadInstance() {

		CustomStopDecision customStopDecision = new CustomStopDecision();
		LoadScenarioTyped<ThreadInstanceStopper> ls = new LoadScenarioTyped<ThreadInstanceStopper>() {

			@Override
			public ThreadInstanceStopper createInstance() {
				return new ThreadInstanceStopper(this, customStopDecision);
			}

			@Override
			public void loadScenario(ThreadInstanceStopper t) {
				testLogic(t);
			}
		};

		new ExecutionBuilder(new LoadBuilder(ls).stopDecision(customStopDecision).build()).resultFormatter(null)
				.storeResultRuntime().build().execute().andWait();
	}

	void testLogic(ThreadInstanceStopper threadInstanceStopper) {

		threadInstanceStopper.getCustomStopDecision().stop();
	}
}
