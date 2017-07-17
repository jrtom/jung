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

import java.io.Reader;
import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.google.common.graph.MutableNetwork;

import edu.uci.ics.jung.io.graphml.DummyEdge;
import edu.uci.ics.jung.io.graphml.DummyGraphObjectBase;
import edu.uci.ics.jung.io.graphml.DummyVertex;
import edu.uci.ics.jung.io.graphml.KeyMap;

public abstract class AbstractParserTest {

    
    private ElementParserRegistry<MutableNetwork<DummyVertex,DummyEdge>,DummyVertex,DummyEdge> registry;

    @Before
    public void setUp() throws Exception {
        registry = new ElementParserRegistry<MutableNetwork<DummyVertex,DummyEdge>,DummyVertex,DummyEdge>(
                new KeyMap(), 
                new DummyGraphObjectBase.UndirectedNetworkFactory(), 
                new DummyVertex.Factory(), 
                new DummyEdge.EdgeFactory()); 
//                new DummyEdge.HyperEdgeFactory());
    }

    @After
    public void tearDown() throws Exception {
        registry = null;
    }
    
    protected Object readObject(String xml) throws Exception {

        Reader fileReader = new StringReader(xml);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = factory.createXMLEventReader(fileReader);
        xmlEventReader = factory.createFilteredReader(xmlEventReader,
                new GraphMLEventFilter());
        
        try {        
            while( xmlEventReader.hasNext() ) {
                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    
                    StartElement start = event.asStartElement();
                    String name = start.getName().getLocalPart();
                    return registry.getParser(name).parse(xmlEventReader, start);
                }
            }
        } finally {
            xmlEventReader.close();
        }
        
        Assert.fail("failed to read object from XML: " + xml);
        return null;
    }
}
