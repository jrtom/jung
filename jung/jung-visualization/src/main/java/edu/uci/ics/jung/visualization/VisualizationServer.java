/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package edu.uci.ics.jung.visualization;

import java.awt.Graphics;
import java.awt.RenderingHints.Key;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * @author tom
 *
 * @param <V>
 * @param <E>
 */
public interface VisualizationServer<V, E> {

    /**
     * set whether this class uses its offscreen image or not. If
     * true, then doubleBuffering in the superclass is set to 'false'
     */
    void setDoubleBuffered(boolean doubleBuffered);

    /**
     * whether this class uses double buffering. The superclass
     * will be the opposite state.
     */
    boolean isDoubleBuffered();

    /**
     * @return Returns the model.
     */
    VisualizationModel<V, E> getModel();

    /**
     * @param model The model to set.
     */
    void setModel(VisualizationModel<V, E> model);

    /**
     * In response to changes from the model, repaint the
     * view, then fire an event to any listeners.
     * Examples of listeners are the GraphZoomScrollPane and
     * the BirdsEyeVisualizationViewer
     */
    void stateChanged(ChangeEvent e);

    /**
     * Sets the showing Renderer to be the input Renderer. Also
     * tells the Renderer to refer to this visualizationviewer
     * as a PickedKey. (Because Renderers maintain a small
     * amount of state, such as the PickedKey, it is important
     * to create a separate instance for each VV instance.)
     */
    void setRenderer(Renderer<V, E> r);

    /**
     * Returns the renderer used by this instance.
     */
    Renderer<V, E> getRenderer();

    /**
     * Removes the current graph layout, and adds a new one.
     * @param layout the new layout to set
     */
    void setGraphLayout(Layout<V, E> layout);

    /**
     * Removes the current graph layout, and adds a new one,
     * optionally re-scaling the view to show the entire layout
     * @param layout the new layout to set
     * @param scaleToLayout whether to scale the view to show the whole layout
     */
//    void setGraphLayout(Layout<V, E> layout, boolean scaleToLayout);

    /**
     * Returns the current graph layout.
     * Passes thru to the model
     */
    Layout<V, E> getGraphLayout();

    /** 
     * 
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    void setVisible(boolean aFlag);

    /**
     * Returns a flag that says whether the visRunner thread is running. If
     * it is not, then you may need to restart the thread. 
     */
//    boolean isVisRunnerRunning();

    /**
     * Transform the mouse point with the inverse transform
     * of the VisualizationViewer. This maps from screen coordinates
     * to graph coordinates.
     * @param p the point to transform (typically, a mouse point)
     * @return a transformed Point2D
     */
//    Point2D inverseTransform(Point2D p);
//
//    Point2D inverseViewTransform(Point2D p);
//
//    Point2D inverseLayoutTransform(Point2D p);

    /**
     * Transform the mouse point with the current transform
     * of the VisualizationViewer. This maps from graph coordinates
     * to screen coordinates.
     * @param p the point to transform
     * @return a transformed Point2D
     */
//    Point2D transform(Point2D p);
//
//    Point2D viewTransform(Point2D p);
//
//    Point2D layoutTransform(Point2D p);

    /**
     * @param transformer The transformer to set.
     */
//    void setViewTransformer(MutableTransformer transformer);
//
//    void setLayoutTransformer(MutableTransformer transformer);
//
//    MutableTransformer getViewTransformer();
//
//    MutableTransformer getLayoutTransformer();

    /**
     * @return Returns the renderingHints.
     */
    Map<Key, Object> getRenderingHints();

    /**
     * @param renderingHints The renderingHints to set.
     */
    void setRenderingHints(Map<Key, Object> renderingHints);

    /**
     * @param paintable The paintable to add.
     */
    void addPreRenderPaintable(Paintable paintable);

    /**
     * @param paintable The paintable to remove.
     */
    void removePreRenderPaintable(Paintable paintable);

    /**
     * @param paintable The paintable to add.
     */
    void addPostRenderPaintable(Paintable paintable);

    /**
     * @param paintable The paintable to remove.
     */
    void removePostRenderPaintable(Paintable paintable);

    /**
     * Adds a <code>ChangeListener</code>.
     * @param l the listener to be added
     */
    void addChangeListener(ChangeListener l);

    /**
     * Removes a ChangeListener.
     * @param l the listener to be removed
     */
    void removeChangeListener(ChangeListener l);

    /**
     * Returns an array of all the <code>ChangeListener</code>s added
     * with addChangeListener().
     *
     * @return all of the <code>ChangeListener</code>s added or an empty
     *         array if no listeners have been added
     */
    ChangeListener[] getChangeListeners();

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created.
     * @see EventListenerList
     */
    void fireStateChanged();

    /**
     * @return Returns the pickedState.
     */
    PickedState<V> getPickedVertexState();

    /**
     * @return Returns the pickedState.
     */
    PickedState<E> getPickedEdgeState();

    /**
     * @param pickedState The pickedState to set.
     */
    void setPickedVertexState(PickedState<V> pickedVertexState);

    void setPickedEdgeState(PickedState<E> pickedEdgeState);

    /**
     * @return Returns the GraphElementAccessor.
     */
    GraphElementAccessor<V, E> getPickSupport();

    /**
     * @param pickSupport The pickSupport to set.
     */
    void setPickSupport(GraphElementAccessor<V, E> pickSupport);

    Point2D getCenter();

    RenderContext<V, E> getRenderContext();

    void setRenderContext(RenderContext<V, E> renderContext);
    
    void repaint();
    
    /**
     * an interface for the preRender and postRender
     */
    interface Paintable {
        public void paint(Graphics g);
        public boolean useTransform();
    }



}