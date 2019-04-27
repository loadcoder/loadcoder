/*******************************************************************************
 * Copyright (C) 2019 Stefan Vahlgren at Loadcoder
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

import static com.loadcoder.statics.StopDesisions.iterations;
import static com.loadcoder.statics.ThrottleMode.*;
import static com.loadcoder.statics.Time.*;
import static com.loadcoder.statics.Time.SECOND;

import org.testng.annotations.Test;

import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.scenario.TypedLoadScenario;

public class TypedLoadScenarioTest {

	public static class ThreadInstance {
		private LoadScenario ls;

		public LoadScenario getLs() {
			return ls;
		}

		public ThreadInstance(LoadScenario ls) {
			System.out.println("created a new ThreadInstance");
			this.ls = ls;
		}
	}

	@Test
	public void testThreadInstance() {

		TypedLoadScenario<ThreadInstance> ls = new TypedLoadScenario<ThreadInstance>() {

			public void loadScenario(ThreadInstance t) {
				testLogic(t);
			}

			@Override
			public ThreadInstance createInstance() {
				return new ThreadInstance(this);
			}

			@Override
			public void preThreadExecution(ThreadInstance t) {
			}

			@Override
			public void postThreadExecution(ThreadInstance t) {
			}

		};

		Load l = new LoadBuilder(ls).amountOfThreads(10).stopDecision(iterations(10))
				.throttleIterations(1, PER_SECOND, SHARED).rampup(10 * SECOND).build();

		new ExecutionBuilder(l).build().execute().andWait();

	}

	void testLogic(ThreadInstance t) {
		t.getLs().load("foo", () -> {
			System.out.println("foo" + Thread.currentThread());
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
		TypedLoadScenario<ThreadInstanceStopper> ls = new TypedLoadScenario<ThreadInstanceStopper>() {

			@Override
			public ThreadInstanceStopper createInstance() {
				return new ThreadInstanceStopper(this, customStopDecision);
			}

			@Override
			public void loadScenario(ThreadInstanceStopper t) {
				testLogic(t);
			}
		};

		new ExecutionBuilder(new LoadBuilder(ls).stopDecision(customStopDecision).build()).build().execute().andWait();
	}

	void testLogic(ThreadInstanceStopper threadInstanceStopper) {

		if (1 == 1) {
			threadInstanceStopper.getCustomStopDecision().stop();
		}
	}
}
