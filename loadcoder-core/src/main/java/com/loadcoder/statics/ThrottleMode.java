/*******************************************************************************
 * Copyright (C) 2018 Team Loadcoder
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

public enum ThrottleMode {
	
	/**
	 * If using PER_THREAD, the intensity will be per thread.
	 * So if using PER_THREAD with 10 / SECOND and with two threads running,
	 * the total load will be 20 / SECOND
	 */
	PER_THREAD, 

	/**
	 * If using SHARED, the intensity will be shared among all threads.
	 * So if using SHARED with 10 / SECOND and with two threads running,
	 * the total load will be 10 / SECOND
	 */
	SHARED
}
