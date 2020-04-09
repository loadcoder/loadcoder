/*******************************************************************************
 * Copyright (C) 2020 Stefan Vahlgren at Loadcoder
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
package com.loadcoder.cluster.clients;

import static com.loadcoder.statics.Statics.*;
public class ClientUtils {
	
	public static String getHostValue(String configVariableName){
		String configVariableValue = getConfiguration(configVariableName);
		return configVariableValue == null ? "master" : configVariableValue;
	}

	public interface Statable <T> {
		boolean statement();
	}
	public static <T> void throwIfTrue(Statable<T> statable, String message) {
		if(statable.statement()) {
			throw new RuntimeException(message);
		}
	}
}
