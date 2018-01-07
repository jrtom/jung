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
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.ExceptionConverter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Parses an element that just contains text.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class StringElementParser<G extends MutableNetwork<N, E>, N, E>
    extends AbstractElementParser<G, N, E> {

  public StringElementParser(ParserContext<G, N, E> parserContext) {
    super(parserContext);
  }

  public String parse(XMLEventReader xmlEventReader, StartElement start) throws GraphIOException {

    try {
      String str = null;

      while (xmlEventReader.hasNext()) {

        XMLEvent event = xmlEventReader.nextEvent();
        if (event.isStartElement()) {

          // Parse the unknown element.
          getUnknownParser().parse(xmlEventReader, event.asStartElement());
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
