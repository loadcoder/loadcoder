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

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.loadcoder.load.measure.TransactionExecutionResultBuffer;
import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeResultUpdaterRunnerTest {

	@Test
	public void runtimeResultUpdaterRunnerTest() {
		Execution e = Mockito.mock(Execution.class);

		TransactionExecutionResultBuffer buffer = new TransactionExecutionResultBuffer();
		buffer.getBuffer().add(new TransactionExecutionResult("a1", System.currentTimeMillis(), 10, true, null));
		when(e.getTransactionExecutionResultBuffer()).thenReturn(buffer);
		
		RuntimeResultUser user = (a)->{
			assertNotNull(a.get("a1"));
			assertEquals(a.get("a1").size(), 1);
		};
		
		RuntimeResultUpdaterRunner runtimeResultUpdaterRunner = new RuntimeResultUpdaterRunner(e, user);
		runtimeResultUpdaterRunner.swapOutDataAndCallUser();
		assertEquals(buffer.getBuffer().size(), 0);
		
	}
}
