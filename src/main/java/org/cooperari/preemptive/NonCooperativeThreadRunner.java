//
//   Copyright 2014-2019 Eduardo R. B. Marques
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package org.cooperari.preemptive;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.cooperari.errors.CDeadlockError;
import org.cooperari.errors.CInternalError;
import org.cooperari.core.CSession;
import org.cooperari.core.CWorkspace;

/**
 * Helper class to run a set of threads in non-cooperative manner.
 * 
 * Creating an object of this type will launch a set of threads 
 * and make the calling thread wait for their completion.
 * 
 * The launched threads run in daemon mode and all go
 * through an initialization barrier before executing their
 * code (no thread exits before all of them started).
 * 
 * @since 0.1
 */
public class NonCooperativeThreadRunner implements UncaughtExceptionHandler {

  /**
   * Array of threads.
   */
  private final Thread[] _threads;

  /**
   * Initialization flag.
   */
  private final AtomicBoolean _sync;

  /**
   * Uncaught exception. 
   * TODO: should be a list
   */
  private Throwable _uncaughtException;

  /**
   * Constructor. 
   * @param rv Runnable objects.
   */
  public NonCooperativeThreadRunner(Runnable[] rv) {
    _threads = new Thread[rv.length];
    _sync = new AtomicBoolean(false);
    _uncaughtException = null;
    for (int i = 0; i < rv.length; i++) {
      Thread t = new NCThread(i, rv[i]);
      t.setUncaughtExceptionHandler(this);
      t.start();
      _threads[i] = t;
    }


    HashSet<Thread> set = new HashSet<>();
    for (Thread t : _threads) {
      set.add(t); 
    }
    int ldCtr = 0;
    _sync.set(true);
    assert CWorkspace.debug("waiting for all threads to join");
    while(!set.isEmpty()) { 
      int aliveThreads = set.size();
      int waitingThreads = 0, blockedThreads = 0;
      Iterator<Thread> itr = set.iterator();
      while (itr.hasNext()) {
        Thread t = itr.next();
        assert CWorkspace.debug("%s %s", t.getName(), t.getState());
        if (!t.isAlive()) {
          --aliveThreads;
          itr.remove();
        } else if (t.getState() == Thread.State.WAITING) {
          waitingThreads ++;
        } else if (t.getState() == Thread.State.BLOCKED) {
          blockedThreads++;
        }
      } 
      assert CWorkspace.debug("A %d B %d W %d CTR %d", aliveThreads, blockedThreads, waitingThreads, ldCtr);
      if (aliveThreads > 0) { 
        if (blockedThreads + waitingThreads == aliveThreads) {
          assert CWorkspace.debug("POSSIBLE DEADLOCK");

          ThreadMXBean bean = ManagementFactory.getThreadMXBean();
          long[] threadIds = bean.findDeadlockedThreads(); 
          ldCtr++;
          if (threadIds != null || ldCtr >= 1000) {
            throw new CDeadlockError("All threads deadlocked.");
          }
        } else {
          ldCtr = 0;
        }
        try { Thread.sleep(1); } catch(Throwable e) { }
      }
      
    }
    if (_uncaughtException != null) {
      if (_uncaughtException instanceof Error)
        throw (Error) _uncaughtException;
      if (_uncaughtException instanceof RuntimeException)
        throw (RuntimeException) _uncaughtException;
      throw new CInternalError(_uncaughtException); // should not happen
    }
  }

  /**
   * Listener method for uncaught exceptions.
   * @param t Thread.
   * @param e Exception.
   */
  @Override
  public void uncaughtException(Thread t, Throwable e) {
    _uncaughtException = e;
    assert CWorkspace.debug(t, e);
  }

  /**
   * Inner class for threads that execute non-cooperatively.
   */
  private class NCThread extends Thread {
    /**
     * Runnable object.
     */
    private final Runnable _runnable;

    /**
     * Constructor. 
     * @param id Numerical thread identifier.
     * @param r Runnable to use in association to the thread.
     */
    public NCThread(int id, Runnable r) {
      super("Thread-"+ id);
      setDaemon(true);
      _runnable = r;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      CWorkspace.debug("%s starting!", getName());

      // Initialization barrier.
      CSession.getRuntime().join();
      while (! _sync.get()) {
        try { Thread.sleep(1); } catch(Throwable e) { }
      }
      CWorkspace.debug("%s passed sync. barrier!", getName());
      _runnable.run();
      CWorkspace.debug("%s done!", getName());
    }
  };
}
