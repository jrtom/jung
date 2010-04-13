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

import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.Metadata;

/**
 * Base class for element parsers - provides some minimal functionality.
 * 
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public abstract class AbstractElementParser<G extends Hypergraph<V,E>,V,E> implements ElementParser {

    final private ParserContext<G,V,E> parserContext;
    protected AbstractElementParser(ParserContext<G,V,E> parserContext) {
        this.parserContext = parserContext;
    }
    
    public ParserContext<G,V,E> getParserContext() {
        return this.parserContext;
    }
    
    public ElementParser getParser(String localName) {
        return parserContext.getElementParserRegistry().getParser(localName);
    }
    
    public void applyKeys(Metadata metadata) {
        getParserContext().getKeyMap().applyKeys(metadata);
    }
    
    public ElementParser getUnknownParser() {
        return parserContext.getElementParserRegistry().getUnknownElementParser();
    }
    
    protected void verifyMatch(StartElement start, EndElement end)
            throws GraphIOException {

        String startName = start.getName().getLocalPart();
        String endName = end.getName().getLocalPart();
        if (!startName.equals(endName)) {
            throw new GraphIOException(
                    "Failed parsing document: Start/end tag mismatch! "
                            + "StartTag:" + startName + ", EndTag: "
                            + endName);
        }
    }
}
