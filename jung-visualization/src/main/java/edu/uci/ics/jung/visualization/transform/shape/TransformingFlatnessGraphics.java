/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 11, 2005
 */

package edu.uci.ics.jung.visualization.transform.shape;

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer;
import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * subclassed to pass certain operations thru the Function before the base class method is applied
 * This is useful when you want to animate non-affine transformations to the Graphics2D used to draw
 * elements of the graph.
 *
 * @author Tom Nelson
 */
public class TransformingFlatnessGraphics extends TransformingGraphics {

  float flatness = 0;

  public TransformingFlatnessGraphics(BidirectionalTransformer transformer) {
    this(transformer, null);
  }

  public TransformingFlatnessGraphics(BidirectionalTransformer transformer, Graphics2D delegate) {
    super(transformer, delegate);
  }

  public void draw(Shape s, float flatness) {
    Shape shape = null;
    if (transformer instanceof ShapeFlatnessTransformer) {
      shape = ((ShapeFlatnessTransformer) transformer).transform(s, flatness);
    } else {
      shape = ((ShapeTransformer) transformer).transform(s);
    }
    delegate.draw(shape);
  }

  public void fill(Shape s, float flatness) {
    Shape shape = null;
    if (transformer instanceof HyperbolicTransformer) {
      shape = ((HyperbolicShapeTransformer) transformer).transform(s, flatness);
    } else {
      shape = ((ShapeTransformer) transformer).transform(s);
    }
    delegate.fill(shape);
  }
}
