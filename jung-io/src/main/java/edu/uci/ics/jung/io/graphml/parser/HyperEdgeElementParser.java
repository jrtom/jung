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

import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.*;

/**
 * Parses hyper edge elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class HyperEdgeElementParser<G extends Hypergraph<V,E>,V,E> extends AbstractElementParser<G,V,E> {

    public HyperEdgeElementParser(ParserContext<G,V,E> parserContext) {
        super(parserContext);
    }
    
    public HyperEdgeMetadata parse(XMLEventReader xmlEventReader, StartElement start)
            throws GraphIOException {

        try {
            // Create the new edge.
            HyperEdgeMetadata edge = new HyperEdgeMetadata();

            // Parse the attributes.
            @SuppressWarnings("unchecked")
            Iterator<Attribute> iterator = start.getAttributes();
            while (iterator.hasNext()) {
                Attribute attribute = (Attribute) iterator.next();
                String name = attribute.getName().getLocalPart();
                String value = attribute.getValue();
                if (edge.getId() == null && GraphMLConstants.ID_NAME.equals(name)) {
                    edge.setId(value);
                } else {
                    edge.setProperty(name, value);
                }
            }

            while (xmlEventReader.hasNext()) {

                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = (StartElement) event;

                    String name = element.getName().getLocalPart();
                    if(GraphMLConstants.DESC_NAME.equals(name)) {
                        String desc = (String)getParser(name).parse(xmlEventReader, element);
                        edge.setDescription(desc);
                    } else if(GraphMLConstants.DATA_NAME.equals(name)) {
                        DataMetadata data = (DataMetadata)getParser(name).parse(xmlEventReader, element);
                        edge.addData(data);
                    } else if(GraphMLConstants.ENDPOINT_NAME.equals(name)) {
                        EndpointMetadata ep = (EndpointMetadata)getParser(name).parse(xmlEventReader, element);
                        edge.addEndpoint(ep);
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
            applyKeys(edge);
            
            return edge;
            
        } catch (Exception e) {
            ExceptionConverter.convert(e);
        }

        return null;
    }
}
