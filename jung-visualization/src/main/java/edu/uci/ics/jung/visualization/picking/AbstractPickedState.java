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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.event.EventListenerList;

/**
 * An abstract class to support ItemEvents for PickedState
 * 
 * @author Tom Nelson
 */
public abstract class AbstractPickedState<T> implements PickedState<T> {
    
    protected EventListenerList listenerList = new EventListenerList();

    public void addItemListener(ItemListener l) {
        listenerList.add(ItemListener.class, l);
        
    }

    public void removeItemListener(ItemListener l) {
        listenerList.remove(ItemListener.class, l);
    }
    
    protected void fireItemStateChanged(ItemEvent e) {
        Object[] listeners = listenerList.getListenerList();
        for ( int i = listeners.length-2; i>=0; i-=2 ) {
            if ( listeners[i]==ItemListener.class ) {
                ((ItemListener)listeners[i+1]).itemStateChanged(e);
            }
        }
    }   
}