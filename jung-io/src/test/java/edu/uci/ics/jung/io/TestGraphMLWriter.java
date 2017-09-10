/*
 * Created on Jun 22, 2008
 *
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.graph.util.TestGraphs;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

public class TestGraphMLWriter extends TestCase {
  public void testBasicWrite() throws IOException, ParserConfigurationException, SAXException {
    Network<String, Number> g = TestGraphs.createTestGraph(true);
    GraphMLWriter<String, Number> gmlw = new GraphMLWriter<String, Number>();
    Function<Number, String> edge_weight =
        new Function<Number, String>() {
          public String apply(Number n) {
            return String.valueOf(n.intValue());
          }
        };

    Function<String, String> vertex_name = Functions.identity();
    //TransformerUtils.nopTransformer();

    gmlw.addEdgeData("weight", "integer value for the edge", Integer.toString(-1), edge_weight);
    gmlw.addVertexData("name", "identifier for the vertex", null, vertex_name);
    gmlw.setEdgeIDs(edge_weight);
    gmlw.setVertexIDs(vertex_name);
    gmlw.save(g, new FileWriter("src/test/resources/testbasicwrite.graphml"));

    // TODO: now read it back in and compare the graph connectivity
    // and other metadata with what's in TestGraphs.pairs[], etc.
    GraphMLReader<MutableNetwork<String, Object>, String, Object> gmlr =
        new GraphMLReader<MutableNetwork<String, Object>, String, Object>();
    MutableNetwork<String, Object> g2 = NetworkBuilder.directed().allowsSelfLoops(true).build();
    gmlr.load("src/test/resources/testbasicwrite.graphml", g2);
    Map<String, GraphMLMetadata<Object>> edge_metadata = gmlr.getEdgeMetadata();
    Function<Object, String> edge_weight2 = edge_metadata.get("weight").transformer;
    validateTopology(g, g2, edge_weight, edge_weight2);

    File f = new File("src/test/resources/testbasicwrite.graphml");
    f.delete();
  }

  public <T extends Comparable<T>> void validateTopology(
      Network<T, Number> g,
      Network<T, Object> g2,
      Function<Number, String> edge_weight,
      Function<Object, String> edge_weight2) {
    Assert.assertEquals(g2.edges().size(), g.edges().size());
    List<T> g_vertices = new ArrayList<T>(g.nodes());
    List<T> g2_vertices = new ArrayList<T>(g2.nodes());
    Collections.sort(g_vertices);
    Collections.sort(g2_vertices);
    Assert.assertEquals(g_vertices, g2_vertices);

    Set<String> g_edges = new HashSet<String>();
    for (Number n : g.edges()) {
      g_edges.add(String.valueOf(n));
    }
    Set<Object> g2_edges = new HashSet<Object>(g2.edges());
    Assert.assertEquals(g_edges, g2_edges);

    for (T v : g2.nodes()) {
      for (T w : g2.nodes()) {
        Assert.assertEquals(g.adjacentNodes(v).contains(w), g2.adjacentNodes(v).contains(w));
        Set<String> e = new HashSet<String>();
        for (Number n : g.edgesConnecting(v, w)) {
          e.add(String.valueOf(n));
        }
        Set<Object> e2 = new HashSet<Object>(g2.edgesConnecting(v, w));
        Assert.assertEquals(e.size(), e2.size());
        Assert.assertEquals(e, e2);
      }
    }

    for (Object o : g2.edges()) {
      String weight = edge_weight.apply(new Double((String) o));
      String weight2 = edge_weight2.apply(o);
      Assert.assertEquals(weight2, weight);
    }
  }
}
