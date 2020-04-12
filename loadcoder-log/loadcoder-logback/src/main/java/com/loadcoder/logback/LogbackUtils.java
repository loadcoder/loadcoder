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

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

public class LogbackUtils {

	/**
	 * USE WITH CAUTION
	 * It not a good idea to create an appender programmatically for the resultlogger.
	 * When the result logger is instantiated, it will pick up the appenders from the logback.xml,
	 * if no specific logger are defined the root logger will be picked up which results in that
	 * the results will be logged out to all the root appenders
	 * 
	 * @param resultFile is the file that the appender will log to
	 * @return a new instance of SharedDirFileAppenderLogback
	 */
	@Deprecated
	public static SharedDirFileAppenderLogback getSharedDirFileAppender(File resultFile){
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();

        ple.setPattern("%m%n");
        ple.setContext(lc);
        ple.start();

		SharedDirFileAppenderLogback fileAppender = new SharedDirFileAppenderLogback();
        fileAppender.setFile(resultFile.getAbsolutePath());
        fileAppender.setEncoder(ple);
        fileAppender.setContext(lc);
        fileAppender.setAppend(false);
        fileAppender.start();
        return fileAppender;
	}
}
