/*******************************************************************************
 * Copyright (C) 2018 Stefan Vahlgren at Loadcoder
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
package com.loadcoder.load.scenario;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.exceptions.NoResultOrFormatterException;
import com.loadcoder.result.Logs;
import com.loadcoder.result.Result;

public class FinishedLoad{
	Load s;
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	public final static String RESULTFILE_DEFAULT = "result.log";
	FinishedLoad(Load s){
		this.s = s;
	}

	/**
	 * @return a new Result instance from the finished load
	 * @throws NoResultOrFormatterException
	 */
	public Result getReportedResultFromResultFile() throws NoResultOrFormatterException{
		File f = Logs.getResultFileInLogDir();
		return getReportedResultFromResultFile(f);
	}
	
	public Result getReportedResultFromResultFile(File resultFile)
			throws NoResultOrFormatterException {
		if (s.getResultFormatter() != null) {
			try {
				long startTime = System.currentTimeMillis();
				Result result = s.getResultFormatter().toResultList(resultFile);
				long executionTime = System.currentTimeMillis() - startTime;
				log.debug(String.format("Time taken to generate result: %s ms",
						executionTime));

				return result;
			} catch (IOException ioe) {
				throw new RuntimeException("Could not read the resultFile "
						+ resultFile, ioe);
			}
		}
		throw new NoResultOrFormatterException("The report can not be produced"
				+ " since the ResultFormatter or/and ResultDestination "
				+ "seems to be missning in the scenario");
	}

}
