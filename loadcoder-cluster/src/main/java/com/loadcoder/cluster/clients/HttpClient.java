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
package com.loadcoder.cluster.clients;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HttpClient {

	protected String protocolAsString(boolean https) {
		return https ? "https" : "http";
	}

	protected HttpResponse sendPost(String body, String url, List<Header> headers) {
		try {
			return sendChecked(body, url, "POST", headers);
		} catch (Exception e) {
			throw new RuntimeException("POST to url: " + url + " did not work", e);
		}
	}

	protected HttpResponse sendGet(String url, List<Header> headers) {
		try {
			return get(url, "GET", headers);
		} catch (Exception e) {
			throw new RuntimeException("GET to url: " + url + " did not work", e);
		}
	}

	protected HttpResponse get(String url, String httpVerb, List<Header> headers) {
		try {
			URL yahoo = new URL(url);
			HttpURLConnection con = (HttpURLConnection) yahoo.openConnection();
			headers.stream().forEach(header -> con.setRequestProperty(header.getName(), header.getValue()));

			int responseCode = con.getResponseCode();

			InputStream is;
			if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
				is = con.getInputStream();
			} else {
				/* error from server */
				is = con.getErrorStream();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null)
				response.append(inputLine);
			in.close();
			return new HttpResponse().setBody(response.toString()).setStatusCode(responseCode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected HttpResponse sendChecked(String body, String url, String httpVerb, List<Header> headers) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod(httpVerb);
		// add reuqest header
		headers.stream().forEach(header -> con.setRequestProperty(header.getName(), header.getValue()));

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		if (body != null) {
			wr.writeBytes(body);
		}
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();

		InputStream _is;
		if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
			_is = con.getInputStream();
		} else {
			/* error from server */
			_is = con.getErrorStream();
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(_is));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return new HttpResponse().setBody(response.toString()).setStatusCode(responseCode);
	}

}