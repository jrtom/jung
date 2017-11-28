/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform;

import java.awt.Component;
import java.awt.geom.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LensTransformer wraps a MutableAffineTransformer and modifies the transform and inverseTransform
 * methods so that they create a projection of the graph points within an elliptical lens.
 *
 * <p>LensTransformer uses an affine transform to cause translation, scaling, rotation, and shearing
 * while applying a possibly non-affine filter in its transform and inverseTransform methods.
 *
 * @author Tom Nelson
 */
public abstract class LensTransformer extends MutableTransformerDecorator
    implements MutableTransformer {

  private static final Logger log = LoggerFactory.getLogger(LensTransformer.class);

  protected Lens lens;

  /** @param component the component used for rendering */
  public LensTransformer(Component component) {
    this(new Lens(component));
  }

  /**
   * Create an instance with a possibly shared lens.
   *
   * @param lens
   */
  public LensTransformer(Lens lens) {
    super(new MutableAffineTransformer());
    this.lens = lens;
  }

  /**
   * @param component the component used for rendering
   * @param delegate the transformer to use
   */
  public LensTransformer(Component component, MutableTransformer delegate) {
    this(new Lens(component), delegate);
  }

  /**
   * @param lens
   * @param delegate the transformer to use
   */
  public LensTransformer(Lens lens, MutableTransformer delegate) {
    super(delegate);
    this.lens = lens;
  }

  public Lens getLens() {
    return lens;
  }

  public void setToIdentity() {
    this.delegate.setToIdentity();
  }

  /** override base class transform to project the fisheye effect */
  public abstract Point2D transform(Point2D graphPoint);

  /** override base class to un-project the fisheye effect */
  public abstract Point2D inverseTransform(Point2D viewPoint);
}
