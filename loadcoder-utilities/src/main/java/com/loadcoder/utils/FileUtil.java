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
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {

	private static Map<String, FileSystem> FILESYSTEM = new HashMap<>();

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

	private static BufferedReader getResourceAsBufferedReader(String resourcePath) {
		InputStream in = FileUtil.class.getResourceAsStream(resourcePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		return reader;
	}

	private static String bufferedReaderToString(BufferedReader reader) {
		try {
			char[] charArray = new char[8 * 1024];
			StringBuilder builder = new StringBuilder();
			int numCharsRead;
			while ((numCharsRead = reader.read(charArray, 0, charArray.length)) != -1) {
				builder.append(charArray, 0, numCharsRead);
			}
			String result = builder.toString();
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

	public static Map<String, String> readAllResourceFilesInResourceDir(String resourcePath) {
		List<String> allResourcesInResourceDir = listAllResourcesInResourceDir(resourcePath);
		Map<String, String> result = new HashMap<String, String>();
		allResourcesInResourceDir.stream().forEach(filePath -> {
			String content = readResourceAsString(filePath);
			result.put(filePath, content);
		});
		return result;
	}

	/**
	 * Read a file as a resource. Useful when the file is stored within a jar file
	 * 
	 * @param resourcePath If the searched file is file.txt and located in the root
	 *                     of the resource directory, the resourcePath would be
	 *                     /file.txt
	 * @return the content of the resource file
	 */
	public static String readResourceAsString(String resourcePath) {
		BufferedReader reader = getResourceAsBufferedReader(resourcePath);
		String results = bufferedReaderToString(reader);
		return results;
	}

	public static List<String> readResourceAsLines(String resourcePath) {
		BufferedReader reader = getResourceAsBufferedReader(resourcePath);
		return reader.lines().collect(Collectors.toList());
	}

	public static List<String> listAllResourcesInResourceDir(String resourcePathDir) {
		try {
			URI uri = FileUtil.class.getResource(resourcePathDir).toURI();
			Path searchPath;
			if (uri.getScheme().equals("jar")) {

				String jarURIWithoutResourcePath = uri.toString().replace(resourcePathDir, "");
				FileSystem f = FILESYSTEM.get(jarURIWithoutResourcePath);
				if (f == null) {
					f = FileSystems.newFileSystem(uri, Collections.emptyMap());
					FILESYSTEM.put(jarURIWithoutResourcePath, f);
				}
				searchPath = f.getPath(resourcePathDir);
			} else {
				searchPath = Paths.get(uri);
			}
			List<String> result = new ArrayList<>();
			Stream<Path> walk = Files.walk(searchPath, 1);
			for (Iterator<Path> pathIterator = walk.iterator(); pathIterator.hasNext();) {
				Path path = pathIterator.next();
				String pathString = path.toString();
				String fileNameResource = pathString.replace(searchPath.toString(), "");
				if (fileNameResource.isEmpty()) {
					continue;
				}
				String resourcePathInDir = resourcePathDir + fileNameResource;
				result.add(resourcePathInDir);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		createDir(new File("/tmp/ccc/ddd"));
	}

	public static void createDir(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				return;
			} else {
				throw new RuntimeException("Tried to create a dir, but the path is a file:" + file.getAbsolutePath());
			}
		} else {
			File parent = file.getAbsoluteFile().getParentFile();
			if (!parent.exists()) {
				createDir(parent);
			}
			file.mkdir();
		}
	}
}
