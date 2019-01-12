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
package com.loadcoder.result;

import java.io.File;
import java.io.IOException;

@FunctionalInterface
public interface SharedDirFileAppender{
	
	
	/**
	 * Method to change to directory for which this appender writes its logfiles to
	 * @param newDir is the new directory where the log file should be written from here on.
	 * @throws IOException is thrown if there is some problem to use the new log directory
	 */
	public void changeToDir(File newDir) throws IOException;
}
