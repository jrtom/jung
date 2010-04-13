/*
 * Created on Apr 21, 2007
 *
 * Copyright (c) 2007, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.graph;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.OrderedSparseMultigraph;
import edu.uci.ics.jung.graph.SetHypergraph;
import edu.uci.ics.jung.graph.SortedSparseMultigraph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;


public class HypergraphTest extends AbstractHypergraphTest
{
    
    public HypergraphTest(Factory<? extends Hypergraph<Integer,Character>> factory)
    {
        super(factory);
    }
    
    @Override
    public void setUp()
    {
        h = factory.create();
        System.out.println(h.getClass().getSimpleName());
    }
    
    public static Test suite()
    {
        TestSuite ts = new TestSuite("HypergraphTest");
        
        ts.addTest(new HypergraphTest(SetHypergraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(DirectedOrderedSparseMultigraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(DirectedSparseGraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(DirectedSparseMultigraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(OrderedSparseMultigraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(SortedSparseMultigraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(SparseGraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(SparseMultigraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(UndirectedOrderedSparseMultigraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(UndirectedSparseGraph.<Integer,Character>getFactory()));
        ts.addTest(new HypergraphTest(UndirectedSparseMultigraph.<Integer,Character>getFactory()));
//        ts.addTest(new HypergraphTest(.getFactory()));
        
        return ts;
    }
    
}
