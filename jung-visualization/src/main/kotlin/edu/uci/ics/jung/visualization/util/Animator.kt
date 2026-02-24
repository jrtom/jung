/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.util

import edu.uci.ics.jung.algorithms.util.IterativeContext

/**
 * @author Tom Nelson - tomnelson@dev.java.net
 */
open class Animator @JvmOverloads constructor(
    protected val process: IterativeContext,
    /** How long the relaxer thread pauses between iteration loops. */
    var sleepTime: Long = 10L
) : Runnable {

    protected var stop: Boolean = false
    protected var thread: Thread? = null

    fun start() {
        // in case its running
        stop()
        stop = false
        thread = Thread(this).apply {
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    @Synchronized
    fun stop() {
        stop = true
    }

    override fun run() {
        while (!process.done() && !stop) {
            process.step()

            if (stop) {
                return
            }

            try {
                Thread.sleep(sleepTime)
            } catch (ie: InterruptedException) {
            }
        }
    }
}
