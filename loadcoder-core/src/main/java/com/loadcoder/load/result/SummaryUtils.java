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
package com.loadcoder.load.result;

import static com.loadcoder.load.result.Summary.*;

import com.loadcoder.load.measure.Result;

public class SummaryUtils {

	public static void printSimpleSummary(Result result, String resultName) {
		
		Summary resultSummarizer = new Summary(result);
		resultSummarizer
		.log((a)->{return String.format("Summary for %s", resultName);})
		.log(duration())
		.table()
		.column("Transaction", transactionNames())
		.column("MAX", max())
		.column("AVG", avg())
		.column("80%", percentile(80))
		.column("90%", percentile(90))
		.print();
	}
}
