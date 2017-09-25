package edu.uci.ics.jung.algorithms.layout;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.graph.util.Graphs;
import java.util.Collection;
import org.junit.Test;

public class LayoutTest {

  @Test
  public void testIt() {
    //create a graph
    MutableNetwork<Number, Number> original =
        NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();
    MutableNetwork<Number, Number> ig = Graphs.synchronizedNetwork(original);
    Collection<Number> nodes = ig.nodes();
    System.err.println("nodes:" + nodes);
    ig.addNode(92929);
    System.err.println("nodes:" + nodes);
  }
}
