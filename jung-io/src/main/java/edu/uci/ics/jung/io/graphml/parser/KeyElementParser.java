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
import edu.uci.ics.jung.io.graphml.GraphMLConstants;
import edu.uci.ics.jung.io.graphml.Key;
import edu.uci.ics.jung.io.graphml.ExceptionConverter;

/**
 * Parses key elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class KeyElementParser<G extends Hypergraph<V,E>,V,E> extends AbstractElementParser<G,V,E> {

    public KeyElementParser(ParserContext<G,V,E> parserContext) {
        super(parserContext);
    }
    
    public Key parse(XMLEventReader xmlEventReader, StartElement start)
            throws GraphIOException {

        try {
            // Create the new key. ForType defaults to ALL.
            Key key = new Key();

            // Parse the attributes.
            @SuppressWarnings("unchecked")
            Iterator<Attribute> iterator = start.getAttributes();
            while (iterator.hasNext()) {
                Attribute attribute = iterator.next();
                String name = attribute.getName().getLocalPart();
                String value = attribute.getValue();
                if (key.getId() == null && GraphMLConstants.ID_NAME.equals(name)) {
                    key.setId(value);
                } else if (key.getAttributeName() == null
                        && GraphMLConstants.ATTRNAME_NAME.equals(name)) {
                    key.setAttributeName(value);
                } else if (key.getAttributeType() == null
                        && GraphMLConstants.ATTRTYPE_NAME.equals(name)) {
                    key.setAttributeType(value);
                } else if (GraphMLConstants.FOR_NAME.equals(name)) {
                    key.setForType(convertFor(value));
                }
            }

            // Make sure the id has been set.
            if (key.getId() == null) {
                throw new GraphIOException(
                        "Element 'key' is missing attribute 'id'");
            }

            while (xmlEventReader.hasNext()) {

                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = (StartElement) event;

                    String name = element.getName().getLocalPart();
                    if(GraphMLConstants.DESC_NAME.equals(name)) {
                        String desc = (String)getParser(name).parse(xmlEventReader, element);
                        key.setDescription(desc);
                    } else if(GraphMLConstants.DEFAULT_NAME.equals(name)) {
                        String defaultValue = (String)getParser(name).parse(xmlEventReader, element);
                        key.setDefaultValue(defaultValue);
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

            return key;
            
        } catch (Exception e) {
            ExceptionConverter.convert(e);
        }

        return null;
    }

    static public Key.ForType convertFor(String value) {

        if (value != null) {

            if (GraphMLConstants.GRAPH_NAME.equals(value)) {
                return Key.ForType.GRAPH;
            }
            if (GraphMLConstants.EDGE_NAME.equals(value)) {
                return Key.ForType.EDGE;
            }
            if (GraphMLConstants.ENDPOINT_NAME.equals(value)) {
                return Key.ForType.ENDPOINT;
            }
            if (GraphMLConstants.HYPEREDGE_NAME.equals(value)) {
                return Key.ForType.HYPEREDGE;
            }
            if (GraphMLConstants.NODE_NAME.equals(value)) {
                return Key.ForType.NODE;
            }
            if (GraphMLConstants.PORT_NAME.equals(value)) {
                return Key.ForType.PORT;
            }
        }

        return Key.ForType.ALL;
    }
}
