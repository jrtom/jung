/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.model;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** two or three dimensional layoutmodel */
public interface LayoutModel<N> extends Function<N, Point> {

  /** @return the width of the layout area */
  int getWidth();

  /** @return the height of the layout area */
  int getHeight();

  /**
   * allow the passed LayoutAlgorithm to operate on this LayoutModel
   *
   * @param layoutAlgorithm the algorithm to apply to this model's Points
   */
  void accept(LayoutAlgorithm<N> layoutAlgorithm);

  /**
   * return a mapping of Nodes to Point locations
   *
   * @return
   */
  Map<N, Point> getLocations();

  /**
   * @param width to set
   * @param helght to set
   */
  void setSize(int width, int helght);

  /** stop a relaxer Thread from continuing to operate */
  void stopRelaxer();

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @param relaxing
   */
  void setRelaxing(boolean relaxing);

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @return relaxing
   */
  boolean isRelaxing();

  /**
   * a handle to the relaxer thread may be used to attach a process to run after relax is complete
   *
   * @return the CompletableFuture
   */
  CompletableFuture getTheFuture();

  /**
   * @param node the node whose locked state is being queried
   * @return <code>true</code> if the position of node <code>v</code> is locked
   */
  boolean isLocked(N node);

  /**
   * Changes the layout coordinates of {@code node} to {@code location}.
   *
   * @param node the node whose location is to be specified
   * @param location the coordinates of the specified location
   */
  void set(N node, Point location);

  /**
   * Changes the layout coordinates of {@code node} to {@code x, y}.
   *
   * @param node
   * @param x
   * @param y
   */
  void set(N node, double x, double y);

  /**
   * @param node the node of interest
   * @return the Point location for node
   */
  Point get(N node);

  /** @return the {@code Graph} that this model is mediating */
  Graph<N> getGraph();

  /** @param graph the {@code Graph} to set */
  void setGraph(Graph<N> graph);

  void lock(N node, boolean locked);

  void lock(boolean locked);

  boolean isLocked();

  void setInitializer(Function<N, Point> initializer);

  interface ChangeListener {
    void changed();
  }

  /**
   * This exists so that LayoutModel will not have dependencies on java awt or swing event classes
   * This event type tells the viewing system that it should re-draw itself to show the latest
   * changes
   */
  interface ChangeSupport {

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addChangeListener(ChangeListener l);

    void removeChangeListener(ChangeListener l);

    void fireChanged();
  }

  /** @return the support for LayoutStateChange events */
  LayoutStateChangeSupport getLayoutStateChangeSupport();

  /** support for LayoutStateChangeEvents and their Listeners. */
  interface LayoutStateChangeSupport {
    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addLayoutStateChangeListener(LayoutStateChangeListener l);

    void removeLayoutStateChangeListener(LayoutStateChangeListener l);

    void fireLayoutStateChanged(LayoutModel source, boolean state);
  }

  /**
   * This event type alerts listeners whether the LayoutModel is active or not. When the layout
   * model is 'active', during the relax phase, a listener can choose not to update unit the layout
   * model is inactive. The Spatial Data structures on the view side would waste a lot of competing
   * compute cycles staying up to date with a changing layout model.
   */
  class LayoutStateChangeEvent {
    public final LayoutModel layoutModel;
    public final boolean active;

    public LayoutStateChangeEvent(LayoutModel layoutModel, boolean active) {
      this.layoutModel = layoutModel;
      this.active = active;
    }

    @Override
    public String toString() {
      return "LayoutStateChangeEvent{" + "layoutModel=" + layoutModel + ", active=" + active + '}';
    }
  }

  /**
   * a consumer for a LayoutStateChangeEvent most use-cases in JUNG are the view side spatial data
   * structures
   */
  interface LayoutStateChangeListener {
    void layoutStateChanged(LayoutStateChangeEvent evt);
  }
}
