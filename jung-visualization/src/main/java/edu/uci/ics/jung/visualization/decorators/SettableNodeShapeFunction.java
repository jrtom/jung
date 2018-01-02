/*
 * Created on Jul 18, 2004
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
public interface SettableNodeShapeFunction<N> extends Function<N, Shape> {
  public abstract void setSizeTransformer(Function<N, Integer> vsf);

  public abstract void setAspectRatioTransformer(Function<N, Float> varf);
}
