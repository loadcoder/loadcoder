/*******************************************************************************
 * Copyright (C) 2021 Team Loadcoder
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeStatistics implements RuntimeResultConsumer {

	int fails = 0;
	int amountOfRequests = 0;

	@Override
	public void useData(Map<String, List<TransactionExecutionResult>> transactionsMap) {
		synchronized (this) {
			Iterator<Entry<String, List<TransactionExecutionResult>>> it = transactionsMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, List<TransactionExecutionResult>> entry = it.next();
				List<TransactionExecutionResult> list = entry.getValue();
				for (TransactionExecutionResult trans : list) {
					if (!trans.isStatus()) {
						fails++;
					}
				}
				amountOfRequests += list.size();
			}
		}

	}

	public double getFailRate() {
		synchronized (this) {
			if (amountOfRequests == 0) {
				return 0;
			} else {
				return ((double) fails) / amountOfRequests;
			}
		}
	}

	public int getAmountOfFails() {
		synchronized (this) {
			return fails;
		}
	}

}
