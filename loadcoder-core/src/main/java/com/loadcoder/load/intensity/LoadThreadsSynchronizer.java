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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
