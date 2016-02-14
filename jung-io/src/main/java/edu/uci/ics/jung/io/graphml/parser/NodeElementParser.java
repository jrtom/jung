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
 * Parses node elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class NodeElementParser<G extends Hypergraph<V,E>,V,E> extends AbstractElementParser<G,V,E> {

    public NodeElementParser(ParserContext<G,V,E> parserContext) {
        super(parserContext);
    }
    
    @SuppressWarnings("unchecked")
    public NodeMetadata parse(XMLEventReader xmlEventReader, StartElement start)
            throws GraphIOException {

        try {
            // Create the new node.
            NodeMetadata node = new NodeMetadata();

            // Parse the attributes.
            Iterator<Attribute> iterator = start.getAttributes();
            while (iterator.hasNext()) {
                Attribute attribute = iterator.next();
                String name = attribute.getName().getLocalPart();
                String value = attribute.getValue();
                if (node.getId() == null && GraphMLConstants.ID_NAME.equals(name)) {
                    node.setId(value);
                } else {
                    node.setProperty(name, value);
                }
            }

            // Make sure the name has been set.
            if (node.getId() == null) {
                throw new GraphIOException(
                        "Element 'node' is missing attribute 'id'");
            }

            while (xmlEventReader.hasNext()) {

                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = (StartElement) event;

                    String name = element.getName().getLocalPart();
                    if(GraphMLConstants.DESC_NAME.equals(name)) {
                        String desc = (String)getParser(name).parse(xmlEventReader, element);
                        node.setDescription(desc);
                    } else if(GraphMLConstants.DATA_NAME.equals(name)) {
                        DataMetadata data = (DataMetadata)getParser(name).parse(xmlEventReader, element);
                        node.addData(data);
                    } else if(GraphMLConstants.PORT_NAME.equals(name)) {
                        PortMetadata port = (PortMetadata)getParser(name).parse(xmlEventReader, element);
                        node.addPort(port);
                    } else {
                        
                        // Treat anything else as unknown
                        getUnknownParser().parse(xmlEventReader, element);
                    }

                } else if (event.isEndElement()) {
                    EndElement end = (EndElement) event;
                    verifyMatch(start, end);
                    break;
                }
            }
            
            // Apply the keys to this object.
            applyKeys(node);

            return node;
            
        } catch (Exception e) {
            ExceptionConverter.convert(e);
        }

        return null;
    }
}
