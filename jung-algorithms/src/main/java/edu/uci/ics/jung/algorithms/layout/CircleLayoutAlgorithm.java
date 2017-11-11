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

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that positions nodes equally spaced on a regular circle.
 *
 * @author Masanori Harada
 * @author Tom Nelson - adapted to an algorithm
 */
public class CircleLayoutAlgorithm<N, P> extends AbstractLayoutAlgorithm<N, P> {

  private static final Logger log = LoggerFactory.getLogger(CircleLayoutAlgorithm.class);
  private double radius;
  private List<N> node_ordered_list;

  public CircleLayoutAlgorithm(DomainModel<P> domainModel) {
    super(domainModel);
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
  public void setNodeOrder(LayoutModel<N, P> layoutModel, Comparator<N> comparator) {
    if (node_ordered_list == null) {
      node_ordered_list = new ArrayList<N>(layoutModel.getGraph().nodes());
    }
    Collections.sort(node_ordered_list, comparator);
  }

  /**
   * Sets the order of the nodes in the layout according to the ordering of {@code node_list}.
   *
   * @param node_list a list specifying the ordering of the nodes
   */
  public void setNodeOrder(LayoutModel<N, P> layoutModel, List<N> node_list) {
    Preconditions.checkArgument(
        node_list.containsAll(layoutModel.getGraph().nodes()),
        "Supplied list must include all nodes of the graph");
    this.node_ordered_list = node_list;
  }

  public void reset() {}

  public void visit(LayoutModel<N, P> layoutModel) {

    log.trace("visiting " + layoutModel);
    if (layoutModel != null) {
      setNodeOrder(layoutModel, new ArrayList<N>(layoutModel.getGraph().nodes()));

      double height = layoutModel.getHeight();
      double width = layoutModel.getWidth();

      if (radius <= 0) {
        radius = 0.45 * (height < width ? height : width);
      }

      int i = 0;
      for (N node : node_ordered_list) {
        P coord = layoutModel.apply(node);

        double angle = (2 * Math.PI * i) / node_ordered_list.size();

        P location =
            domainModel.newPoint(
                Math.cos(angle) * radius + width / 2, Math.sin(angle) * radius + height / 2);
        layoutModel.set(node, location);
        log.trace("set " + node + " to " + location);

        i++;
      }
    }
  }
}
