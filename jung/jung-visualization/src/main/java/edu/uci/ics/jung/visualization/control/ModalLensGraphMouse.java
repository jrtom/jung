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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * an implementation of the AbstractModalGraphMouse that includes plugins for
 * manipulating a view that is using a LensTransformer.
 * 
 * @author Tom Nelson 
 *
 */
public class ModalLensGraphMouse extends AbstractModalGraphMouse implements
        ModalGraphMouse {

	/**
	 * not included in the base class
	 */
    protected LensMagnificationGraphMousePlugin magnificationPlugin;
    
    public ModalLensGraphMouse() {
        this(1.1f, 1/1.1f);
    }

    public ModalLensGraphMouse(float in, float out) {
        this(in, out, new LensMagnificationGraphMousePlugin());
    }

    public ModalLensGraphMouse(LensMagnificationGraphMousePlugin magnificationPlugin) {
        this(1.1f, 1/1.1f, magnificationPlugin);
    }
    
    public ModalLensGraphMouse(float in, float out, LensMagnificationGraphMousePlugin magnificationPlugin) {
    	super(in,out);
        this.in = in;
        this.out = out;
        this.magnificationPlugin = magnificationPlugin;
        loadPlugins();
        setModeKeyListener(new ModeKeyAdapter(this));
    }
    
    protected void loadPlugins() {
        pickingPlugin = new PickingGraphMousePlugin();
        animatedPickingPlugin = new AnimatedPickingGraphMousePlugin();
        translatingPlugin = new LensTranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
        scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
        rotatingPlugin = new RotatingGraphMousePlugin();
        shearingPlugin = new ShearingGraphMousePlugin();
        
        add(magnificationPlugin);
        add(scalingPlugin);

        setMode(Mode.TRANSFORMING);
    }
    public static class ModeKeyAdapter extends KeyAdapter {
    	private char t = 't';
    	private char p = 'p';
    	protected ModalGraphMouse graphMouse;

    	public ModeKeyAdapter(ModalGraphMouse graphMouse) {
			this.graphMouse = graphMouse;
		}

		public ModeKeyAdapter(char t, char p, ModalGraphMouse graphMouse) {
			this.t = t;
			this.p = p;
			this.graphMouse = graphMouse;
		}
		
		public void keyTyped(KeyEvent event) {
			char keyChar = event.getKeyChar();
			if(keyChar == t) {
				((Component)event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				graphMouse.setMode(Mode.TRANSFORMING);
			} else if(keyChar == p) {
				((Component)event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				graphMouse.setMode(Mode.PICKING);
			}
		}
    }

}
