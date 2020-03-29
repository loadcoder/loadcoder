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
package com.loadcoder.utils;

import static com.loadcoder.load.LoadUtility.tryCatch;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class FileUtil {

	public static byte[] getFileContent(String path){
		Path p = Paths.get(path);
		byte[] bytes = tryCatch(()->Files.readAllBytes(p), (e)->{throw new RuntimeException(e);});
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
	
//	public static String readFile(String path) {
//		Path p = Paths.get(path);
//		try {
//			byte[] src = Files.readAllBytes(p);
//			String s = new String(src);
//			return s;
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}

    // get file from classpath, resources folder
	public static File getFileFromResources(String fileName) {

        ClassLoader classLoader = FileUtil.class.getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
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
}
