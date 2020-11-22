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
package com.loadcoder.load;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LoadUtilityTest {

	private static Logger log = LoggerFactory.getLogger(LoadUtilityTest.class);
	
	@BeforeMethod
	public void before(Method m){
		log.info("Test {}:{} about to start", m.getDeclaringClass().getSimpleName(), m.getName() );
	}
	
	@Test
	public void testPadding(){
		int size = 5;
		String result = LoadUtility.rightpad("foo", size);
		log.info(String.format("<%s>", result));
		Assert.assertEquals(result.length(), size);
	}
	
}
