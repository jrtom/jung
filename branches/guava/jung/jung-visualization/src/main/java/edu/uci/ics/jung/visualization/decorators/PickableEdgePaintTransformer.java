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
 * Paints each edge according to the <code>Paint</code>
 * parameters given in the constructor, so that picked and
 * non-picked edges can be made to look different.
 * 
 * @author Tom Nelson 
 * @author Joshua O'Madadhain
 * 
 */
public class PickableEdgePaintTransformer<E> implements Transformer<E,Paint> {
    protected PickedInfo<E> pi;
    protected Paint draw_paint;
    protected Paint picked_paint;

    /**
     * 
     * @param pi            specifies which vertices report as "picked"
     * @param draw_paint    <code>Paint</code> used to draw edge shapes
     * @param picked_paint  <code>Paint</code> used to draw picked edge shapes
     */
    public PickableEdgePaintTransformer(PickedInfo<E> pi, Paint draw_paint, Paint picked_paint) {
        if (pi == null)
            throw new IllegalArgumentException("PickedInfo instance must be non-null");
        this.pi = pi;
        this.draw_paint = draw_paint;
        this.picked_paint = picked_paint;
    }
    
    /**
     * 
     */
    public Paint transform(E e) {
        if (pi.isPicked(e)) {
            return picked_paint;
        }
        else {
            return draw_paint;
        }
    }
}
