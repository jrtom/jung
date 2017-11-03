/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jul 7, 2003
 *
 */
package edu.uci.ics.jung.algorithms.layout3d;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Network;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3f;

/**
 * Implements some of the dirty work of writing a layout algorithm, allowing the user to express
 * their major intent more simply. When writing a <tt>Layout</tt>, there are many shared tasks:
 * handling tracking locked nodes, applying filters, and tracing nearby vertices. This package
 * automates all of those.
 *
 * @author Danyel Fisher, Scott White
 * @param <N>
 */
public abstract class AbstractLayout<N, E> implements Layout<N, E> {

  /** a set of vertices that should not move in relation to the other vertices */
  private Set<N> dontmove = new HashSet<>();

  private BoundingSphere size;
  //	protected Set<N> nodes;
  protected boolean initialized;
  protected Network<N, E> network;

  protected LoadingCache<N, Point3f> locations =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> new Point3f()));

  /**
   * Constructor. Initializes the current layoutSize to be 100x100, both the graph and the showing
   * graph to the argument, and creates the <tt>dontmove</tt> set.
   */
  protected AbstractLayout(Network<N, E> network) {
    Preconditions.checkNotNull(network);
    this.network = network;
  }

  protected AbstractLayout(Network<N, E> network, Function<N, Point3f> initializer) {
    this.network = network;
    Function<N, Point3f> chain = initializer.andThen(p -> (Point3f) p.clone());
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
    initialized = true;
  }

  protected AbstractLayout(Network<N, E> network, BoundingSphere size) {
    this.network = network;
    this.size = size;
  }

  protected AbstractLayout(
      Network<N, E> network, Function<N, Point3f> initializer, BoundingSphere size) {
    this.network = network;
    Function<N, Point3f> chain = initializer.andThen(p -> (Point3f) p.clone());
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
    this.size = size;
  }

  public void setNetwork(Network<N, E> network) {
    this.network = network;
    if (size != null && network != null) {
      initialize();
    }
  }

  public Network<N, E> getNetwork() {
    return this.network;
  }

  /**
   * When a visualization is resized, it presumably wants to fix the locations of the vertices and
   * possibly to reinitialize its data. The current method calls <tt>initializeLocations</tt>
   * followed by <tt>initialize_local</tt>. TODO: A better implementation wouldn't destroy the
   * current information, but would either scale the current visualization, or move the nodes toward
   * the new center.
   */
  public void setSize(BoundingSphere size) {
    Preconditions.checkNotNull(size);

    BoundingSphere oldSize = this.size;
    this.size = size;
    initialize();

    if (oldSize != null) {
      adjustLocations(oldSize, size);
    }
  }

  private void adjustLocations(BoundingSphere oldSize, BoundingSphere size) {

    float oldWidth = 0;
    float oldHeight = 0;
    float oldDepth = 0;
    float width = 0;
    float height = 0;
    float depth = 0;

    oldWidth = oldHeight = oldDepth = (float) (2 * oldSize.getRadius());
    width = height = depth = (float) (2 * size.getRadius());

    float xOffset = (oldWidth - width) / 2;
    float yOffset = (oldHeight - height) / 2;
    float zOffset = (oldDepth - depth) / 2;

    // now, move each vertex to be at the new screen center
    while (true) {
      try {
        for (N node : nodes()) {
          offsetVertex(node, xOffset, yOffset, zOffset);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
  }

  public boolean isLocked(N v) {
    return dontmove.contains(v);
  }

  public Collection<N> nodes() {
    return this.network.nodes();
  }

  /**
   * Initializer, calls <tt>intialize_local</tt> and <tt>initializeLocations</tt> to start
   * construction process.
   */
  public abstract void initialize();

  public void setInitializer(Function<N, Point3f> initializer) {
    Preconditions.checkArgument(
        !this.equals(initializer), "Layout cannot be initialized with itself");
    Function<N, Point3f> chain = initializer.andThen(p -> (Point3f) p.clone());
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
    initialized = true;
  }

  /**
   * Returns the current layoutSize of the visualization space, accoring to the last call to
   * resize().
   *
   * @return the current layoutSize of the screen
   */
  public BoundingSphere getSize() {
    return size;
  }

  /**
   * Returns the Coordinates object that stores the vertex' x and y location.
   *
   * @param v A Vertex that is a part of the Graph being visualized.
   * @return A Coordinates object with x and y locations.
   */
  private Point3f getCoordinates(N v) {
    return locations.getUnchecked(v);
  }

  public Point3f apply(N v) {
    return getCoordinates(v);
  }

  /**
   * Returns the x coordinate of the vertex from the Coordinates object. in most cases you will be
   * better off calling getLocation(Vertex v);
   */
  public double getX(N v) {
    assert getCoordinates(v) != null : "Cannot getX for an unmapped vertex " + v;
    return getCoordinates(v).x;
  }

  /**
   * Returns the y coordinate of the vertex from the Coordinates object. In most cases you will be
   * better off calling getLocation(Vertex v)
   */
  public double getY(N v) {
    assert getCoordinates(v) != null : "Cannot getY for an unmapped vertex " + v;
    return getCoordinates(v).y;
  }

  /**
   * @param v
   * @param xOffset
   * @param yOffset
   */
  protected void offsetVertex(N v, float xOffset, float yOffset, float zOffset) {
    Point3f c = getCoordinates(v);
    c.set(c.x + xOffset, c.y + yOffset, c.z + zOffset);
    setLocation(v, c);
  }

  /**
   * Forcibly moves a vertex to the (x,y) location by setting its x and y locations to the inputted
   * location. Does not add the vertex to the "dontmove" list, and (in the default implementation)
   * does not make any adjustments to the rest of the graph.
   */
  public void setLocation(N picked, float x, float y, float z) {
    Point3f coord = getCoordinates(picked);
    coord.set(x, y, z);
  }

  public void setLocation(N picked, Point3f p) {
    Point3f coord = getCoordinates(picked);
    coord.set(p);
  }

  /** Adds the vertex to the DontMove list */
  public void lock(N v, boolean state) {
    if (state == true) dontmove.add(v);
    else dontmove.remove(v);
  }

  public void lock(boolean lock) {
    for (N v : nodes()) {
      lock(v, lock);
    }
  }
}
