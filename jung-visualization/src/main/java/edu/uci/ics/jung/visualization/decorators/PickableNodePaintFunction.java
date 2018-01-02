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
 * Paints each node according to the <code>Paint</code> parameters given in the constructor, so that
 * picked and non-picked nodes can be made to look different.
 */
public class PickableNodePaintFunction<N> implements Function<N, Paint> {

  protected Paint fill_paint;
  protected Paint picked_paint;
  protected PickedInfo<N> pi;

  /**
   * @param pi specifies which nodes report as "picked"
   * @param fill_paint <code>Paint</code> used to fill node shapes
   * @param picked_paint <code>Paint</code> used to fill picked node shapes
   */
  public PickableNodePaintFunction(PickedInfo<N> pi, Paint fill_paint, Paint picked_paint) {
    this.pi = Preconditions.checkNotNull(pi);
    this.fill_paint = Preconditions.checkNotNull(fill_paint);
    this.picked_paint = Preconditions.checkNotNull(picked_paint);
  }

  public Paint apply(N v) {
    return pi.isPicked(v) ? picked_paint : fill_paint;
  }
}
