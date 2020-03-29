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
package com.loadcoder.statics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DockerConfigurationHelper {

	public static Set<String> getAllNodeIds() {
		return getAllNodeIds(Configuration.getConfiguration());
	}

	protected static Set<String> getAllNodeIds(Map<String, String> configuration) {

		Set<String> result = new HashSet<>();
		configuration.entrySet().stream().forEach(entry -> {
			String key = entry.getKey();
			if (key.matches("node\\..*\\.host")) {
				String id = key.replace("node.", "").replace(".host", "");
				result.add(id);
			}
		});
		return result;
	}
}
