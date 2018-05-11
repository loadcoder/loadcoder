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
package com.loadcoder.logback;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.log.ResultLogger;

import static com.loadcoder.statics.Logging.*;

public class SharedDirFileAppenderLogbackTest extends TestNGBase{
	public static Logger resultLogger = LoggerFactory.getLogger(SharedDirFileAppenderLogbackTest.class);
	
	@Test
	public void testLogging(Method method) {
		File rootDir = new File("target/logs/" + method.getName() + "/" + System.currentTimeMillis());
		File firstDir = new File(rootDir.getAbsolutePath() + "/first");
		File firstLogFile = new File(firstDir.getAbsolutePath() + "/result-logback.log");

		Logger log = ResultLogger.resultLogger;
		setResultDestination(firstDir);

		log.info("foo");
		try {
			List<String> rows = LoadUtility.readFile(firstLogFile);
			Assert.assertEquals(rows.size(), 1);
			Assert.assertEquals(rows.get(0), "foo");
		}catch(IOException ioe) {
			Assert.fail("Could not get content from the file firstDir" + firstLogFile, ioe);
		}
		
		File secondDir = new File(rootDir.getAbsolutePath() + "/second");
		File secondLogFile = new File(secondDir.getAbsolutePath() + "/result-logback.log");
		setResultDestination(secondDir);
		log.info("bar");
		try {
			List<String> rows = LoadUtility.readFile(secondLogFile);
			Assert.assertEquals(rows.size(), 1);
			Assert.assertEquals(rows.get(0), "bar");
		}catch(IOException ioe) {
			Assert.fail("Could not get content from the file firstDir" + secondLogFile, ioe);
		}
	}
}
