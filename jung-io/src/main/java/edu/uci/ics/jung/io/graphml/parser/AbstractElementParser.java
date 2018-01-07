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
import edu.uci.ics.jung.io.graphml.Metadata;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

/**
 * Base class for element parsers - provides some minimal functionality.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public abstract class AbstractElementParser<G extends MutableNetwork<N, E>, N, E>
    implements ElementParser {

  private final ParserContext<G, N, E> parserContext;

  protected AbstractElementParser(ParserContext<G, N, E> parserContext) {
    this.parserContext = parserContext;
  }

  public ParserContext<G, N, E> getParserContext() {
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

  protected void verifyMatch(StartElement start, EndElement end) throws GraphIOException {

    String startName = start.getName().getLocalPart();
    String endName = end.getName().getLocalPart();
    if (!startName.equals(endName)) {
      throw new GraphIOException(
          "Failed parsing document: Start/end tag mismatch! "
              + "StartTag:"
              + startName
              + ", EndTag: "
              + endName);
    }
  }
}
