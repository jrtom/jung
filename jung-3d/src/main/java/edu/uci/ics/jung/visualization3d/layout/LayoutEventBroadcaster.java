/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 23, 2005
 */

package edu.uci.ics.jung.visualization3d.layout;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout3d.Layout;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point3f;

/**
 * A LayoutDecorator the fires ChangeEvents when certain methods are called. Used to wrap a Layout
 * so that the visualization components can be notified of changes.
 *
 * @see LayoutDecorator
 * @author Tom Nelson
 */
public class LayoutEventBroadcaster<N, E> extends LayoutDecorator<N, E>
    implements ChangeEventSupport {

  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);

  public LayoutEventBroadcaster(Layout<N, E> delegate) {
    super(delegate);
  }

  //    /**
  //     * @see edu.uci.ics.jung.algorithms.layout.Layout#step()
  //     */
  public void step() {
    super.step();
    fireStateChanged();
  }

  /** @see edu.uci.ics.jung.algorithms.layout.Layout#initialize() */
  public void initialize() {
    super.initialize();
    fireStateChanged();
  }

  /**
   * @param v
   * @param location
   * @see edu.uci.ics.jung.algorithms.layout.Layout#setLocation(java.lang.Object,
   *     java.awt.geom.Point2D)
   */
  public void setLocation(N v, Point3f location) {
    super.setLocation(v, location);
    fireStateChanged();
  }

  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }

  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
  }

  public ChangeListener[] getChangeListeners() {
    return changeSupport.getChangeListeners();
  }

  public void fireStateChanged() {
    changeSupport.fireStateChanged();
  }

  public void setNetwork(Network<N, E> graph) {
    delegate.setNetwork(graph);
  }

  public Network<N, E> getNetwork() {
    return delegate.getNetwork();
  }
}
