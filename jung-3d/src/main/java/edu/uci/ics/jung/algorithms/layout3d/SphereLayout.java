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
package edu.uci.ics.jung.algorithms.layout3d;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Network;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3f;

/**
 * A {@code Layout} implementation that positions nodes equally spaced on a regular circle.
 *
 * @author Masanori Harada
 */
public class SphereLayout<N, E> extends AbstractLayout<N, E> {

  private double radius;
  private List<N> node_ordered_list;

  protected LoadingCache<N, Point3f> circleNodeDatas =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<N, Point3f>() {
                public Point3f load(N node) {
                  return new Point3f();
                }
              });

  public SphereLayout(Network<N, E> g) {
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
    Preconditions.checkArgument(
        node_list.containsAll(nodes()), "Supplied list must include all nodes of the graph");
    this.node_ordered_list = node_list;
  }

  public void reset() {
    initialize();
  }

  public void initialize() {
    BoundingSphere d = getSize();

    if (d != null) {
      if (node_ordered_list == null) {
        setNodeOrder(new ArrayList<N>(nodes()));
      }

      double height = d.getRadius() * 2;
      double width = d.getRadius() * 2;
      double depth = d.getRadius() * 2;

      if (radius <= 0) {
        radius = 0.45 * (height < width ? height : width);
      }

      int i = 0;
      double offset = 2.0 / nodes().size();
      int count = nodes().size();
      double rnd = 1.0;
      double increment = Math.PI * (3. - Math.sqrt(5.));
      for (N node : node_ordered_list) {

        double y = ((i * offset) - 1) + (offset / 2);
        double r = Math.sqrt(1 - Math.pow(y, 2));

        double phi = ((i + rnd) % count) * increment;

        double x = Math.cos(phi) * r;
        double z = Math.sin(phi) * r;

        x *= radius;
        y *= radius;
        z *= radius;
        Point3f coord = apply(node);
        coord.set((float) x, (float) y, (float) z);
        locations.put(node, coord);
        i++;
      }
    }
  }

  protected Point3f getCircleData(N node) {
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
