/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io;

import com.google.common.collect.BiMap;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xml.sax.SAXException;

/**
 * @author Scott White
 * @author Tom Nelson - converted to jung2
 */
public class TestGraphMLReader extends TestCase {

  Supplier<MutableNetwork<Number, Number>> graphFactory;
  Supplier<Number> nodeFactory;
  Supplier<Number> edgeFactory;
  GraphMLReader<MutableNetwork<Number, Number>, Number, Number> gmlreader;

  public static Test suite() {
    return new TestSuite(TestGraphMLReader.class);
  }

  @Override
  protected void setUp() throws ParserConfigurationException, SAXException {
    graphFactory =
        new Supplier<MutableNetwork<Number, Number>>() {
          public MutableNetwork<Number, Number> get() {
            return NetworkBuilder.directed()
                .allowsSelfLoops(true)
                .allowsParallelEdges(true)
                .build();
          }
        };
    nodeFactory =
        new Supplier<Number>() {
          int n = 0;

          public Number get() {
            return n++;
          }
        };
    edgeFactory =
        new Supplier<Number>() {
          int n = 0;

          public Number get() {
            return n++;
          }
        };
    gmlreader =
        new GraphMLReader<MutableNetwork<Number, Number>, Number, Number>(nodeFactory, edgeFactory);
  }

  public void testLoad() throws IOException {
    String testFilename = "toy_graph.ml";

    Network<Number, Number> graph = loadGraph(testFilename);

    Assert.assertEquals(graph.nodes().size(), 3);
    Assert.assertEquals(graph.edges().size(), 3);

    BiMap<Number, String> node_ids = gmlreader.getNodeIDs();

    Number joe = node_ids.inverse().get("1");
    Number bob = node_ids.inverse().get("2");
    Number sue = node_ids.inverse().get("3");

    Assert.assertNotNull(joe);
    Assert.assertNotNull(bob);
    Assert.assertNotNull(sue);

    Map<String, GraphMLMetadata<Number>> node_metadata = gmlreader.getNodeMetadata();
    Function<Number, String> name = node_metadata.get("name").transformer;
    Assert.assertEquals(name.apply(joe), "Joe");
    Assert.assertEquals(name.apply(bob), "Bob");
    Assert.assertEquals(name.apply(sue), "Sue");

    Assert.assertTrue(graph.predecessors(joe).contains(bob));
    Assert.assertTrue(graph.predecessors(bob).contains(joe));
    Assert.assertTrue(graph.predecessors(sue).contains(joe));
    Assert.assertFalse(graph.predecessors(joe).contains(sue));
    Assert.assertFalse(graph.predecessors(sue).contains(bob));
    Assert.assertFalse(graph.predecessors(bob).contains(sue));

    File testFile = new File(testFilename);
    testFile.delete();
  }

  public void testAttributes() throws IOException {
    MutableNetwork<Number, Number> graph =
        NetworkBuilder.undirected().allowsSelfLoops(true).build();
    gmlreader.load("src/test/resources/edu/uci/ics/jung/io/graphml/attributes.graphml", graph);

    Assert.assertEquals(graph.nodes().size(), 6);
    Assert.assertEquals(graph.edges().size(), 7);

    // test node IDs
    BiMap<Number, String> node_ids = gmlreader.getNodeIDs();
    for (Map.Entry<Number, String> entry : node_ids.entrySet()) {
      Assert.assertEquals(entry.getValue().charAt(0), 'n');
      Assert.assertEquals(
          Integer.parseInt(entry.getValue().substring(1)), entry.getKey().intValue());
    }

    // test edge IDs
    BiMap<Number, String> edge_ids = gmlreader.getEdgeIDs();
    for (Map.Entry<Number, String> entry : edge_ids.entrySet()) {
      Assert.assertEquals(entry.getValue().charAt(0), 'e');
      Assert.assertEquals(
          Integer.parseInt(entry.getValue().substring(1)), entry.getKey().intValue());
    }

    // test data
    //        Map<String, SettableTransformer<Number, String>> node_data = gmlreader
    //                .getNodeData();
    //        Map<String, SettableTransformer<Number, String>> edge_data = gmlreader
    //                .getEdgeData();
    Map<String, GraphMLMetadata<Number>> node_metadata = gmlreader.getNodeMetadata();
    Map<String, GraphMLMetadata<Number>> edge_metadata = gmlreader.getEdgeMetadata();

    // test node colors
    //        Transformer<Number, String> node_color = node_data.get("d0");
    Function<Number, String> node_color = node_metadata.get("d0").transformer;
    Assert.assertEquals(node_color.apply(0), "green");
    Assert.assertEquals(node_color.apply(1), "yellow");
    Assert.assertEquals(node_color.apply(2), "blue");
    Assert.assertEquals(node_color.apply(3), "red");
    Assert.assertEquals(node_color.apply(4), "yellow");
    Assert.assertEquals(node_color.apply(5), "turquoise");

    // test edge weights
    //        Transformer<Number, String> edge_weight = edge_data.get("d1");
    Function<Number, String> edge_weight = edge_metadata.get("d1").transformer;
    Assert.assertEquals(edge_weight.apply(0), "1.0");
    Assert.assertEquals(edge_weight.apply(1), "1.0");
    Assert.assertEquals(edge_weight.apply(2), "2.0");
    Assert.assertEquals(edge_weight.apply(3), null);
    Assert.assertEquals(edge_weight.apply(4), null);
    Assert.assertEquals(edge_weight.apply(5), null);
    Assert.assertEquals(edge_weight.apply(6), "1.1");
  }

  private Network<Number, Number> loadGraph(String testFilename) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(testFilename));
    writer.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>\n");
    writer.write("<?meta name=\"GENERATOR\" content=\"XML::Smart 1.3.1\" ?>\n");
    writer.write("<graph edgedefault=\"directed\">\n");
    writer.write("<node id=\"1\" name=\"Joe\"/>\n");
    writer.write("<node id=\"2\" name=\"Bob\"/>\n");
    writer.write("<node id=\"3\" name=\"Sue\"/>\n");
    writer.write("<edge source=\"1\" target=\"2\"/>\n");
    writer.write("<edge source=\"2\" target=\"1\"/>\n");
    writer.write("<edge source=\"1\" target=\"3\"/>\n");
    writer.write("</graph>\n");
    writer.close();

    MutableNetwork<Number, Number> graph = graphFactory.get();
    gmlreader.load(testFilename, graph);
    return graph;
  }

  //    public void testSave() {
  //        String testFilename = "toy_graph.ml";
  //        Network<Number,Number> oldGraph = loadGraph(testFilename);
  ////        GraphMLFile<Number,Number> graphmlFile = new GraphMLFile();
  //        String newFilename = testFilename + "_save";
  //        gmlreader.save(oldGraph,newFilename);
  //		Network<Number,Number> newGraph = gmlreader.load(newFilename);
  //        Assert.assertEquals(oldGraph.nodes().size(),newGraph.nodes().size());
  //        Assert.assertEquals(oldGraph.edges().size(),newGraph.edges().size());
  //        File testFile = new File(testFilename);
  //        testFile.delete();
  //        File newFile = new File(newFilename);
  //        newFile.delete();
  //
  //
  //    }
}
