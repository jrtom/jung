/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An interface for algorithms that proceed iteratively. */
public interface IterativeContext {

  interface WithPreRelax extends IterativeContext {}

  /** Advances one step. */
  void step();

  /** @return {@code true} if this iterative process is finished, and {@code false} otherwise. */
  boolean done();

  /** may be passed to a Thread to run the context */
  class ContextRunnable implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ContextRunnable.class);
    IterativeContext process;
    long sleepTime = 10L;

    public ContextRunnable(IterativeContext process) {
      this.process = process;
    }

    @Override
    public void run() {
      while (!process.done()) {

        process.step();

        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException ie) {
          ie.printStackTrace();
        }
      }
      log.trace("process is done!");
    }
  }
}
