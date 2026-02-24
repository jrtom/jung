/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io.graphml.parser

import com.google.common.graph.MutableNetwork
import edu.uci.ics.jung.io.graphml.EdgeMetadata
import edu.uci.ics.jung.io.graphml.GraphMetadata
import edu.uci.ics.jung.io.graphml.KeyMap
import edu.uci.ics.jung.io.graphml.NodeMetadata
import java.util.function.Function

/**
 * Provides resources related to the current parsing context.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * @param G The graph type
 * @param N The node type
 * @param E The edge type
 */
class ParserContext<G : MutableNetwork<N, E>, N, E>(
  val elementParserRegistry: ElementParserRegistry<G, N, E>,
  val keyMap: KeyMap,
  private val graphTransformer: Function<GraphMetadata, G>,
  private val nodeTransformer: Function<NodeMetadata, N>,
  private val edgeTransformer: Function<EdgeMetadata, E>
) {

  fun createGraph(metadata: GraphMetadata): G = graphTransformer.apply(metadata)

  fun createNode(metadata: NodeMetadata): N = nodeTransformer.apply(metadata)

  fun createEdge(metadata: EdgeMetadata): E = edgeTransformer.apply(metadata)
}
