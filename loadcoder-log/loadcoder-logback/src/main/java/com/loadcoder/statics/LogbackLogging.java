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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.result.Logs;
import com.loadcoder.utils.DateTimeUtil;
import com.loadcoder.utils.FileUtil;

public class LogbackLogging extends Logs {

	/**
	 * This method will be called from the test to set the result dir
	 * 
	 * @param sharedDirForLogsPath is the path to the new directory
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
	 * <p>
	 * 
	 * Note that in order for this method to work properly, an implementation of
	 * SharedDirFileAppender must be used and created through the used logging
	 * framework. A logback implementation is provided in loadcoder-logback module.
	 * 
	 * 
	 * @param sharedDirForLogs is the path to the new directory
	 */
	public static void setResultDestination(File sharedDirForLogs) {
		Logger initiateLogging = LoggerFactory.getLogger(LogbackLogging.class);
		try {
			Logs.changeToSharedDir(sharedDirForLogs);
			initiateLogging.info("New Result destination:{}", sharedDirForLogs.getAbsolutePath());
		} catch (IOException ioe) {
			throw new RuntimeException(
					String.format("Could not use the file %s in dir as a result destination", sharedDirForLogs), ioe);
		}
	}

	/**
	 * Helper method to get a unique directory for logs
	 * 
	 * @param rootDirPathForAllLogs is the base directory for all your logs
	 * 
	 * @param nameOfTheTest         is the name of the directory where you want to
	 *                              store logs from all executions for a particular
	 *                              test, which will be located inside
	 *                              {@code rootDirPathForAllLogs}
	 * 
	 * @return a File that will have a path according to following pattern:
	 *         {@code rootDirPathForAllLogs/nameOfTheTest/<date and time>(-<unique modifier>)}
	 */
	public static File getNewLogDir(String rootDirPathForAllLogs, String nameOfTheTest) {
		return getNewLogDir(rootDirPathForAllLogs + "/" + nameOfTheTest);
	}

	protected static File getLatestLogDir(String rootDirPathForAllLogs, String nameOfTheTest) {
		List<Path> directories = FileUtil.listDirectory(rootDirPathForAllLogs + "/" + nameOfTheTest);

		directories.sort((a, b) -> {
			FileTime aTime = FileUtil.getCreationDate(a);
			long aMillis = aTime.toMillis();

			FileTime bTime = FileUtil.getCreationDate(b);
			long bMillis = bTime.toMillis();
			long diff = bMillis - aMillis;
			int diffSec = (int) (diff / 1000);
			return diffSec;
		});

		Path latestPath = directories.get(0);
		File latestFile = latestPath.toFile();
		return latestFile;
	}

	public static File getLatestResultFile(String rootDirPathForAllLogs, String nameOfTheTest) {
		File f = getLatestLogDir("target", "simpleLoadTest");
		File resultFile = new File(f, Logs.RESULTFILE_DEFAULT);
		return resultFile;
	}

	/**
	 * Helper method to get a unique directory for logs
	 * 
	 * @param dirForAllLogs is the directory for all your logs
	 * 
	 * @return a File that will have a path according to following pattern:
	 *         {@code dirForAllLogs/<date and time>(-<unique modifier>)}
	 */
	public static File getNewLogDir(String dirForAllLogs) {
		String dateTime = DateTimeUtil.getDateTimeNowString();

		File logDir = new File(dirForAllLogs + "/" + dateTime);

		int uniqueIterator = 2;
		while (logDir.exists()) {
			logDir = new File(dirForAllLogs + "/" + dateTime + "_" + uniqueIterator);
			uniqueIterator++;
		}
		return logDir;
	}

}
