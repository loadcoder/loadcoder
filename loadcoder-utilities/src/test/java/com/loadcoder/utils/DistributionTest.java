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
package com.loadcoder.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.loadcoder.utils.distribution.Distribution;
import com.loadcoder.utils.distribution.Item;

public class DistributionTest {

	@Test
	public void testDistribution() {

		boolean fooFound = false;
		boolean barFound = false;
		for (int i = 0; i < 20; i++) {
			String choise = Distribution.getRandomByWeight(new Item<>("foo", 1), new Item<>("bar", 1));
			if (choise.equals("foo")) {
				fooFound = true;
			} else {
				barFound = true;
			}
			if (fooFound && barFound) {
				break;
			}
		}
		if (!(fooFound && barFound)) {
			fail("didnt find both Items in the Distribution");
		}

		String choiseSingle = Distribution.getRandomByWeight(new Item<>("foo", 1));
		assertThat(choiseSingle, is("foo"));

		String choiseOnlyOneWeight = Distribution.getRandomByWeight(new Item<>("foo", 1), new Item<>("bar", 0));
		assertThat(choiseOnlyOneWeight, is("foo"));

		try {
			Distribution.getRandomByWeight(new Item<>("foo", 0));
			fail("Expected an exception here");
		} catch (RuntimeException rte) {
		}

		List<Item<String>> itemList = Arrays.asList(new Item<>("foo", 1), new Item<>("bar", 0));

		String itemFromItemArray = Distribution.getRandomByWeight(itemList);
		assertThat(itemFromItemArray, is("foo"));
	}

}
