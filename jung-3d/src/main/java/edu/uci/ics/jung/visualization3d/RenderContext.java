/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization3d;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.util.Context;
import java.util.function.Function;
import javax.media.j3d.Appearance;
import javax.media.j3d.Node;

public interface RenderContext<N, E> {

  Function<E, Appearance> getEdgeAppearanceTransformer();

  void setEdgeAppearanceTransformer(Function<E, Appearance> edgeAppearanceTransformer);

  Function<Context<Graph<N>, E>, Node> getEdgeShapeTransformer();

  void setEdgeShapeTransformer(Function<Context<Graph<N>, E>, Node> edgeShapeTransformer);

  PickedState<E> getPickedEdgeState();

  void setPickedEdgeState(PickedState<E> pickedEdgeState);

  PickedState<N> getPickedVertexState();

  void setPickedVertexState(PickedState<N> pickedVertexState);

  Function<N, Appearance> getVertexAppearanceTransformer();

  void setVertexAppearanceTransformer(Function<N, Appearance> vertexAppearanceTransformer);

  Function<N, Node> getVertexShapeTransformer();

  void setVertexShapeTransformer(Function<N, Node> vertexShapeTransformer);

  Function<Object, String> getVertexStringer();

  void setVertexStringer(Function<Object, String> vertexStringer);
}
