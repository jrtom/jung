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

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.KeyMap;
import edu.uci.ics.jung.io.graphml.NodeMetadata;

/**
 * Provides resources related to the current parsing context. 
 * 
 * @author Nathan Mittler - nathan.mittler@gmail.com
 *
 * @param <G> The graph type
 * @param <V> The vertex type
 * @param <E> The edge type
 */
public class ParserContext<G extends Hypergraph<V, E>, V, E> {

    private final KeyMap keyMap;
    private final ElementParserRegistry<G,V,E> elementParserRegistry;
    private final Function<GraphMetadata, G> graphTransformer;
    private final Function<NodeMetadata, V> vertexTransformer;
    private final Function<EdgeMetadata, E> edgeTransformer;
    private final Function<HyperEdgeMetadata, E> hyperEdgeTransformer;
    
    public ParserContext(ElementParserRegistry<G,V,E> elementParserRegistry, 
            KeyMap keyMap,
            Function<GraphMetadata, G> graphTransformer,
            Function<NodeMetadata, V> vertexTransformer,
            Function<EdgeMetadata, E> edgeTransformer,                        
            Function<HyperEdgeMetadata, E> hyperEdgeTransformer ) {
        this.elementParserRegistry = elementParserRegistry;
        this.keyMap = keyMap;
        this.graphTransformer = graphTransformer;
        this.vertexTransformer = vertexTransformer;
        this.edgeTransformer = edgeTransformer;                
        this.hyperEdgeTransformer = hyperEdgeTransformer;                
    }

    public ElementParserRegistry<G,V,E> getElementParserRegistry() {
        return elementParserRegistry;
    }
    
    public KeyMap getKeyMap() {
        return keyMap;
    }
    
    public G createGraph(GraphMetadata metadata) {
        return graphTransformer.apply(metadata);
    }
    
    public V createVertex(NodeMetadata metadata) {
        return vertexTransformer.apply(metadata);
    }
    
    public E createEdge(EdgeMetadata metadata) {
        return edgeTransformer.apply(metadata);
    }
    
    public E createHyperEdge(HyperEdgeMetadata metadata) {
        return hyperEdgeTransformer.apply(metadata);
    }
}
