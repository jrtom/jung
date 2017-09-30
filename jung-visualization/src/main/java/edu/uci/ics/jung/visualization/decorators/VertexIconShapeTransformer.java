/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 1, 2005
 */

package edu.uci.ics.jung.visualization.decorators;

import edu.uci.ics.jung.visualization.util.ImageShapeUtils;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A default implementation that stores images in a Map keyed on the vertex. Also applies a shaping
 * function to images to extract the shape of the opaque part of a transparent image.
 *
 * @author Tom Nelson
 */
public class VertexIconShapeTransformer<V> implements Function<V, Shape> {
  protected Map<Image, Shape> shapeMap = new HashMap<Image, Shape>();
  protected Map<V, Icon> iconMap;
  protected Function<V, Shape> delegate;

  /**
   * Creates an instance with the specified delegate.
   *
   * @param delegate the vertex-to-shape function to use if no image is present for the vertex
   */
  public VertexIconShapeTransformer(Function<V, Shape> delegate) {
    this.delegate = delegate;
  }

  /** @return Returns the delegate. */
  public Function<V, Shape> getDelegate() {
    return delegate;
  }

  /** @param delegate The delegate to set. */
  public void setDelegate(Function<V, Shape> delegate) {
    this.delegate = delegate;
  }

  /**
   * get the shape from the image. If not available, get the shape from the delegate
   * VertexShapeFunction
   */
  public Shape apply(V v) {
    Icon icon = iconMap.get(v);
    if (icon != null && icon instanceof ImageIcon) {
      Image image = ((ImageIcon) icon).getImage();
      Shape shape = (Shape) shapeMap.get(image);
      if (shape == null) {
        shape = ImageShapeUtils.getShape(image, 30);
        if (shape.getBounds().getWidth() > 0 && shape.getBounds().getHeight() > 0) {
          // don't cache a zero-sized shape, wait for the image
          // to be ready
          int width = image.getWidth(null);
          int height = image.getHeight(null);
          AffineTransform transform = AffineTransform.getTranslateInstance(-width / 2, -height / 2);
          shape = transform.createTransformedShape(shape);
          shapeMap.put(image, shape);
        }
      }
      return shape;
    } else {
      return delegate.apply(v);
    }
  }

  /** @return the iconMap */
  public Map<V, Icon> getIconMap() {
    return iconMap;
  }

  /** @param iconMap the iconMap to set */
  public void setIconMap(Map<V, Icon> iconMap) {
    this.iconMap = iconMap;
  }

  /** @return the shapeMap */
  public Map<Image, Shape> getShapeMap() {
    return shapeMap;
  }

  /** @param shapeMap the shapeMap to set */
  public void setShapeMap(Map<Image, Shape> shapeMap) {
    this.shapeMap = shapeMap;
  }
}
