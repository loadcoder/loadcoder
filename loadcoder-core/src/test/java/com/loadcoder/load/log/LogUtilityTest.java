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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.testng.TestNGBase;

import static com.loadcoder.statics.LogbackLogging.*;

public class LogUtilityTest extends TestNGBase{
	
	@Test
	public void logUtility(Method method){
		
		String rootDir = "target/" + this.getClass().getSimpleName() + "/" + method.getName() + "/" + System.currentTimeMillis();
		File rootDirFile = new File(rootDir);
		Logger log = LoggerFactory.getLogger("resultlogger-shared");
	
		for(int i = 0; i<10; i++){
			File f = getNewLogDir(rootDir);
			setResultDestination(f);
			log.info("message{}", i);
		}
		
		File[] subDirs = rootDirFile.listFiles();
		Assert.assertEquals(subDirs.length, 10);
		List<String> readLines = new ArrayList<String>();
		try{
		for(File directory : subDirs){
			List<String> content = LoadUtility.readFile(new File(directory.getAbsoluteFile() + "/result.log"));
			Assert.assertEquals(content.size(), 1, "result in " + directory + " was unexpected");
			String line = content.get(0);
			Assert.assertTrue(line.matches("message.*"));
			
			//make sure that all files contents are unique
			Assert.assertFalse(readLines.contains(line));
			readLines.add(line);
		}
		}catch(IOException e){
			
		}
	}
}
