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
import edu.uci.ics.jung.io.graphml.PortMetadata;
import java.util.Iterator;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Parses port elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class PortElementParser<G extends MutableNetwork<N, E>, N, E>
    extends AbstractElementParser<G, N, E> {

  public PortElementParser(ParserContext<G, N, E> parserContext) {
    super(parserContext);
  }

  public PortMetadata parse(XMLEventReader xmlEventReader, StartElement start)
      throws GraphIOException {

    try {

      // Create the new port.
      PortMetadata port = new PortMetadata();

      // Parse the attributes.
      @SuppressWarnings("unchecked")
      Iterator<Attribute> iterator = start.getAttributes();
      while (iterator.hasNext()) {
        Attribute attribute = iterator.next();
        String name = attribute.getName().getLocalPart();
        String value = attribute.getValue();
        if (port.getName() == null && GraphMLConstants.NAME_NAME.equals(name)) {
          port.setName(value);
        } else {
          port.setProperty(name, value);
        }
      }

      // Make sure the name has been set.
      if (port.getName() == null) {
        throw new GraphIOException("Element 'port' is missing attribute 'name'");
      }

      while (xmlEventReader.hasNext()) {

        XMLEvent event = xmlEventReader.nextEvent();
        if (event.isStartElement()) {
          StartElement element = (StartElement) event;

          String name = element.getName().getLocalPart();
          if (GraphMLConstants.DESC_NAME.equals(name)) {
            String desc = (String) getParser(name).parse(xmlEventReader, element);
            port.setDescription(desc);
          } else if (GraphMLConstants.DATA_NAME.equals(name)) {
            DataMetadata data = (DataMetadata) getParser(name).parse(xmlEventReader, element);
            port.addData(data);
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

      // Apply the keys to this port.
      applyKeys(port);

      return port;

    } catch (Exception e) {
      ExceptionConverter.convert(e);
    }

    return null;
  }
}
