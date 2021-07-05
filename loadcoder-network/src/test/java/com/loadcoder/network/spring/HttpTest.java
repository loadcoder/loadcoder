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

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.load.scenario.ResultModel;
import com.loadcoder.result.Result;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpTest {

	@LocalServerPort
	private int port;

	@Test
	public void willDefaultHttpCallWork() {

		SpringUtil.setClient(new RestTemplate());

		ResponseEntity<String> resp = SpringUtil.http("http://localhost:" + port + "/test/get?email=foo");
		assertEquals("hello foo", resp.getBody());
	}

	public class ResultModelExtension<R> extends ResultModel<R> {

		public ResultModelExtension(String transactionName) {
			super(transactionName);
		}

		public void setResp(R r) {
			super.setResp(r);
		}
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

	@Test
	public void testCheck() {
		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("foo", () -> SpringUtil.http("http://localhost:" + port + "/test/get?email=foo"))
						.handleResult(r -> {
							SpringUtil.check(r, 200);
						}).perform();

				load("foo2", () -> SpringUtil.http("http://localhost:" + port + "/test/get?email=foo"))
						.handleResult(r -> {
							SpringUtil.check(r, 201);
						}).perform();

			}
		};

		Result r = ls.test();
		assertTrue(r.getResultLists().get("foo").get(0).getStatus());
		assertFalse(r.getResultLists().get("foo2").get(0).getStatus());

	}
}
