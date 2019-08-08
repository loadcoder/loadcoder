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

import static com.loadcoder.statics.StopDesisions.iterations;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.exceptions.InvalidLoadStateException;
import com.loadcoder.load.testng.TestNGBase;

public class ExecutionTest extends TestNGBase {

	@Test(expectedExceptions = InvalidLoadStateException.class, expectedExceptionsMessageRegExp = "E001.*")
	public void testToStartAlreadyStartedExecution() {

		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				load("quick", () -> {
				}).perform();
			}
		};
		Load l = new LoadBuilder(ls).stopDecision(iterations(1)).build();
		Execution e = new ExecutionBuilder(l).resultFormatter(null).build();
		e.execute().andWait();

		/*
		 * should throw exception since it shall not be possible to execute the same
		 * execution more than once
		 */
		e.execute();
	}

	/**
	 * Tests that a InvalidLoadStateException is thrown when executing a load
	 * created before another load was created with the same LoadScenario.
	 */
	@Test(expectedExceptions = InvalidLoadStateException.class, expectedExceptionsMessageRegExp = "E002.*")
	public void startAgain() {

		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				load("slow", () -> LoadUtility.sleep(200)).perform();
			}
		};
		Load l = new LoadBuilder(ls).stopDecision(iterations(5)).build();
		new LoadBuilder(ls).stopDecision(iterations(5)).build();

		Execution e = new ExecutionBuilder(l).build();
		e.execute();
	}

	@Test
	public void buildNewLoadBeforePreviousLoadFinished() {

		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				LoadUtility.sleep(500);
			}
		};

		Load l = new LoadBuilder(ls).stopDecision(iterations(1)).build();
		new ExecutionBuilder(l).build().execute();

		try {
			new LoadBuilder(ls).stopDecision(iterations(1)).build();

			fail("expected an InvalidLoadStateException here");
		} catch (InvalidLoadStateException ilse) {
			assertThat(ilse.getMessage(), containsString("E003"));
		}

		LoadUtility.sleep(1000);
		new LoadBuilder(ls).stopDecision(iterations(1)).build();

	}
}
