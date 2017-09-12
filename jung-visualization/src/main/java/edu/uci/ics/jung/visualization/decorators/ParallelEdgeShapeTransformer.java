/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on March 10, 2005
 */
package edu.uci.ics.jung.visualization.decorators;

import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import java.awt.Shape;
import java.util.function.Function;

/**
 * An abstract class for edge-to-Shape functions that work with parallel edges.
 *
 * @author Tom Nelson
 */
public abstract class ParallelEdgeShapeTransformer<E> implements Function<E, Shape> {
  /** Specifies the distance between control points for edges being drawn in parallel. */
  protected float control_offset_increment = 20.f;

  protected EdgeIndexFunction<E> edgeIndexFunction;

  public void setControlOffsetIncrement(float y) {
    control_offset_increment = y;
  }

  public void setEdgeIndexFunction(EdgeIndexFunction<E> edgeIndexFunction) {
    this.edgeIndexFunction = edgeIndexFunction;
  }

  public EdgeIndexFunction<E> getEdgeIndexFunction() {
    return edgeIndexFunction;
  }
}
