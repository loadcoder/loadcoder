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
package com.loadcoder.utils;

import static com.loadcoder.load.LoadUtility.tryCatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {

	public static byte[] getFileContent(String path) {
		Path p = Paths.get(path);
		byte[] bytes = tryCatch(() -> Files.readAllBytes(p), (e) -> {
			throw new RuntimeException(e);
		});
		return bytes;
	}

	public static String readFile(File f) {
		return readFile(f.getPath());
	}

	public static String readFile(String path) {
		byte[] bytes = getFileContent(path);
		String s = new String(bytes);
		return s;
	}

	public static File getFileFromResources(String fileName) {

		ClassLoader classLoader = FileUtil.class.getClassLoader();

		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException("file is not found!");
		} else {
			return new File(resource.getFile());
		}
	}

	public static URL getURLFromResources(String fileName) {

		ClassLoader classLoader = FileUtil.class.getClassLoader();

		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException("file is not found!");
		} else {
			return resource;
		}
	}

	private static BufferedReader getResourceAsBufferedReader(String resourcePath) {
		InputStream in = FileUtil.class.getResourceAsStream(resourcePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		return reader;
	}

	public static String getResourceAsString(String resourcePath) {
		try (BufferedReader reader = getResourceAsBufferedReader(resourcePath)) {

			String result = bufferedReaderToString(reader);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String bufferedReaderToString(BufferedReader reader) {
		StringBuffer buffer = new StringBuffer();
		reader.lines().collect(Collectors.toList()).forEach(line -> buffer.append(line + "\n"));
		String result = buffer.toString();
		return result;
	}

	public static void writeFile(byte[] bytes, File destination) {
		Path p2 = Paths.get(destination.getAbsolutePath());
		try {
			Files.write(p2, bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Path> listDirectory(String directoryPath) {
		try (Stream<Path> paths = Files.list(Paths.get(directoryPath))) {
			return paths.collect(Collectors.toList());
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public static List<File> getPathsAsFileList(List<Path> paths) {
		List<File> files = paths.stream().map(p -> p.toFile()).collect(Collectors.toList());
		return files;
	}

	public static FileTime getCreationDate(Path file) {
		BasicFileAttributes attr;
		try {
			attr = Files.readAttributes(file, BasicFileAttributes.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		FileTime fileTime = attr.creationTime();
		return fileTime;
	}
}
