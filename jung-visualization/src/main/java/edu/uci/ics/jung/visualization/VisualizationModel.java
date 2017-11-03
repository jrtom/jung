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
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithm;
import edu.uci.ics.jung.visualization.layout.LayoutModel;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import java.awt.Dimension;
import javax.swing.event.ChangeListener;

/** */
public interface VisualizationModel<N, E, P> { //extends LayoutModel<N,P> {

  Dimension DEFAULT_SIZE = new Dimension(600, 600);

  /** @param d the space to use to lay out this graph */
  void setLayoutSize(Dimension d);

  /** @return the current layoutSize of the visualization's space */
  Dimension getLayoutSize();

  void setLayoutAlgorithm(LayoutAlgorithm<N, P> layoutAlgorithm);

  LayoutAlgorithm<N, P> getLayoutAlgorithm();

  LayoutModel<N, P> getLayoutModel();

  void setLayoutModel(LayoutModel<N, P> layoutModel);

  Network<N, E> getNetwork();

  void setNetwork(Network<N, E> network);

  //  void setNetwork(Network<N, E> network, boolean forceUpdate);

  Spatial<N> getSpatial();

  void addChangeListener(ChangeListener changeListener);
}
