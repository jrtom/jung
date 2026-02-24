package edu.uci.ics.jung.layout.model

import com.google.common.collect.Lists
import java.util.Collections
import org.slf4j.LoggerFactory

/**
 * an Event system for LayoutModels to announce the change in the location of Nodes This replaces
 * the dependency on the javax.swing.ChangeEvent, and is used to alert the visualization system that
 * it should re-draw the visualization
 *
 * @author Tom Nelson
 */
class DefaultLayoutModelChangeSupport : LayoutModel.ChangeSupport {

  override var isFireEvents: Boolean = true
    set(value) {
      log.trace("setFireEvents {}", value)
      field = value
      // any time we turn this back on, fire an event in case
      // anything was missed while it was off
      if (value) {
        fireChanged()
      }
    }

  override val changeListeners: MutableList<LayoutModel.ChangeListener> =
    Collections.synchronizedList(Lists.newArrayList())

  override fun addChangeListener(l: LayoutModel.ChangeListener) {
    changeListeners.add(l)
  }

  override fun removeChangeListener(l: LayoutModel.ChangeListener) {
    changeListeners.remove(l)
  }

  override fun fireChanged() {
    if (isFireEvents && changeListeners.isNotEmpty()) {
      for (listener in changeListeners) {
        listener.changed()
      }
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(DefaultLayoutModelChangeSupport::class.java)
  }
}
