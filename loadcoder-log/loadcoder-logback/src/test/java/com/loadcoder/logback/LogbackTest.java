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

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.qos.logback.classic.Logger;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Logs;

public class LogbackTest extends TestNGBase{

	String rootLogDir = "target";
	
	public static SharedDirFileAppenderLogback getFileAppender(String filename){
		SharedDirFileAppenderLogback theResultSharedDirFileAppender = LogbackUtils.getSharedDirFileAppender(new File(filename));
		return theResultSharedDirFileAppender;
	}

	@Test
	public void test(Method method) throws IOException {

		String dirPath = rootLogDir + "/" +method.getName();
		String logFileName =  "1.log";
		String logFileName2 = "2.log";
		String fullPathFile = dirPath + "/" + logFileName;
		String fullPathFile2 = dirPath + "/" + logFileName2;
		
		/*
		 * Create 2 Appenders with Loggers and verify that they are appending to 
		 * separate files
		 */
		SharedDirFileAppenderLogback sharedDirFileAppender = getFileAppender(fullPathFile);
		SharedDirFileAppenderLogback sharedDirFileAppender2 = getFileAppender(fullPathFile2);

		Logger logger = (Logger)LoggerFactory.getLogger(this.getClass() +"1");
		logger.addAppender(sharedDirFileAppender);

		Logger logger2 = (Logger) LoggerFactory.getLogger(this.getClass() +"2");
		logger2.addAppender(sharedDirFileAppender2);

		String msg = "foo1";
		logger.info(msg);
		String msg2 = "bar2";
		logger2.info(msg2);

		dirPath = rootLogDir + "/newDir";
		String fullPathFile3 = dirPath + "/" + logFileName;
		String fullPathFile4 = dirPath + "/" + logFileName2;
	
		
		/*
		 * Change the dir where the files should be appended
		 */
		Logs.changeToSharedDir(new File(dirPath));
		
		String msg3 = "foo3";
		logger.info(msg3);
		String msg4 = "bar4";
		logger2.info(msg4);

		List<String> content = LoadUtility.readFile(new File(fullPathFile));
		Assert.assertTrue(content.size() == 1);
		Assert.assertTrue(content.get(0).contains(msg));

		List<String> content2 = LoadUtility.readFile(new File(fullPathFile2));
		Assert.assertTrue(content2.size() == 1);
		Assert.assertTrue(content2.get(0).contains(msg2));
		
		content = LoadUtility.readFile(new File(fullPathFile3));
		Assert.assertTrue(content.size() == 1);
		Assert.assertTrue(content.get(0).contains(msg3));

		content2 = LoadUtility.readFile(new File(fullPathFile4));
		Assert.assertTrue(content2.size() == 1);
		Assert.assertTrue(content2.get(0).contains(msg4));
	}
	
}
