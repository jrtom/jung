/*
 * Created on Jul 16, 2004
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import java.awt.Shape;
import java.util.function.Function;

/** @author Joshua O'Madadhain */
public class EllipseNodeShapeFunction<N> extends AbstractNodeShapeFunction<N>
    implements Function<N, Shape> {
  public EllipseNodeShapeFunction() {}

  public EllipseNodeShapeFunction(Function<N, Integer> vsf, Function<N, Float> varf) {
    super(vsf, varf);
  }

  public Shape apply(N v) {
    return factory.getEllipse(v);
  }
}
