/*
 * Created on Mar 10, 2005
 *
 * Copyright (c) 2005, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization3d.decorators;

import edu.uci.ics.jung.visualization.picking.PickedInfo;
import java.awt.Paint;
import java.util.function.Function;

/**
 * Paints each vertex according to the <code>Paint</code> parameters given in the constructor, so
 * that picked and non-picked vertices can be made to look different.
 */
public class PickableVertexPaintTransformer<V> implements Function<V, Paint> {

  //    protected Paint draw_paint;
  protected Paint fill_paint;
  protected Paint picked_paint;
  protected PickedInfo<V> pi;

  /**
   * @param pi specifies which vertices report as "picked"
   * @param fill_paint <code>Paint</code> used to fill vertex shapes
   * @param picked_paint <code>Paint</code> used to fill picked vertex shapes
   */
  public PickableVertexPaintTransformer(
      PickedInfo<V> pi,
      //    		Paint draw_paint,
      Paint fill_paint,
      Paint picked_paint) {
    if (pi == null) throw new IllegalArgumentException("PickedInfo instance must be non-null");
    this.pi = pi;
    //        this.draw_paint = draw_paint;
    this.fill_paint = fill_paint;
    this.picked_paint = picked_paint;
  }

  //    public Paint getDrawPaint(V v)
  //    {
  //        return draw_paint;
  //    }

  public Paint apply(V v) {
    if (pi.isPicked(v)) return picked_paint;
    else return fill_paint;
  }
}
