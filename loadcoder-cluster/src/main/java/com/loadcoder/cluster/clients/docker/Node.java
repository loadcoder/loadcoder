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

import java.io.File;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig.Builder;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.SSLConfig;
import com.loadcoder.utils.FileUtil;

public class Node {

	String id;
	String host;
	String internalHost;
	String port;
	boolean useMTLS;
	String mtlsPassword;
	
	DockerClient dockerClient;

	public Node(String id, String host, String internalHost, String port, boolean useMTLS, String mtlsPassword) {
		this.id = id;
		this.host = host;
		this.internalHost = internalHost;
		this.port = port;
		this.useMTLS = useMTLS;
		this.mtlsPassword = mtlsPassword;
	}

	public String getId() {
		return id;
	}

	public String getHost() {
		return host;
	}

	public String getInternalHost() {
		return internalHost;
	}

	public DockerClient getDockerClient() {
		synchronized (this) {
			if (dockerClient == null) {

				Builder dockerClientConfigBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder()
						.withDockerHost("tcp://" + host + ":" + port);

				if (useMTLS) {
					SSLConfig sslConf = () -> {
						return getSSLContext(this.host);
					};
					dockerClientConfigBuilder.withCustomSslConfig(sslConf);
				}

				DockerClientConfig config = dockerClientConfigBuilder.build();
				DockerClient docker = DockerClientBuilder.getInstance(config).build();
				this.dockerClient = docker;

			}
		}
		return this.dockerClient;
	}

	SSLContext getSSLContext(String host) {
		try {
			if (mtlsPassword == null) {
				throw new RuntimeException("docker.mtls.password is not set. This can be set either through jvm arg"
						+ "or by the loadcoder configuration file."
						+ "If you don't want to use a secure MTLS connection to for the Docker communication,"
						+ "you can disable this by setting docker.mtls=false in loadcoder configuration file");
			}
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

			File f = FileUtil.getFileFromResources(host + ".jks");
			char[] passwordChars = mtlsPassword.toCharArray();
			SSLContext sslContext = SSLContextBuilder.create().loadKeyMaterial(f, passwordChars, passwordChars)
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
			return sslContext;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
