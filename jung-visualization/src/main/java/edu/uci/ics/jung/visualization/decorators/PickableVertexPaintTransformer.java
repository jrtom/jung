/*
 * Created on Mar 10, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import com.google.common.base.Preconditions;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import java.awt.Paint;
import java.util.function.Function;

/**
 * Paints each vertex according to the <code>Paint</code> parameters given in the constructor, so
 * that picked and non-picked vertices can be made to look different.
 */
public class PickableVertexPaintTransformer<V> implements Function<V, Paint> {

  protected Paint fill_paint;
  protected Paint picked_paint;
  protected PickedInfo<V> pi;

  /**
   * @param pi specifies which vertices report as "picked"
   * @param fill_paint <code>Paint</code> used to fill vertex shapes
   * @param picked_paint <code>Paint</code> used to fill picked vertex shapes
   */
  public PickableVertexPaintTransformer(PickedInfo<V> pi, Paint fill_paint, Paint picked_paint) {
    this.pi = Preconditions.checkNotNull(pi);
    this.fill_paint = Preconditions.checkNotNull(fill_paint);
    this.picked_paint = Preconditions.checkNotNull(picked_paint);
  }

  public Paint apply(V v) {
    return pi.isPicked(v) ? picked_paint : fill_paint;
  }
}
