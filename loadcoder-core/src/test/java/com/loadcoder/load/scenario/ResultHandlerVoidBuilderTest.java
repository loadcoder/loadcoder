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

import org.testng.annotations.Test;

import com.google.common.util.concurrent.RateLimiter;
import com.loadcoder.load.scenario.Load.LoadBuilder;
import com.loadcoder.load.testng.TestNGBase;

public class ResultHandlerVoidBuilderTest extends TestNGBase{

	@Test
	public void create() {
		
		LoadScenario ls = new LoadScenario() {
			
			@Override
			public void loadScenario() {
			}
		};
		
		new LoadBuilder(ls).build();
		RateLimiter rl = RateLimiter.create(1);
		new ResultHandlerVoidBuilder("t1", ()->{}, ls, rl);

	}
	
	
}
