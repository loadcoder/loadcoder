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
package com.loadcoder.logback;

import java.io.File;
import java.io.IOException;

import com.loadcoder.result.Logs;
import com.loadcoder.result.SharedDirFileAppender;

import ch.qos.logback.core.FileAppender;

/**
 * An extension of FileAppender. The purpose of this class is to have a fileappender with the ability
 * to change the filepath for it and every other instances of its type at once.
 * This is useful when executing multiple tests where the logs of each test must be separated
 */
public class SharedDirFileAppenderLogback extends FileAppender implements SharedDirFileAppender{
	
	public SharedDirFileAppenderLogback(){
		synchronized(Logs.sharedDirFileAppenders) {
			Logs.sharedDirFileAppenders.add(this);	
		}
	}

	public void changeToDir(File newDir) throws IOException{
		String filename = getFile();
		File file = new File(filename);

		String nameOfTheFile = file.getName();
		String newFile = newDir + "/" + nameOfTheFile;
		openFile(newFile);
	}

}
