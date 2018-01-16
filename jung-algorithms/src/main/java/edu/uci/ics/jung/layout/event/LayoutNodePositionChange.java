package edu.uci.ics.jung.layout.event;

import com.google.common.collect.Lists;
import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.model.Point;
import java.util.Collections;
import java.util.List;

/**
 * Event support to indicate that a Node's position has changed. The jung-visualization spatial data
 * structures will consume this event and re-insert the node or edge.
 *
 * @author Tom Nelson
 */
public interface LayoutNodePositionChange {

  /**
   * indicates support for this event model
   *
   * @param <N>
   */
  interface Producer<N> {
    Support<N> getLayoutNodePositionSupport();
  }

  /**
   * method signatures required for producers of this event model
   *
   * @param <N>
   */
  interface Support<N> {
    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addLayoutNodePositionChangeListener(LayoutNodePositionChange.Listener<N> l);

    void removeLayoutNodePositionChangeListener(LayoutNodePositionChange.Listener<N> l);

    List<LayoutNodePositionChange.Listener<N>> getLayoutNodePositionChangeListeners();

    void fireLayoutNodePositionChanged(N node, Point location);
  }

  /**
   * implementations of support for this event model
   *
   * @param <N> the node type managed by the LayoutModel
   */
  class SupportImpl<N> implements Support<N> {

    /** to fire or not to fire.... */
    protected boolean fireEvents;

    /** listeners for these changes */
    protected List<Listener<N>> changeListeners =
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
    public void addLayoutNodePositionChangeListener(LayoutNodePositionChange.Listener l) {
      changeListeners.add(l);
    }

    @Override
    public void removeLayoutNodePositionChangeListener(LayoutNodePositionChange.Listener l) {
      changeListeners.remove(l);
    }

    @Override
    public List<LayoutNodePositionChange.Listener<N>> getLayoutNodePositionChangeListeners() {
      return changeListeners;
    }

    @Override
    public void fireLayoutNodePositionChanged(N node, Point location) {
      if (changeListeners.size() > 0) {
        Event<N> layoutEvent = new Event(node, location);
        for (Listener layoutChangeListener : changeListeners) {
          layoutChangeListener.layoutNodePositionChanged(layoutEvent);
        }
      }
    }
  }

  /**
   * Event payload. Contains the Node and its location
   *
   * @param <N>
   */
  class Event<N> {
    public final N node;
    public final Point location;

    public Event(N node, Point location) {
      this.node = node;
      this.location = location;
    }
  }

  class NetworkEvent<N> extends Event<N> {
    final Network<N, ?> network;

    public NetworkEvent(N node, Network<N, ?> network, Point location) {
      super(node, location);
      this.network = network;
    }

    public NetworkEvent(Event<N> layoutEvent, Network<N, ?> network) {
      super(layoutEvent.node, layoutEvent.location);
      this.network = network;
    }

    public Network<N, ?> getNetwork() {
      return this.network;
    }
  }

  /**
   * implemented by consumers for this event model
   *
   * @param <N>
   */
  interface Listener<N> {
    void layoutNodePositionChanged(Event<N> evt);

    void layoutNodePositionChanged(NetworkEvent<N> evt);
  }
}
