/*******************************************************************************
 * Copyright (C) 2020 Team Loadcoder
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
package com.loadcoder.network;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class GenerateLoadTestFromHarTest {

	@Test(groups = "manual")
	public void readFileTest() {
		LoadTestGenerator generator = new LoadTestGenerator();
		generator.generate("src/test/resources/loadcoder.har", "target/newtest" + System.currentTimeMillis());
	}

	@Test
	public void blackListTest() {
		LoadTestGenerator generator = new LoadTestGenerator();
		boolean blackListed = generator.blackListed("http://loadcoder.com/loadcoder.js");
		boolean notBlackListed = generator.blackListed("http://loadcoder.com/apirequest");

		assertEquals(blackListed, true);
		assertEquals(notBlackListed, false);
	}

}
