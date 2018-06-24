package edu.uci.ics.jung.layout.model;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * default implementation of LayoutModel.LayoutStateChangeSupport Manages a list of listeners and a
 * mutable flag to assert whether or not it should fire events
 */
public class DefaultLayoutStateChangeSupport implements LayoutModel.LayoutStateChangeSupport {

  private static final Logger log = LoggerFactory.getLogger(DefaultLayoutStateChangeSupport.class);
  /** to fire or not to fire.... */
  protected boolean fireEvents;

  /** listeners for these changes */
  protected List<LayoutModel.LayoutStateChangeListener> changeListeners =
      Collections.synchronizedList(Lists.newArrayList());

  @Override
  public boolean isFireEvents() {
    return fireEvents;
  }

  @Override
  public void setFireEvents(boolean fireEvents) {
    this.fireEvents = fireEvents;
  }

  @Override
  public void addLayoutStateChangeListener(LayoutModel.LayoutStateChangeListener l) {
    changeListeners.add(l);
  }

  @Override
  public void removeLayoutStateChangeListener(LayoutModel.LayoutStateChangeListener l) {
    changeListeners.remove(l);
  }

  @Override
  public List<LayoutModel.LayoutStateChangeListener> getLayoutStateChangeListeners() {
    return changeListeners;
  }

  @Override
  public void fireLayoutStateChanged(LayoutModel layoutModel, boolean state) {
    log.trace("fireLayoutStateChange to {}", state);
    if (changeListeners.size() > 0) {
      // make an event and fire it
      LayoutModel.LayoutStateChangeEvent evt =
          new LayoutModel.LayoutStateChangeEvent(layoutModel, state);
      for (LayoutModel.LayoutStateChangeListener listener : changeListeners) {
        listener.layoutStateChanged(evt);
      }
    } else {
      log.trace("there are no listeners for {}", this);
    }
  }
}
