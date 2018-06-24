package edu.uci.ics.jung.layout.model;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * an Event system for LayoutModels to announce the change in the location of Nodes This replaces
 * the dependency on the javax.swing.ChangeEvent, and is used to alert the visualization system that
 * it should re-draw the visualization
 *
 * @author Tom Nelson
 */
public class DefaultLayoutModelChangeSupport implements LayoutModel.ChangeSupport {

  private static final Logger log = LoggerFactory.getLogger(DefaultLayoutModelChangeSupport.class);

  protected boolean fireEvents = true;

  protected List<LayoutModel.ChangeListener> changeListeners =
      Collections.synchronizedList(Lists.newArrayList());

  @Override
  public boolean isFireEvents() {
    return fireEvents;
  }

  @Override
  public void setFireEvents(boolean fireEvents) {
    log.trace("setFireEvents {}", fireEvents);
    this.fireEvents = fireEvents;
    // any time we turn this back on, fire an event in case
    // anything was missed while it was off
    if (fireEvents) {
      fireChanged();
    }
  }

  @Override
  public void addChangeListener(LayoutModel.ChangeListener l) {
    this.changeListeners.add(l);
  }

  @Override
  public void removeChangeListener(LayoutModel.ChangeListener l) {
    this.changeListeners.remove(l);
  }

  @Override
  public void fireChanged() {

    if (this.fireEvents && !changeListeners.isEmpty()) {
      for (LayoutModel.ChangeListener listener : changeListeners) {
        listener.changed();
      }
    }
  }

  @Override
  public List<LayoutModel.ChangeListener> getChangeListeners() {
    return changeListeners;
  }
}
