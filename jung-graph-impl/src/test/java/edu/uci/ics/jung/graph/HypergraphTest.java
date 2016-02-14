/*
 * Created on Apr 21, 2007
 *
 * Copyright (c) 2007, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.google.common.base.Supplier;


public class HypergraphTest extends AbstractHypergraphTest
{
    
    public HypergraphTest(Supplier<? extends Hypergraph<Integer,Character>> factory)
    {
        super(factory);
    }
    
    @Override
    public void setUp()
    {
        h = factory.get();
//        System.out.println(h.getClass().getSimpleName());
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
