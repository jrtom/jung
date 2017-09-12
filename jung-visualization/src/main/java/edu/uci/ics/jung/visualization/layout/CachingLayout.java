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
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.visualization.util.Caching;
import java.awt.geom.Point2D;
import java.util.Set;
import java.util.function.Function;

/**
 * A LayoutDecorator that caches locations in a clearable Map. This can be used to ensure that edge
 * endpoints are always the same as vertex locations when they are drawn in the render loop during
 * the time that the layout's relaxer thread is changing the locations.
 *
 * @see LayoutDecorator
 * @author Tom Nelson
 */
public class CachingLayout<V> extends LayoutDecorator<V> implements Caching {

  protected LoadingCache<V, Point2D> locations;

  public CachingLayout(Layout<V> delegate) {
    super(delegate);
    Function<V, Point2D> chain = delegate.andThen(p -> (Point2D) p.clone());
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
  }

  public void clear() {
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(() -> new Point2D.Double()));
  }

  public void init() {}

  public Point2D apply(V v) {
    return locations.getUnchecked(v);
  }

  @Override
  public Set<V> nodes() {
    return delegate.nodes();
  }
}
