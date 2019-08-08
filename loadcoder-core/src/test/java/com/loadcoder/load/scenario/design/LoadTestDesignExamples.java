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
package com.loadcoder.load.scenario.design;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.FinishedExecution;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.scenario.Scenario;
import com.loadcoder.load.scenario.StopDecision;
import com.loadcoder.load.scenario.LoadScenarioTyped;
import com.loadcoder.result.Result;
import com.loadcoder.statics.StopDecisions;
import com.loadcoder.statics.ThrottleMode;
import com.loadcoder.statics.Time;
import com.loadcoder.statics.TimeUnit;

public class LoadTestDesignExamples {

	public class TypeInstance extends TypeInstanceBase {

		TypedLoadLogic logic;
		int i;

		TypeInstance(Scenario tls) {
			super(tls);
			/*
			 * The logic instance is constructed using this TypeInstance, making the thread
			 * specific entities available in the logic code
			 */
			logic = new TypedLoadLogic(this);
		}

		TypedLoadLogic getLogic() {
			return logic;
		}

		public int getI() {
			return i;
		}

		public void setI(int i) {
			this.i = i;
		}
	}

	static int i = 0;

	synchronized int getI() {
		return i++;
	}

	class TypedLoadLogic extends ScenarioLogicTyped<TypeInstance> {

//		TypeInstance t;

		public TypedLoadLogic(TypeInstance t) {
			/*
			 * The scenario must be passed on to the superclass ScenarioLogic in order to
			 * have the scenario available in the logic when the transactions are made
			 */
			super(t);
		}

		void loadLogic() {

			load("foo", () -> {
				System.out.println(typeInstance.getI());
			}).perform();

		}
	}

	class LoadLogic extends ScenarioLogic {

		public LoadLogic(Scenario s) {
			super(s);
		}

		void loadLogic() {

			load("foo", () -> {
				System.out.println("bar");
			}).perform();

		}
	}

	/**
	 * This test exemplifies a best practice to implement a load test where a
	 * TypedLoadScenario is used and the test logic is broken out into separate
	 * classes.
	 */
	@Test
	public void testTypeInstanceAndLoadLogic() {

		/*
		 * The scenario is of TypeInstance which is a subclass TypeInstanceBase. This
		 * lets the user call a method called load and this will in its turn call the
		 * scenarios real load method.
		 * 
		 * Furthermore, the instance of TypeInstance keeps the test logic instance(s) so
		 * they can be either uniquely created for each thread, or shared between them.
		 */
		LoadScenarioTyped<TypeInstance> ls = new LoadScenarioTyped<TypeInstance>() {

			@Override
			public TypeInstance createInstance() {
				return new TypeInstance(this);
			}

			@Override
			public void loadScenario(TypeInstance t) {
				// modify the type instance
				t.setI(5);
				// get the values
				int i = t.getI();
				// call the logic implemented in other classes
				t.getLogic().loadLogic();

				/*
				 * Of course its possible to still have test logic directly in the loadScenario
				 * implementation.
				 */
				load("bar", () -> {
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).build();

		FinishedExecution finishedExecution = new ExecutionBuilder(l).storeResultRuntime().build().execute().andWait();

		Result result = finishedExecution.getResultFromMemory();
		assertEquals(result.getAmountOfTransactions(), 2);

	}

	/**
	 * This test exemplifies a best practice to implement an untyped load test where a
	 * LoadScenario is used and the test logic is broken out into separate classes.
	 */
	@Test
	public void testLoadLogic() {

		LoadScenario ls = new LoadScenario() {
			/*
			 * The LoadLogic may be defined either inside or outside the scenario.
			 * All threads will use the same instance
			 */
			LoadLogic ll = new LoadLogic(this);

			@Override
			public void loadScenario() {
				ll.loadLogic();
			}
		};

		Load l = new LoadBuilder(ls).build();

		FinishedExecution finishedExecution = new ExecutionBuilder(l).storeResultRuntime().build().execute().andWait();

		Result result = finishedExecution.getResultFromMemory();
		assertEquals(result.getAmountOfTransactions(), 1);

	}

	/**
	 * This test exemplifies a best practice how to test a load test logic that has
	 * been broken out in separate classes, without the need to start the real load
	 * test. The use case for this is if you want to verify that your test logic
	 * works where the test logic code can be executed much more conveniently
	 */
	@Test
	public void testLoadLogicAlone() {

		/*
		 * The TypedLoadLogic is dependent on a TypeInstance, which we first create
		 * using an empty Scenario.
		 */
		TypeInstance t = new TypeInstance(new Scenario());

		TypedLoadLogic ll = t.getLogic();
		ll.loadLogic();
		t.setI(4);
		ll.loadLogic();

		// testing that an untyped LoadLogic can be tested directly as well.
		LoadLogic ll2 = new LoadLogic(t.getScenario());
		ll2.loadLogic();
	}

	@Test(groups = "manual")
	public void testThatLoadWorksWithTheLoadLogic() {

		LoadScenarioTyped<TypeInstance> ls = new LoadScenarioTyped<TypeInstance>() {

			@Override
			public TypeInstance createInstance() {
				return new TypeInstance(this);
			}

			@Override
			public void loadScenario(TypeInstance t) {
				t.getLogic().loadLogic();
				load("bar", () -> {
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).throttle(10, Time.PER_SECOND, ThrottleMode.SHARED)
				.stopDecision(StopDecisions.iterations(5)).build();

		FinishedExecution finishedExecution = new ExecutionBuilder(l).storeResultRuntime().build().execute().andWait();

		Result result = finishedExecution.getResultFromMemory();
		assertEquals(result.getAmountOfTransactions(), 10);

	}

}
