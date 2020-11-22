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
package com.loadcoder.load.intensity;

public class ThreadSynchronizer extends Thread {

	Thread current;

	int joined = 0;

	int amountToBeSynchronized = 0;

	public synchronized boolean syncMe() {
		joined++;
		if (joined >= amountToBeSynchronized) {
			this.interrupt();
			return false;
		} else {
			try {
				this.join();
				return true;
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public ThreadSynchronizer(Runnable runnable, int amountToBeSynchronized) {
		super(runnable);
		this.amountToBeSynchronized = amountToBeSynchronized;
	}
}