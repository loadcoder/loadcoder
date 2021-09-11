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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {

	Logger log = LoggerFactory.getLogger(this.getClass());

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

	/**
	 * A constants for buffer size used to read/write data
	 */
	private static final int BUFFER_SIZE = 4096;

	public byte[] zipToBytes(List<File> listFiles) throws FileNotFoundException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		List<File> list = new ArrayList<>();
		for (File file : listFiles) {
			if (file.isDirectory()) {
				zipDirectory(file, file.getName(), list);
			} else {
			}
		}
		byte[] bytes = os.toByteArray();

		os.flush();
		os.close();
		return bytes;
	}

	private void zipDirectory(File folder, String parentFolder, List<File> restulList)
			throws FileNotFoundException, IOException {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				zipDirectory(file, parentFolder + "/" + file.getName(), restulList);
				continue;
			}
			restulList.add(file);
		}
	}

	public static class ZipBuilder extends ZipBuilderFileAdder {

		public ZipBuilder(String basedirPath) {
			super(basedirPath, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		}

		public ZipBuilderBlackListed blacklist(String... list) {
			return new ZipBuilderBlackListed(basedirPath, relativeFilePathsFromBaseRootToAdd, Arrays.asList(list),
					relativePathChangers);
		}

		public ZipBuilderWhiteListed whitelist(String... list) {
			return new ZipBuilderWhiteListed(basedirPath, relativeFilePathsFromBaseRootToAdd, Arrays.asList(list),
					relativePathChangers);
		}

		public ZipBuilder addFile(String relativeFilePathFromBaseRootToAdd) {
			relativeFilePathsFromBaseRootToAdd.add(relativeFilePathFromBaseRootToAdd);
			return this;
		}

	}

	public static class ZipBuilderFileAdder {

		protected final String basedirPath;
		protected final List<String> relativeFilePathsFromBaseRootToAdd;
		protected final List<String> baseDirChildrenFilter;
		protected final List<RelativePathChanger> relativePathChangers;

		ZipBuilderFileAdder(String basedirPath, List<String> relativeFilePathsFromBaseRootToAdd,
				List<String> baseDirChildrenFilter, List<RelativePathChanger> relativePathChangers) {

			File f = new File(basedirPath);
			if (!f.exists()) {
				throw new RuntimeException("The specified path did not exist: " + basedirPath);
			}
			String absoluteBaseDirPath = f.getAbsolutePath();
			this.basedirPath = absoluteBaseDirPath;
			this.relativeFilePathsFromBaseRootToAdd = relativeFilePathsFromBaseRootToAdd;
			this.baseDirChildrenFilter = baseDirChildrenFilter;
			this.relativePathChangers = relativePathChangers;
		}

		public ZipBuilderFileAdder addFile(String relativeFilePathFromBaseRootToAdd) {
			relativeFilePathsFromBaseRootToAdd.add(relativeFilePathFromBaseRootToAdd);
			return this;
		}

		public ZipBuilderFileAdder changeFilePathInZip(RelativePathChanger relativePathChanger) {
			relativePathChangers.add(relativePathChanger);
			return this;
		}

		public ZipDefinition build() {
			File basedir = new File(basedirPath);
			File[] files = basedir.listFiles();
			List<File> fileToBeZiped = filterOutListedFiles(files, baseDirChildrenFilter);
			List<File> result = new ArrayList<>();
			try {
				zip(fileToBeZiped, result);
				for (String s : relativeFilePathsFromBaseRootToAdd) {
					matchAndZipFiles(basedir, s, result);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return new ZipDefinition(basedirPath, result, relativePathChangers);
		}

		public void matchAndZipFiles(File dirToIterate, String matchingFile, List<File> result) {
			File[] files = dirToIterate.listFiles();
			String[] matchedSplit = matchingFile.split("/", 2);
			String restToBeMatched = null;
			String nameToBeMatched = matchedSplit[0];
			if (matchedSplit.length > 1) {
				restToBeMatched = matchedSplit[1];
			}
			for (File f : files) {

				if (f.getName().matches(nameToBeMatched)) {

					if (matchedSplit.length == 1 || restToBeMatched == null || restToBeMatched.isEmpty()) {
						try {
							zip(Arrays.asList(f), result);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					} else {
						if (f.isDirectory()) {
							matchAndZipFiles(f, restToBeMatched, result);
						}
					}
				}
			}

		}

		public interface RelativePathChanger {
			String changeRelativeFilePathInZip(String originalFilePath, File fileToBeZipped);
		}

		public static class ZipDefinition {
			final String basedirPath;
			final List<File> fileToBeZiped;
			final List<RelativePathChanger> relativePathChangers;

			public ZipDefinition(String basedirPath, List<File> fileToBeZiped,
					List<RelativePathChanger> relativePathChangers) {
				this.basedirPath = basedirPath;
				this.fileToBeZiped = fileToBeZiped;
				this.relativePathChangers = relativePathChangers;
			}

			protected List<File> getFilesToBeZiped() {
				return fileToBeZiped;
			}

			public byte[] zipToBytes() {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(os);

				for (File file : fileToBeZiped) {
					String relativePath = file.getAbsolutePath().replace(basedirPath, "");
					for (RelativePathChanger relativePathChanger : relativePathChangers) {
						relativePath = relativePathChanger.changeRelativeFilePathInZip(relativePath, file);
					}
					zipFile(zos, file, relativePath);
				}
				try {
					zos.flush();
					zos.close();
					byte[] bytes = os.toByteArray();

					os.flush();
					os.close();
					return bytes;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			private void zipFile(ZipOutputStream zos, File fileToZip, String relativeToPath) {
				try {
					zos.putNextEntry(new ZipEntry(relativeToPath));
					writeFileToZipOutput(fileToZip, zos);
					zos.closeEntry();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			private void writeFileToZipOutput(File file, ZipOutputStream zos)
					throws FileNotFoundException, IOException {
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
				long bytesRead = 0;
				byte[] bytesIn = new byte[BUFFER_SIZE];
				int read = 0;
				while ((read = bis.read(bytesIn)) != -1) {
					zos.write(bytesIn, 0, read);
					bytesRead += read;
				}
				bis.close();
			}
		}

		protected List<File> filterOutListedFiles(File[] filesInDirectory, List<String> namesToFilter) {
			return filterOutBlackListedFiles(filesInDirectory, namesToFilter);
		}

		protected List<File> filterOutBlackListedFiles(File[] filesInDirectory, List<String> blackListedFiles) {
			List<File> fileToBeZiped = Arrays.asList(filesInDirectory);
			for (String whiteListedName : blackListedFiles) {
				fileToBeZiped = fileToBeZiped.stream().filter(file -> {
					return !(file.getName().equals(whiteListedName) || file.getName().matches(whiteListedName));
				}).collect(Collectors.toList());
			}
			return fileToBeZiped;
		}

		protected List<File> filterOutWhiteListedFiles(File[] filesInDirectory, List<String> whiteListedFiles) {
			List<File> fileToBeZiped = new ArrayList<>();
			for (File listedFile : filesInDirectory) {
				String listedFileName = listedFile.getName();
				for (String whiteListedName : whiteListedFiles) {
					if (listedFileName.equals(whiteListedName) || listedFileName.matches(whiteListedName)) {
						fileToBeZiped.add(listedFile);
						break;
					}
				}
			}
			return fileToBeZiped;
		}

		public void zip(List<File> listFiles, List<File> resultFile) throws FileNotFoundException, IOException {
			for (File file : listFiles) {
				if (file.isDirectory()) {
					zipDirectory(file, file.getName(), resultFile);
				} else {
					resultFile.add(file);
				}
			}
		}

		private void zipDirectory(File folder, String parentFolder, List<File> restulList)
				throws FileNotFoundException, IOException {
			for (File file : folder.listFiles()) {
				if (file.isDirectory()) {
					zipDirectory(file, parentFolder + "/" + file.getName(), restulList);
					continue;
				}
				restulList.add(file);
			}
		}
	}

	public static class ZipBuilderBlackListed extends ZipBuilderFileAdder {
		ZipBuilderBlackListed(String basedirPath, List<String> relativeFilePathsFromBaseRootToAdd, List<String> asdf,
				List<RelativePathChanger> relativePathChangers) {
			super(basedirPath, relativeFilePathsFromBaseRootToAdd, asdf, relativePathChangers);
		}
	}

	public static class ZipBuilderWhiteListed extends ZipBuilderFileAdder {
		ZipBuilderWhiteListed(String basedirPath, List<String> relativeFilePathsFromBaseRootToAdd, List<String> asdf,
				List<RelativePathChanger> relativePathChangers) {
			super(basedirPath, relativeFilePathsFromBaseRootToAdd, asdf, relativePathChangers);
		}

		@Override
		protected List<File> filterOutListedFiles(File[] filesInDirectory, List<String> namesToFilter) {
			return filterOutWhiteListedFiles(filesInDirectory, namesToFilter);
		}
	}

	public static ZipBuilder zipBuilder(String string) {
		// TODO Auto-generated method stub
		return new ZipBuilder(string);
	}
}