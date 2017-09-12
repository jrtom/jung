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
public abstract class AbstractVertexShapeTransformer<V>
    implements SettableVertexShapeTransformer<V> {
  protected Function<? super V, Integer> vsf;
  protected Function<? super V, Float> varf;
  protected VertexShapeFactory<V> factory;
  public static final int DEFAULT_SIZE = 8;
  public static final float DEFAULT_ASPECT_RATIO = 1.0f;

  public AbstractVertexShapeTransformer(
      Function<? super V, Integer> vsf, Function<? super V, Float> varf) {
    this.vsf = vsf;
    this.varf = varf;
    factory = new VertexShapeFactory<V>(vsf, varf);
  }

  public AbstractVertexShapeTransformer() {
    this(n -> DEFAULT_SIZE, n -> DEFAULT_ASPECT_RATIO);
  }

  public void setSizeTransformer(Function<V, Integer> vsf) {
    this.vsf = vsf;
    factory = new VertexShapeFactory<V>(vsf, varf);
  }

  public void setAspectRatioTransformer(Function<V, Float> varf) {
    this.varf = varf;
    factory = new VertexShapeFactory<V>(vsf, varf);
  }
}
