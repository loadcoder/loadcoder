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

import static com.loadcoder.load.LoadUtility.sleep;

import java.lang.Thread.State;
import java.util.List;

public class LoadStateRunner implements Runnable {

	Load l;

	public LoadStateRunner(Load l) {
		this.l = l;
	}

	public void run() {
		// wait until there are some threads and the amount is equal to the target
		while (l.getThreads() == null || l.getThreads().size() != l.getAmountOfThreads()) {
			sleep(200);
		}

		// Wait until all threads are in State TERMINATED
		whileloop: while (true) {
			List<Thread> threads = l.getThreads();
			for (Thread t : threads) {
				if (t.getState() != State.TERMINATED) {
					try {
						t.join();
					} catch (InterruptedException ie) {
						throw new Error("detts ska inte handa");
					}

					continue whileloop;
				}
			}
			break;
		}
	}
}
