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

import java.util.Arrays;

import com.loadcoder.result.ResultFormatter;

public class ExecutionBuilder {
	ResultFormatter resultFormatter;
	RuntimeResultUser user;
	final Load[] loads;

	public ExecutionBuilder runtimeResultUser(RuntimeResultUser user) {
		this.user = user;
		return this;
	}

	public ExecutionBuilder resultFormatter(ResultFormatter resultFormatter) {
		this.resultFormatter = resultFormatter;
		return this;
	}

	public ExecutionBuilder(Load... loads) {
		this.loads = loads;
	}

	public Execution build() {
		return new Execution(resultFormatter, user, Arrays.asList(loads));
	}
}