/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Feb 17, 2004
 */
package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * This class translates mouse clicks into vertex clicks
 *
 * @author danyelf
 */
public class MouseListenerTranslator extends MouseAdapter {

  private VisualizationViewer vv;
  private GraphMouseListener gel;

  /**
   * @param gel listens for mouse events
   * @param vv the viewer used for visualization
   */
  public MouseListenerTranslator(GraphMouseListener gel, VisualizationViewer vv) {
    this.gel = gel;
    this.vv = vv;
  }

  /**
   * Transform the point to the coordinate system in the VisualizationViewer, then use either
   * PickSuuport (if available) or Layout to find a Vertex
   *
   * @param point
   * @return
   */
  private Object getVertex(Point2D point) {
    // adjust for scale and offset in the VisualizationViewer
    Point2D p = point;
    //vv.getRenderContext().getBasicTransformer().inverseViewTransform(point);
    NetworkElementAccessor pickSupport = vv.getPickSupport();
    //        Layout<V> layout = vv.getGraphLayout();
    Object v = null;
    if (pickSupport != null) {
      v = pickSupport.getNode(p.getX(), p.getY());
    }
    return v;
  }
  /** @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent) */
  public void mouseClicked(MouseEvent e) {
    Object v = getVertex(e.getPoint());
    if (v != null) {
      gel.graphClicked(v, e);
    }
  }

  /** @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent) */
  public void mousePressed(MouseEvent e) {
    Object v = getVertex(e.getPoint());
    if (v != null) {
      gel.graphPressed(v, e);
    }
  }

  /** @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent) */
  public void mouseReleased(MouseEvent e) {
    Object v = getVertex(e.getPoint());
    if (v != null) {
      gel.graphReleased(v, e);
    }
  }
}
