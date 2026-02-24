package edu.uci.ics.jung.layout.model

import com.google.common.collect.Lists
import java.util.Collections
import org.slf4j.LoggerFactory

/**
 * default implementation of LayoutModel.LayoutStateChangeSupport Manages a list of listeners and a
 * mutable flag to assert whether or not it should fire events
 */
class DefaultLayoutStateChangeSupport : LayoutModel.LayoutStateChangeSupport {

  /** to fire or not to fire.... */
  override var isFireEvents: Boolean = false

  /** listeners for these changes */
  override val layoutStateChangeListeners: MutableList<LayoutModel.LayoutStateChangeListener> =
    Collections.synchronizedList(Lists.newArrayList())

  override fun addLayoutStateChangeListener(l: LayoutModel.LayoutStateChangeListener) {
    layoutStateChangeListeners.add(l)
  }

  override fun removeLayoutStateChangeListener(l: LayoutModel.LayoutStateChangeListener) {
    layoutStateChangeListeners.remove(l)
  }

  override fun fireLayoutStateChanged(source: LayoutModel<*>, state: Boolean) {
    log.trace("fireLayoutStateChange to {}", state)
    if (layoutStateChangeListeners.isNotEmpty()) {
      // make an event and fire it
      val evt = LayoutModel.LayoutStateChangeEvent(source, state)
      for (listener in layoutStateChangeListeners) {
        listener.layoutStateChanged(evt)
      }
    } else {
      log.trace("there are no listeners for {}", this)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(DefaultLayoutStateChangeSupport::class.java)
  }
}
