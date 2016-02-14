/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 7, 2005
 */

package edu.uci.ics.jung.visualization.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * a GraphMouse that accepts plugins for various mouse events.
 * 
 * @author Tom Nelson 
 *
 *
 */
public class PluggableGraphMouse implements VisualizationViewer.GraphMouse {

    MouseListener[] mouseListeners;
    MouseMotionListener[] mouseMotionListeners;
    MouseWheelListener[] mouseWheelListeners;
    Set<GraphMousePlugin> mousePluginList = new LinkedHashSet<GraphMousePlugin>();
    Set<MouseMotionListener> mouseMotionPluginList = new LinkedHashSet<MouseMotionListener>();
    Set<MouseWheelListener> mouseWheelPluginList = new LinkedHashSet<MouseWheelListener>();

    public void add(GraphMousePlugin plugin) {
        if(plugin instanceof MouseListener) {
            mousePluginList.add(plugin);
            mouseListeners = null;
        }
        if(plugin instanceof MouseMotionListener) {
            mouseMotionPluginList.add((MouseMotionListener)plugin);
            mouseMotionListeners = null;
        }
        if(plugin instanceof MouseWheelListener) {
            mouseWheelPluginList.add((MouseWheelListener)plugin);
            mouseWheelListeners = null;
        }
    }

    public void remove(GraphMousePlugin plugin) {
        if(plugin instanceof MouseListener) {
            boolean wasThere = mousePluginList.remove(plugin);
            if(wasThere) mouseListeners = null;
        }
        if(plugin instanceof MouseMotionListener) {
            boolean wasThere = mouseMotionPluginList.remove(plugin);
            if(wasThere) mouseMotionListeners = null;
        }
        if(plugin instanceof MouseWheelListener) {
            boolean wasThere = mouseWheelPluginList.remove(plugin);
            if(wasThere) mouseWheelListeners = null;
        }
    }
    
    private void checkMouseListeners() {
        if(mouseListeners == null) {
            mouseListeners = (MouseListener[])
            mousePluginList.toArray(new MouseListener[mousePluginList.size()]);
        }
    }
    
    private void checkMouseMotionListeners() {
        if(mouseMotionListeners == null){
            mouseMotionListeners = (MouseMotionListener[])
            mouseMotionPluginList.toArray(new MouseMotionListener[mouseMotionPluginList.size()]);
        } 
    }
    
    private void checkMouseWheelListeners() {
        if(mouseWheelListeners == null) {
            mouseWheelListeners = (MouseWheelListener[])
            mouseWheelPluginList.toArray(new MouseWheelListener[mouseWheelPluginList.size()]);
        }
    }

    public void mouseClicked(MouseEvent e) {
        checkMouseListeners();
        for(int i=0; i<mouseListeners.length; i++) {
            mouseListeners[i].mouseClicked(e);
            if(e.isConsumed()) break;
        }
    }
    
    public void mousePressed(MouseEvent e) {
        checkMouseListeners();
        for(int i=0; i<mouseListeners.length; i++) {
            mouseListeners[i].mousePressed(e);
            if(e.isConsumed()) break;
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        checkMouseListeners();
        for(int i=0; i<mouseListeners.length; i++) {
            mouseListeners[i].mouseReleased(e);
            if(e.isConsumed()) break;
        }
    }
    
    public void mouseEntered(MouseEvent e) {
        checkMouseListeners();
        for(int i=0; i<mouseListeners.length; i++) {
            mouseListeners[i].mouseEntered(e);
            if(e.isConsumed()) break;
        }
    }
    
    public void mouseExited(MouseEvent e) {
        checkMouseListeners();
        for(int i=0; i<mouseListeners.length; i++) {
            mouseListeners[i].mouseExited(e);
            if(e.isConsumed()) break;
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        checkMouseMotionListeners();
        for(int i=0; i<mouseMotionListeners.length; i++) {
            mouseMotionListeners[i].mouseDragged(e);
            if(e.isConsumed()) break;
        }
    }
    
    public void mouseMoved(MouseEvent e) {
        checkMouseMotionListeners();
        for(int i=0; i<mouseMotionListeners.length; i++) {
            mouseMotionListeners[i].mouseMoved(e);
            if(e.isConsumed()) break;
        }
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        checkMouseWheelListeners();
        for(int i=0; i<mouseWheelListeners.length; i++) {
            mouseWheelListeners[i].mouseWheelMoved(e);
            if(e.isConsumed()) break;
        }
    }
}
