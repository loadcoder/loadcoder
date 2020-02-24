/*******************************************************************************
 * Copyright (C) 2020 Stefan Vahlgren at Loadcoder
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class PackageSender {

	public static byte[] readFileAsPackage(File file) {

		try {
			byte[] fileContent = Files.readAllBytes(file.toPath());
			return fileContent;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public static void performPOSTRequest(String urlString, byte[] body) {

		RestTemplate t = getRestTemplateIgnoringTLS();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/octet-stream; charset=utf-8");
		HttpEntity<byte[]> ent = new HttpEntity<>(body);
		t.exchange(urlString, HttpMethod.POST, ent, String.class);
		
		System.out.println("done");
//		("Content-type", "application/octet-stream; charset=utf-8");

	}
	
	static RestTemplate getRestTemplateIgnoringTLS(){
		try {
			TrustStrategy acceptingTrustStrategy = (chain, authType) -> {
				return true;
			};
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

			LayeredConnectionSocketFactory layeredConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);

			CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(layeredConnectionSocketFactory)
					.build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

			requestFactory.setHttpClient(client);
			RestTemplate restTemplate = new RestTemplate(requestFactory);
			return restTemplate;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	
    public static File findFile(String directory, String fileNameRegExpMatcher) {
        // Creates an array in which we will store the names of files and directories
        String[] pathnames;

        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        File f = new File(directory);
        if(!f.exists()) {
    		throw new RuntimeException("Directory " + f.getPath() + " does not exist");
        }
        
        // Populates the array with names of files and directories
        pathnames = f.list();

        // For each pathname in the pathnames array
        for (String pathname : pathnames) {
        	if(pathname.matches(fileNameRegExpMatcher)) {
        		File foundFile = new File(f, pathname);
        		if(foundFile.exists()) {
        			return foundFile;
        		}
        	}
        }
		throw new RuntimeException("Could not found a file with name matching " + fileNameRegExpMatcher + " in directory " + f.getPath());
    }
}
