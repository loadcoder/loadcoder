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
import java.lang.reflect.Method;
import java.util.List;

import static com.loadcoder.statics.LogbackLogging.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.TestUtility;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Logs;



public class LogbackLoggingTest extends TestNGBase{

	Logger resultLog = LoggerFactory.getLogger(Logs.class);
	Logger infoLog = LoggerFactory.getLogger(this.getClass());
			
	@Test
	public void setResultDirectoryTest(Method method) {
		String sharedDirForLogsPath = String.format("target/%s/%s", method.getName(), System.currentTimeMillis());
		setResultDestination(new File(sharedDirForLogsPath));
		
		resultLog.info("{} result", method.getName());
		infoLog.info("{} info", method.getName());
		
		List<String> content = TestUtility.readFile(new File(sharedDirForLogsPath + "/info-logback.log"));
		Assert.assertEquals(content.size(), 2);
		Assert.assertEquals(content.get(1), method.getName()+ " info");
	}
	
	@Test
	public void getNewLogDirTest(Method method) {
		File sharedDirForLogs = getNewLogDir("target", method.getName());
		
		setResultDestination(sharedDirForLogs);
		
		resultLog.info("{} result", method.getName());
		infoLog.info("{} info", method.getName());
		
		List<String> content = TestUtility.readFile(new File(sharedDirForLogs + "/result-logback.log"));
		Assert.assertEquals(1, content.size());
		Assert.assertEquals(content.get(0), method.getName()+ " result");
	}
}
