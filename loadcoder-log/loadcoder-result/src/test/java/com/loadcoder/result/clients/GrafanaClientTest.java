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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.loadcoder.result.ResultExtension;
import com.loadcoder.result.TransactionExecutionResult;
import com.loadcoder.result.clients.DateTimeUtil;
import com.loadcoder.result.clients.GrafanaClient;

public class GrafanaClientTest {

	/**
	 * This test creates a new dashbord in Grafana. Since Grafana needs to be
	 * available, this test is manual Run the test, then log in to Grafana and see
	 * if a Dashboard has been created.
	 */
	@Test(groups = "manual")
	public void createDashboard(Method method) {

		long end = System.currentTimeMillis();
		Map<String, List<TransactionExecutionResult>> list = new HashMap<String, List<TransactionExecutionResult>>();
		list.put("foo", Arrays.asList(new TransactionExecutionResult("foo", end - 10_000, 5, true, null),
				new TransactionExecutionResult(end - 5_000, 6, true, null)));
		ResultExtension r = new ResultExtension(list);

		// base64 encoded default grafana user:password
		String authorizationValue = "Basic YWRtaW46YWRtaW4=";
		GrafanaClient cli = new GrafanaClient("localhost", 3000, false, authorizationValue);
		int responseCode = cli.createNewDashboardFromResult(method.getName(), r);
		assertEquals(responseCode, 200);
		responseCode = cli.createNewDashboard(method.getName(), Arrays.asList("foo"));
	}

	@Test
	public void testCalendarUtil(Method method) {

		String dateTime = DateTimeUtil.convertMilliSecondsToFormattedDate(System.currentTimeMillis(),
				GrafanaClient.TIMESPAN_FORMAT);
		assertTrue(dateTime.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.000"));
	}

}
