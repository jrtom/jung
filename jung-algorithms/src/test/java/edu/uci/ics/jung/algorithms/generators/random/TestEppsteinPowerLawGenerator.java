/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random;

import com.google.common.graph.Graph;
import java.util.Random;
import java.util.function.Supplier;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Scott White
 */
public class TestEppsteinPowerLawGenerator extends TestCase {
  Supplier<Integer> nodeFactory;

  public static Test suite() {
    return new TestSuite(TestEppsteinPowerLawGenerator.class);
  }

  @Override
  protected void setUp() {
    nodeFactory =
        new Supplier<Integer>() {
          int count;

          public Integer get() {
            return count++;
          }
        };
  }

  public void testSimpleDirectedCase() {

    for (int r = 0; r < 10; r++) {
      EppsteinPowerLawGenerator<Integer> generator =
          new EppsteinPowerLawGenerator<Integer>(nodeFactory, 10, 40, r);
      generator.setRandom(new Random(2));

      Graph<Integer> graph = generator.get();
      Assert.assertEquals(graph.nodes().size(), 10);
      Assert.assertEquals(graph.edges().size(), 40);
    }
  }

  // TODO: convert what is needed for this test
  //    public void testPowerLawProperties() {
  //
  //        //long start = System.currentTimeMillis();
  //        EppsteinPowerLawGenerator generator = new EppsteinPowerLawGenerator(nodeFactory,
  // edgeFactory,
  //        		500,1500,100000);
  //        generator.setSeed(5);
  //        Graph graph = (Graph) generator.generateGraph();
  //        //long stop = System.currentTimeMillis();
  //        //System.out.println((stop-start)/1000l);
  //
  //        DoubleArrayList degreeList = DegreeDistributions.getOutdegreeValues(graph.getNodes());
  //        int maxDegree = (int) Descriptive.max(degreeList);
  //        Histogram degreeHistogram = GraphStatistics.createHistogram(degreeList,0,maxDegree,1);
  //        //for (int index=0;index<maxDegree;index++) {
  //        //    System.out.println(degreeHistogram.binIndex(index) + " " +
  // degreeHistogram.binHeight(index));
  //        //}
  //        //if it's power law, 0 is going to have the highest bin count
  //        Assert.assertTrue(degreeHistogram.binHeight(0) + degreeHistogram.binHeight(1) >
  // degreeHistogram.binHeight(2) + degreeHistogram.binHeight(3));
  //
  //        generator = new EppsteinPowerLawGenerator(500,1500,0);
  //        graph = (Graph) generator.generateGraph();
  //        degreeList = DegreeDistributions.getOutdegreeValues(graph.getNodes());
  //        maxDegree = (int) Descriptive.max(degreeList);
  //        degreeHistogram = GraphStatistics.createHistogram(degreeList,0,maxDegree,1);
  //        //for (int index=0;index<maxDegree;index++) {
  //        //    System.out.println(degreeHistogram.binIndex(index) + " " +
  // degreeHistogram.binHeight(index));
  //        //}
  //        //if it's not power law, 0 is not going to have the highest bin count rather it will
  // start to go up
  //        Assert.assertTrue(degreeHistogram.binHeight(0) < degreeHistogram.binHeight(1));
  //
  //
  //
  //    }

}
