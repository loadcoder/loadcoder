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
package com.loadcoder.load.chart.logic;

import java.util.ArrayList;
import java.util.List;

import com.loadcoder.load.chart.logic.ResultChartTest.YGiver;
import com.loadcoder.result.TransactionExecutionResult;

public class ResultChartTestUtility {

	public static List<List<TransactionExecutionResult>> getTranses(int amount) {
		return getTranses(amount, 1000);
	}

	public static List<List<TransactionExecutionResult>> getTranses2(long[]... is) {

		List<List<TransactionExecutionResult>> listOfLists = new ArrayList<List<TransactionExecutionResult>>();
		List<TransactionExecutionResult> ttanses = new ArrayList<TransactionExecutionResult>();
		listOfLists.add(ttanses);
		for (long[] i : is) {
			TransactionExecutionResult trans = new TransactionExecutionResult("a", i[0], i[1], true, null);
			ttanses.add(trans);
		}
		return listOfLists;
	}

	public static List<List<TransactionExecutionResult>> getTranses(int amount, long timeBetweenTransactions) {
		return getTranses(amount, timeBetweenTransactions, (i) -> {
			return i + 1;
		});
	}

	public static List<List<TransactionExecutionResult>> getTranses(int amount, long timeBetweenTransactions,
			YGiver y) {
		return getTranses(amount, 1, timeBetweenTransactions, y);
	}

	public static List<List<TransactionExecutionResult>> getTranses(int amount, int serieses,
			long timeBetweenTransactions, YGiver y) {

		List<List<TransactionExecutionResult>> listOfLists = new ArrayList<List<TransactionExecutionResult>>();
		for (int j = 0; j < serieses; j++) {
			List<TransactionExecutionResult> ttanses = new ArrayList<TransactionExecutionResult>();
			listOfLists.add(ttanses);

			long start = 1000;
			for (int i = 0; i < amount; i++) {
				TransactionExecutionResult trans = new TransactionExecutionResult("a" + j,
						start + i * timeBetweenTransactions, j * 10 + y.y(i), true, null,
						Thread.currentThread().getName());
				ttanses.add(trans);
			}
		}
		return listOfLists;
	}

}
