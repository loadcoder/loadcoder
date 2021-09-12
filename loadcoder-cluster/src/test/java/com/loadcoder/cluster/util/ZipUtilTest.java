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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.cluster.util.ZipUtil.FileCounter;
import com.loadcoder.cluster.util.ZipUtil.ZipBuilderFileAdder.ZipDefinition;

public class ZipUtilTest {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void zipDefault() {

		File directory = new File("src/test/resources/countertest");

		ZipDefinition de = ZipUtil.zipBuilder(directory.getAbsolutePath()).build();

		List<File> filesToBeZiped = de.getFilesToBeZiped();
		assertEquals(3, filesToBeZiped.size());
	}

	@Test
	public void emptyWhiteList() {

		File directory = new File("src/test/resources/countertest");

		ZipDefinition de = ZipUtil.zipBuilder(directory.getAbsolutePath()).whitelist().build();

		List<File> filesToBeZiped = de.getFilesToBeZiped();
		assertEquals(0, filesToBeZiped.size());
	}

	@Test
	public void whiteList() {

		File directory = new File("src/test/resources/countertest");

		ZipDefinition de = ZipUtil.zipBuilder(directory.getAbsolutePath()).whitelist("a").build();

		List<File> filesToBeZiped = de.getFilesToBeZiped();
		assertEquals(2, filesToBeZiped.size());
	}

	@Test
	public void emptyBlackList() {

		File directory = new File("src/test/resources/countertest");

		ZipDefinition de = ZipUtil.zipBuilder(directory.getAbsolutePath()).blacklist().build();

		List<File> filesToBeZiped = de.getFilesToBeZiped();
		assertEquals(3, filesToBeZiped.size());
	}

	@Test
	public void blackList() {

		File directory = new File("src/test/resources/countertest");

		ZipDefinition de = ZipUtil.zipBuilder(directory.getAbsolutePath()).blacklist("a").build();

		List<File> filesToBeZiped = de.getFilesToBeZiped();
		assertEquals(1, filesToBeZiped.size());
	}

	@Test
	public void testCountFilesInDirectory() {
		FileCounter counter = new FileCounter();
		File f = new File("src/test/resources/countertest");
		try {
			int files = counter.getNumberOfFilesInDir(f);
			assertEquals(files, 6);
		} catch (TooManyFilesFoundException tmffe) {
			fail("Unexpected TooManyFilesFoundException was caught", tmffe);
		}
	}

	@Test
	public void testCountTooManyFilesInDirectory() {
		FileCounter counter = new FileCounter();
		File f = new File("src/test/resources/countertest");
		try {
			int oneLessThanActualNumberOfFiles = 4;
			counter.getNumberOfFilesInDir(f, oneLessThanActualNumberOfFiles);
			fail("TooManyFilesFoundException wasn't thrown");
		} catch (TooManyFilesFoundException tmffe) {

		}
	}

}
