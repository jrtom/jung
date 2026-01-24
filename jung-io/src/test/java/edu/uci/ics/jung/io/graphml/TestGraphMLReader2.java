/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io.graphml;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import edu.uci.ics.jung.io.GraphIOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestGraphMLReader2 {
  static final String graphMLDocStart =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
          + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">";

  private GraphMLReader2<MutableNetwork<DummyNode, DummyEdge>, DummyNode, DummyEdge> reader;

  @After
  public void tearDown() throws Exception {
    if (reader != null) {
      reader.close();
    }
    reader = null;
  }

  @Test(expected = GraphIOException.class)
  public void testEmptyFile() throws Exception {

    String xml = "";
    readGraph(
        xml,
        new DummyGraphObjectBase.UndirectedNetworkFactory(),
        new DummyNode.Factory(),
        new DummyEdge.EdgeFactory());
  }

  @Test
  public void testBasics() throws Exception {

    String xml =
        graphMLDocStart
            + "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">"
            + "<default>yellow</default>"
            + "</key>"
            + "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>"
            + "<graph id=\"G\" edgedefault=\"undirected\">"
            + "<node id=\"n0\">"
            + "<data key=\"d0\">green</data>"
            + "</node>"
            + "<node id=\"n1\"/>"
            + "<node id=\"n2\">"
            + "<data key=\"d0\">blue</data>"
            + "</node>"
            + "<edge id=\"e0\" source=\"n0\" target=\"n2\">"
            + "<data key=\"d1\">1.0</data>"
            + "</edge>"
            + "</graph>"
            + "</graphml>";

    // Read the graph object.
    Network<DummyNode, DummyEdge> graph =
        readGraph(
            xml,
            new DummyGraphObjectBase.UndirectedNetworkFactory(),
            new DummyNode.Factory(),
            new DummyEdge.EdgeFactory());

    // Check out the graph.
    Assert.assertNotNull(graph);
    Assert.assertEquals(3, graph.nodes().size());
    Assert.assertEquals(1, graph.edges().size());

    // Check out metadata.
    Assert.assertEquals(1, reader.getGraphMLDocument().getGraphMetadata().size());
    List<EdgeMetadata> edges =
        new ArrayList<EdgeMetadata>(
            reader.getGraphMLDocument().getGraphMetadata().get(0).getEdgeMap().values());
    Assert.assertEquals(1, edges.size());
    Assert.assertEquals("n0", edges.get(0).getSource());
    Assert.assertEquals("n2", edges.get(0).getTarget());
  }

  @Test
  public void testData() throws Exception {

    String xml =
        graphMLDocStart
            + "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">"
            + "<default>yellow</default>"
            + "</key>"
            + "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>"
            + "<graph id=\"G\" edgedefault=\"undirected\">"
            + "<node id=\"n0\">"
            + "<data key=\"d0\">green</data>"
            + "</node>"
            + "<node id=\"n1\"/>"
            + "<node id=\"n2\">"
            + "<data key=\"d0\">blue</data>"
            + "</node>"
            + "<edge id=\"e0\" source=\"n0\" target=\"n2\">"
            + "<data key=\"d1\">1.0</data>"
            + "</edge>"
            + "</graph>"
            + "</graphml>";

    // Read the graph object.
    readGraph(
        xml,
        new DummyGraphObjectBase.UndirectedNetworkFactory(),
        new DummyNode.Factory(),
        new DummyEdge.EdgeFactory());

    // Check out metadata.
    Assert.assertEquals(1, reader.getGraphMLDocument().getGraphMetadata().size());
    List<EdgeMetadata> edges =
        new ArrayList<EdgeMetadata>(
            reader.getGraphMLDocument().getGraphMetadata().get(0).getEdgeMap().values());
    List<NodeMetadata> nodes =
        new ArrayList<NodeMetadata>(
            reader.getGraphMLDocument().getGraphMetadata().get(0).getNodeMap().values());
    Collections.sort(
        nodes,
        new Comparator<NodeMetadata>() {
          public int compare(NodeMetadata o1, NodeMetadata o2) {
            return o1.getId().compareTo(o2.getId());
          }
        });
    Assert.assertEquals(1, edges.size());
    Assert.assertEquals("1.0", edges.get(0).getProperties().get("d1"));
    Assert.assertEquals(3, nodes.size());
    Assert.assertEquals("green", nodes.get(0).getProperties().get("d0"));
    Assert.assertEquals("yellow", nodes.get(1).getProperties().get("d0"));
    Assert.assertEquals("blue", nodes.get(2).getProperties().get("d0"));
  }

  @Test(expected = GraphIOException.class)
  public void testEdgeWithInvalidNode() throws Exception {

    String xml =
        graphMLDocStart
            + "<key id=\"d0\" for=\"node\" attr.name=\"color\" attr.type=\"string\">"
            + "<default>yellow</default>"
            + "</key>"
            + "<key id=\"d1\" for=\"edge\" attr.name=\"weight\" attr.type=\"double\"/>"
            + "<graph id=\"G\" edgedefault=\"undirected\">"
            + "<node id=\"n0\">"
            + "<data key=\"d0\">green</data>"
            + "</node>"
            + "<node id=\"n1\"/>"
            + "<node id=\"n2\">"
            + "<data key=\"d0\">blue</data>"
            + "</node>"
            + "<edge id=\"e0\" source=\"n0\" target=\"n3\">"
            + // Invalid
            // node: n3
            "<data key=\"d1\">1.0</data>"
            + "</edge>"
            + "</graphml>";

    readGraph(
        xml,
        new DummyGraphObjectBase.UndirectedNetworkFactory(),
        new DummyNode.Factory(),
        new DummyEdge.EdgeFactory());
  }

  @Test
  public void testAttributesFile() throws Exception {

    // Read the graph object.
    Network<DummyNode, DummyEdge> graph =
        readGraphFromFile(
            "attributes.graphml",
            new DummyGraphObjectBase.UndirectedNetworkFactory(),
            new DummyNode.Factory(),
            new DummyEdge.EdgeFactory());

    Assert.assertEquals(6, graph.nodes().size());
    Assert.assertEquals(7, graph.edges().size());

    Assert.assertEquals(1, reader.getGraphMLDocument().getGraphMetadata().size());

    // Test node ids
    int id = 0;
    List<NodeMetadata> nodes =
        new ArrayList<NodeMetadata>(
            reader.getGraphMLDocument().getGraphMetadata().get(0).getNodeMap().values());
    Collections.sort(
        nodes,
        new Comparator<NodeMetadata>() {
          public int compare(NodeMetadata o1, NodeMetadata o2) {
            return o1.getId().compareTo(o2.getId());
          }
        });
    Assert.assertEquals(6, nodes.size());
    for (NodeMetadata md : nodes) {
      Assert.assertEquals('n', md.getId().charAt(0));
      Assert.assertEquals(id++, Integer.parseInt(md.getId().substring(1)));
    }

    // Test edge ids
    id = 0;
    List<EdgeMetadata> edges =
        new ArrayList<EdgeMetadata>(
            reader.getGraphMLDocument().getGraphMetadata().get(0).getEdgeMap().values());
    Collections.sort(
        edges,
        new Comparator<EdgeMetadata>() {
          public int compare(EdgeMetadata o1, EdgeMetadata o2) {
            return o1.getId().compareTo(o2.getId());
          }
        });
    Assert.assertEquals(7, edges.size());
    for (EdgeMetadata md : edges) {
      Assert.assertEquals('e', md.getId().charAt(0));
      Assert.assertEquals(id++, Integer.parseInt(md.getId().substring(1)));
    }

    Assert.assertEquals("green", nodes.get(0).getProperties().get("d0"));
    Assert.assertEquals("yellow", nodes.get(1).getProperties().get("d0"));
    Assert.assertEquals("blue", nodes.get(2).getProperties().get("d0"));
    Assert.assertEquals("red", nodes.get(3).getProperties().get("d0"));
    Assert.assertEquals("yellow", nodes.get(4).getProperties().get("d0"));
    Assert.assertEquals("turquoise", nodes.get(5).getProperties().get("d0"));

    Assert.assertEquals("1.0", edges.get(0).getProperties().get("d1"));
    Assert.assertEquals("1.0", edges.get(1).getProperties().get("d1"));
    Assert.assertEquals("2.0", edges.get(2).getProperties().get("d1"));
    Assert.assertEquals(null, edges.get(3).getProperties().get("d1"));
    Assert.assertEquals(null, edges.get(4).getProperties().get("d1"));
    Assert.assertEquals(null, edges.get(5).getProperties().get("d1"));
    Assert.assertEquals("1.1", edges.get(6).getProperties().get("d1"));
  }

  private Network<DummyNode, DummyEdge> readGraph(
      String xml,
      Function<GraphMetadata, MutableNetwork<DummyNode, DummyEdge>> gf,
      DummyNode.Factory nf,
      DummyEdge.EdgeFactory ef)
      throws GraphIOException {
    Reader fileReader = new StringReader(xml);
    reader =
        new GraphMLReader2<MutableNetwork<DummyNode, DummyEdge>, DummyNode, DummyEdge>(
            fileReader, gf, nf, ef);

    return reader.readGraph();
  }

  private Network<DummyNode, DummyEdge> readGraphFromFile(
      String file,
      Function<GraphMetadata, MutableNetwork<DummyNode, DummyEdge>> gf,
      DummyNode.Factory nf,
      DummyEdge.EdgeFactory ef)
      throws Exception {
    InputStream is = getClass().getResourceAsStream(file);
    Reader fileReader = new InputStreamReader(is);
    reader =
        new GraphMLReader2<MutableNetwork<DummyNode, DummyEdge>, DummyNode, DummyEdge>(
            fileReader, gf, nf, ef);

    return reader.readGraph();
  }
}
