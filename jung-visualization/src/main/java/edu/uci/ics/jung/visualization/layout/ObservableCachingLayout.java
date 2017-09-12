/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */

package edu.uci.ics.jung.visualization.layout;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.util.Caching;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.swing.event.ChangeListener;

/**
 * A LayoutDecorator that fires ChangeEvents when certain methods are called. Used to wrap a Layout
 * so that the visualization components can be notified of changes.
 *
 * @see LayoutDecorator
 * @author Tom Nelson
 */
public class ObservableCachingLayout<V> extends LayoutDecorator<V>
    implements ChangeEventSupport, Caching, LayoutEventSupport<V> {

  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);
  protected Graph<V> graph;

  protected LoadingCache<V, Point2D> locations;

  private List<LayoutChangeListener<V>> layoutChangeListeners =
      new ArrayList<LayoutChangeListener<V>>();

  public ObservableCachingLayout(Graph<V> graph, Layout<V> delegate) {
    super(delegate);
    this.graph = graph;
    Function<V, Point2D> chain = delegate.andThen(p -> (Point2D) p.clone());
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
  }

  @Override
  public void step() {
    super.step();
    fireStateChanged();
  }

  @Override
  public void initialize() {
    super.initialize();
    fireStateChanged();
  }

  @Override
  public boolean done() {
    if (delegate instanceof IterativeContext) {
      return ((IterativeContext) delegate).done();
    }
    return true;
  }

  @Override
  public void setLocation(V v, Point2D location) {
    super.setLocation(v, location);
    fireStateChanged();
    fireLayoutChanged(v);
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

  public void clear() {
    this.locations.invalidateAll();
  }

  public Point2D apply(V v) {
    return locations.getUnchecked(v);
  }

  private void fireLayoutChanged(V v) {
    LayoutEvent<V> evt = new LayoutEvent<V>(v, graph);
    for (LayoutChangeListener<V> listener : layoutChangeListeners) {
      listener.layoutChanged(evt);
    }
  }

  public void addLayoutChangeListener(LayoutChangeListener<V> listener) {
    layoutChangeListeners.add(listener);
  }

  public void removeLayoutChangeListener(LayoutChangeListener<V> listener) {
    layoutChangeListeners.remove(listener);
  }

  @Override
  public Set<V> nodes() {
    return graph.nodes();
  }
}
