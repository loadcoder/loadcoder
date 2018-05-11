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
package com.loadcoder.load.measure;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class ResultFormatter{
	
	public abstract String toString(TransactionExecutionResult TransactionExecutionResult);
	protected abstract List<List<TransactionExecutionResult>> toResultLists(File file) throws IOException;
	
	public Result toResultList(File file) throws IOException{
		List<List<TransactionExecutionResult>> resultLists = toResultLists(file);
		
		Result r = new Result(resultLists);

		return r;
	}
	
}
