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

import static org.assertj.core.api.Assertions.fail;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResourceAccessException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"server.ssl.trust-store=classpath:truststore.jks", "server.ssl.trust-store-password=changeit",
		"server.ssl.key-store=classpath:clientkeystore.jks", "server.ssl.key-store-password=changeit",
		"server.ssl.client-auth=need" })
public class MTLSTest {
	@LocalServerPort
	private int port;

	@Test
	public void readingCrytpgraphyFromFilesWorks() {

		SpringHttpClient client = new SpringHttpClient(
				SpringUtil.clientBuilder().trustStore(new File("src/test/resources/truststore.jks"), "changeit")
						.keyStore(new File("src/test/resources/clientkeystore.jks"), "changeit").build());

		ResponseEntity<String> resp = client.http("https://localhost:" + port + "/test/get?email=foo");
		assertEquals("hello foo", resp.getBody());
	}

	@Test
	public void mtlsClientWorks() {

		SpringHttpClient client = new SpringHttpClient(SpringUtil.clientBuilder()
				.trustStore("truststore.jks", "changeit").keyStore("clientkeystore.jks", "changeit").build());

		ResponseEntity<String> resp = client.http("https://localhost:" + port + "/test/get?email=foo");
		assertEquals("hello foo", resp.getBody());
	}

	@Test
	public void httpLeadsToSSLError() {

		SpringHttpClient client = new SpringHttpClient(
				SpringUtil.clientBuilder().trustStore("truststore.jks", "changeit").build());

		try {
			client.http("https://localhost:" + port + "/test/get?email=foo");
			fail("Did not expect to come here since no client cert is used");
		} catch (ResourceAccessException e) {
			// silent catch OK
		} catch (RuntimeException e) {
			fail("Caught wrong kind of exception:", e.getClass().getSimpleName());
		}
	}

}
