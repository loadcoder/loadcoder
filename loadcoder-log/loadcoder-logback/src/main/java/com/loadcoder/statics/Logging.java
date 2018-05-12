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

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.log.Logs;
import com.loadcoder.logback.LogbackUtils;

public class Logging {

	private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
	
	/**
	 * This method will be called from the test to set the result dir
	 * @param sharedDirForLogsPath
	 */
	public static void setResultDirectory(String sharedDirForLogsPath){
		setResultDestination(new File(sharedDirForLogsPath));
	}
	
	/**
	 * This method will be called from the test to set the result dir
	 * @param sharedDirForLogs
	 */
	public static void setResultDestination(File sharedDirForLogs){
		Logger initiateLogging = LoggerFactory.getLogger(LogbackUtils.class);
		initiateLogging.info("New Result destination:{}", sharedDirForLogs.getAbsolutePath());
		try{
			Logs.changeToSharedDir(sharedDirForLogs);
		}catch(IOException ioe){
			throw new RuntimeException(String.format(
					"Could not use the file %s in dir as a result destination", sharedDirForLogs), ioe);
		}
	}
	
	public static File getNewLogDir(String rootDirPathForAllLogs, String nameOfTheTest){
		return getNewLogDir(rootDirPathForAllLogs + "/" + nameOfTheTest);
	}
	
	/**
	 * @return File to directory with a nicely formatted name
	 * Nifty method in order to get a File with a path to a new directory with nicely formatted name
	 */
	public static File getNewLogDir(String rootDirPathForAllLogs){
		LocalDateTime timePoint = LocalDateTime.now();     
		String dateTime = timePoint.format(format);
				
		File logDir = new File(rootDirPathForAllLogs  + "/" + dateTime);

		int uniqueIterator = 2;
		while(logDir.exists()){
			logDir = new File(rootDirPathForAllLogs + "/" + dateTime + "_" +uniqueIterator);
			uniqueIterator++;
		}
		return logDir;
	}
}
