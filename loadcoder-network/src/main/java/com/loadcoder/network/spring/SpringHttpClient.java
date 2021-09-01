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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.loadcoder.load.scenario.ResultModel;

public class SpringHttpClient {

	private Logger log = LoggerFactory.getLogger(SpringHttpClient.class);
	
	final RestTemplate template;
	
	public SpringHttpClient() {
		this.template = new RestTemplate();
	}
	
	public SpringHttpClient(RestTemplate template) {
		this.template = template;
	}
	
	private static HttpHeaders DEFAULT_HEADERS = new HttpHeaders();

	public ResponseEntity<String> http(String url) {
		return http(url, DEFAULT_HEADERS);
	}

	public ResponseEntity<String> http(String url, HttpHeaders headers) {
		HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
		return template.exchange(url, HttpMethod.GET, requestEntity, String.class);
	}

	public ResponseEntity<String> http(String url, HttpMethod httpMethod, HttpHeaders headers, String body) {
		HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
		return template.exchange(url, httpMethod, requestEntity, String.class);
	}

	public ResponseEntity<String> http(String url, HttpMethod httpMethod, HttpHeaders headers, byte[] body) {
		HttpEntity<byte[]> requestEntity = new HttpEntity<>(body, headers);
		return template.exchange(url, httpMethod, requestEntity, String.class);
	}
	
	public static void check(ResultModel<ResponseEntity<String>> r, int expectedHttpCode) {
		if (r.getResponse() != null && r.getResponse().getStatusCodeValue() != expectedHttpCode) {
			r.setStatus(false);
		}
	}
}
