package edu.uci.ics.jung.layout.util

import edu.uci.ics.jung.algorithms.util.IterativeContext
import org.slf4j.LoggerFactory

/**
 * a [Runnable] object to pass to the [Thread] that will perform the relax function on a
 * graph layout
 *
 * @author Tom Nelson
 */
class VisRunnable(
  private val iterativeContext: IterativeContext
) : Runnable {

  private val sleepTime: Long = 10
  @Volatile
  private var stop: Boolean = false

  init {
    log.trace("created a VisRunnable {} for {}", hashCode(), iterativeContext)
  }

  fun stop() {
    log.trace("told {} to stop", this)
    stop = true
  }

  override fun run() {
    while (!iterativeContext.done() && !stop) {
      try {
        iterativeContext.step()
        try {
          Thread.sleep(sleepTime)
        } catch (ex: InterruptedException) {
        }
      } catch (ex: Exception) {
        ex.printStackTrace()
      }
    }
    if (iterativeContext.done()) {
      log.trace("done here because {} is done", hashCode())
    }
    if (stop) {
      log.trace("done here because {} stop = {}", hashCode(), stop)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(VisRunnable::class.java)
  }
}
