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

	private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

	/**
	 * Change the date and time format of the directory that the method
	 * {@code getNewLogDir} creates
	 * 
	 * @param dateTimeFormatter
	 */
	public static void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
		format = dateTimeFormatter;
	}

	/**
	 * This method will be called from the test to set the result dir
	 * 
	 * @param sharedDirForLogsPath
	 */
	public static void setResultDirectory(String sharedDirForLogsPath) {
		setResultDestination(new File(sharedDirForLogsPath));
	}

	/**
	 * set the resultDir for your tests logs. For example
	 * {@code setResultDestination(new File("logs/mytest/"));}
	 * <p>
	 * 
	 * Also have a look at method {@code getNewLogDir} that helps out to create a
	 * unique dir for the new execution.
	 * <p>
	 * {@code setResultDestination(getNewLogDir("logs", "mytest"));}
	 * 
	 * @param sharedDirForLogs
	 */
	public static void setResultDestination(File sharedDirForLogs) {
		Logger initiateLogging = LoggerFactory.getLogger(LogbackUtils.class);
		initiateLogging.info("New Result destination:{}", sharedDirForLogs.getAbsolutePath());
		try {
			Logs.changeToSharedDir(sharedDirForLogs);
		} catch (IOException ioe) {
			throw new RuntimeException(
					String.format("Could not use the file %s in dir as a result destination", sharedDirForLogs), ioe);
		}
	}

	/**
	 * Helper method to get a unique directory for logs
	 * 
	 * @param rootDirPathForAllLogs
	 *            is the base directory for all your logs
	 * 
	 * @param nameOfTheTest
	 *            is the name of the directory where you want to store logs from all
	 *            executions for a particular test, which will be located inside {@code rootDirPathForAllLogs}
	 * 
	 * @return a File that will have a path according to following pattern:
	 *         {@code rootDirPathForAllLogs/nameOfTheTest/<date and time>(-<unique modifier>)}
	 */
	public static File getNewLogDir(String rootDirPathForAllLogs, String nameOfTheTest) {
		return getNewLogDir(rootDirPathForAllLogs + "/" + nameOfTheTest);
	}

	/**
	 * Helper method to get a unique directory for logs
	 * 
	 * @param dirForAllLogs
	 *            is the directory for all your logs
	 * 
	 * @return a File that will have a path according to following pattern:
	 *         {@code rootDirPathForAllLogs/nameOfTheTest/<date and time>(-<unique modifier>)}
	 */
	public static File getNewLogDir(String dirForAllLogs) {
		LocalDateTime timePoint = LocalDateTime.now();
		String dateTime = timePoint.format(format);

		File logDir = new File(dirForAllLogs + "/" + dateTime);

		int uniqueIterator = 2;
		while (logDir.exists()) {
			logDir = new File(dirForAllLogs + "/" + dateTime + "_" + uniqueIterator);
			uniqueIterator++;
		}
		return logDir;
	}
}
