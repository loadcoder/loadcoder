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
package com.loadcoder.load.scenario.design;

import com.loadcoder.load.scenario.Load.Transaction;
import com.loadcoder.load.scenario.Load.TransactionVoid;
import com.loadcoder.load.scenario.ResultHandlerBuilder;
import com.loadcoder.load.scenario.ResultHandlerVoidBuilder;
import com.loadcoder.load.scenario.Scenario;

public class ScenarioLogic {

	Scenario theActualScenario;
	
	public ScenarioLogic(Scenario scenario) {
		this.theActualScenario = scenario;
	}
	
	protected <T> ResultHandlerBuilder<T> load(String defaultName, Transaction<T> transaction) {
		return theActualScenario.load(defaultName, transaction);
	}

	protected ResultHandlerVoidBuilder load(String defaultName, TransactionVoid transaction) {
		return theActualScenario.load(defaultName, transaction);
	}

}
