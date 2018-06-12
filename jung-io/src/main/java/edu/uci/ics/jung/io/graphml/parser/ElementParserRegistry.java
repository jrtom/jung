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
import edu.uci.ics.jung.io.graphml.GraphMLConstants;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.KeyMap;
import edu.uci.ics.jung.io.graphml.NodeMetadata;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry for all element parsers.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class ElementParserRegistry<G extends MutableNetwork<N, E>, N, E> {

  private final Map<String, ElementParser> parserMap = new HashMap<String, ElementParser>();

  private final ElementParser unknownElementParser = new UnknownElementParser();

  public ElementParserRegistry(
      KeyMap keyMap,
      Function<GraphMetadata, G> graphTransformer,
      Function<NodeMetadata, N> nodeTransformer,
      Function<EdgeMetadata, E> edgeTransformer) {
    //            Function<HyperEdgeMetadata, E> hyperEdgeTransformer) {

    // Create the parser context.
    ParserContext<G, N, E> context =
        new ParserContext<G, N, E>(
            this, keyMap, graphTransformer, nodeTransformer, edgeTransformer);

    parserMap.put(GraphMLConstants.DEFAULT_NAME, new StringElementParser<G, N, E>(context));
    parserMap.put(GraphMLConstants.DESC_NAME, new StringElementParser<G, N, E>(context));
    parserMap.put(GraphMLConstants.KEY_NAME, new KeyElementParser<G, N, E>(context));
    parserMap.put(GraphMLConstants.DATA_NAME, new DataElementParser<G, N, E>(context));
    parserMap.put(GraphMLConstants.PORT_NAME, new PortElementParser<G, N, E>(context));
    parserMap.put(GraphMLConstants.NODE_NAME, new NodeElementParser<G, N, E>(context));
    parserMap.put(GraphMLConstants.GRAPH_NAME, new GraphElementParser<G, N, E>(context));
    parserMap.put(GraphMLConstants.ENDPOINT_NAME, new EndpointElementParser<G, N, E>(context));
    parserMap.put(GraphMLConstants.EDGE_NAME, new EdgeElementParser<G, N, E>(context));
    // TODO: restore this once we have a Hypergraph type again
    //        parserMap.put(GraphMLConstants.HYPEREDGE_NAME, new
    // HyperEdgeElementParser<G,V,E>(context));
  }

  public ElementParser getUnknownElementParser() {
    return unknownElementParser;
  }

  public ElementParser getParser(String localName) {
    ElementParser parser = parserMap.get(localName);
    if (parser == null) {
      parser = unknownElementParser;
    }

    return parser;
  }
}
