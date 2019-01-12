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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartedExecution {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Execution execution;

	protected StartedExecution(Execution exeuction) {
		this.execution = exeuction;
	}

	/**
	 * Wait here until all the loads finishes
	 * 
	 * @return FinishedLoad instance of the finished load
	 */
	public FinishedExecution andWait() {
		long start = System.currentTimeMillis();

		for (Load load : execution.getLoads()) {
			try {
				load.getLoadStateThread().join();
			} catch (InterruptedException ie) {
				log.error("Unexpected InterruptedException caught", ie);
			}
		}

		if (execution.getRuntimeResultUpdaterThread() != null) {
			execution.getRuntimeResultUpdaterThread().interrupt();
		}
		log.debug("Load executed {} ms", (System.currentTimeMillis() - start));
		return new FinishedExecution(execution);
	}
}
