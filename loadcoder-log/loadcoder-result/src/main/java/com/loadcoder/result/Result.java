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

import com.loadcoder.statics.Formatter;

public class Result {

	List<List<TransactionExecutionResult>> resultLists;

	long start;
	long end;
	long duration;

	int amountOfFails;
	int amountOfTransactions;

	File resultFile;

	public Result(File fileToGenerateResultFrom) {
		this(fileToGenerateResultFrom, Formatter.SIMPLE_RESULT_FORMATTER);
	}

	protected Result(List<List<TransactionExecutionResult>> resultLists) {
		init(resultLists);
	}

	public Result(File fileToGenerateResultFrom, ResultFormatter resultFormatter) {

		this.resultFile = fileToGenerateResultFrom;

		List<List<TransactionExecutionResult>> result;
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

	protected void init(List<List<TransactionExecutionResult>> resultLists) {

		this.resultLists = resultLists;

		long start = 0;
		long end = 0;

		int fails = 0;
		int transactions = 0;
		if (resultLists.size() > 0 && resultLists.get(0).size() > 0)
			start = resultLists.get(0).get(0).getTs();

		/*
		 * resultLists here needs to be synchronized with the usage of the same instace
		 * in RuntimeResultUpdaterRunner:run, where new elements are added to this list
		 */
		synchronized (resultLists) {

			for (List<TransactionExecutionResult> resultList : resultLists) {
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
		}
		setStart(start);
		setEnd(end);
		setDuration(end - start);
		setAmountOfFails(fails);
		setAmountOfTransactions(transactions);
	}

	public File getResultFile() {
		return resultFile;
	}

	public void setResultLists(List<List<TransactionExecutionResult>> resultLists) {
		this.resultLists = resultLists;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	protected void setAmountOfFails(int noOfFails) {
		this.amountOfFails = noOfFails;
	}

	protected void setAmountOfTransactions(int noOfTransactions) {
		this.amountOfTransactions = noOfTransactions;
	}

	public long getDuration() {
		return duration;
	}

	public int getAmountOfTransactions() {
		return amountOfTransactions;
	}

	public List<List<TransactionExecutionResult>> getResultLists() {
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
