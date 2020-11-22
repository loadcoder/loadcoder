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
package com.loadcoder.network.okhttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import static com.loadcoder.load.LoadUtility.*;

public class OKHttpUtils {

	/**
	 * Extract the stream with GZIP and then returns the resulting string
	 * @param stream the input stream to be extracted
	 * @return the resulting string
	 */
	public static String gzipDecompresser(InputStream stream) {
		try {
			GZIPInputStream gzis = new GZIPInputStream(stream);

			InputStreamReader reader = new InputStreamReader(gzis);
			BufferedReader in = new BufferedReader(reader);

			String result = "";
			String readed;
			while ((readed = in.readLine()) != null) {
				result = result + readed;
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the response body as a string. If the header content-encoding is gzip, the body will first be extracted
	 * @param response the response to get get the body from
	 * @return the response body
	 */
	public static String getResponseBodyAsString(Response response) {
		String contentEncoding = response.header("content-encoding");
		String body;
		if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
			InputStream stream = tryCatch(() -> response.body().byteStream(), (e) -> {
				throw new RuntimeException(e);
			});
			body = gzipDecompresser(stream);
		} else {
			body = tryCatch(() -> response.body().string(), (e) -> {
				throw new RuntimeException(e);
			});
		}
		return body;
	}

	
	/**
	 * Creates an empty RequestBody
	 * @return the empty RequestBody
	 */
	public static RequestBody getEmptyRequestHeader() {
		final RequestBody requestBody = new RequestBody() {

			@Override
			public void writeTo(BufferedSink sink) throws IOException {
			}

			@Override
			public MediaType contentType() {
				return null;
			}
		};
		return requestBody;
	}
}
