/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 7, 2003
 *
 */
package edu.uci.ics.jung.algorithms.layout;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Abstract class for implementations of {@code Layout}. It handles some of the basic functions:
 * storing coordinates, maintaining the dimensions, initializing the locations, maintaining locked
 * nodes.
 *
 * @author Danyel Fisher, Scott White
 * @author Tom Nelson - converted to jung2
 * @param <N> the node type
 * @param <E> the edge type
 */
public abstract class AbstractLayout<N> implements Layout<N> {

  /** A set of nodes that are fixed in place and not affected by the layout algorithm */
  private Set<N> dontmove = new HashSet<N>();

  protected Dimension size;
  protected Set<N> nodes;
  protected boolean initialized;

  protected LoadingCache<N, Point2D> locations =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, Point2D>() {
                public Point2D load(N node) {
                  return new Point2D.Double();
                }
              });

  /**
   * Creates an instance for {@code graph} which does not initialize the node locations.
   *
   * @param graph the graph on which the layout algorithm is to operate
   */
  protected AbstractLayout(Graph<N> graph) {
    Preconditions.checkNotNull(graph);
    this.nodes = graph.nodes();
  }

  /**
   * Creates an instance for {@code graph} which initializes the node locations using {@code
   * initializer}.
   *
   * @param graph the graph on which the layout algorithm is to operate
   * @param initializer specifies the starting positions of the nodes
   */
  protected AbstractLayout(Graph<N> graph, Function<N, Point2D> initializer) {
    this.nodes = graph.nodes();
    Function<N, Point2D> chain = initializer.andThen(p -> (Point2D) p.clone());
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
    initialized = true;
  }

  /**
   * Creates an instance for {@code graph} which sets the size of the layout to {@code size}.
   *
   * @param graph the graph on which the layout algorithm is to operate
   * @param size the dimensions of the region in which the layout algorithm will place nodes
   */
  protected AbstractLayout(Graph<N> graph, Dimension size) {
    this.nodes = graph.nodes();
    this.size = size;
  }

  /**
   * Creates an instance for {@code graph} which initializes the node locations using {@code
   * initializer} and sets the size of the layout to {@code size}.
   *
   * @param graph the graph on which the layout algorithm is to operate
   * @param initializer specifies the starting positions of the nodes
   * @param size the dimensions of the region in which the layout algorithm will place nodes
   */
  protected AbstractLayout(Graph<N> graph, Function<N, Point2D> initializer, Dimension size) {
    this.nodes = graph.nodes();
    Function<N, Point2D> chain = initializer.andThen(p -> (Point2D) p.clone());
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
    this.size = size;
  }

  /**
   * When a visualization is resized, it presumably wants to fix the locations of the nodes and
   * possibly to reinitialize its data. The current method calls <tt>initializeLocations</tt>
   * followed by <tt>initialize_local</tt>.
   */
  public void setSize(Dimension size) {
    Preconditions.checkNotNull(size);
    Dimension oldSize = this.size;
    this.size = size;
    initialize();

    if (oldSize != null) {
      adjustLocations(oldSize, size);
    }
  }

  private void adjustLocations(Dimension oldSize, Dimension size) {

    int xOffset = (size.width - oldSize.width) / 2;
    int yOffset = (size.height - oldSize.height) / 2;

    // now, move each node to be at the new screen center
    while (true) {
      try {
        for (N node : nodes) {
          offsetnode(node, xOffset, yOffset);
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
  }

  public boolean isLocked(N node) {
    return dontmove.contains(node);
  }

  public void setInitializer(Function<N, Point2D> initializer) {
    if (this.equals(initializer)) {
      throw new IllegalArgumentException("Layout cannot be initialized with itself");
    }
    Function<N, Point2D> chain = initializer.andThen(p -> (Point2D) p.clone());
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
    initialized = true;
  }

  /**
   * Returns the current size of the visualization space, accoring to the last call to resize().
   *
   * @return the current size of the screen
   */
  public Dimension getSize() {
    return size;
  }

  /**
   * Returns the Coordinates object that stores the node' x and y location.
   *
   * @param v A node that is a part of the Graph being visualized.
   * @return A Coordinates object with x and y locations.
   */
  private Point2D getCoordinates(N node) {
    return locations.getUnchecked(node);
  }

  public Point2D apply(N node) {
    return getCoordinates(node);
  }

  /**
   * Returns the x coordinate of the node from the Coordinates object. in most cases you will be
   * better off calling transform(node).
   *
   * @param v the node whose x coordinate is to be returned
   * @return the x coordinate of {@code node}
   */
  public double getX(N node) {
    Preconditions.checkNotNull(getCoordinates(node), "Cannot getX for an unmapped node %s", node);
    return getCoordinates(node).getX();
  }

  /**
   * Returns the y coordinate of the node from the Coordinates object. In most cases you will be
   * better off calling transform(node).
   *
   * @param v the node whose y coordinate is to be returned
   * @return the y coordinate of {@code node}
   */
  public double getY(N node) {
    Preconditions.checkNotNull(getCoordinates(node), "Cannot getY for an unmapped node %s", node);
    return getCoordinates(node).getY();
  }

  /**
   * @param v the node whose coordinates are to be offset
   * @param xOffset the change to apply to this node's x coordinate
   * @param yOffset the change to apply to this node's y coordinate
   */
  protected void offsetnode(N node, double xOffset, double yOffset) {
    Point2D c = getCoordinates(node);
    c.setLocation(c.getX() + xOffset, c.getY() + yOffset);
    setLocation(node, c);
  }

  /** @return the graph that this layout operates on */
  public Set<N> nodes() {
    return nodes;
  }

  /**
   * Forcibly moves a node to the (x,y) location by setting its x and y locations to the specified
   * location. Does not add the node to the "dontmove" list, and (in the default implementation)
   * does not make any adjustments to the rest of the graph.
   *
   * @param picked the node whose location is being set
   * @param x the x coordinate of the location to set
   * @param y the y coordinate of the location to set
   */
  public void setLocation(N picked, double x, double y) {
    Point2D coord = getCoordinates(picked);
    coord.setLocation(x, y);
  }

  public void setLocation(N picked, Point2D p) {
    Point2D coord = getCoordinates(picked);
    coord.setLocation(p);
  }

  /**
   * Locks {@code node} in place if {@code state} is {@code true}, otherwise unlocks it.
   *
   * @param v the node whose position is to be (un)locked
   * @param state {@code true} if the node is to be locked, {@code false} if to be unlocked
   */
  public void lock(N node, boolean state) {
    if (state == true) {
      dontmove.add(node);
    } else {
      dontmove.remove(node);
    }
  }

  /** @param lock {@code true} to lock all nodes in place, {@code false} to unlock all nodes */
  public void lock(boolean lock) {
    for (N node : nodes) {
      lock(node, lock);
    }
  }
}
