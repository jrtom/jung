/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.algorithms.layout.LayoutModel;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

/**
 * A version of the AnimatedPickingGraphMousePlugin that is for the SatelliteVisualizationViewer.
 * The difference it that when you pick a Vertex in the Satellite View, the 'master view' is
 * translated to move that Vertex to the center.
 *
 * @see AnimatedPickingGraphMousePlugin
 * @author Tom Nelson
 */
public class SatelliteAnimatedPickingGraphMousePlugin<N, E>
    extends AnimatedPickingGraphMousePlugin<N, E> implements MouseListener, MouseMotionListener {

  /** create an instance */
  public SatelliteAnimatedPickingGraphMousePlugin() {
    this(InputEvent.BUTTON1_MASK | InputEvent.CTRL_MASK);
  }

  public SatelliteAnimatedPickingGraphMousePlugin(int selectionModifiers) {
    super(selectionModifiers);
  }

  /** override subclass method to translate the master view instead of this satellite view */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    if (e.getModifiers() == modifiers) {
      final VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
      if (vv instanceof SatelliteVisualizationViewer) {
        final VisualizationViewer<N, E> vvMaster =
            ((SatelliteVisualizationViewer<N, E>) vv).getMaster();

        if (vertex != null) {
          LayoutModel<N, Point2D> layoutModel = vvMaster.getModel().getLayoutModel();
          Point2D q = layoutModel.apply(vertex);
          Point2D lvc =
              vvMaster
                  .getRenderContext()
                  .getMultiLayerTransformer()
                  .inverseTransform(Layer.LAYOUT, vvMaster.getCenter());
          final double dx = (lvc.getX() - q.getX()) / 10;
          final double dy = (lvc.getY() - q.getY()) / 10;

          Runnable animator =
              new Runnable() {

                public void run() {
                  for (int i = 0; i < 10; i++) {
                    vvMaster
                        .getRenderContext()
                        .getMultiLayerTransformer()
                        .getTransformer(Layer.LAYOUT)
                        .translate(dx, dy);
                    try {
                      Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                  }
                }
              };
          Thread thread = new Thread(animator);
          thread.start();
        }
      }
    }
  }
}
