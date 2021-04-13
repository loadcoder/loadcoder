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
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateBuilder {

	private TrustStore trustStore;
	private boolean trustAll = false;

	private KeyStore keyStore;

	private HttpRequestFactoryHandler httpRequestFactoryHandler;
	private HttpClientBuilderHandler httpClientBuilderHandler;

	private static final TrustStrategy TRUST_ALL = (X509Certificate[] chain, String authType) -> true;

	public RestTemplateBuilder trustStore(File filePath, String pwd) {
		this.trustStore = new TrustStore(filePath, pwd);
		this.trustAll = false;
		return this;
	}

	public RestTemplateBuilder trustStore(String resourcePath, String pwd) {
		this.trustStore = new TrustStore(resourcePath, pwd);
		this.trustAll = false;
		return this;
	}

	public RestTemplateBuilder trustAll() {
		this.trustStore = null;
		trustAll = true;
		return this;
	}

	public RestTemplateBuilder keyStore(File filePath, String pwd) {
		this.keyStore = new KeyStore(filePath, pwd);
		return this;
	}

	public RestTemplateBuilder keyStore(String resourcePath, String pwd) {
		this.keyStore = new KeyStore(resourcePath, pwd);
		return this;
	}

	public RestTemplateBuilder handleRequestFactory(HttpRequestFactoryHandler httpRequestFactoryHandler) {
		this.httpRequestFactoryHandler = httpRequestFactoryHandler;
		return this;
	}

	public RestTemplateBuilder handleHttpClientBuilder(HttpClientBuilderHandler httpClientBuilderHandler) {
		this.httpClientBuilderHandler = httpClientBuilderHandler;
		return this;
	}

	public RestTemplate build() {
		return getClient();
	}

	public RestTemplate getClient() {

		try {
			SSLContextBuilder builder = SSLContextBuilder.create();

			if (trustStore != null) {

				if (trustStore.getFilePath() != null) {
					builder.loadTrustMaterial(trustStore.getFilePath(), trustStore.getPwd().toCharArray());
				} else {
					URL trustStoreUrl = RestTemplateBuilder.class.getClassLoader().getResource(trustStore.getResourcePath());
					builder.loadTrustMaterial(trustStoreUrl, trustStore.getPwd().toCharArray());
				}
			} else {
				if (trustAll) {
					builder.loadTrustMaterial(null, TRUST_ALL);
				}
			}

			if (keyStore != null) {
				if (keyStore.getFilePath() != null) {
					builder.loadKeyMaterial(keyStore.getFilePath(), keyStore.getPwd().toCharArray(),
							keyStore.getPwd().toCharArray());
				} else {
					URL keyStoreUrl = RestTemplateBuilder.class.getClassLoader().getResource(keyStore.getResourcePath());
					builder.loadKeyMaterial(keyStoreUrl, keyStore.getPwd().toCharArray(),
							keyStore.getPwd().toCharArray());
				}
			}

			SSLContext sslContext = builder.build();
			HttpClientBuilder httpClientBuilder = HttpClients.custom().setMaxConnTotal(200).setMaxConnPerRoute(200)
					.disableCookieManagement().setSSLContext(sslContext);
			if (httpClientBuilderHandler != null) {
				httpClientBuilderHandler.handle(httpClientBuilder);
			}
			HttpClient client = httpClientBuilder.build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(client);

			if (httpRequestFactoryHandler != null) {
				httpRequestFactoryHandler.handle(requestFactory);
			}

			return new RestTemplate(requestFactory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
