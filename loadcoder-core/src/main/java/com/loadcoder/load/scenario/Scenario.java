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

import com.loadcoder.load.scenario.Load.Transaction;
import com.loadcoder.load.scenario.Load.TransactionVoid;

public class Scenario {

	public <T> ResultHandlerVoidBuilder load(String defaultName, TransactionVoid transaction) {
		ResultHandlerVoidBuilder resultHandlerBuilder = new ResultHandlerVoidBuilder(transaction, null, null, null,
				null, defaultName, null);
		return resultHandlerBuilder;
	}

	public <T> ResultHandlerBuilder<T> load(String defaultName, Transaction<T> transaction) {
		ResultHandlerBuilder<T> resultHandlerBuilder = new ResultHandlerBuilder<T>(transaction, null, null, null, null,
				defaultName, null);
		return resultHandlerBuilder;
	}
}
