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

import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicIconFactory;


/** 
 * 
 * AbstractModalGraphMouse is a PluggableGraphMouse class that
 * manages a collection of plugins for picking and
 * transforming the graph. Additionally, it carries the notion
 * of a Mode: Picking or Translating. Switching between modes
 * allows for a more natural choice of mouse modifiers to
 * be used for the various plugins. The default modifiers are
 * intended to mimick those of mainstream software applications
 * in order to be intuitive to users.
 * 
 * To change between modes, two different controls are offered,
 * a combo box and a menu system. These controls are lazily created
 * in their respective 'getter' methods so they don't impact
 * code that does not intend to use them.
 * The menu control can be placed in an unused corner of the
 * GraphZoomScrollPane, which is a common location for mouse
 * mode selection menus in mainstream applications.
 * 
 * Users must implement the loadPlugins() method to create and
 * install the GraphMousePlugins. The order of the plugins is
 * important, as they are evaluated against the mask parameters
 * in the order that they are added.
 * 
 * @author Tom Nelson
 */
public abstract class AbstractModalGraphMouse extends PluggableGraphMouse 
    implements ModalGraphMouse, ItemSelectable {
    
	/**
	 * used by the scaling plugins for zoom in
	 */
    protected float in;
    /**
     * used by the scaling plugins for zoom out
     */
    protected float out;
    /**
     * a listener for mode changes
     */
    protected ItemListener modeListener;
    /**
     * a JComboBox control available to set the mode
     */
    protected JComboBox modeBox;
    /**
     * a menu available to set the mode
     */
    protected JMenu modeMenu;
    /**
     * the current mode
     */
    protected Mode mode;
    /**
     * listeners for mode changes
     */
    protected EventListenerList listenerList = new EventListenerList();

    protected GraphMousePlugin pickingPlugin;
    protected GraphMousePlugin translatingPlugin;
    protected GraphMousePlugin animatedPickingPlugin;
    protected GraphMousePlugin scalingPlugin;
    protected GraphMousePlugin rotatingPlugin;
    protected GraphMousePlugin shearingPlugin;
    protected KeyListener modeKeyListener;
    

    protected AbstractModalGraphMouse(float in, float out) {
		this.in = in;
		this.out = out;
	}

	/**
     * create the plugins, and load the plugins for TRANSFORMING mode
     *
     */
    protected abstract void loadPlugins();
    
    /**
     * setter for the Mode.
     */
    public void setMode(Mode mode) {
        if(this.mode != mode) {
            fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
                    this.mode, ItemEvent.DESELECTED));
            this.mode = mode;
            if(mode == Mode.TRANSFORMING) {
                setTransformingMode();
            } else if(mode == Mode.PICKING) {
                setPickingMode();
            }
            if(modeBox != null) {
                modeBox.setSelectedItem(mode);
            }
            fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED));
        }
    }
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.control.ModalGraphMouse#setPickingMode()
     */
    protected void setPickingMode() {
        remove(translatingPlugin);
        remove(rotatingPlugin);
        remove(shearingPlugin);
        add(pickingPlugin);
        add(animatedPickingPlugin);
    }
    
    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.control.ModalGraphMouse#setTransformingMode()
     */
    protected void setTransformingMode() {
        remove(pickingPlugin);
        remove(animatedPickingPlugin);
        add(translatingPlugin);
        add(rotatingPlugin);
        add(shearingPlugin);
    }

    /**
     * @param zoomAtMouse The zoomAtMouse to set.
     */
    public void setZoomAtMouse(boolean zoomAtMouse) {
        ((ScalingGraphMousePlugin) scalingPlugin).setZoomAtMouse(zoomAtMouse);
    }
    
    /**
     * listener to set the mode from an external event source
     */
    class ModeListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            setMode((Mode) e.getItem());
        }
    }

    /* (non-Javadoc)
     * @see edu.uci.ics.jung.visualization.control.ModalGraphMouse#getModeListener()
     */
    public ItemListener getModeListener() {
		if (modeListener == null) {
			modeListener = new ModeListener();
		}
		return modeListener;
	}
    
    /**
	 * @return the modeKeyListener
	 */
	public KeyListener getModeKeyListener() {
		return modeKeyListener;
	}

	/**
	 * @param modeKeyListener the modeKeyListener to set
	 */
	public void setModeKeyListener(KeyListener modeKeyListener) {
		this.modeKeyListener = modeKeyListener;
	}

	/**
	 * @return Returns the modeBox.
	 */
    public JComboBox getModeComboBox() {
        if(modeBox == null) {
            modeBox = new JComboBox(new Mode[]{Mode.TRANSFORMING, Mode.PICKING});
            modeBox.addItemListener(getModeListener());
        }
        modeBox.setSelectedItem(mode);
        return modeBox;
    }
    
    /**
     * create (if necessary) and return a menu that will change
     * the mode
     * @return the menu
     */
    public JMenu getModeMenu() {
        if(modeMenu == null) {
            modeMenu = new JMenu();// {
            Icon icon = BasicIconFactory.getMenuArrowIcon();
            modeMenu.setIcon(BasicIconFactory.getMenuArrowIcon());
            modeMenu.setPreferredSize(new Dimension(icon.getIconWidth()+10, 
            		icon.getIconHeight()+10));

            final JRadioButtonMenuItem transformingButton = 
                new JRadioButtonMenuItem(Mode.TRANSFORMING.toString());
            transformingButton.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                        setMode(Mode.TRANSFORMING);
                    }
                }});
            
            final JRadioButtonMenuItem pickingButton =
                new JRadioButtonMenuItem(Mode.PICKING.toString());
            pickingButton.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                        setMode(Mode.PICKING);
                    }
                }});
            ButtonGroup radio = new ButtonGroup();
            radio.add(transformingButton);
            radio.add(pickingButton);
            transformingButton.setSelected(true);
            modeMenu.add(transformingButton);
            modeMenu.add(pickingButton);
            modeMenu.setToolTipText("Menu for setting Mouse Mode");
            addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						if(e.getItem() == Mode.TRANSFORMING) {
							transformingButton.setSelected(true);
						} else if(e.getItem() == Mode.PICKING) {
							pickingButton.setSelected(true);
						}
					}
				}});
        }
        return modeMenu;
    }
    
    /**
     * add a listener for mode changes
     */
    public void addItemListener(ItemListener aListener) {
        listenerList.add(ItemListener.class,aListener);
    }

    /**
     * remove a listener for mode changes
     */
    public void removeItemListener(ItemListener aListener) {
        listenerList.remove(ItemListener.class,aListener);
    }

    /**
     * Returns an array of all the <code>ItemListener</code>s added
     * to this JComboBox with addItemListener().
     *
     * @return all of the <code>ItemListener</code>s added or an empty
     *         array if no listeners have been added
     * @since 1.4
     */
    public ItemListener[] getItemListeners() {
        return listenerList.getListeners(ItemListener.class);
    }
    
    public Object[] getSelectedObjects() {
        if ( mode == null )
            return new Object[0];
        else {
            Object result[] = new Object[1];
            result[0] = mode;
            return result;
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.
     * @param e  the event of interest
     *  
     * @see EventListenerList
     */
    protected void fireItemStateChanged(ItemEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for ( int i = listeners.length-2; i>=0; i-=2 ) {
            if ( listeners[i]==ItemListener.class ) {
                ((ItemListener)listeners[i+1]).itemStateChanged(e);
            }
        }
    }
}
