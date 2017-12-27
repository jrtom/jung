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
import edu.uci.ics.jung.io.graphml.DataMetadata;
import edu.uci.ics.jung.io.graphml.ExceptionConverter;
import edu.uci.ics.jung.io.graphml.GraphMLConstants;
import java.util.Iterator;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Parses the data element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class DataElementParser<G extends MutableNetwork<N, E>, N, E>
    extends AbstractElementParser<G, N, E> {

  public DataElementParser(ParserContext<G, N, E> parserContext) {
    super(parserContext);
  }

  public DataMetadata parse(XMLEventReader xmlEventReader, StartElement start)
      throws GraphIOException {

    try {
      // Create the new port.
      DataMetadata data = new DataMetadata();

      // Parse the attributes.
      @SuppressWarnings("unchecked")
      Iterator<Attribute> iterator = start.getAttributes();
      while (iterator.hasNext()) {
        Attribute attribute = (Attribute) iterator.next();
        String name = attribute.getName().getLocalPart();
        String value = attribute.getValue();
        if (data.getKey() == null && GraphMLConstants.KEY_NAME.equals(name)) {
          data.setKey(value);
        }
      }

      // Make sure the key has been set.
      if (data.getKey() == null) {
        throw new GraphIOException("Element 'data' is missing attribute 'key'");
      }

      while (xmlEventReader.hasNext()) {

        XMLEvent event = xmlEventReader.nextEvent();
        if (event.isStartElement()) {
          StartElement element = (StartElement) event;

          // Treat any child elements as unknown
          getUnknownParser().parse(xmlEventReader, element);
        }
        if (event.isCharacters()) {
          Characters characters = (Characters) event;
          data.setValue(characters.getData());
        }
        if (event.isEndElement()) {
          EndElement end = (EndElement) event;
          verifyMatch(start, end);
          break;
        }
      }

      return data;

    } catch (Exception e) {
      ExceptionConverter.convert(e);
    }

    return null;
  }
}
