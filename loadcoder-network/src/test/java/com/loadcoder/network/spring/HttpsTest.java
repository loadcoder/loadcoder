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

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.loadcoder.network.spring.springboot.TestServer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"server.ssl.trust-store=classpath:truststore.jks", "server.ssl.trust-store-password=changeit",
		"server.ssl.key-store=classpath:clientkeystore.jks", "server.ssl.key-store-password=changeit" })
public class HttpsTest {

	@LocalServerPort
	private int port;

	@Test
	public void mtlsClientWorks() {

		SpringUtil.setClient(SpringUtil.clientBuilder().trustStore("truststore.jks", "changeit")
				.keyStore("clientkeystore.jks", "changeit").build());

		ResponseEntity<String> resp = SpringUtil.http("https://localhost:" + port + "/test/get?email=foo");
		assertEquals("Hello foo", resp.getBody());
	}

	@Test
	public void readingCrytpgraphyFromFilesWorks() {

		SpringUtil.setClient(
				SpringUtil.clientBuilder().trustStore(new File("src/test/resources/truststore.jks"), "changeit")
						.keyStore(new File("src/test/resources/clientkeystore.jks"), "changeit").build());
		ResponseEntity<String> resp = SpringUtil.http("https://localhost:" + port + "/test/get?email=foo");
		assertEquals("Hello foo", resp.getBody());
	}

}
