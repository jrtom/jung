/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on March 10, 2005
 */
package edu.uci.ics.jung.visualization.decorators;

import com.google.common.graph.Network;
import edu.uci.ics.jung.visualization.util.Context;
import java.awt.Shape;
import java.util.function.Function;

/**
 * An interface for decorators that return a <code>Shape</code> for a specified edge.
 *
 * @author Tom Nelson
 */
public abstract class AbstractEdgeShapeFunction<N, E>
    implements Function<Context<Network<N, E>, E>, Shape> {

  /** Specifies how far apart to place the control points for edges being drawn in parallel. */
  protected float control_offset_increment = 20.f;

  /** Sets the value of <code>control_offset_increment</code>. */
  public void setControlOffsetIncrement(float y) {
    control_offset_increment = y;
  }
}
