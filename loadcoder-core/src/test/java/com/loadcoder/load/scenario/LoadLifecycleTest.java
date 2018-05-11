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

import junit.framework.Assert;

import static com.loadcoder.statics.ContinueDesisions.*;

import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.exceptions.InvalidLoadStateException;
import com.loadcoder.load.intensity.PerTimeUnit;
import com.loadcoder.load.intensity.ThrottleMode;
import com.loadcoder.load.scenario.Load.LoadBuilder;
import com.loadcoder.load.testng.TestNGBase;

public class LoadLifecycleTest extends TestNGBase{

	@Test(expectedExceptions=InvalidLoadStateException.class)
	public void startTheSameLoadTwice() {
		
		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				load("t1", ()->{LoadUtility.sleep(100);}).perform();
			}
		};
		
		Load l = new LoadBuilder(ls)
				.continueCriteria(iterations(100))
				.intensity(3, PerTimeUnit.SECOND, ThrottleMode.SHARED)
				.build();

		l.runLoad();
		l.runLoad();
	}
	
	@Test(expectedExceptions=InvalidLoadStateException.class, expectedExceptionsMessageRegExp="E002.*")
	public void startALoadDifferentFromTheLastBeingBuiltTestForThatScenario() {
		
		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				load("t1", ()->{LoadUtility.sleep(100);}).perform();
			}
		};
		
		Load l = new LoadBuilder(ls)
				.continueCriteria(iterations(100))
				.intensity(3, PerTimeUnit.SECOND, ThrottleMode.SHARED)
				.build();
		
		new LoadBuilder(ls)
				.continueCriteria(iterations(100))
				.intensity(3, PerTimeUnit.SECOND, ThrottleMode.SHARED)
				.build();
		
		l.runLoad();
	}

	@Test
	public void buildNewLoadBeforePreviousLoadFinished() {
		
		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				LoadUtility.sleep(1000);
			}
		};
		
		Load l = new LoadBuilder(ls).continueCriteria(iterations(1)).build();

		l.runLoad();
		
		try {
			new LoadBuilder(ls).continueCriteria(iterations(1)).build();

			Assert.fail("expected an InvalidLoadStateException here");
		}catch(InvalidLoadStateException ilse) {}	
		
		LoadUtility.sleep(2000);
		new LoadBuilder(ls).continueCriteria(iterations(1)).build();

	}

}
