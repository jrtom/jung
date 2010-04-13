/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 26, 2005
 */

package edu.uci.ics.jung.visualization.control;

import java.awt.event.InputEvent;
/**
 * 
 * @author Tom Nelson 
 *
 */
public class ModalSatelliteGraphMouse extends DefaultModalGraphMouse implements
        ModalGraphMouse {

    public ModalSatelliteGraphMouse() {
        this(1.1f, 1/1.1f);
    }

    public ModalSatelliteGraphMouse(float in, float out) {
        super(in, out);
    }
    
    protected void loadPlugins() {
        pickingPlugin = new PickingGraphMousePlugin();
        animatedPickingPlugin = new SatelliteAnimatedPickingGraphMousePlugin();
        translatingPlugin = new SatelliteTranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
        scalingPlugin = new SatelliteScalingGraphMousePlugin(new CrossoverScalingControl(), 0);
        rotatingPlugin = new SatelliteRotatingGraphMousePlugin();
        shearingPlugin = new SatelliteShearingGraphMousePlugin();
        
        add(scalingPlugin);
        
        setMode(Mode.TRANSFORMING);
    }
}
