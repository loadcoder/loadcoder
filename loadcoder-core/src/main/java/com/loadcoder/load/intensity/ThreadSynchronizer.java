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