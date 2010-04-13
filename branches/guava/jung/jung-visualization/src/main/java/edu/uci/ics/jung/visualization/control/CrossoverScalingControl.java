/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 * Created on Mar 8, 2005
 *
 */
package edu.uci.ics.jung.visualization.control;

import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/** 
 * A scaling control that has a crossover point.
 * When the overall scale of the view and
 * model is less than the crossover point, the scaling is applied
 * to the view's transform and the graph nodes, labels, etc grow
 * smaller. This preserves the overall shape of the graph.
 * When the scale is larger than the crossover, the scaling is
 * applied to the graph layout. The graph spreads out, but the
 * vertices and labels grow no larger than their original size. 
 * 
 * @author Tom Nelson
 */
public class CrossoverScalingControl implements ScalingControl {

    /**
     * Point where scale crosses over from view to layout.
     */
    protected double crossover = 1.0;
    
    /**
     * Sets the crossover point to the specified value.
     */
	public void setCrossover(double crossover) {
	    this.crossover = crossover;
	}

    /**
     * Returns the current crossover value.
     */
    public double getCrossover() {
        return crossover;
    }
    
	/**
     * @see edu.uci.ics.jung.visualization.control.ScalingControl#scale(VisualizationViewer, float, Point2D)
     */
	public void scale(VisualizationServer<?,?> vv, float amount, Point2D at) {
	        
	    MutableTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
	    MutableTransformer viewTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
	    double modelScale = layoutTransformer.getScale();
	    double viewScale = viewTransformer.getScale();
	    double inverseModelScale = Math.sqrt(crossover)/modelScale;
	    double inverseViewScale = Math.sqrt(crossover)/viewScale;
	    double scale = modelScale * viewScale;
	    
	    Point2D transformedAt = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, at);
	    
        if((scale*amount - crossover)*(scale*amount - crossover) < 0.001) {
            // close to the control point, return both transformers to a scale of sqrt crossover value
            layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt);
            viewTransformer.scale(inverseViewScale, inverseViewScale, at);
        } else if(scale*amount < crossover) {
            // scale the viewTransformer, return the layoutTransformer to sqrt crossover value
	        viewTransformer.scale(amount, amount, at);
	        layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt);
	    } else {
            // scale the layoutTransformer, return the viewTransformer to crossover value
	        layoutTransformer.scale(amount, amount, transformedAt);
	        viewTransformer.scale(inverseViewScale, inverseViewScale, at);
	    }
	    vv.repaint();
	}
}
