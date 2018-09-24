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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.loadcoder.result.TransactionExecutionResult;
import com.loadcoder.result.clients.InfluxDBClient;
import com.loadcoder.result.clients.InfluxDBClient.InfluxDBTestExecution;

public class InfluxDBClientTest {

	@Test(groups = "manual")
	public void createEntry() {
		InfluxDBClient cli = new InfluxDBClient("localhost", 8086, false, "stefansDB");
		InfluxDBTestExecution exe = cli.createTestExecution("foo" +System.currentTimeMillis());
		int responseCode = -1;
		Map<String, List<TransactionExecutionResult>> transactions = new HashMap<String, List<TransactionExecutionResult>>();
		transactions.put("hej",
				Arrays.asList(new TransactionExecutionResult(System.currentTimeMillis(), 0L, true, "")));

		responseCode = exe.writeTransactions(transactions);
		assertEquals(responseCode, 204);
	}
}