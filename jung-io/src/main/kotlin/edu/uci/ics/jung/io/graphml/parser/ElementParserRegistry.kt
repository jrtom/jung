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
import edu.uci.ics.jung.io.graphml.GraphMLConstants
import edu.uci.ics.jung.io.graphml.GraphMetadata
import edu.uci.ics.jung.io.graphml.KeyMap
import edu.uci.ics.jung.io.graphml.NodeMetadata
import java.util.function.Function

/**
 * Registry for all element parsers.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class ElementParserRegistry<G : MutableNetwork<N, E>, N, E>(
  keyMap: KeyMap,
  graphTransformer: Function<GraphMetadata, G>,
  nodeTransformer: Function<NodeMetadata, N>,
  edgeTransformer: Function<EdgeMetadata, E>
) {

  private val parserMap: Map<String, ElementParser>

  val unknownElementParser: ElementParser = UnknownElementParser()

  init {
    // Create the parser context.
    val context = ParserContext(this, keyMap, graphTransformer, nodeTransformer, edgeTransformer)

    parserMap = mapOf(
      GraphMLConstants.DEFAULT_NAME to StringElementParser<G, N, E>(context),
      GraphMLConstants.DESC_NAME to StringElementParser<G, N, E>(context),
      GraphMLConstants.KEY_NAME to KeyElementParser<G, N, E>(context),
      GraphMLConstants.DATA_NAME to DataElementParser<G, N, E>(context),
      GraphMLConstants.PORT_NAME to PortElementParser<G, N, E>(context),
      GraphMLConstants.NODE_NAME to NodeElementParser<G, N, E>(context),
      GraphMLConstants.GRAPH_NAME to GraphElementParser<G, N, E>(context),
      GraphMLConstants.ENDPOINT_NAME to EndpointElementParser<G, N, E>(context),
      GraphMLConstants.EDGE_NAME to EdgeElementParser<G, N, E>(context)
    )
  }

  fun getParser(localName: String): ElementParser =
    parserMap[localName] ?: unknownElementParser
}
