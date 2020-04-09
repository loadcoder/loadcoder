/*******************************************************************************
 * Copyright (C) 2020 Stefan Vahlgren at Loadcoder
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
package com.loadcoder.cluster;

import org.testng.annotations.Test;

import com.loadcoder.cluster.clients.docker.DockerClusterClient;
import com.loadcoder.cluster.clients.influxdb.InfluxDBClient;
import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.scenario.ExecutionBuilder;
import com.loadcoder.load.scenario.Load;
import com.loadcoder.load.scenario.LoadBuilder;
import com.loadcoder.load.scenario.LoadScenario;
import static com.loadcoder.statics.Statics.*;
public class InfluxReportTest {

	@Test
	public void influxReporterTest() {
		LoadScenario ls = new LoadScenario() {

			@Override
			public void loadScenario() {
				load("simple-transaction", () -> { LoadUtility.sleep(54);
				}).perform();
			}
		};

		Load l = new LoadBuilder(ls).stopDecision(iterations(50)).throttle(2, PER_SECOND, SHARED).build();
		new ExecutionBuilder(l).storeAndConsumeResultRuntime(InfluxDBClient
				.setupInfluxDataConsumer(new DockerClusterClient(), "LoadcoderClusterTests", "InfluxReportTest"))
				.build().execute().andWait();
	}
}
