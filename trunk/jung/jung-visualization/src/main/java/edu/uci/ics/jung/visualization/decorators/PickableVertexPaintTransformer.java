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
package edu.uci.ics.jung.visualization.decorators;

import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * Paints each vertex according to the <code>Paint</code>
 * parameters given in the constructor, so that picked and
 * non-picked vertices can be made to look different.
 */
public class PickableVertexPaintTransformer<V> implements Transformer<V,Paint> {

    protected Paint fill_paint;
    protected Paint picked_paint;
    protected PickedInfo<V> pi;
    
    /**
     * 
     * @param pi            specifies which vertices report as "picked"
     * @param draw_paint    <code>Paint</code> used to draw vertex shapes
     * @param fill_paint    <code>Paint</code> used to fill vertex shapes
     * @param picked_paint  <code>Paint</code> used to fill picked vertex shapes
     */
    public PickableVertexPaintTransformer(PickedInfo<V> pi, 
    		Paint fill_paint, Paint picked_paint)
    {
        if (pi == null)
            throw new IllegalArgumentException("PickedInfo instance must be non-null");
        this.pi = pi;
        this.fill_paint = fill_paint;
        this.picked_paint = picked_paint;
    }

    public Paint transform(V v)
    {
        if (pi.isPicked(v))
            return picked_paint;
        else
            return fill_paint;
    }

}
