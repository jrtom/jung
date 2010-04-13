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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.*;
import edu.uci.ics.jung.io.graphml.GraphMetadata.EdgeDefault;

/**
 * Parses graph elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class GraphElementParser<G extends Hypergraph<V,E>,V,E> extends AbstractElementParser<G,V,E> {

    public GraphElementParser(ParserContext<G,V,E> parserContext) {
        super(parserContext);
    }

    @SuppressWarnings("unchecked")
    public GraphMetadata parse(XMLEventReader xmlEventReader, StartElement start)
            throws GraphIOException {

        try {
            // Create the new graph.
            GraphMetadata graphMetadata = new GraphMetadata();

            // Parse the attributes.
            Iterator iterator = start.getAttributes();
            while (iterator.hasNext()) {
                Attribute attribute = (Attribute) iterator.next();
                String name = attribute.getName().getLocalPart();
                String value = attribute.getValue();
                if (graphMetadata.getId() == null
                        && GraphMLConstants.ID_NAME.equals(name)) {
                    
                    graphMetadata.setId(value);
                } else if (graphMetadata.getEdgeDefault() == null
                        && GraphMLConstants.EDGEDEFAULT_NAME.equals(name)) {
                    
                    graphMetadata.setEdgeDefault(GraphMLConstants.DIRECTED_NAME
                            .equals(value) ? EdgeDefault.DIRECTED
                            : EdgeDefault.UNDIRECTED);
                } else {
                    graphMetadata.setProperty(name, value);
                }
            }

            // Make sure the graphdefault has been set.
            if (graphMetadata.getEdgeDefault() == null) {
                throw new GraphIOException(
                        "Element 'graph' is missing attribute 'edgedefault'");
            }
            
            Map<String, V> idToVertexMap = new HashMap<String, V>();
            Collection<EdgeMetadata> edgeMetadata = new LinkedList<EdgeMetadata>();
            Collection<HyperEdgeMetadata> hyperEdgeMetadata = new LinkedList<HyperEdgeMetadata>();

            while (xmlEventReader.hasNext()) {

                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = (StartElement) event;

                    String name = element.getName().getLocalPart();
                    if (GraphMLConstants.DESC_NAME.equals(name)) {
                        
                        // Parse the description and set it in the graph.
                        String desc = (String) getParser(name).parse(
                                xmlEventReader, element);
                        graphMetadata.setDescription(desc);
                        
                    } else if (GraphMLConstants.DATA_NAME.equals(name)) {
                        
                        // Parse the data element and store the property in the graph.
                        DataMetadata data = (DataMetadata) getParser(name).parse(
                                xmlEventReader, element);
                        graphMetadata.addData(data);
                        
                    } else if (GraphMLConstants.NODE_NAME.equals(name)) {
                        
                        // Parse the node metadata
                        NodeMetadata metadata = (NodeMetadata) getParser(name).parse(
                                xmlEventReader, element);
                        
                        // Create the vertex object and store it in the metadata
                        V vertex = getParserContext().createVertex(metadata);
                        metadata.setVertex(vertex);
                        idToVertexMap.put(metadata.getId(), vertex);
                        
                        // Add it to the graph
                        graphMetadata.addNodeMetadata(vertex, metadata);
                        
                    } else if (GraphMLConstants.EDGE_NAME.equals(name)) {
                        
                        // Parse the edge metadata
                        EdgeMetadata metadata = (EdgeMetadata) getParser(name).parse(
                                xmlEventReader, element);
                        
                        // Set the directed property if not overridden.
                        if (metadata.isDirected() == null) {
                            metadata.setDirected(graphMetadata.getEdgeDefault() == EdgeDefault.DIRECTED);
                        }
                        
                        // Create the edge object and store it in the metadata
                        E edge = getParserContext().createEdge(metadata);
                        edgeMetadata.add(metadata);
                        metadata.setEdge(edge);
                        
                        // Add it to the graph.
                        graphMetadata.addEdgeMetadata(edge, metadata);
                        
                    } else if (GraphMLConstants.HYPEREDGE_NAME.equals(name)) {
                        
                        // Parse the edge metadata
                        HyperEdgeMetadata metadata = (HyperEdgeMetadata) getParser(name).parse(
                                xmlEventReader, element);
                        
                        // Create the edge object and store it in the metadata
                        E edge = getParserContext().createHyperEdge(metadata);
                        hyperEdgeMetadata.add(metadata);
                        metadata.setEdge(edge);
                        
                        // Add it to the graph
                        graphMetadata.addHyperEdgeMetadata(edge, metadata);
                        
                    } else {

                        // Treat anything else as unknown
                        getUnknownParser().parse(xmlEventReader, element);
                    }

                }
                if (event.isEndElement()) {
                    EndElement end = (EndElement) event;
                    verifyMatch(start, end);
                    break;
                }
            }
            
            // Apply the keys to this object.
            applyKeys(graphMetadata);   
            
            // Create the graph object and store it in the metadata
            G graph = getParserContext().createGraph(graphMetadata);
            graphMetadata.setGraph(graph);
            
            // Add all of the vertices to the graph object.
            addVerticesToGraph(graph, idToVertexMap.values());
            
            // Add the edges to the graph object.
            addEdgesToGraph(graph, edgeMetadata, idToVertexMap);
            addHyperEdgesToGraph(graph, hyperEdgeMetadata, idToVertexMap);

            return graphMetadata;

        } catch (Exception e) {
            ExceptionConverter.convert(e);
        }

        return null;
    }
    
    private void addVerticesToGraph(G graph, Collection<V> vertices) {
        
        for (V vertex : vertices) {
            graph.addVertex(vertex);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void addEdgesToGraph(G graph, Collection<EdgeMetadata> metadata, 
            Map<String,V> idToVertexMap) throws GraphIOException {
        
        for (EdgeMetadata emd : metadata) {

            // Get the edge out of the metadata
            E edge = (E)emd.getEdge();
            
            // Get the verticies.
            V source = idToVertexMap.get(emd.getSource());
            V target = idToVertexMap.get(emd.getTarget());
            if (source == null || target == null) {
                throw new GraphIOException(
                        "edge references undefined source or target vertex. "
                                + "Source: " + emd.getSource()
                                + ", Target: " + emd.getTarget());
            }

            // Add it to the graph.
            if (graph instanceof Graph) {
                ((Graph<V, E>) graph).addEdge(edge, source, target, emd
                        .isDirected() ? EdgeType.DIRECTED
                        : EdgeType.UNDIRECTED);
            } else {
                graph.addEdge(edge, new Pair<V>(source, target));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void addHyperEdgesToGraph(G graph, Collection<HyperEdgeMetadata> metadata, 
            Map<String,V> idToVertexMap) throws GraphIOException {
        
        for (HyperEdgeMetadata emd : metadata) {

            // Get the edge out of the metadata
            E edge = (E)emd.getEdge();
            
            // Add the verticies to a list.
            List<V> verticies = new ArrayList<V>();
            List<EndpointMetadata> endpoints = emd.getEndpoints();
            for (EndpointMetadata ep : endpoints) {
                V v = idToVertexMap.get(ep.getNode());
                if (v == null) {
                    throw new GraphIOException(
                            "hyperedge references undefined vertex: "
                                    + ep.getNode());
                }
                verticies.add(v);
            }

            // Add it to the graph.
            graph.addEdge(edge, verticies);
        }
    }
}
