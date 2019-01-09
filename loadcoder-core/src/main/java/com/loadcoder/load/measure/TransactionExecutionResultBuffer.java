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

import java.util.ArrayList;
import java.util.List;

import com.loadcoder.result.TransactionExecutionResult;

public class TransactionExecutionResultBuffer {

	private List<TransactionExecutionResult> buffer = new ArrayList<TransactionExecutionResult>();

	/**
	 * CAUTION. This method shall only be used for tests of Loadcoder. Do not use
	 * this method if you are building load tests. The reason for this is becasue
	 * the use of the buffer needs to be synchrnoized as it invoked by parallell
	 * threads
	 * 
	 * @return the {@code List<TransactionExecutionResult> buffer} 
	 */
	public List<TransactionExecutionResult> getBufferForTesting() {
		return buffer;
	}

	/**
	 * 
	 * Add a TransactionExecutionResult into the buffer
	 * 
	 * @param transactionExecutionResult to be added into the buffer
	 */
	public void addResult(TransactionExecutionResult transactionExecutionResult) {
		synchronized (this) {
			buffer.add(transactionExecutionResult);
		}
	}

	/**
	 * Swapping out the used buffer List with a new one.
	 * 
	 * @return the used buffer List that will contain all made
	 *         TransactionExecutionResult from when the buffer was swapped last time
	 */
	public List<TransactionExecutionResult> swap() {
		List<TransactionExecutionResult> swappedOut;
		synchronized (this) {
			swappedOut = buffer;
			buffer = new ArrayList<TransactionExecutionResult>();
		}
		return swappedOut;
	}

}
