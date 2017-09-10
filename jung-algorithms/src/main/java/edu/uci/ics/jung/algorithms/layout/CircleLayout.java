/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Dec 4, 2003
 */
package edu.uci.ics.jung.algorithms.layout;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A {@code Layout} implementation that positions nodes equally spaced on a regular circle.
 *
 * @author Masanori Harada
 */
public class CircleLayout<N> extends AbstractLayout<N> {

  private double radius;
  private List<N> node_ordered_list;

  protected LoadingCache<N, CircleNodeData> circleNodeDatas =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, CircleNodeData>() {
                public CircleNodeData load(N node) {
                  return new CircleNodeData();
                }
              });

  public CircleLayout(Graph<N> g) {
    super(g);
  }

  /** @return the radius of the circle. */
  public double getRadius() {
    return radius;
  }

  /**
   * Sets the radius of the circle. Must be called before {@code initialize()} is called.
   *
   * @param radius the radius of the circle
   */
  public void setRadius(double radius) {
    this.radius = radius;
  }

  /**
   * Sets the order of the nodes in the layout according to the ordering specified by {@code
   * comparator}.
   *
   * @param comparator the comparator to use to order the nodes
   */
  public void setNodeOrder(Comparator<N> comparator) {
    if (node_ordered_list == null) {
      node_ordered_list = new ArrayList<N>(nodes());
    }
    Collections.sort(node_ordered_list, comparator);
  }

  /**
   * Sets the order of the nodes in the layout according to the ordering of {@code node_list}.
   *
   * @param node_list a list specifying the ordering of the nodes
   */
  public void setNodeOrder(List<N> node_list) {
    if (!node_list.containsAll(nodes())) {
      throw new IllegalArgumentException("Supplied list must include " + "all nodes of the graph");
    }
    this.node_ordered_list = node_list;
  }

  public void reset() {
    initialize();
  }

  public void initialize() {
    Dimension d = getSize();

    if (d != null) {
      if (node_ordered_list == null) {
        setNodeOrder(new ArrayList<N>(nodes()));
      }

      double height = d.getHeight();
      double width = d.getWidth();

      if (radius <= 0) {
        radius = 0.45 * (height < width ? height : width);
      }

      int i = 0;
      for (N node : node_ordered_list) {
        Point2D coord = apply(node);

        double angle = (2 * Math.PI * i) / node_ordered_list.size();

        coord.setLocation(
            Math.cos(angle) * radius + width / 2, Math.sin(angle) * radius + height / 2);

        CircleNodeData data = getCircleData(node);
        data.setAngle(angle);
        i++;
      }
    }
  }

  protected CircleNodeData getCircleData(N node) {
    return circleNodeDatas.getUnchecked(node);
  }

  protected static class CircleNodeData {
    private double angle;

    protected double getAngle() {
      return angle;
    }

    protected void setAngle(double angle) {
      this.angle = angle;
    }

    @Override
    public String toString() {
      return "CircleNodeData: angle=" + angle;
    }
  }
}
