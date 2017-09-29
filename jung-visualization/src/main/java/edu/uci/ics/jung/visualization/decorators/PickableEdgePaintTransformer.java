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
 * Paints each edge according to the <code>Paint</code> parameters given in the constructor, so that
 * picked and non-picked edges can be made to look different.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class PickableEdgePaintTransformer implements Function<Object, Paint> {
  protected PickedInfo pi;
  protected Paint draw_paint;
  protected Paint picked_paint;

  /**
   * @param pi specifies which vertices report as "picked"
   * @param draw_paint <code>Paint</code> used to draw edge shapes
   * @param picked_paint <code>Paint</code> used to draw picked edge shapes
   */
  public PickableEdgePaintTransformer(PickedInfo pi, Paint draw_paint, Paint picked_paint) {
    this.pi = Preconditions.checkNotNull(pi);
    this.draw_paint = Preconditions.checkNotNull(draw_paint);
    this.picked_paint = Preconditions.checkNotNull(picked_paint);
  }

  /** */
  public Paint apply(Object e) {
    if (pi.isPicked(e)) {
      return picked_paint;
    } else {
      return draw_paint;
    }
  }
}
