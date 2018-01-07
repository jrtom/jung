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

import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.Lens;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

/**
 * Extends TranslatingGraphMousePlugin and adds the capability to drag and resize the viewing lens
 * in the graph view. Mouse1 in the center moves the lens, mouse1 on the edge resizes the lens. The
 * default mouse button and modifiers can be overridden in the constructor.
 *
 * @author Tom Nelson
 */
public class LensTranslatingGraphMousePlugin extends TranslatingGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  protected boolean dragOnLens;
  protected boolean dragOnEdge;
  protected double edgeOffset;
  /** create an instance with default modifiers */
  public LensTranslatingGraphMousePlugin() {
    this(MouseEvent.BUTTON1_MASK);
  }

  /**
   * create an instance with passed modifer value
   *
   * @param modifiers the mouse event modifier to activate this function
   */
  public LensTranslatingGraphMousePlugin(int modifiers) {
    super(modifiers);
  }

  /**
   * Check the event modifiers. Set the 'down' point for later use. If this event satisfies the
   * modifiers, change the cursor to the system 'move cursor'
   *
   * @param e the event
   */
  public void mousePressed(MouseEvent e) {
    VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);
    MutableTransformer layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT);
    Point2D p = e.getPoint();
    if (viewTransformer instanceof LensTransformer) {
      //        viewTransformer = ((LensTransformer) viewTransformer).getDelegate();
      p = ((LensTransformer) viewTransformer).getDelegate().inverseTransform(p);
    } else {
      p = viewTransformer.inverseTransform(p);
    }
    boolean accepted = checkModifiers(e);
    if (accepted) {
      vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      if (layoutTransformer instanceof LensTransformer) {
        Lens lens = ((LensTransformer) layoutTransformer).getLens();
        testViewCenter(lens, p);
      }
      if (viewTransformer instanceof LensTransformer) {
        Lens lens = ((LensTransformer) viewTransformer).getLens();
        testViewCenter(lens, p);
      }
      vv.repaint();
    }
    super.mousePressed(e);
  }

  /**
   * called to change the location of the lens
   *
   * @param lens
   * @param point
   */
  private void setViewCenter(Lens lens, Point2D point) {
    lens.setCenter(point);
  }

  /**
   * called to change the radius of the lens
   *
   * @param lens
   * @param point
   */
  private void setViewRadius(Lens lens, Point2D point) {
    double distanceFromCenter = lens.getDistanceFromCenter(point);
    lens.setRadius(distanceFromCenter + edgeOffset);
  }

  /**
   * called to set up translating the lens center or changing the layoutSize
   *
   * @param lens
   * @param point
   */
  private void testViewCenter(Lens lens, Point2D point) {
    double distanceFromCenter = lens.getDistanceFromCenter(point);
    if (distanceFromCenter < 10) {
      lens.setCenter(point);
      dragOnLens = true;
    } else if (Math.abs(distanceFromCenter - lens.getRadius()) < 10) {
      edgeOffset = lens.getRadius() - distanceFromCenter;
      lens.setRadius(distanceFromCenter + edgeOffset);
      dragOnEdge = true;
    }
  }

  /** unset the 'down' point and change the cursoe back to the system default cursor */
  public void mouseReleased(MouseEvent e) {
    super.mouseReleased(e);
    dragOnLens = false;
    dragOnEdge = false;
    edgeOffset = 0;
  }

  /**
   * check the modifiers. If accepted, move or resize the lens according to the dragging of the
   * mouse pointer
   *
   * @param e the event
   */
  public void mouseDragged(MouseEvent e) {
    boolean accepted = checkModifiers(e);
    if (accepted) {

      VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>) e.getSource();
      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();
      MutableTransformer layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT);

      MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);
      Lens lens =
          (layoutTransformer instanceof LensTransformer)
              ? ((LensTransformer) layoutTransformer).getLens()
              : (viewTransformer instanceof LensTransformer)
                  ? ((LensTransformer) viewTransformer).getLens()
                  : null;
      if (lens != null) {
        Point2D p = e.getPoint();
        if (viewTransformer instanceof LensTransformer) {
          p = ((LensTransformer) viewTransformer).getDelegate().inverseTransform(p);
        } else {
          p = viewTransformer.inverseTransform(p);
        }

        vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        if (dragOnLens) {
          setViewCenter(lens, p);
          e.consume();
          vv.repaint();

        } else if (dragOnEdge) {
          setViewRadius(lens, p);
          e.consume();
          vv.repaint();

        } else {

          super.mouseDragged(e);
        }
      }
    }
  }
}
