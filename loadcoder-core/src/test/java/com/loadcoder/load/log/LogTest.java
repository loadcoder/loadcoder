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
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.loadcoder.load.TestUtility;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.scenario.Load.LoadBuilder;

public class LogTest {

	@Test
	public void test() {

		List<String> rows = TestUtility.readFile(new File("result.log"));
		int sizeBeforeTest = rows.size();
		
		String uniqueTransactionId = this.getClass().getName()+System.currentTimeMillis();
		LoadScenario ls = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				load(uniqueTransactionId, ()->{/*some fancy transaction*/}).perform();
			}
		};
		
		Load l = new LoadBuilder(ls).build();
		l.runLoad().andWait();
		
		List<String> rowsAfterTest = TestUtility.readFile(new File("result.log"));
		Assert.assertEquals(sizeBeforeTest + 1, rowsAfterTest.size());
		Assert.assertTrue(rowsAfterTest.get(rowsAfterTest.size() -1).contains(uniqueTransactionId));

	}
}
