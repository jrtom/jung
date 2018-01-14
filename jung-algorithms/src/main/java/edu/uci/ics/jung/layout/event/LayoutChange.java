package edu.uci.ics.jung.layout.event;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

/**
 * For the most general change to a LayoutModel. There is no Event payload, only an indication that
 * there was a change. A visualization would consume the event and re-draw itself. Use-cases for
 * firing this event are when the Graph or LayoutAlgorithm is changed in the LayoutModel
 *
 * @author Tom Nelson
 */
public interface LayoutChange {

  /** indicates support for this type of event dispatch */
  interface HasSupport {
    LayoutChange.Support getLayoutChangeSupport();
  }

  /** method signatures to add/remove listeners and fire events */
  interface Support {
    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addLayoutChangeListener(LayoutChange.Listener l);

    void removeLayoutChangeListener(LayoutChange.Listener l);

    List<Listener> getLayoutChangeListeners();

    void fireLayoutChanged();
  }

  /** implementation of support. Manages a List of listeners */
  class SupportImpl implements Support {

    /** to fire or not to fire.... */
    protected boolean fireEvents;

    /** listeners for these changes */
    protected List<Listener> changeListeners = Collections.synchronizedList(Lists.newArrayList());

    @Override
    public boolean isFireEvents() {
      return fireEvents;
    }

    @Override
    public void setFireEvents(boolean fireEvents) {
      this.fireEvents = fireEvents;
      // fire an event in case anything was missed while inactive
      if (fireEvents) {
        fireLayoutChanged();
      }
    }

    @Override
    public void addLayoutChangeListener(LayoutChange.Listener l) {
      changeListeners.add(l);
    }

    @Override
    public void removeLayoutChangeListener(LayoutChange.Listener l) {
      changeListeners.remove(l);
    }

    @Override
    public List<LayoutChange.Listener> getLayoutChangeListeners() {
      return changeListeners;
    }

    @Override
    public void fireLayoutChanged() {
      if (changeListeners.size() > 0) {
        for (Listener layoutChangeListener : changeListeners) {
          layoutChangeListener.layoutChanged();
        }
      }
    }
  }

  /** implemented by a consumer of this type of event */
  interface Listener {
    void layoutChanged();
  }
}
