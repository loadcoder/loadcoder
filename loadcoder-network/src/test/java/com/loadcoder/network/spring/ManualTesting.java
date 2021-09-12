/*******************************************************************************
 * Copyright (C) 2021 Team Loadcoder
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
package com.loadcoder.network.spring;

import static com.loadcoder.statics.Statics.PER_SECOND;
import static com.loadcoder.statics.Statics.PER_THREAD;
import static com.loadcoder.statics.Statics.SECOND;
import static com.loadcoder.statics.Statics.duration;

import java.io.File;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import com.loadcoder.load.chart.logic.RuntimeChart;
import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenarioTyped;
import com.loadcoder.result.Result;

public class ManualTesting {

	@Test
	public void loadTest() {
		
		LoadScenarioTyped<RestTemplate> ls = new LoadScenarioTyped<RestTemplate>() {
			@Override
			public void loadScenario(RestTemplate t) {
				load("mtlsCall", () -> t.getForEntity("https://localhost:8490/customer/get?email=hello", String.class)).perform();
			}

			@Override
			public RestTemplate createInstance() {
				RestTemplate client = SpringUtil.clientBuilder()
						.keyStore(new File("clientkeystore.jks"), "changeit")
						.trustAll()
						.build();
				return client;
			}
		};
		
		Load l = new LoadBuilder(ls)
				.amountOfThreads(40)
				.throttle(3,PER_SECOND, PER_THREAD)
				.stopDecision(duration(300 * SECOND))
				.rampup(123 * SECOND +359)
				.build();
		
		RuntimeChart chart = new RuntimeChart();
		Result r = new ExecutionBuilder(l).storeAndConsumeResultRuntime(chart).build().execute().andWait().getReportedResultFromResultFile();
		chart.waitUntilClosed();
	}
}
