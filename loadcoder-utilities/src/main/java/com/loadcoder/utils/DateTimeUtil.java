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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTimeUtil {

	private static final String DATETIME_FORMAT_DEFAULT = "yyyyMMdd-HHmmss";

	private static DateTimeFormatter dateTimeFormatterDefault = DateTimeFormatter.ofPattern(DATETIME_FORMAT_DEFAULT);

	private static SimpleDateFormat simpleDateFormatDefault = new SimpleDateFormat(DATETIME_FORMAT_DEFAULT);

	public static String getDateTimeNowString() {
		LocalDateTime timePoint = LocalDateTime.now();
		String dateTime = timePoint.format(dateTimeFormatterDefault);
		return dateTime;
	}

	public static String getDefaultDateTimeFormat() {
		return DATETIME_FORMAT_DEFAULT;
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

	public static Date getAsDate(String m) throws ParseException {
		Date d = simpleDateFormatDefault.parse(m);
		return d;
	}

	public static String getMillisAsHoursMinutesSecondsString(long millis) {
		String result = String.format("%dh %dmin %dsec", TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		if (result.startsWith("0h ")) {
			result = result.replace("0h ", "");
		}
		if (result.contains("0min ")) {
			result = result.replace("0min ", "");
		}
		if (result.contains(" 0sec")) {
			result = result.replace(" 0sec", "");
		}
		return result;
	}
}
