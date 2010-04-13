/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package edu.uci.ics.jung.visualization.control;

import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/**
 * scales to the absolute value passed as an argument.
 * It first resets the scaling transformers, then uses
 * the relative CrossoverScalingControl to achieve the
 * absolute value.
 * 
 * @author Tom Nelson 
 *
 */
public class AbsoluteCrossoverScalingControl extends CrossoverScalingControl
        implements ScalingControl {

    /**
     * scale to the absolute value passed as 'amount'.
     * 
     */
    public void scale(VisualizationViewer<?,?> vv, float amount, Point2D at) {
        MutableTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
        MutableTransformer viewTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
        double modelScale = layoutTransformer.getScale();
        double viewScale = viewTransformer.getScale();
        double inverseModelScale = Math.sqrt(crossover)/modelScale;
        double inverseViewScale = Math.sqrt(crossover)/viewScale;
        
        Point2D transformedAt = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, at);
        
        // return the transformers to 1.0
        layoutTransformer.scale(inverseModelScale, inverseModelScale, transformedAt);
        viewTransformer.scale(inverseViewScale, inverseViewScale, at);

        super.scale(vv, amount, at);
    }
}
