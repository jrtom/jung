/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.visualization;

import java.awt.Dimension;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.layout.ObservableCachingLayout;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;

/**
 * The model containing state values for 
 * visualizations of graphs. 
 * Refactored and extracted from the 1.6.0 version of VisualizationViewer
 * 
 * @author Tom Nelson
 */
public class DefaultVisualizationModel<V, E> implements VisualizationModel<V,E>, ChangeEventSupport {
    
    ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);

    /**
	 * manages the thread that applies the current layout algorithm
	 */
	protected Relaxer relaxer;
	
	/**
	 * the layout algorithm currently in use
	 */
	protected Layout<V,E> layout;
	
	/**
	 * listens for changes in the layout, forwards to the viewer
	 *
	 */
    protected ChangeListener changeListener;
    
    /**
     * 
     * @param layout The Layout to apply, with its associated Graph
     */
	public DefaultVisualizationModel(Layout<V,E> layout) {
        this(layout, null);
	}
    
	/**
	 * Create an instance with the specified layout and dimension.
	 * @param layout the layout to use
	 * @param d The preferred size of the View that will display this graph
	 */
	public DefaultVisualizationModel(Layout<V,E> layout, Dimension d) {
        if(changeListener == null) {
            changeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    fireStateChanged();
                }
            };
        }
		setGraphLayout(layout, d);
	}
	
	/**
	 * Removes the current graph layout, and adds a new one.
	 * @param layout   the new layout to use
	 * @param viewSize the size of the View that will display this layout
	 */
	public void setGraphLayout(Layout<V,E> layout, Dimension viewSize) {
		// remove listener from old layout
	    if(this.layout != null && this.layout instanceof ChangeEventSupport) {
	        ((ChangeEventSupport)this.layout).removeChangeListener(changeListener);
        }
	    // set to new layout
	    if(layout instanceof ChangeEventSupport) {
	    	this.layout = layout;
	    } else {
	    	this.layout = new ObservableCachingLayout<V,E>(layout);
	    }
		
		((ChangeEventSupport)this.layout).addChangeListener(changeListener);

        if(viewSize == null) {
            viewSize = new Dimension(600,600);
        }
		Dimension layoutSize = layout.getSize();
		// if the layout has NOT been initialized yet, initialize its size
		// now to the size of the VisualizationViewer window
		if(layoutSize == null) {
		    layout.setSize(viewSize);
        }
        if(relaxer != null) {
        	relaxer.stop();
        	relaxer = null;
        }
        if(layout instanceof IterativeContext) {
        	layout.initialize();
            if(relaxer == null) {
            	relaxer = new VisRunner((IterativeContext)this.layout);
            	relaxer.prerelax();
            	relaxer.relax();
            }
        }
        fireStateChanged();
	}

	/**
	 * set the graph Layout and if it is not already initialized, initialize
	 * it to the default VisualizationViewer preferred size of 600x600
	 */
	public void setGraphLayout(Layout<V,E> layout) {
	    setGraphLayout(layout, null);
	}

    /**
	 * Returns the current graph layout.
	 */
	public Layout<V,E> getGraphLayout() {
	        return layout;
	}

	/**
	 * @return the relaxer
	 */
	public Relaxer getRelaxer() {
		return relaxer;
	}

	/**
	 * @param relaxer the relaxer to set
	 */
	public void setRelaxer(VisRunner relaxer) {
		this.relaxer = relaxer;
	}

    /**
     * Adds a <code>ChangeListener</code>.
     * @param l the listener to be added
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    /**
     * Removes a ChangeListener.
     * @param l the listener to be removed
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    /**
     * Returns an array of all the <code>ChangeListener</code>s added
     * with addChangeListener().
     *
     * @return all of the <code>ChangeListener</code>s added or an empty
     *         array if no listeners have been added
     */
    public ChangeListener[] getChangeListeners() {
        return changeSupport.getChangeListeners();
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created.
     * The primary listeners will be views that need to be repainted
     * because of changes in this model instance
     * @see EventListenerList
     */
    public void fireStateChanged() {
        changeSupport.fireStateChanged();
    }   
    
}
