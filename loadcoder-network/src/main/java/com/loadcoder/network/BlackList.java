/*******************************************************************************
 * Copyright (C) 2020 Team Loadcoder
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
package com.loadcoder.network;

import java.util.List;

public class BlackList {

	Matcher matcher;
	List<String> blackList;

	public BlackList(Matcher matcher, List<String> blackList) {
		this.matcher = matcher;
		this.blackList = blackList;
	}

	public boolean blackListed(String url) {
		for (String match : blackList) {
			if (matcher.match(url, match)) {
				return true;
			}
		}
		return false;
	}
}
