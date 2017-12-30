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
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.util.LayoutEventSupport;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import java.awt.Dimension;
import javax.swing.event.ChangeListener;

/** */
public interface VisualizationModel<N, E> extends LayoutEventSupport<N>, ChangeEventSupport {

  enum SpatialSupport {
    RTREE,
    QUADTREE,
    GRID,
    NONE
  }
  /** @return the current layoutSize of the visualization's space */
  Dimension getLayoutSize();

  void setLayoutAlgorithm(LayoutAlgorithm<N> layoutAlgorithm);

  LayoutAlgorithm<N> getLayoutAlgorithm();

  LayoutModel<N> getLayoutModel();

  void setLayoutModel(LayoutModel<N> layoutModel);

  Network<N, E> getNetwork();

  void setNetwork(Network<N, E> network);

  void setNetwork(Network<N, E> network, boolean forceUpdate);

  //  Spatial<N> getSpatial();

  void addChangeListener(ChangeListener changeListener);
}
