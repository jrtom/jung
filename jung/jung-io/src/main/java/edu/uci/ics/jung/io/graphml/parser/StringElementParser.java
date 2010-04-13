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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.ExceptionConverter;

/**
 * Parses an element that just contains text.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class StringElementParser<G extends Hypergraph<V,E>,V,E> extends AbstractElementParser<G,V,E> {

    public StringElementParser(ParserContext<G,V,E> parserContext) {
        super(parserContext);
    }
    
    public String parse(XMLEventReader xmlEventReader, StartElement start)
            throws GraphIOException {

        try {
            String str = null;

            while (xmlEventReader.hasNext()) {

                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {

                    // Parse the unknown element.
                    getUnknownParser().parse(xmlEventReader, event
                            .asStartElement());
                } else if (event.isEndElement()) {
                    EndElement end = (EndElement) event;
                    verifyMatch(start, end);
                    break;
                } else if (event.isCharacters()) {
                    Characters characters = (Characters) event;
                    str = characters.getData();
                }
            }

            return str;

        } catch (Exception e) {
            ExceptionConverter.convert(e);
        }

        return null;
    }
}
