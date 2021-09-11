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
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class PackageSender {

	static RestTemplate restTemplate = new RestTemplate();

	public static byte[] readFileAsPackage(File file) {

		try {
			byte[] fileContent = Files.readAllBytes(file.toPath());
			return fileContent;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public static void performPOSTRequest(String urlString, byte[] body) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/octet-stream; charset=utf-8");
		HttpEntity<byte[]> fsdf = new HttpEntity<>(body, headers);
		restTemplate.exchange(urlString, HttpMethod.POST, fsdf, String.class);

	}

	public static File findFile(String directory, String fileNameRegExpMatcher) {
		// Creates an array in which we will store the names of files and directories
		String[] pathnames;

		// Creates a new File instance by converting the given pathname string
		// into an abstract pathname
		File f = new File(directory);
		if (!f.exists()) {
			throw new RuntimeException("Directory " + f.getPath() + " does not exist");
		}

		// Populates the array with names of files and directories
		pathnames = f.list();

		// For each pathname in the pathnames array
		for (String pathname : pathnames) {
			if (pathname.matches(fileNameRegExpMatcher)) {
				File foundFile = new File(f, pathname);
				if (foundFile.exists()) {
					return foundFile;
				}
			}
		}
		throw new RuntimeException(
				"Could not found a file with name matching " + fileNameRegExpMatcher + " in directory " + f.getPath());
	}
}
