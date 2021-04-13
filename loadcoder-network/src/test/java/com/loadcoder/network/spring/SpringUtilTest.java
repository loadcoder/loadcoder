/*******************************************************************************
 * Copyright (C) 2021 Team Loadcoder
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
package com.loadcoder.network.spring;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.loadcoder.network.spring.springboot.TestServer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringUtilTest {

	@LocalServerPort
	private int port;

	@Test
	public void willDefaultHttpCallWork() {

		SpringUtil.setClient(new RestTemplate());

		ResponseEntity<String> resp = SpringUtil.http("http://localhost:" + port + "/test/get?email=foo");
		assertEquals("Hello foo", resp.getBody());
	}

	@Test
	public void clientTimesOut() {
		RestTemplate client = SpringUtil.clientBuilder().handleRequestFactory(a -> {
			a.setReadTimeout(1000);
		}).build();
		try {
			client.getForEntity("http://localhost:" + port + "/test/delay?delay=1500", Void.class);
			fail("Expected timeout");
		} catch (RuntimeException rte) {
		}

		client = SpringUtil.clientBuilder().handleRequestFactory(a -> {
			a.setReadTimeout(2000);
		}).build();

		client.getForEntity("http://localhost:" + port + "/test/delay?delay=1500", Void.class);
	}
}
