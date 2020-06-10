/*******************************************************************************
 * Copyright (C) 2020 Team Loadcoder
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
package com.loadcoder.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

	private static final String DATETIME_FORMAT_DEFAULT = "yyyyMMdd-HHmmss";

	private static DateTimeFormatter dateTimeFormatterDefault = DateTimeFormatter.ofPattern(DATETIME_FORMAT_DEFAULT);

	public static String getDateTimeNowString() {
		LocalDateTime timePoint = LocalDateTime.now();
		String dateTime = timePoint.format(dateTimeFormatterDefault);
		return dateTime;
	}

	/**
	 * Changes the static date and time format of the directory that the method
	 * {@code getNewLogDir} creates
	 * 
	 * @param dateTimeFormatter is the format to be changed to
	 */
	public static void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
		dateTimeFormatterDefault = dateTimeFormatter;
	}

	/**
	 * Get a formatted String of the date equivalent to the timestamp. The used
	 * format is yyyyMMdd-HHmmss
	 * 
	 * @param timestamp is the timestamp in milliseconds
	 * @return a formatted String of the Date and Time
	 */
	public static String convertMilliSecondsToFormattedDate(long timestamp) {
		return convertMilliSecondsToFormattedDate(timestamp, dateTimeFormatterDefault);
	}

	/**
	 * Get a formatted String of the date equivalent to the timestamp.
	 * 
	 * @param timestamp        is the timestamp in milliseconds
	 * @param simpleDateFormat is the format
	 * @return a formatted String of the Date and Time
	 */
	public static String convertMilliSecondsToFormattedDate(long timestamp, DateTimeFormatter simpleDateFormat) {

		LocalDateTime date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
		String result = date.format(simpleDateFormat);
		return result;
	}
}
