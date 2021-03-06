/*******************************************************************************
 * Copyright (C) 2018 Team Loadcoder
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
package com.loadcoder.result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Logs {

	public final static String RESULTFILE_DEFAULT = "result.log";

	public static final List<SharedDirFileAppender> sharedDirFileAppenders = new ArrayList<SharedDirFileAppender>();

	private static File logDir = new File(".");

	private static DirProvider defaultProvider = () -> {
		return new File("");
	};

	private static DirProvider sharedDirProvider = () -> {
		return getLogDir();
	};

	private static DirProvider dirProvider = defaultProvider;

	public static List<SharedDirFileAppender> getshared() {
		return sharedDirFileAppenders;
	}

	public static File getLogDir() {
		return logDir;
	}

	public static File getFile() {
		return dirProvider.getFile();
	}

	/**
	 * 
	 * @return the current result file assuming the files name is result.log
	 */
	public static File getResultFileInLogDir() {
		File resultFile = new File(logDir, "result.log");
		return resultFile;
	}

	protected static void changeToSharedDir(File newDir) throws IOException {
		logDir = newDir;
		dirProvider = sharedDirProvider;
		synchronized (Logs.sharedDirFileAppenders) {
			for (SharedDirFileAppender sharedDirFileAppender : Logs.sharedDirFileAppenders) {
				sharedDirFileAppender.changeToDir(newDir);
			}
		}
	}
}
