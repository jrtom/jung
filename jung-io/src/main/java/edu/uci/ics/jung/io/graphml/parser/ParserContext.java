/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io.graphml.parser;

import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.KeyMap;
import edu.uci.ics.jung.io.graphml.NodeMetadata;
import java.util.function.Function;

/**
 * Provides resources related to the current parsing context.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * @param <G> The graph type
 * @param <N> The node type
 * @param <E> The edge type
 */
public class ParserContext<G extends MutableNetwork<N, E>, N, E> {

  private final KeyMap keyMap;
  private final ElementParserRegistry<G, N, E> elementParserRegistry;
  private final Function<GraphMetadata, G> graphTransformer;
  private final Function<NodeMetadata, N> nodeTransformer;
  private final Function<EdgeMetadata, E> edgeTransformer;
  //    private final Function<HyperEdgeMetadata, E> hyperEdgeTransformer;

  public ParserContext(
      ElementParserRegistry<G, N, E> elementParserRegistry,
      KeyMap keyMap,
      Function<GraphMetadata, G> graphTransformer,
      Function<NodeMetadata, N> nodeTransformer,
      Function<EdgeMetadata, E> edgeTransformer) {
    //            Function<HyperEdgeMetadata, E> hyperEdgeTransformer ) {
    this.elementParserRegistry = elementParserRegistry;
    this.keyMap = keyMap;
    this.graphTransformer = graphTransformer;
    this.nodeTransformer = nodeTransformer;
    this.edgeTransformer = edgeTransformer;
    //        this.hyperEdgeTransformer = hyperEdgeTransformer;
  }

  public ElementParserRegistry<G, N, E> getElementParserRegistry() {
    return elementParserRegistry;
  }

  public KeyMap getKeyMap() {
    return keyMap;
  }

  public G createGraph(GraphMetadata metadata) {
    return graphTransformer.apply(metadata);
  }

  public N createNode(NodeMetadata metadata) {
    return nodeTransformer.apply(metadata);
  }

  public E createEdge(EdgeMetadata metadata) {
    return edgeTransformer.apply(metadata);
  }

  //    public E createHyperEdge(HyperEdgeMetadata metadata) {
  //        return hyperEdgeTransformer.apply(metadata);
  //    }
}
