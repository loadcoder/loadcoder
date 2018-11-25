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
package com.loadcoder.load.exceptions;

public enum ExceptionMessages {

	LOAD_ALREADY_STARTED("E001", "This Load instance has already been started once. "
			+ "Build another Load instace and start that one instead"),

	SCENARIO_BELONGS_TO_OTHER_LOAD("E002", "There is another loadtest connected to the Scenario trying to be executed"),

	PREVIOUS_LOAD_STILL_RUNNING("E003", "There is a previously started Load that is still execting this Scenario."
	+ "Wait until the loadtest is finished before starting another loadtest using the same Scenario");

	private String id;
	private String message;

	private ExceptionMessages(String id, String message){
		this.id = id;
		this.message = message;
	}
	
	public String toString() {
		return String.format("%s: %s", id, message);
	}
}
