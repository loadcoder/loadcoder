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
import com.loadcoder.load.scenario.TypedLoadScenario;
import com.loadcoder.result.Result;

public class LoadTestDesignExamples {

	public class TypeInstance extends TypeInstanceBase {

		TypedLoadLogic logic;
		int i;

		TypeInstance(Scenario tls) {
			super(tls);
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

	class TypedLoadLogic extends ScenarioLogic {

		TypeInstance t;

		public TypedLoadLogic(TypeInstance t) {
			super(t.getScenario());
			this.t = t;
		}

		void loadLogic() {

			load("foo", () -> {
				System.out.println(t.getI());
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

	@Test
	public void testTypeInstanceAndLoadLogic() {

		TypedLoadScenario<TypeInstance> ls = new TypedLoadScenario<TypeInstance>() {

			@Override
			public TypeInstance createInstance() {
				return new TypeInstance(this);
			}

			@Override
			public void loadScenario(TypeInstance t) {
				t.setI(5);
				t.getLogic().loadLogic();

			}
		};

		Load l = new LoadBuilder(ls).build();

		FinishedExecution finishedExecution = new ExecutionBuilder(l).storeResultRuntime().build().execute().andWait();

		Result result = finishedExecution.getResultFromMemory();
		assertEquals(result.getAmountOfTransactions(), 1);

	}

	@Test
	public void testLoadLogic() {

		LoadScenario ls = new LoadScenario() {

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

	@Test
	public void testLoadLogicAlone() {

		TypeInstance t = new TypeInstance(new Scenario());
		TypedLoadLogic ll = new TypedLoadLogic(t);
		LoadLogic ll2 = new LoadLogic(t.getScenario());

		ll.loadLogic();
		t.setI(4);
		ll.loadLogic();

		ll2.loadLogic();
	}
}
