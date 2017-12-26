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
import java.util.function.Function;

/** two or three dimensional layoutmodel */
public interface LayoutModel<N, P> extends Function<N, P> {

  int getWidth();

  int getHeight();

  int getDepth();

  void accept(LayoutAlgorithm<N, P> layoutAlgorithm);

  PointModel<P> getPointModel();

  void setSize(int width, int helght);

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
  void set(N node, P location);

  void set(N node, double x, double y);

  void set(N node, double x, double y, double z);

  P get(N node);

  Graph<N> getGraph();

  void setGraph(Graph<N> graph);

  void lock(N node, boolean locked);

  void lock(boolean locked);

  boolean isLocked();

  void setInitializer(Function<N, P> initializer);

  interface ChangeListener {
    void changed();
  }

  /**
   * This exists so that LayoutModel will not have dependencies on java awt or swing event classes
   *
   * @return
   */
  LayoutStateChangeSupport getLayoutStateChangeSupport();

  interface LayoutStateChangeSupport {
    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addLayoutStateChangeListener(LayoutStateChangeListener l);

    void removeLayoutStateChangeListener(LayoutStateChangeListener l);

    void fireLayoutStateChanged(LayoutModel source, boolean state);
  }

  /**
   * This exists so that LayoutModel will not have dependencies on java awt or swing event classes
   */
  interface ChangeSupport {

    boolean isFireEvents();

    void setFireEvents(boolean fireEvents);

    void addChangeListener(ChangeListener l);

    void removeChangeListener(ChangeListener l);

    void fireChanged();
  }

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

  interface LayoutStateChangeListener {
    void layoutStateChanged(LayoutStateChangeEvent evt);
  }
}
