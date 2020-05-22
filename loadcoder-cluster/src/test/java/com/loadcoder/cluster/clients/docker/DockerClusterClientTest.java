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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.loadcoder.statics.Configuration;

import static com.loadcoder.cluster.clients.docker.MasterContainers.*;

public class DockerClusterClientTest {

	@Test
	public void testGetComponentNameFromHostIpMapping() {
		String host = DockerClusterClient.getHostNameFromHostIpMapping("hostip.foo");
		assertEquals(host, "foo");
	
		host = DockerClusterClient.getHostNameFromHostIpMapping("hostip.foo.bar");
		assertEquals(host, "foo.bar");
	
	}
	
	
	@Test
	public void testInternalHosts() {

		Configuration conf = Mockito.mock(Configuration.class);
		when(conf.getConfiguration("cluster.masternode")).thenReturn("1");
		when(conf.getConfiguration("node.1.host")).thenReturn("publichost");
		
		Map<String, String> config = new HashMap<>();
		config.put("node.1.host", "publichost");
		when(conf.getConfiguration()).thenReturn(config);
		
		
		DockerClusterClient docker = new DockerClusterClient(conf);
		String host = docker.getInternalHost(INFLUXDB);
		assertEquals(host, "publichost");

		when(conf.getConfiguration("node.1.internal.host")).thenReturn("internalmaster");
		docker = new DockerClusterClient(conf);
		host = docker.getInternalHost(INFLUXDB);
		assertEquals(host, "internalmaster");
		

		when(conf.getConfiguration("influxdb.internal.host")).thenReturn("internalinfluxdbhost");
		docker = new DockerClusterClient(conf);
		host = docker.getInternalHost(INFLUXDB);
		assertEquals(host, "internalinfluxdbhost");
		
	}
	
	
	@Test
	public void testHosts() {

		Configuration conf = Mockito.mock(Configuration.class);
		when(conf.getConfiguration("cluster.masternode")).thenReturn("1");
		when(conf.getConfiguration("node.1.host")).thenReturn("publichost");
		
		Map<String, String> config = new HashMap<>();
		config.put("node.1.host", "publichost");
		when(conf.getConfiguration()).thenReturn(config);
		
		DockerClusterClient docker = new DockerClusterClient(conf);
		String host = docker.getHost(INFLUXDB);
		assertEquals(host, "publichost");

		when(conf.getConfiguration("influxdb.host")).thenReturn("influxdbhost");
		docker = new DockerClusterClient(conf);
		host = docker.getHost(INFLUXDB);
		assertEquals(host, "influxdbhost");
		
	}
	
	
	@Test
	public void testNotAsWorker() {

		Configuration conf = Mockito.mock(Configuration.class);
		when(conf.getConfiguration("cluster.masternode")).thenReturn("1");
		when(conf.getConfiguration("node.1.host")).thenReturn("publichost");
		
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("node.1.host", "publichost");
		when(conf.getConfiguration()).thenReturn(configMap);
		
		DockerClusterClient docker = new DockerClusterClient(conf);
		assertEquals(docker.getNodes().size(), 1);
		
		when(conf.getConfiguration("node.1.use-as-worker")).thenReturn("");
		docker = new DockerClusterClient(conf);
		assertEquals(docker.getNodes().size(), 1);
		
		
		when(conf.getConfiguration("node.1.use-as-worker")).thenReturn("false");
		try{
			docker = new DockerClusterClient(conf);
			fail("Exception expected since there shouldnt be any nodes to run Loadcoder in");
		}catch(RuntimeException rte) {}
		
		
		configMap.put("node.2.host", "publichost");
		docker = new DockerClusterClient(conf);
		assertEquals(docker.getNodes().size(), 1);
		
	}
	
	@Test
	public void testGetNodes() {

		Configuration c = Mockito.mock(Configuration.class);
		Map<String, String> config = new HashMap<>();
		config.put("node.foo.host", "localhost");
		config.put("node.bar.host", "localhost");
		when(c.getConfiguration()).thenReturn(config);
		DockerClusterClient client = new DockerClusterClient(c);
		Set<String> ids = client.getAllNodeIds();
		assertTrue(ids.contains("foo"));
		assertTrue(ids.contains("bar"));
	}
}
