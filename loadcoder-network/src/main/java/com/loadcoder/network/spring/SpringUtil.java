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

import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class SpringUtil {

	private static RestTemplate CLIENT;
	private static HttpHeaders DEFAULT_HEADERS = new HttpHeaders();
	private static final TrustStrategy TRUST_ALL = (X509Certificate[] chain, String authType) -> true;
	public static HttpEntity<String> http(String url) {
		return http(url, DEFAULT_HEADERS);
	}

	public static HttpEntity<String> http(String url, HttpHeaders headers) {
		HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
		return CLIENT.exchange(url, HttpMethod.GET, requestEntity, String.class);
	}

	public static ResponseEntity<String> http(String url, HttpMethod httpMethod, HttpHeaders headers, String body) {
		HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
		return CLIENT.exchange(url, httpMethod, requestEntity, String.class);
	}

	public static ResponseEntity<String> http(String url, HttpMethod httpMethod, HttpHeaders headers, byte[] body) {
		HttpEntity<byte[]> requestEntity = new HttpEntity<>(body, headers);
		return CLIENT.exchange(url, httpMethod, requestEntity, String.class);
	}
	
	public static TrustStore trustStore(String pathToTrustStore, String trustStorePassword){
		return new TrustStore(pathToTrustStore, trustStorePassword);
	}
	
	public static KeyStore keyStore(String pathToTrustStore, String trustStorePassword){
		return new KeyStore(pathToTrustStore, trustStorePassword);
	}
	
	public static RestTemplate getClient(TrustStore trustStore) {
		return getClient(trustStore, null);
	}
	
	public static RestTemplate getClient(KeyStore keyStore) {
		return getClient(null, keyStore);
	}
	
	public static RestTemplate getClient(TrustStore trustStore, KeyStore keyStore) {

		URL keyStoreUrl = SpringUtil.class.getClassLoader().getResource(keyStore.getFile());

		try {
			SSLContextBuilder builder = SSLContextBuilder.create();
			 
			if(trustStore != null) {
				URL trustStoreUrl = SpringUtil.class.getClassLoader().getResource(trustStore.getFile());
				builder.loadTrustMaterial(trustStoreUrl, trustStore.getPwd().toCharArray());
			}else {
				builder.loadTrustMaterial(null, TRUST_ALL);	
			}
			
			if(keyStore != null) {
					builder.loadKeyMaterial(keyStoreUrl, keyStore.getPwd().toCharArray(),
							keyStore.getPwd().toCharArray());
			}
					
					SSLContext sslContext =		builder.build();
			HttpClient client = HttpClients.custom()
					.disableCookieManagement()
					.setSSLContext(sslContext)
					.build();
			HttpComponentsClientHttpRequestFactory requestFactory =
					new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(client);
			return new RestTemplate(requestFactory);
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
	}

	public static void setClient(RestTemplate client2) {
		CLIENT = client2;
	}

	
	private static class Store {
		String file;
		String pwd;
		private Store(String file, String pwd) {
			this.file = file;
			this.pwd = pwd;
		}
		
		String getFile() {
			return file;
		}
		
		String getPwd() {
			return pwd;
		}
	}
	
	private static class TrustStore extends Store{
		TrustStore(String file, String pwd){
			super(file, pwd);
		}
	}

	private static class KeyStore extends Store{
		KeyStore(String file, String pwd){
			super(file, pwd);
		}
	}
	
	public static void main(String args[]) {
		RestTemplate client = getClient(trustStore("truststore.jks", "changeit"), keyStore("keystore.p12", "changeit"));
		
		ResponseEntity<String> resp = client.getForEntity("https://localhost:8490/customer/get?email=hej", String.class);
		System.out.println("done");
	}
}
