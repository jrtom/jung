/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 * Created on Apr 12, 2005
 */
package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import java.awt.*;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple implementation of PickSupport that returns the node or edge that is closest to the
 * specified location. This implementation provides the same picking options that were available in
 * previous versions of
 *
 * <p>No element will be returned that is farther away than the specified maximum distance.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class RadiusNetworkElementAccessor<N, E, P> extends RadiusNetworkNodeAccessor<N, P>
    implements NetworkElementAccessor<N, E> {
  private final Network<N, E> network;

  /** Creates an instance with an effectively infinite default maximum distance. */
  public RadiusNetworkElementAccessor(
      Network<N, E> network, LayoutModel<N, P> layoutModel, DomainModel<P> domainModel) {
    this(network, layoutModel, domainModel, Math.sqrt(Double.MAX_VALUE - 1000));
  }

  /**
   * Creates an instance with the specified default maximum distance.
   *
   * @param maxDistance the maximum distance at which any element can be from a specified location
   *     and still be returned
   */
  public RadiusNetworkElementAccessor(
      Network<N, E> network,
      LayoutModel<N, P> layoutModel,
      DomainModel<P> domainModel,
      double maxDistance) {
    super(network.asGraph(), layoutModel, domainModel, maxDistance);
    this.network = network;
  }

  /**
   * Gets the edge nearest to the location of the (x,y) location selected, whose endpoints are &lt;
   * {@code maxDistance}. Iterates through all visible nodes and checks their distance from the
   * location. Override this method to provide a more efficient implementation.
   *
   * <p>// * @param layout the context in which the location is defined
   *
   * @param x the x coordinate of the location
   * @param y the y coordinate of the location // * @param maxDistance the maximum distance at which
   *     any element can be from a specified location and still be returned
   * @return an edge which is associated with the location {@code (x,y)} as given by {@code layout}
   */
  @Override
  public E getEdge(double x, double y) {
    double minDistance = maxDistance * maxDistance;
    E closest = null;
    while (true) {
      try {
        for (E edge : network.edges()) {
          EndpointPair<N> endpoints = network.incidentNodes(edge);
          N node1 = endpoints.nodeU();
          N node2 = endpoints.nodeV();
          // Get coords
          P p1 = layoutModel.apply(node1);
          P p2 = layoutModel.apply(node2);
          double x1 = domainModel.getX(p1);
          double y1 = domainModel.getY(p1);
          double x2 = domainModel.getX(p2);
          double y2 = domainModel.getY(p2);
          // Calculate location on line closest to (x,y)
          // First, check that v1 and v2 are not coincident.
          if (x1 == x2 && y1 == y2) {
            continue;
          }
          double b =
              ((y - y1) * (y2 - y1) + (x - x1) * (x2 - x1))
                  / ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
          //
          double distance2; // square of the distance
          if (b <= 0) {
            distance2 = (x - x1) * (x - x1) + (y - y1) * (y - y1);
          } else if (b >= 1) {
            distance2 = (x - x2) * (x - x2) + (y - y2) * (y - y2);
          } else {
            double x3 = x1 + b * (x2 - x1);
            double y3 = y1 + b * (y2 - y1);
            distance2 = (x - x3) * (x - x3) + (y - y3) * (y - y3);
          }

          if (distance2 < minDistance) {
            minDistance = distance2;
            closest = edge;
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }

  public Set<N> getNodes(Shape rectangle) {
    Set<N> pickednodes = new HashSet<N>();
    while (true) {
      try {
        for (N node : graph.nodes()) {
          P p = layoutModel.apply(node);
          if (rectangle.contains(domainModel.getX(p), domainModel.getY(p))) {
            pickednodes.add(node);
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return pickednodes;
  }
}
