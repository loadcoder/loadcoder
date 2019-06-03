package com.loadcoder.load.intensity;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.GeneralUtils;
import com.loadcoder.load.LoadUtility;

public class LoadThreadsSynchronizer {

  Logger logger = LoggerFactory.getLogger(this.getClass());
  Map<String, ThreadSynchronizer> threadSynchronizers = new ConcurrentHashMap<String, ThreadSynchronizer>();

  boolean stopped = false;
  public Map<String, ThreadSynchronizer> getThreadSynchronizers() {
    return threadSynchronizers;
  }

  public void peakMe(String transactionName, int amountToBeSynchronized, double changeForPeakOccuring) {

    ThreadSynchronizer threadSynchronizer;
    synchronized (this) {
      if(stopped) {
    	  return;
      }
    	threadSynchronizer = threadSynchronizers.get(transactionName);
      if (threadSynchronizer == null) {
        if (GeneralUtils.randomDouble(0.0, 1.0) < changeForPeakOccuring) {
          threadSynchronizer = new ThreadSynchronizer(() -> {
            LoadUtility.sleep(100_000);
          }, amountToBeSynchronized);
          threadSynchronizer.start();
          threadSynchronizers.put(transactionName, threadSynchronizer);
        } else {
          return;
        }
      }
    }
    boolean result = threadSynchronizer.syncMe();
    if (!result) {
      threadSynchronizers.remove(transactionName);
    }
  }

  public void releaseAllThreadSynchronizers() {
    synchronized (this) {
      Iterator<Entry<String, ThreadSynchronizer>> iterator = threadSynchronizers.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, ThreadSynchronizer> entry = iterator.next();
        entry.getValue().interrupt();
      }
      stopped = true;
    }
  }

}
