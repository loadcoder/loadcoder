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

import static com.loadcoder.statics.LogbackLogging.setResultDestination;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.result.ResultLogger;

public class Slf4jTest {

	static Logger resultLog = ResultLogger.resultLogger;

	static Logger infoLog = LoggerFactory.getLogger(Slf4jTest.class);
	
	@Test
	public void testLogging(Method method) throws IOException{
		
		File resultDir = new File("target/" + method.getName() + "/" + System.currentTimeMillis());
		setResultDestination(resultDir);
		String infoMsg = "foo";
		String resultMsg = "bar";
		resultLog.info(resultMsg);
		infoLog.error(infoMsg);
		
		List<String> content = LoadUtility.readFile(new File(resultDir.getAbsolutePath() +"/info-logback.log"));
		Assert.assertEquals(content.size(), 2);
		Assert.assertTrue(content.get(1).contains(infoMsg));

		List<String> content2 = LoadUtility.readFile(new File(resultDir.getAbsolutePath() +"/result-logback.log"));
		Assert.assertEquals(content2.size(), 1);
		Assert.assertTrue(content2.get(0).contains(resultMsg));
		
	}
}
