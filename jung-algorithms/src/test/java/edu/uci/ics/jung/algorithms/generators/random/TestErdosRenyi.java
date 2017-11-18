package edu.uci.ics.jung.algorithms.generators.random;

import com.google.common.graph.Graph;
import java.util.function.Supplier;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** @author W. Giordano, Scott White */
public class TestErdosRenyi extends TestCase {

  Supplier<String> vertexFactory;

  public static Test suite() {
    return new TestSuite(TestErdosRenyi.class);
  }

  @Override
  protected void setUp() {
    vertexFactory =
        new Supplier<String>() {
          int count;

          public String get() {
            return Character.toString((char) ('A' + count++));
          }
        };
  }

  public void test() {

    int numVertices = 100;
    int total = 0;
    for (int i = 1; i <= 10; i++) {
      ErdosRenyiGenerator<String> generator =
          new ErdosRenyiGenerator<>(vertexFactory, numVertices, 0.1);
      generator.setSeed(0);

      Graph<String> graph = generator.get();
      Assert.assertTrue(graph.nodes().size() == numVertices);
      total += graph.edges().size();
    }
    total /= 10.0;
    Assert.assertTrue(total > 495 - 50 && total < 495 + 50);
  }
}
