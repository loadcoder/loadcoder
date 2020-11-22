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

import com.loadcoder.load.scenario.Scenario;

/**
 * A super class to the TypeInstance classes instantiated through TypedLoadScenario.
 * The type instance object should hold the test logic where the Scenario will is 
 * required. This class works as a guideline of how to structure the type instances,
 * the scenario and the test logic.
 */
public class TypeInstanceBase {

	Scenario theActualScenario;

	public Scenario getScenario() {
		return theActualScenario;
	}

	public TypeInstanceBase(Scenario theActualScenario) {
		this.theActualScenario = theActualScenario;
	}
}
