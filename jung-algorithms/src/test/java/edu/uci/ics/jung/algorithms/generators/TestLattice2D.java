package edu.uci.ics.jung.algorithms.generators;

import static edu.uci.ics.jung.algorithms.generators.TestLattice2D.EdgeType.DIRECTED;
import static edu.uci.ics.jung.algorithms.generators.TestLattice2D.EdgeType.UNDIRECTED;
import static edu.uci.ics.jung.algorithms.generators.TestLattice2D.Topology.FLAT;
import static edu.uci.ics.jung.algorithms.generators.TestLattice2D.Topology.TOROIDAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.graph.Network;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestLattice2D {
  enum EdgeType {
    UNDIRECTED,
    DIRECTED
  }

  enum Topology {
    FLAT,
    TOROIDAL
  }

  private EdgeType edgeType;
  private Topology topology;

  @Parameters
  public static Object[][] data() {
    return new Object[][] {
      {UNDIRECTED, FLAT}, {DIRECTED, FLAT}, {UNDIRECTED, TOROIDAL}, {DIRECTED, TOROIDAL}
    };
  }

  public TestLattice2D(EdgeType edgeType, Topology topology) {
    this.edgeType = edgeType;
    this.topology = topology;
  }

  protected Supplier<String> nodeFactory;
  protected Supplier<Integer> edgeFactory;

  @Before
  public void setUp() {
    nodeFactory =
        new Supplier<String>() {
          int count;

          public String get() {
            return Character.toString((char) ('a' + count++));
          }
        };
    edgeFactory =
        new Supplier<Integer>() {
          int count;

          public Integer get() {
            return count++;
          }
        };
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testCreateSingular() {
    try {
      @SuppressWarnings("unused")
      Lattice2DGenerator unused = new Lattice2DGenerator(1, 2, toroidal());
      unused = new Lattice2DGenerator(2, 1, toroidal());
      fail("Did not reject lattice of size < 2");
    } catch (IllegalArgumentException iae) {
    }
  }

  @Test
  public void testGenerateNetwork() {
    for (int rowCount = 4; rowCount <= 6; rowCount++) {
      for (int colCount = 4; colCount <= 6; colCount++) {
        Lattice2DGenerator<String, Integer> generator =
            new Lattice2DGenerator<>(rowCount, colCount, toroidal());
        Network<String, Integer> graph =
            generator.generateNetwork(directed(), nodeFactory, edgeFactory);
        assertEquals(graph.nodes().size(), rowCount * colCount);

        int boundary_adjustment = (toroidal() ? 0 : 1);
        int expectedEdgeCount =
            colCount * (rowCount - boundary_adjustment)
                + rowCount * (colCount - boundary_adjustment);
        if (directed()) {
          expectedEdgeCount *= 2;
        }
        assertEquals(graph.edges().size(), expectedEdgeCount);
        int expectedPerimeterNodes = toroidal() ? 0 : (rowCount - 1) * 2 + (colCount - 1) * 2;
        int nodeCount = graph.nodes().size();
        int expectedInteriorNodes = toroidal() ? nodeCount : nodeCount - expectedPerimeterNodes;
        int perimeterNodes = 0;
        int interiorNodes = 0;
        int degreeMultiplier = directed() ? 2 : 1;
        for (String node : graph.nodes()) {
          if (toroidal()) {
            int expectedDegree = 4 * degreeMultiplier;
            assertEquals(graph.degree(node), expectedDegree);
            interiorNodes++;
          } else {
            int degree = graph.degree(node);
            if (degree == 4 * degreeMultiplier) {
              interiorNodes++;
            } else if (degree == 3 * degreeMultiplier || degree == 2 * degreeMultiplier) {
              perimeterNodes++;
            } else {
              String message =
                  String.format(
                      "degree does not match expectations: "
                          + "degree: %s, multiplier: %s\n"
                          + "row count: %s, col count: %s, toroidal: %s\n"
                          + "graph: %s",
                      degree, degreeMultiplier, rowCount, colCount, toroidal(), graph);
              fail(message);
            }
          }
        }

        assertEquals(expectedInteriorNodes, interiorNodes);
        assertEquals(expectedPerimeterNodes, perimeterNodes);
      }
    }
  }

  private boolean toroidal() {
    switch (topology) {
      case TOROIDAL:
        return true;
      case FLAT:
        return false;
      default:
        throw new IllegalStateException("Unrecognized Topology type: " + topology);
    }
  }

  private boolean directed() {
    switch (edgeType) {
      case DIRECTED:
        return true;
      case UNDIRECTED:
        return false;
      default:
        throw new IllegalStateException("Unrecognized edge type: " + edgeType);
    }
  }
}
