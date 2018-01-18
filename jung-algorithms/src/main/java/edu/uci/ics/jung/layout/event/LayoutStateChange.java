package edu.uci.ics.jung.layout.event;

import com.google.common.collect.Lists;
import edu.uci.ics.jung.layout.model.LayoutModel;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event model to convey that the LayoutModel is either active (busy) or not. Consumers of this
 * event can modify their behavior based on the state of the LayoutModel. A use-case for a consumer
 * of this event is the spatial data structures. When the LayoutModel is active, the spatial data
 * structures do not constantly rebuild and compete with the LayoutAlgorithm relax Thread by doing
 * unnecessary work. When the relax thread completes, this event will alert the spatial data
 * structures to rebuild themselves.
 */
public interface LayoutStateChange {

  /** indicates that an implementor supports being a producer for this event model */
  interface Producer {
    Support getLayoutStateChangeSupport();
  }

  /** required method signatures to be a producer for this event model */
  interface Support {

    static Support create() {
      return new SupportImpl();
    }

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addLayoutStateChangeListener(LayoutStateChange.Listener listener);

    void removeLayoutStateChangeListener(LayoutStateChange.Listener listener);

    List<LayoutStateChange.Listener> getLayoutStateChangeListeners();

    void fireLayoutStateChanged(LayoutModel layoutModel, boolean state);
  }

  /** implementations for a producer of this event model */
  class SupportImpl implements Support {

    private static final Logger log = LoggerFactory.getLogger(LayoutStateChange.SupportImpl.class);

    private SupportImpl() {}

    /** to fire or not to fire.... */
    protected boolean fireEvents;

    /** listeners for these changes */
    protected List<LayoutStateChange.Listener> changeListeners =
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
    public void addLayoutStateChangeListener(LayoutStateChange.Listener l) {
      changeListeners.add(l);
    }

    @Override
    public void removeLayoutStateChangeListener(LayoutStateChange.Listener l) {
      changeListeners.remove(l);
    }

    @Override
    public List<LayoutStateChange.Listener> getLayoutStateChangeListeners() {
      return changeListeners;
    }

    @Override
    public void fireLayoutStateChanged(LayoutModel layoutModel, boolean state) {
      if (changeListeners.size() > 0) {
        // make an event and fire it
        LayoutStateChange.Event evt = new LayoutStateChange.Event(layoutModel, state);
        for (LayoutStateChange.Listener listener : changeListeners) {
          log.trace("telling {} that state is {}", listener.getClass(), state);
          listener.layoutStateChanged(evt);
        }
      }
    }
  }

  /**
   * the event payload produced by this event model and consumed by its Listener consumers. Contains
   * a reference to the LayoutModel and a boolean flag indicating whether the LayoutModel is
   * currently active or not. The LayoutModel is considered active when a relaxer thread is applying
   * a LayoutAlgorithm to change Node positions
   */
  class Event {
    public final LayoutModel layoutModel;
    public final boolean active;

    public Event(LayoutModel layoutModel, boolean active) {
      this.layoutModel = layoutModel;
      this.active = active;
    }

    @Override
    public String toString() {
      return "LayoutStateChange.Event{" + "layoutModel=" + layoutModel + ", active=" + active + '}';
    }
  }

  /** interface required for consumers of this event model */
  interface Listener {
    void layoutStateChanged(Event evt);
  }
}
