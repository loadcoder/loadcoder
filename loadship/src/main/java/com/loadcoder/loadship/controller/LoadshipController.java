/*******************************************************************************
 * Copyright (C) 2021 Team Loadcoder
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
package com.loadcoder.loadship.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loadcoder.loadship.utils.ChecksumUtil;
import com.loadcoder.utils.FileUtil;

@RestController
@RequestMapping(value = "/loadship")
public class LoadshipController {

	final String DEFAULT_FILESTORAGE_ROOTPATH = "loadshipFilestorage";
	File loadshipFilestorage = getFileStorageFile();
	
	Map<String, byte[]> dataStore = new HashMap<String, byte[]>();

	ChecksumUtil util = new ChecksumUtil();
	long lastExecution = System.currentTimeMillis();

	@Autowired
	SystemService systemService;
	
	public LoadshipController() {
	}

	File getFileStorageFile(){
		String fileStorageRootPath = System.getProperty("filestorage.path");
		File rootPath;
		if(fileStorageRootPath == null) {
			rootPath = new File(DEFAULT_FILESTORAGE_ROOTPATH);
		}else {
			rootPath = new File(fileStorageRootPath);
		}
		FileUtil.createDir(rootPath);
		return rootPath;
	}

	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public String status(HttpServletRequest request, HttpServletResponse response) {
		return "Up";
	}
	
	@RequestMapping(value = "/posttest", method = RequestMethod.POST)
	public String posttest(HttpServletRequest request, HttpServletResponse response) {
		return "Up";
	}
	
	@RequestMapping(value = "/data", method = RequestMethod.GET)
	public byte[] getData(HttpServletRequest request, HttpServletResponse response, @RequestParam String checksum) {

		throttle();
		if (dataStore.isEmpty()) {
			throw new RuntimeException("No data available");
		}
		byte[] bytes = dataStore.get(checksum);
		if (bytes != null) {
			return bytes;
		}
		throw new RuntimeException("The provided checksum doesnt match the checksum of the stored data");
	}

	public void throttle() {
		synchronized (this) {
			long now = System.currentTimeMillis();
			if (now - lastExecution < 1000) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			lastExecution = System.currentTimeMillis();
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/data", consumes = "application/octet-stream;UTF-8")
	public void postData(@RequestBody byte[] b){
		synchronized (this) {
			String md5 = ChecksumUtil.md5(b);
			dataStore.put(md5, b);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/file", consumes = "application/octet-stream;UTF-8")
	public void storeFile(@RequestHeader("dirname") String dirName, @RequestHeader("filename") String fileName,@RequestBody byte[] b) {
		synchronized (this) {
			File dirPath = new File(loadshipFilestorage, dirName);
			File filePath = new File(dirPath.getAbsolutePath(), fileName);
			FileUtil.createDir(dirPath);
			if(filePath.exists()) {
				throw new RuntimeException("File already exist:" );
			}
			FileUtil.writeFile(b, filePath);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/file")
	public String getFile(@RequestHeader("dirname") String dirName, @RequestHeader("filename") String fileName) {
		synchronized (this) {
			File dirPath = new File(loadshipFilestorage, dirName);
			File filePath = new File(dirPath.getAbsolutePath(), fileName);
			String content = FileUtil.readFile(filePath);
			return content;
		}
	}
}