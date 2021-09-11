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
package com.loadcoder.network.loadship;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.loadcoder.network.spring.SpringHttpClient;

public class LoadshipClient {

	private static SpringHttpClient client = new SpringHttpClient();

	public static void postFile(String baseUrl, String dirname, String filname, String content) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("dirname", dirname);
		httpHeaders.add("filename", filname);
		httpHeaders.add("Content-type", "application/octet-stream;UTF-8");
		client.http(baseUrl + "/loadship/file", HttpMethod.POST, httpHeaders, content);
	}
	
	public static String getFile(String baseUrl, String dirname, String filname) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("dirname", dirname);
		httpHeaders.add("filename", filname);
		ResponseEntity<String > resp = client.http(baseUrl + "/loadship/file", HttpMethod.GET, httpHeaders, "");
		return resp.getBody();
	}
}
