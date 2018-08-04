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
package com.loadcoder.statics;


public class Time { 
	
	/**
	 * One second in milliseconds
	 */
	public static final long SECOND = 1_000; 
	
	/**
	 * One minute in milliseconds
	 */
	public static final long MINUTE = 60 * SECOND;
	
	/**
	 * One hour in milliseconds
	 */
	public static final long HOUR = 60 * MINUTE;
	
	/**
	 * One day in milliseconds
	 */
	public static final long DAY = 24 * HOUR;

	public static TimeUnit PER_SECOND = TimeUnit.SECOND;
	public static TimeUnit PER_MINUTE = TimeUnit.MINUTE;
	public static TimeUnit PER_HOUR = TimeUnit.HOUR;
}
