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
import java.util.List;
import java.util.Map;

import com.loadcoder.statics.Formatter;

public class Result {

	private Map<String, List<TransactionExecutionResult>> resultLists;

	private long start;
	private long end;
	private long duration;

	private int amountOfFails;
	private int amountOfTransactions;

	private File resultFile;

	/**
	 * Constructor for the Result
	 * @param fileToGenerateResultFrom is the File from where the Result shall be generated
	 */
	public Result(File fileToGenerateResultFrom) {
		this(fileToGenerateResultFrom, Formatter.SIMPLE_RESULT_FORMATTER);
	}

	protected Result(Map<String, List<TransactionExecutionResult>> resultLists) {
		init(resultLists);
	}

	/**
	 * Constructor for the Result
	 * @param fileToGenerateResultFrom is the File from where the Result shall be generated
	 * @param resultFormatter is the ResultFormatter that will parse the fileToGenerateResultFrom
	 * to a Result
	 * 
	 */
	public Result(File fileToGenerateResultFrom, ResultFormatter resultFormatter) {

		this.resultFile = fileToGenerateResultFrom;

		Map<String, List<TransactionExecutionResult>> result;
		try {
			result = resultFormatter.toResultLists(fileToGenerateResultFrom);
		} catch (IOException ioe) {
			throw new RuntimeException(
					String.format("Could not generate a Result from the file %s and given ResultFormatter",
							fileToGenerateResultFrom.getAbsolutePath()),
					ioe);
		}
		init(result);
	}

	private void init(Map<String, List<TransactionExecutionResult>> resultLists) {

		this.resultLists = resultLists;

		long start = Long.MAX_VALUE;
		long end = Long.MIN_VALUE;

		int fails = 0;
		int transactions = 0;

		for (String key : resultLists.keySet()) {
			List<TransactionExecutionResult> resultList = resultLists.get(key);
			transactions += resultList.size();
			for (TransactionExecutionResult transactionExecutionResult : resultList) {
				long ts = transactionExecutionResult.getTs();
				if (ts < start)
					start = ts;
				if (ts > end)
					end = ts;

				if (!transactionExecutionResult.isStatus())
					fails++;
			}
		}

		setStart(start);
		setEnd(end);
		setDuration(end - start);
		setAmountOfFails(fails);
		setAmountOfTransactions(transactions);
	}

	
	/**
	 * Get the Result file used to generate the Result
	 * @return the file of the result
	 */
	public File getResultFile() {
		return resultFile;
	}

	private void setStart(long start) {
		this.start = start;
	}

	private void setEnd(long end) {
		this.end = end;
	}

	private void setDuration(long duration) {
		this.duration = duration;
	}

	private void setAmountOfFails(int noOfFails) {
		this.amountOfFails = noOfFails;
	}

	private void setAmountOfTransactions(int noOfTransactions) {
		this.amountOfTransactions = noOfTransactions;
	}

	public long getDuration() {
		return duration;
	}

	public int getAmountOfTransactions() {
		return amountOfTransactions;
	}

	public Map<String, List<TransactionExecutionResult>> getResultLists() {
		return resultLists;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public int getAmountOfFails() {
		return amountOfFails;
	}

	/**
	 * Merges the provided resultToBeMerged into the Result
	 * @param resultToBeMerged the Result about to be merged into this
	 */
	public void mergeResult(Result resultToBeMerged) {

		if (this.equals(resultToBeMerged))
			return;
		if (resultToBeMerged.getStart() < start) {
			start = resultToBeMerged.getStart();
		}
		resultToBeMerged.setStart(-1);

		if (resultToBeMerged.getStart() > end) {
			end = resultToBeMerged.getEnd();
		}
		resultToBeMerged.setEnd(-1);

		duration = end - start;
		resultToBeMerged.setDuration(-1);

		amountOfFails += resultToBeMerged.getAmountOfFails();
		amountOfTransactions += resultToBeMerged.getAmountOfTransactions();
	}

}
