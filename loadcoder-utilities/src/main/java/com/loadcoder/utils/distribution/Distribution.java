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
package com.loadcoder.utils.distribution;

import java.util.Arrays;
import java.util.List;

import com.loadcoder.load.LoadUtility;

public class Distribution {

	public static <T> T getRandomByWeight(List<Item<T>> items) {
		int totalWeight = calculateTotalWeightForItems(items);
		if (totalWeight < 1) {
			throw new RuntimeException("Total weight for all Distribution Items must above 0, but was " + totalWeight);
		}
		int weightIndex = LoadUtility.random(0, totalWeight);
		Item<T> item = getItemForWeightIndex(weightIndex, items);

		return item.getObject();
	}

	@SafeVarargs
	public static <T> T getRandomByWeight(Item<T>... items) {
		List<Item<T>> list = Arrays.asList(items);
		return getRandomByWeight(list);
	}

	private static <T> Item<T> getItemForWeightIndex(int weightIndex, List<Item<T>> items) {
		int iteratedIndexes = 0;
		for (Item<T> item : items) {
			int executionsPerHour = item.getWeight();
			iteratedIndexes += executionsPerHour;
			if (weightIndex <= iteratedIndexes) {
				return item;
			}
		}
		throw new RuntimeException(
				"Internal error in DomainDistribution:getRandomCaseBasedOnDistribution. Something is buggy with this calculation");
	}

	private static <T> int calculateTotalWeightForItems(List<Item<T>> items) {
		int calculateTotalPerHour = 0;
		for (Item<T> item : items) {
			calculateTotalPerHour += item.getWeight();
		}
		return calculateTotalPerHour;
	}
}
