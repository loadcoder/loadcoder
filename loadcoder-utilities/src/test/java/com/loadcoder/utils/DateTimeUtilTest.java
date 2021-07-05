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

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.time.LocalDateTime;

import org.testng.annotations.Test;
import static com.loadcoder.utils.DateTimeUtil.*;

public class DateTimeUtilTest {

	@Test
	public void willDurationBeFormatedCorrectlyAsTime() {
		String result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(11 * 3600_000 + 12 * 60_000 + 13 * 1000);
		assertEquals(result, "11h 12min 13sec");

		result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(0 * 3600_000 + 12 * 60_000 + 13 * 1000);
		assertEquals(result, "12min 13sec");

		result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(0 * 3600_000 + 0 * 60_000 + 13 * 1000);
		assertEquals(result, "13sec");

		result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(11 * 3600_000 + 0 * 60_000 + 0 * 1000);
		assertEquals(result, "11h");

		result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(0 * 3600_000 + 12 * 60_000 + 0 * 1000);
		assertEquals(result, "12min");

		result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(11 * 3600_000 + 0 * 60_000 + 13 * 1000);
		assertEquals(result, "11h 13sec");

		result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(24 * 3600_000 + 0 * 60_000 + 0 * 1000);
		assertEquals(result, "24h");

		result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(1 * 3600_000 + 59 * 60_000 + 59 * 1000);
		assertEquals(result, "1h 59min 59sec");

		result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(0 * 3600_000 + 20 * 60_000 + 0 * 1000);
		assertEquals(result, "20min");

		result = DateTimeUtil.getMillisAsHoursMinutesSecondsString(0);
		assertEquals(result, "1sec");
	}

	@Test
	public void stringParsableToLocalDateTime() {
		LocalDateTime dateTime = DateTimeUtil.getStringToLocalDateTime("20210416-131415");
		int dayOfMonth = dateTime.getDayOfMonth();
		assertEquals(dayOfMonth, 16);
		int hour = dateTime.getHour();
		assertEquals(hour, 13);

	}

	@Test
	public void latestLogDirFound() {
		File f = getLatestResultDir("src/test/resources/testresults/loadtest-executions");
		assertEquals(f.getName(), "20210101-110000");
	}

	@Test
	public void isGettingTheCorrectLocatDateTimeFromString() {
		LocalDateTime dateTime = DateTimeUtil.getStringToLocalDateTime("20210416-131415");
		int dayOfMonth = dateTime.getDayOfMonth();
		assertEquals(dayOfMonth, 16);
		int hour = dateTime.getHour();
		assertEquals(hour, 13);
	}
}
