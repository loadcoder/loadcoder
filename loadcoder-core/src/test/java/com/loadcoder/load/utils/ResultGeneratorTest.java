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
package com.loadcoder.load.utils;

import static com.loadcoder.statics.Logging.getNewLogDir;
import static com.loadcoder.statics.Logging.setResultDestination;
import static com.loadcoder.statics.Milliseconds.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.loadcoder.load.TestUtility;


public class ResultGeneratorTest {

	@Test
	public void testToGenerateResult(Method method){
		File resultDir = getNewLogDir("target", method.getName());
		setResultDestination(resultDir);
		ResultGenerator.generateResult(1 * MINUTE, 2);
		List<String> resultList = TestUtility.readFile(new File(resultDir.getAbsolutePath() + "/result.log"));
		Assert.assertTrue(resultList.size() > 1);
	}
}
