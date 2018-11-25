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
package com.loadcoder.result.clients;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {

	private static final String DATETIME_FORMAT_DEFAULT = "yyyyMMdd-HHmmss";

	private static final SimpleDateFormat SIMPLEDATEFORMAT_DEFAULT = new SimpleDateFormat(DATETIME_FORMAT_DEFAULT);

	/**
	 * Get a formatted String of the date equivalent to the timestamp. The used
	 * format is yyyyMMdd-HHmmss
	 * 
	 * @param timestamp is the timestamp in milliseconds
	 * @return a formatted String of the Date and Time
	 */
	public static String convertMilliSecondsToFormattedDate(long timestamp) {
		return convertMilliSecondsToFormattedDate(timestamp, SIMPLEDATEFORMAT_DEFAULT);
	}

	/**
	 * Get a formatted String of the date equivalent to the timestamp.
	 * 
	 * @param timestamp is the timestamp in milliseconds
	 * @param simpleDateFormat is the format
	 * @return a formatted String of the Date and Time
	 */
	public static String convertMilliSecondsToFormattedDate(long timestamp, SimpleDateFormat simpleDateFormat) {
		Timestamp ts = new Timestamp(timestamp);
		Date date = new Date(ts.getTime());
		return simpleDateFormat.format(date);
	}
}