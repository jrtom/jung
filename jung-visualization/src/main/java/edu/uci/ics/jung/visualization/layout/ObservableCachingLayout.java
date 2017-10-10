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
import com.google.common.collect.Multimap;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialGrid;
import edu.uci.ics.jung.visualization.util.Caching;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutDecorator that fires ChangeEvents when certain methods are called. Used to wrap a Layout
 * so that the visualization components can be notified of changes.
 *
 * @see LayoutDecorator
 * @author Tom Nelson
 */
public class ObservableCachingLayout<V, E> extends LayoutDecorator<V>
    implements ChangeEventSupport, Caching, LayoutEventSupport<V, E> {

  private static Logger log = LoggerFactory.getLogger(ObservableCachingLayout.class);
  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);
  protected Network<V, E> graph;
  protected Spatial<V> spatial;

  protected LoadingCache<V, Point2D> locations;

  private List<LayoutChangeListener<V, E>> layoutChangeListeners =
      new ArrayList<LayoutChangeListener<V, E>>();

  public ObservableCachingLayout(Network<V, E> graph, Layout<V> delegate) {
    super(delegate);
    this.graph = graph;
    Function<V, Point2D> chain = delegate.andThen(p -> (Point2D) p.clone());
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
    if (delegate.getSize() != null) {
      setupSpatialGrid(delegate.getSize());
    }
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
    if (!spatial.getLayoutArea().contains(location)) {
      spatial.setBounds(getUnion(spatial.getLayoutArea(), location));
      Multimap<Integer, V> spatialMap = spatial.getMap();
      spatial.getMap().clear();
      for (V node : graph.nodes()) {
        spatialMap.put(spatial.getBoxNumberFromLocation(this.apply(node)), node);
      }
      if (log.isDebugEnabled()) {
        log.debug("now the updated map is " + spatialMap);
      }
    }
  }

  protected void setupSpatialGrid(Dimension delegateSize) {
    this.spatial =
        new SpatialGrid(new Rectangle(0, 0, delegateSize.width, delegateSize.height), 10, 10);
    spatial.recalculate(this.delegate, graph.nodes());
  }

  @Override
  public void setSize(Dimension d) {
    super.setSize(d);
    setupSpatialGrid(delegate.getSize());
  }

  private Rectangle2D getUnion(Rectangle2D rect, Point2D p) {
    double left = Math.min(p.getX(), rect.getX());
    double top = Math.min(p.getY(), rect.getY());
    double right = Math.max(p.getX(), rect.getX() + rect.getWidth());
    double bottom = Math.max(p.getY(), rect.getY() + rect.getHeight());

    return new Rectangle2D.Double(left, top, right - left, bottom - top);
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
    if (spatial != null) spatial.recalculate(this.delegate, nodes());
    changeSupport.fireStateChanged();
  }

  public void clear() {
    this.locations.invalidateAll();
  }

  public Point2D apply(V v) {
    return locations.getUnchecked(v);
  }

  private void fireLayoutChanged(V v) {
    if (!layoutChangeListeners.isEmpty()) {
      LayoutEvent<V, E> evt = new LayoutEvent<V, E>(v, graph);
      for (LayoutChangeListener<V, E> listener : layoutChangeListeners) {
        listener.layoutChanged(evt);
      }
    }
  }

  public void addLayoutChangeListener(LayoutChangeListener<V, E> listener) {
    layoutChangeListeners.add(listener);
  }

  public void removeLayoutChangeListener(LayoutChangeListener<V, E> listener) {
    layoutChangeListeners.remove(listener);
  }

  @Override
  public Set<V> nodes() {
    return graph.nodes();
  }

  public String toString() {
    return "Observable version of " + this.delegate.toString();
  }

  public Spatial getSpatial() {
    return this.spatial;
  }
}
