/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 15, 2005
 */

package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.MouseWheelEvent;

/**
 * Overrides ScalingGraphMousePlugin so that mouse events in the satellite view will cause scaling
 * in the main view
 *
 * @see ScalingGraphMousePlugin
 * @author Tom Nelson
 */
public class SatelliteScalingGraphMousePlugin extends ScalingGraphMousePlugin {

  public SatelliteScalingGraphMousePlugin(ScalingControl scaler, int modifiers) {
    super(scaler, modifiers);
  }

  public SatelliteScalingGraphMousePlugin(
      ScalingControl scaler, int modifiers, float in, float out) {
    super(scaler, modifiers, in, out);
  }

  /**
   * zoom the master view display in or out, depending on the direction of the mouse wheel motion.
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    boolean accepted = checkModifiers(e);
    if (accepted == true) {
      VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();

      if (vv instanceof SatelliteVisualizationViewer) {
        VisualizationViewer<?, ?> vvMaster = ((SatelliteVisualizationViewer<?, ?>) vv).getMaster();

        int amount = e.getWheelRotation();

        if (amount > 0) {
          scaler.scale(vvMaster, in, vvMaster.getCenter());

        } else if (amount < 0) {
          scaler.scale(vvMaster, out, vvMaster.getCenter());
        }
        e.consume();
        vv.repaint();
      }
    }
  }
}
