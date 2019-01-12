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
package com.loadcoder.result;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Extend this abstract class in order to implement your own ResultFormatter.
 */
public abstract class ResultFormatter{
	
	/**
	 * Generates and returns a String representation of the TransactionExecutionResult
	 * @param TransactionExecutionResult get the String representation from
	 * @return the string representation of TransactionExecutionResult
	 */
	public abstract String toString(TransactionExecutionResult TransactionExecutionResult);
	
	
	/**
	 * Reads the File and parses the expected TransactionExecutionResults.
	 * All TransactionExecutionResults with the same name is then added to a list,
	 * and then all lists with the TransactionExecutionResult are added to a list of lists
	 * @param file to read and parse the TransactionExecutionResults from
	 * @return a list of list of TransactionExecutionResults
	 * @throws IOException is thrown if there is some problem to read the input file
	 */
	protected abstract Map<String, List<TransactionExecutionResult>> toResultLists(File file) throws IOException;
	
	public Result toResultList(File file) throws IOException{
		Result r = new Result(file, this);
		return r;
	}
	
}
