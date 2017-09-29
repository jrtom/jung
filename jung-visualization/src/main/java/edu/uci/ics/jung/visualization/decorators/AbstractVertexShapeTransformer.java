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

import edu.uci.ics.jung.visualization.util.VertexShapeFactory;
import java.util.function.Function;

/** @author Joshua O'Madadhain */
public abstract class AbstractVertexShapeTransformer implements SettableVertexShapeTransformer {
  protected Function<Object, Integer> vsf;
  protected Function<Object, Float> varf;
  protected VertexShapeFactory factory;
  public static final int DEFAULT_SIZE = 8;
  public static final float DEFAULT_ASPECT_RATIO = 1.0f;

  public AbstractVertexShapeTransformer(
      Function<Object, Integer> vsf, Function<Object, Float> varf) {
    this.vsf = vsf;
    this.varf = varf;
    factory = new VertexShapeFactory(vsf, varf);
  }

  public AbstractVertexShapeTransformer() {
    this(n -> DEFAULT_SIZE, n -> DEFAULT_ASPECT_RATIO);
  }

  public void setSizeTransformer(Function<Object, Integer> vsf) {
    this.vsf = vsf;
    factory = new VertexShapeFactory(vsf, varf);
  }

  public void setAspectRatioTransformer(Function<Object, Float> varf) {
    this.varf = varf;
    factory = new VertexShapeFactory(vsf, varf);
  }
}
