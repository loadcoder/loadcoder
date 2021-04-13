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
package com.loadcoder.network.spring;

import java.io.File;

public abstract class CryptographyStore {
	private final String resourcePath;
	private final File filePath;
	private final String pwd;

	public CryptographyStore(String resourcePath, String pwd) {
		if (resourcePath == null || resourcePath.isEmpty()) {
			throw new RuntimeException("resourcePath was null or empty");
		}
		this.resourcePath = resourcePath;
		this.filePath = null;
		this.pwd = pwd;
	}

	public CryptographyStore(File filePath, String pwd) {
		if (filePath == null) {
			throw new RuntimeException("filePath was null or empty");
		}
		this.resourcePath = null;
		this.filePath = filePath;
		this.pwd = pwd;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public File getFilePath() {
		return filePath;
	}

	public String getPwd() {
		return pwd;
	}
}
