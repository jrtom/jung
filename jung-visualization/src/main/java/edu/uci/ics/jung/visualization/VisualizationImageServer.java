/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that could be used on the server side of a thin-client application. It creates the jung
 * visualization, then produces an image of it.
 *
 * @author tom
 * @param <N> the node type
 * @param <E> the edge type
 */
@SuppressWarnings("serial")
public class VisualizationImageServer<N, E> extends BasicVisualizationServer<N, E> {

  Map<RenderingHints.Key, Object> renderingHints = new HashMap<RenderingHints.Key, Object>();

  /**
   * Creates a new instance with the specified layout and preferred layoutSize.
   *
   * @param layoutAlgorithm the Layout instance; provides the node locations
   * @param preferredSize the preferred layoutSize of the image
   */
  public VisualizationImageServer(
      Network<N, E> network, LayoutAlgorithm<N> layoutAlgorithm, Dimension preferredSize) {
    super(network, layoutAlgorithm, preferredSize);
    setSize(preferredSize);
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    addNotify();
  }

  public Image getImage(Point2D center, Dimension d) {
    int width = getWidth();
    int height = getHeight();

    float scalex = (float) width / d.width;
    float scaley = (float) height / d.height;
    try {
      renderContext
          .getMultiLayerTransformer()
          .getTransformer(MultiLayerTransformer.Layer.VIEW)
          .scale(scalex, scaley, center);

      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics = bi.createGraphics();
      graphics.setRenderingHints(renderingHints);
      paint(graphics);
      graphics.dispose();
      return bi;
    } finally {
      renderContext
          .getMultiLayerTransformer()
          .getTransformer(MultiLayerTransformer.Layer.VIEW)
          .setToIdentity();
    }
  }
}
