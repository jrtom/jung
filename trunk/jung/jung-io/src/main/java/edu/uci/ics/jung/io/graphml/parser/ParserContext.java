/*
 * Copyright (c) 2008, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */

package edu.uci.ics.jung.io.graphml.parser;

import org.apache.commons.collections15.Transformer;

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
    private final Transformer<GraphMetadata, G> graphTransformer;
    private final Transformer<NodeMetadata, V> vertexTransformer;
    private final Transformer<EdgeMetadata, E> edgeTransformer;
    private final Transformer<HyperEdgeMetadata, E> hyperEdgeTransformer;
    
    public ParserContext(ElementParserRegistry<G,V,E> elementParserRegistry, 
            KeyMap keyMap,
            Transformer<GraphMetadata, G> graphTransformer,
            Transformer<NodeMetadata, V> vertexTransformer,
            Transformer<EdgeMetadata, E> edgeTransformer,                        
            Transformer<HyperEdgeMetadata, E> hyperEdgeTransformer ) {
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
        return graphTransformer.transform(metadata);
    }
    
    public V createVertex(NodeMetadata metadata) {
        return vertexTransformer.transform(metadata);
    }
    
    public E createEdge(EdgeMetadata metadata) {
        return edgeTransformer.transform(metadata);
    }
    
    public E createHyperEdge(HyperEdgeMetadata metadata) {
        return hyperEdgeTransformer.transform(metadata);
    }
}
