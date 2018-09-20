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
package com.loadcoder.load.log;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.load.TestUtility;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.statics.LogbackLogging;

/**
 * test that it is possible to run two test in the same execution. logging and result should be verified
 */
public class MultipleTests extends TestNGBase{

	Logger log = LoggerFactory.getLogger(com.loadcoder.result.Logs.class);
	
	@Test
	public void testOne(Method method){
		File dir = LogbackLogging.getNewLogDir("target", method.getName() );
		File resultFile = new File(dir, "result.log");
		LogbackLogging.setResultDestination(dir);
		log.info(method.getName());
		List<String> lines = TestUtility.readFile(resultFile);
		assertEquals(lines.size(), 1);
		assertEquals(lines.get(0), "testOne");
	}
	
	@Test
	public void testTwo(Method method){
		File dir = LogbackLogging.getNewLogDir("target", method.getName() );
		File resultFile = new File(dir, "result.log");
		LogbackLogging.setResultDestination(dir);
		log.info(method.getName());
		List<String> lines = TestUtility.readFile(resultFile);
		assertEquals(lines.size(), 1);
		assertEquals(lines.get(0), "testTwo");
	}
	
}

