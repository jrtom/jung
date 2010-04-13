/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * 
 */
package edu.uci.ics.jung.visualization.util;

import edu.uci.ics.jung.algorithms.util.IterativeContext;

/**
 * 
 * 
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 */
public class Animator implements Runnable {
	
	protected IterativeContext process;
	protected boolean stop;
	protected Thread thread;
	
	/**
	 * how long the relaxer thread pauses between iteration loops.
	 */
	protected long sleepTime = 10L;

	
	public Animator(IterativeContext process) {
		this(process, 10L);
	}

	public Animator(IterativeContext process, long sleepTime) {
		this.process = process;
		this.sleepTime = sleepTime;
	}

	/**
	 * @return the relaxerThreadSleepTime
	 */
	public long getSleepTime() {
		return sleepTime;
	}

	/**
	 * @param relaxerThreadSleepTime the relaxerThreadSleepTime to set
	 */
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}
	
	public void start() {
		// in case its running
		stop();
		stop = false;
		thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	public synchronized void stop() {
		stop = true;
	}

	public void run() {
		while (!process.done() && !stop) {

			process.step();

			if (stop)
				return;

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException ie) {
			}
		}
	}
}
