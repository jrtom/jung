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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.util.Pair;


public abstract class AbstractHypergraphTest extends TestCase
{
    protected Supplier<? extends Hypergraph<Integer,Character>> factory;
    protected Hypergraph<Integer,Character> h;
    
    public AbstractHypergraphTest(Supplier<? extends Hypergraph<Integer,Character>> factory)
    {
        this.factory = factory;
    }
    
    @Override
    public void runTest() throws Exception {
        setUp();
        testAddVertex();
        testAddEdge();
        testEdgeEndpoints();
        tearDown();
    }

    /**
     * test for the following:
     * <ul>
     * <li>add successful iff arg is not present
     * <li>count increases by 1 iff add is successful
     * <li>null vertex argument actively rejected
     * <li>vertex reported as present iff add is successful
     * </ul>
     */
    public void testAddVertex()
    {
        int count = h.getVertexCount();
        assertTrue(h.addVertex(new Integer(1)));
        assertEquals(count+1, h.getVertexCount());
        assertTrue(h.containsVertex(1));
        boolean success = false;
        try
        {
            success = h.addVertex(null);
            fail("Implementation should disallow null vertices");
        }
        catch (IllegalArgumentException iae) {}
        catch (NullPointerException npe)
        {
            fail("Implementation should actively prevent null vertices");
        }
        assertFalse(success);
        assertFalse(h.addVertex(1));
        assertEquals(count+1, h.getVertexCount());
        assertFalse(h.containsVertex(2));
    }
    
    /**
     * test for the following:
     * <ul>
     * <li>add successful iff edge is not present 
     * <li>edge count increases by 1 iff add successful
     * <li>null edge arg actively rejected
     * <li>edge reported as present iff add is successful
     * <li>throw if edge is present with different endpoints
     * </ul>
     */
    public void testAddEdge()
    {
        int edge_count = h.getEdgeCount();
        int vertex_count = h.getVertexCount();
        Pair<Integer> p = new Pair<Integer>(2, 3);
        assertTrue(h.addEdge('a', p));
        assertEquals(edge_count+1, h.getEdgeCount());
        assertEquals(vertex_count+2, h.getVertexCount());
        assertTrue(h.containsEdge('a'));
        boolean success = false;
        try
        {
            success = h.addEdge('b', null);
            fail("Implementation should disallow null pairs/collections");
            success = h.addEdge(null, p);
            fail("Implementation should disallow null edges");
        }
        catch (IllegalArgumentException iae) {}
        catch (NullPointerException npe)
        {
            fail("Implementation should actively prevent null edges, pairs, and collections");
        }
        assertFalse(success);
        // adding the same edge with an equal Pair should return false
        assertFalse(h.addEdge('a', new Pair<Integer>(2,3)));
        // adding the same edge with the same Pair should return false
        assertFalse(h.addEdge('a', p));
        try
        {
            success = h.addEdge('a', new Pair<Integer>(3,4));
            fail("Implementation should disallow existing edge objects from connecting new pairs/collections");
        }
        catch (IllegalArgumentException iae) {}
        assertEquals(edge_count+1, h.getEdgeCount());
        assertFalse(h.containsEdge('b'));
    }
    
    /**
     * test for the following:
     * <ul>
     * <li>if Graph, reject # of endpoints != 2
     * <li>otherwise, accept any (non-negative) number of endpoints
     * 
     * </ul>
     *
     */
    public void testEdgeEndpoints()
    {
        Collection<Integer> c = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++)
        {
            try
            {
                h.addEdge((char)i, c);
                c.add(i);
            }
            catch (IllegalArgumentException iae)
            {
                if (h instanceof Graph)
                {
                    if (c.size() == 2)
                        fail("improperly rejected incident vertex collection " + c);
                }
                else
                    fail("hypergraph implementations should accept any positive number of incident vertices");
            }
        }
    }
    
    /**
     * should return null if any of the following is true
     * <ul>
     * <li>v1 is null 
     * <li>v2 is null
     * <li>there is no edge connecting v1 to v2 in this graph
     * </ul>
     * otherwise should return _an_ edge connecting v1 to v2.
     * May be directed or undirected (depending on the graph);
     * may be any of the edges in the graph that so connect v1 and v2.
     * 
     * Must _not_ return any directed edge for which v1 and v2 are distinct
     * and v2 is the source.
     */
    public void testFindEdge()
    {
        
    }
}
