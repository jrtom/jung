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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import edu.uci.ics.jung.io.GraphIOException;

/**
 * Interface for all element parsers.  All parsers will be registered with the registry.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * 
 * @see ElementParserRegistry
 */
public interface ElementParser {
    Object parse(XMLEventReader xmlEventReader, StartElement start)
            throws GraphIOException;
}
