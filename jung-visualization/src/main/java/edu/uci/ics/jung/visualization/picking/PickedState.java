/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 *
 * Created on Apr 2, 2005
 */
package edu.uci.ics.jung.visualization.picking;

import java.awt.ItemSelectable;
import java.util.Set;

/**
 * An interface for classes that keep track of the "picked" state
 * of edges or vertices.
 * 
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public interface PickedState<T> extends PickedInfo<T>, ItemSelectable {
    /**
     * Marks <code>v</code> as "picked" if <code>b == true</code>,
     * and unmarks <code>v</code> as picked if <code>b == false</code>.
     * @param v the element to be picked/unpicked
     * @param b true if {@code v} is to be marked as picked, false if to be marked as unpicked
     * @return the "picked" state of <code>v</code> prior to this call
     */
    boolean pick(T v, boolean b);
    
    /**
     * Clears the "picked" state from all elements.
     */
    void clear();
    
    /**
     * @return all "picked" elements.
     */
    Set<T> getPicked();
    
    /** 
     * @return <code>true</code> if <code>v</code> is currently "picked".
     */
    boolean isPicked(T v);

}