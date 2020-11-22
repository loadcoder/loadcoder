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

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LoadScenarioTypedTest {

	@Test
	public void testTwoTypedLoadScenario() {

		List<Object> verifier = new ArrayList<Object>();

		LoadScenarioTyped<Object> ls = new LoadScenarioTyped<Object>() {

			@Override
			public void loadScenario(Object t) {

			}

			@Override
			public Object createInstance() {
				// TODO Auto-generated method stub
				Object o = new Object();
				synchronized (this) {
					verifier.add(o);
				}
				return o;
			}
		};

		new ExecutionBuilder(new LoadBuilder(ls).amountOfThreads(2).build()).build().execute().andWait();
		Assert.assertEquals(verifier.size(), 2);
		Assert.assertNotEquals(verifier.get(0), verifier.get(1));
		verifier.clear();

		new ExecutionBuilder(new LoadBuilder(ls).build()).build().execute().andWait();
		Assert.assertEquals(verifier.size(), 1);

	}
}
