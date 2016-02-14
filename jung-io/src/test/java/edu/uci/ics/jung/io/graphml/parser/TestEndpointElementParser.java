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

import org.junit.Assert;
import org.junit.Test;

import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.EndpointMetadata;
import edu.uci.ics.jung.io.graphml.EndpointMetadata.EndpointType;

public class TestEndpointElementParser extends AbstractParserTest {

    @Test(expected= GraphIOException.class)
    public void testNoNode() throws Exception {
        
        String xml = 
            "<endpoint/>";
        
        readObject(xml);
    }

    @Test
    public void testId() throws Exception {
        
        String xml = 
            "<endpoint node=\"1\" id=\"ep1\"/>";
        
        EndpointMetadata ep = (EndpointMetadata) readObject(xml);
        Assert.assertNotNull(ep);
        Assert.assertEquals("1", ep.getNode());
        Assert.assertEquals("ep1", ep.getId());
        Assert.assertEquals(null, ep.getDescription());        
        Assert.assertEquals(null, ep.getPort());
        Assert.assertEquals(EndpointType.UNDIR, ep.getEndpointType());
    }

    @Test
    public void testDesc() throws Exception {
        
        String xml = 
            "<endpoint node=\"1\" id=\"ep1\">" +
                "<desc>hello world</desc>" +
            "</endpoint>";
        
        EndpointMetadata ep = (EndpointMetadata) readObject(xml);
        Assert.assertNotNull(ep);
        Assert.assertEquals("1", ep.getNode());
        Assert.assertEquals("ep1", ep.getId());
        Assert.assertEquals("hello world", ep.getDescription());        
        Assert.assertEquals(null, ep.getPort());
        Assert.assertEquals(EndpointType.UNDIR, ep.getEndpointType());
    }

    @Test
    public void testPort() throws Exception {
        
        String xml = 
            "<endpoint node=\"1\" port=\"abc123\" id=\"ep1\">" +
            "</endpoint>";
        
        EndpointMetadata ep = (EndpointMetadata) readObject(xml);
        Assert.assertNotNull(ep);
        Assert.assertEquals("1", ep.getNode());
        Assert.assertEquals("ep1", ep.getId());
        Assert.assertEquals(null, ep.getDescription());        
        Assert.assertEquals("abc123", ep.getPort());
        Assert.assertEquals(EndpointType.UNDIR, ep.getEndpointType());
    }

    @Test
    public void testTypeIn() throws Exception {
        
        String xml = 
            "<endpoint node=\"1\" type=\"in\">" +
            "</endpoint>";
        
        EndpointMetadata ep = (EndpointMetadata) readObject(xml);
        Assert.assertNotNull(ep);
        Assert.assertEquals("1", ep.getNode());
        Assert.assertEquals(null, ep.getId());
        Assert.assertEquals(null, ep.getDescription());        
        Assert.assertEquals(null, ep.getPort());
        Assert.assertEquals(EndpointType.IN, ep.getEndpointType());
    }

    @Test
    public void testTypeOut() throws Exception {
        
        String xml = 
            "<endpoint node=\"1\" type=\"out\">" +
            "</endpoint>";
        
        EndpointMetadata ep = (EndpointMetadata) readObject(xml);
        Assert.assertNotNull(ep);
        Assert.assertEquals("1", ep.getNode());
        Assert.assertEquals(null, ep.getId());
        Assert.assertEquals(null, ep.getDescription());        
        Assert.assertEquals(null, ep.getPort());
        Assert.assertEquals(EndpointType.OUT, ep.getEndpointType());
    }

    @Test
    public void testTypeUndir() throws Exception {
        
        String xml = 
            "<endpoint node=\"1\" type=\"undir\">" +
            "</endpoint>";
        
        EndpointMetadata ep = (EndpointMetadata) readObject(xml);
        Assert.assertNotNull(ep);
        Assert.assertEquals("1", ep.getNode());
        Assert.assertEquals(null, ep.getId());
        Assert.assertEquals(null, ep.getDescription());        
        Assert.assertEquals(null, ep.getPort());
        Assert.assertEquals(EndpointType.UNDIR, ep.getEndpointType());
    }

    @Test
    public void testTypeInvalid() throws Exception {
        
        String xml = 
            "<endpoint node=\"1\" type=\"blaa\">" +
            "</endpoint>";
        
        EndpointMetadata ep = (EndpointMetadata) readObject(xml);
        Assert.assertNotNull(ep);
        Assert.assertEquals("1", ep.getNode());
        Assert.assertEquals(null, ep.getId());
        Assert.assertEquals(null, ep.getDescription());        
        Assert.assertEquals(null, ep.getPort());
        Assert.assertEquals(EndpointType.UNDIR, ep.getEndpointType());
    }
}
