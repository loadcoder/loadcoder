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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	protected static class FileCounter {
		int count;
		int MAX_EXPECTED_FILES_TO_FIND_DEFAULT = 500;
		int maxExpectedFilesToFind;

		protected int getNumberOfFilesInDir(File f) throws TooManyFilesFoundException {
			count = 0;
			maxExpectedFilesToFind = MAX_EXPECTED_FILES_TO_FIND_DEFAULT;
			countFiles(f);
			return count;
		}

		protected int getNumberOfFilesInDir(File f, int maxExpectedFilesToFind) throws TooManyFilesFoundException {
			count = 0;
			this.maxExpectedFilesToFind = maxExpectedFilesToFind;
			countFiles(f);
			return count;
		}

		private void countFiles(File f) throws TooManyFilesFoundException {
			File[] files = f.listFiles();

			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					count++;
					if (count > maxExpectedFilesToFind) {
						throw new TooManyFilesFoundException();
					}
					File file = files[i];

					if (file.isDirectory()) {
						countFiles(file);
					}
				}
			}
		}
	}

	protected List<File> filterOutWhiteListedFiles(File[] filesInDirectory, String... whiteListedFiles) {
		List<File> fileToBeZiped = new ArrayList<>();
		for (File listedFile : filesInDirectory) {
			String listedFileName = listedFile.getName();
			for (String whiteListedName : whiteListedFiles) {
				if (listedFileName.equals(whiteListedName)) {
					fileToBeZiped.add(listedFile);
					break;
				}
			}
		}
		return fileToBeZiped;
	}

	public void zip(File directory, File zipFileDestination, String... whiteListedFiles) {

		File[] files = directory.listFiles();
		List<File> fileToBeZiped = filterOutWhiteListedFiles(files, whiteListedFiles);

		try {
			zip(fileToBeZiped, zipFileDestination);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public byte[] zipToBytes(File directory, String... whiteListedFiles) {

		File[] files = directory.listFiles();
		List<File> fileToBeZiped = filterOutWhiteListedFiles(files, whiteListedFiles);

		try {
			byte[] bytes = zipToBytes(fileToBeZiped);
			return bytes;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * A constants for buffer size used to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;

	/**
	 * Compresses a list of files to a destination zip file
	 * 
	 * @param listFiles   A collection of files and directories
	 * @param destZipFile The path of the destination zip file
	 * @throws FileNotFoundException when a file that is specified in the listFiles parameter can't be found
	 * @throws IOException when there is some problem with reading or writing the files
	 */
	public void zip(List<File> listFiles, File destZipFile) throws FileNotFoundException, IOException {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile));
		for (File file : listFiles) {
			if (file.isDirectory()) {
				zipDirectory(file, file.getName(), zos);
			} else {
				zipFile(file, zos);
			}
		}
		zos.flush();
		zos.close();
	}

	public byte[] zipToBytes(List<File> listFiles) throws FileNotFoundException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(os);
		for (File file : listFiles) {
			if (file.isDirectory()) {
				zipDirectory(file, file.getName(), zos);
			} else {
				zipFile(file, zos);
			}
		}
		zos.flush();
		zos.close();
		byte[] bytes = os.toByteArray();

		os.flush();
		os.close();
		return bytes;
	}

	/**
	 * Adds a directory to the current zip output stream
	 * 
	 * @param folder       the directory to be added
	 * @param parentFolder the path of parent directory
	 * @param zos          the current zip output stream
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos)
			throws FileNotFoundException, IOException {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				zipDirectory(file, parentFolder + "/" + file.getName(), zos);
				continue;
			}
			zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			long bytesRead = 0;
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = bis.read(bytesIn)) != -1) {
				zos.write(bytesIn, 0, read);
				bytesRead += read;
			}
			zos.closeEntry();
		}
	}

	/**
	 * Adds a file to the current zip output stream
	 * 
	 * @param file the file to be added
	 * @param zos  the current zip output stream
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void zipFile(File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
		zos.putNextEntry(new ZipEntry(file.getName()));
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		long bytesRead = 0;
		byte[] bytesIn = new byte[BUFFER_SIZE];
		int read = 0;
		while ((read = bis.read(bytesIn)) != -1) {
			zos.write(bytesIn, 0, read);
			bytesRead += read;
		}
		zos.closeEntry();
	}
}