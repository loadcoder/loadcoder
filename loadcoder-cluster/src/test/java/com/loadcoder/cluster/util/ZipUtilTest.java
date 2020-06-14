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
package com.loadcoder.cluster.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.cluster.util.ZipUtil.FileCounter;

public class ZipUtilTest {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void testZipDsirectory() {
		ZipUtil zip  = new ZipUtil();
		File directory = new File("src/test/resources/countertest");
		File zipFile = new File("target/testZipDirectory.zip");
		zip.zip(directory, zipFile);
	}
	
	@Test
	public void testZipDirectoryWhiteList() {
		ZipUtil zip  = new ZipUtil();
		File directory = new File(".");
		File zipFile = new File("target/testZipDirectory.zip");
		zip.zip(directory, zipFile, "src", "pom.xml");
	}
	
	@Test
	public void testZipDirectoryWhiteListToByte() {
		ZipUtil zip  = new ZipUtil();
		File directory = new File(".");
		File zipFile = new File("target/testZipDirectory.zip");
		byte[] bytes = zip.zipToBytes(directory, "src", "pom.xml");
	
		try {
			Files.write(new File("target/bytes.zip").toPath(), bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testCountFilesInDirectory() {
		FileCounter counter = new FileCounter();
		File f = new File("src/test/resources/countertest");
		try{
			int files = counter.getNumberOfFilesInDir(f);
			assertEquals(files, 6);
		}catch(TooManyFilesFoundException tmffe) {
			fail("Unexpected TooManyFilesFoundException was caught", tmffe);
		}
	}
	
	@Test
	public void testCountTooManyFilesInDirectory() {
		FileCounter counter = new FileCounter();
		File f = new File("src/test/resources/countertest");
		try{
			int oneLessThanActualNumberOfFiles = 4;
			int files = counter.getNumberOfFilesInDir(f, oneLessThanActualNumberOfFiles);
			fail("TooManyFilesFoundException wasn't thrown");
		}catch(TooManyFilesFoundException tmffe) {

		}
	}
	
}
