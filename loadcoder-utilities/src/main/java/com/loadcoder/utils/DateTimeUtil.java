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

import java.io.File;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DateTimeUtil {

	private static final String DATETIME_FORMAT_DEFAULT = "yyyyMMdd-HHmmss";

	private static DateTimeFormatter dateTimeFormatterDefault = DateTimeFormatter.ofPattern(DATETIME_FORMAT_DEFAULT);

	private static SimpleDateFormat simpleDateFormatDefault = new SimpleDateFormat(DATETIME_FORMAT_DEFAULT);

	public static String getDateTimeNowString() {
		LocalDateTime timePoint = LocalDateTime.now();
		String dateTime = timePoint.format(dateTimeFormatterDefault);
		return dateTime;
	}

	public static LocalDateTime getStringToLocalDateTime(String dateTime) {
		LocalDateTime timePoint = LocalDateTime.parse(dateTime, dateTimeFormatterDefault);
		return timePoint;
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

		if (millis < 1000) {
			return "1sec";
		}

		String result = String.format("%dh %dmin %dsec", TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
		String[] splitted = result.split(" ");
		result = "";

		for (String s : splitted) {
			if (!s.startsWith("0")) {
				result += s + " ";
			}
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}

	private static class FileLocalDateTime {
		File f;
		LocalDateTime t;
	}

	public static File getLatestResultDir(String path) {
		List<Path> paths = FileUtil.listDirectory(path);
		List<File> files = FileUtil.getPathsAsFileList(paths);
		List<FileLocalDateTime> fileLocalDateTimes = files.stream().filter(a -> a.isDirectory()).map(a -> {
			FileLocalDateTime f = new FileLocalDateTime();
			f.f = a;
			try {
				String aName = a.getName();
				LocalDateTime aDate = DateTimeUtil.getStringToLocalDateTime(aName);
				f.t = aDate;
			} catch (RuntimeException r) {
			}
			return f;
		}).filter(a -> a.t != null).collect(Collectors.toList());
		fileLocalDateTimes.sort((a, b) -> {
			if (a.t.isAfter(b.t)) {
				return -1;
			} else {
				return 1;
			}
		});
		return fileLocalDateTimes.get(0).f;
	}

	public static File latestResultFile(String testResultBaseDir, String resultFileName) {
		File latestExeuctionDir = getLatestResultDir(testResultBaseDir);
		List<Path> paths = FileUtil.listDirectory(latestExeuctionDir.getAbsolutePath());
		List<File> files = FileUtil.getPathsAsFileList(paths);
		for (File logFile : files) {
			if (logFile.getName().equals(resultFileName)) {
				return logFile;
			}
		}
		throw new RuntimeException("Could not find result.log in directory" + latestExeuctionDir.getAbsolutePath());
	}

	public static File latestResultFile(String testResultBaseDir) {
		return latestResultFile(testResultBaseDir, "result.log");
	}
}
