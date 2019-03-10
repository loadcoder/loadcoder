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
package com.loadcoder.load.scenario;

import static com.loadcoder.statics.LogbackLogging.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.loadcoder.result.Logs;
import com.loadcoder.result.Result;

public class FinishedLoadTest {

	Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void getReportWhenUsingSharedDirAppenderTest() {
		
		setResultDestination(getNewLogDir("target/" + this.getClass().getSimpleName()));
		LoadScenario ls = new LoadScenario() {
			public void loadScenario() {
				load("t1", ()->{}).perform();
			}
		};
		
		Load s = new LoadBuilder(ls).build();
		FinishedExecution finished = new ExecutionBuilder(s).build().execute().andWait();
		
		Result r = finished.getReportedResultFromResultFile(Logs.getResultFileInLogDir());
		Assert.assertEquals(r.getAmountOfTransactions(), 1);
	}
}
