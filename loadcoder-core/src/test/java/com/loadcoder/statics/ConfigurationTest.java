/*******************************************************************************
 * Copyright (C) 2020 Team Loadcoder
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

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.loadcoder.statics.Configuration.ConfigHolder;

public class ConfigurationTest {


	final private static String HOSTIP_REGEXP = "hostip..*";
	
	@Test
	public void testConfig() {
		
		assertTrue("hostip.foo".matches(HOSTIP_REGEXP));
		
		Map<String, String> testConfig = new HashMap<String, String>();
		testConfig.put("hostip.foo", "bar");
		ConfigHolder confHolder = Mockito.mock(ConfigHolder.class);
		when(confHolder.getConfig()).thenReturn(testConfig);
		Configuration conf = new Configuration(confHolder);

		Map<String, String> map = conf.getMatchingConfig(HOSTIP_REGEXP);
		assertEquals(map.size(), 1);
	}
}
