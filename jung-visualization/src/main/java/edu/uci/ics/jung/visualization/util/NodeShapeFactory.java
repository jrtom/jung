/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 20, 2004
 */
package edu.uci.ics.jung.visualization.util;

import com.google.common.base.Preconditions;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for generating <code>Shape</code>s for drawing nodes. The available shapes
 * include rectangles, rounded rectangles, ellipses, regular polygons, and regular stars. The
 * dimensions of the requested shapes are defined by the specified node layoutSize function
 * (specified by a {@code Function<? super N, Integer>}) and node aspect ratio function (specified
 * by a {@code Function<? super N, Float>}) implementations: the width of the bounding box of the
 * shape is given by the node layoutSize, and the height is given by the layoutSize multiplied by
 * the node's aspect ratio.
 *
 * @author Joshua O'Madadhain
 */
public class NodeShapeFactory<N> {

  private static final Logger log = LoggerFactory.getLogger(NodeShapeFactory.class);
  protected Function<? super N, Integer> vsf;
  protected Function<? super N, Float> varf;

  /**
   * Creates an instance with the specified node layoutSize and aspect ratio functions.
   *
   * @param vsf provides a layoutSize (width) for each node
   * @param varf provides a height/width ratio for each node
   */
  public NodeShapeFactory(Function<? super N, Integer> vsf, Function<? super N, Float> varf) {
    this.vsf = vsf;
    this.varf = varf;
  }

  /**
   * Creates a <code>NodeShapeFactory</code> with a constant layoutSize of 10 and a constant aspect
   * ratio of 1.
   */
  public NodeShapeFactory() {
    this(n -> 10, n -> 1.0f);
  }

  private static final Rectangle2D theRectangle = new Rectangle2D.Float();

  /**
   * Returns a <code>Rectangle2D</code> whose width and height are defined by this instance's
   * layoutSize and aspect ratio functions for this node.
   *
   * @param v the node for which the shape will be drawn
   * @return a rectangle for this node
   */
  public Rectangle2D getRectangle(N v) {
    float width = vsf.apply(v);
    float height = width * varf.apply(v);
    float h_offset = -(width / 2);
    float v_offset = -(height / 2);
    theRectangle.setFrame(h_offset, v_offset, width, height);
    return theRectangle;
  }

  private static final Ellipse2D theEllipse = new Ellipse2D.Float();

  /**
   * Returns a <code>Ellipse2D</code> whose width and height are defined by this instance's
   * layoutSize and aspect ratio functions for this node.
   *
   * @param v the node for which the shape will be drawn
   * @return an ellipse for this node
   */
  public Ellipse2D getEllipse(N v) {
    theEllipse.setFrame(getRectangle(v));
    return theEllipse;
  }

  private static final RoundRectangle2D theRoundRectangle = new RoundRectangle2D.Float();
  /**
   * Returns a <code>RoundRectangle2D</code> whose width and height are defined by this instance's
   * layoutSize and aspect ratio functions for this node. The arc layoutSize is set to be half the
   * minimum of the height and width of the frame.
   *
   * @param v the node for which the shape will be drawn
   * @return an round rectangle for this node
   */
  public RoundRectangle2D getRoundRectangle(N v) {
    Rectangle2D frame = getRectangle(v);
    float arc_size = (float) Math.min(frame.getHeight(), frame.getWidth()) / 2;
    theRoundRectangle.setRoundRect(
        frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight(), arc_size, arc_size);
    return theRoundRectangle;
  }

  //  private static final GeneralPath thePolygon = new GeneralPath();

  /**
   * Returns a regular <code>num_sides</code>-sided <code>Polygon</code> whose bounding box's width
   * and height are defined by this instance's layoutSize and aspect ratio functions for this node.
   *
   * @param v the node for which the shape will be drawn
   * @param num_sides the number of sides of the polygon; must be &ge; 3.
   * @return a regular polygon for this node
   */
  public Shape getRegularPolygon(N v, int num_sides) {
    GeneralPath thePolygon = new GeneralPath();
    Preconditions.checkArgument(num_sides >= 3, "Number of sides must be >= 3");
    Rectangle2D frame = getRectangle(v);
    float width = (float) frame.getWidth();
    float height = (float) frame.getHeight();

    // generate coordinates
    double angle = 0;
    thePolygon.reset();
    thePolygon.moveTo(0, 0);
    thePolygon.lineTo(width, 0);
    double theta = (2 * Math.PI) / num_sides;
    for (int i = 2; i < num_sides; i++) {
      angle -= theta;
      float delta_x = (float) (width * Math.cos(angle));
      float delta_y = (float) (width * Math.sin(angle));
      Point2D prev = thePolygon.getCurrentPoint();
      thePolygon.lineTo((float) prev.getX() + delta_x, (float) prev.getY() + delta_y);
    }
    thePolygon.closePath();

    // scale polygon to be right layoutSize, translate to center at (0,0)
    Rectangle2D r = thePolygon.getBounds2D();
    double scale_x = width / r.getWidth();
    double scale_y = height / r.getHeight();
    float translationX = (float) (r.getMinX() + r.getWidth() / 2);
    float translationY = (float) (r.getMinY() + r.getHeight() / 2);

    AffineTransform at = AffineTransform.getScaleInstance(scale_x, scale_y);
    at.translate(-translationX, -translationY);

    return at.createTransformedShape(thePolygon);
  }

  /**
   * Returns a regular <code>Polygon</code> of <code>num_points</code> points whose bounding box's
   * width and height are defined by this instance's layoutSize and aspect ratio functions for this
   * node.
   *
   * @param v the node for which the shape will be drawn
   * @param num_points the number of points of the polygon; must be &ge; 5.
   * @return an star shape for this node
   */
  public Shape getRegularStar(N v, int num_points) {
    GeneralPath thePolygon = new GeneralPath();
    Preconditions.checkArgument(num_points >= 5, "Number of points must be >= 5");
    Rectangle2D frame = getRectangle(v);
    float width = (float) frame.getWidth();
    float height = (float) frame.getHeight();

    // generate coordinates
    double theta = (2 * Math.PI) / num_points;
    double angle = -theta / 2;
    thePolygon.reset();
    thePolygon.moveTo(0, 0);
    float delta_x = width * (float) Math.cos(angle);
    float delta_y = width * (float) Math.sin(angle);
    Point2D prev = thePolygon.getCurrentPoint();
    thePolygon.lineTo((float) prev.getX() + delta_x, (float) prev.getY() + delta_y);
    for (int i = 1; i < num_points; i++) {
      angle += theta;
      delta_x = width * (float) Math.cos(angle);
      delta_y = width * (float) Math.sin(angle);
      prev = thePolygon.getCurrentPoint();
      thePolygon.lineTo((float) prev.getX() + delta_x, (float) prev.getY() + delta_y);
      angle -= theta * 2;
      delta_x = width * (float) Math.cos(angle);
      delta_y = width * (float) Math.sin(angle);
      prev = thePolygon.getCurrentPoint();
      if (prev != null) {
        thePolygon.lineTo((float) prev.getX() + delta_x, (float) prev.getY() + delta_y);
      } else {
        log.error("somehow, prev is null");
      }
    }
    thePolygon.closePath();

    // scale polygon to be right layoutSize, translate to center at (0,0)
    Rectangle2D r = thePolygon.getBounds2D();
    double scale_x = width / r.getWidth();
    double scale_y = height / r.getHeight();

    float translationX = (float) (r.getMinX() + r.getWidth() / 2);
    float translationY = (float) (r.getMinY() + r.getHeight() / 2);

    AffineTransform at = AffineTransform.getScaleInstance(scale_x, scale_y);
    at.translate(-translationX, -translationY);

    return at.createTransformedShape(thePolygon);
  }
}
