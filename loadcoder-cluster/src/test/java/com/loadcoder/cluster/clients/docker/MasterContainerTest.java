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
package com.loadcoder.cluster.clients.docker;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.statics.Configuration;

public class MasterContainerTest extends TestNGBase{

	@Test
	public void getTestToGetPort() {
		
		Configuration mock = Mockito.mock(Configuration.class);
		MasterContainers.GRAFANA.setConfiguration(mock);
		
		when(mock.getConfiguration("grafana.port")).thenReturn("3010");
		String port = MasterContainers.GRAFANA.getPort();
		assertEquals(port, "3010");
	
		mock = Mockito.mock(Configuration.class);
		MasterContainers.GRAFANA.setConfiguration(mock);
		port = MasterContainers.GRAFANA.getPort();
		assertEquals(port, "3000");
	
	}
	
	@Test
	public void testDifferentPortConfigurations() {
		
		Configuration mock = Mockito.mock(Configuration.class);
		MasterContainers.GRAFANA.setConfiguration(mock);
		
		when(mock.getConfiguration("grafana.server.port")).thenReturn("3030");
		when(mock.getConfiguration("grafana.exposed.port")).thenReturn("3020");
		when(mock.getConfiguration("grafana.port")).thenReturn("3010");

		String port = MasterContainers.GRAFANA.getPort();
		assertEquals(port, "3010");
		port = MasterContainers.GRAFANA.getExposedPort();
		assertEquals(port, "3020");
		port = MasterContainers.GRAFANA.getServerPort();
		assertEquals(port, "3030");
	
	
	}	
}
