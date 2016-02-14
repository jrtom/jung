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

import javax.swing.Icon;

import com.google.common.base.Function;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * Supplies an Icon for each vertex according to the <code>Icon</code>
 * parameters given in the constructor, so that picked and
 * non-picked vertices can be made to look different.
 */
public class PickableVertexIconTransformer<V> implements Function<V,Icon> {

    protected Icon icon;
    protected Icon picked_icon;
    protected PickedInfo<V> pi;
    
    /**
     * 
     * @param pi            specifies which vertices report as "picked"
     * @param icon    <code>Icon</code> used to represent vertices
     * @param picked_icon  <code>Icon</code> used to represent picked vertices
     */
    public PickableVertexIconTransformer(PickedInfo<V> pi, Icon icon, Icon picked_icon)
    {
        if (pi == null)
            throw new IllegalArgumentException("PickedInfo instance must be non-null");
        this.pi = pi;
        this.icon = icon;
        this.picked_icon = picked_icon;
    }

    /**
     * Returns the appropriate <code>Icon</code>, depending on picked state.
     */
	public Icon apply(V v) {
        if (pi.isPicked(v))
            return picked_icon;
        else
            return icon;
	}
}
