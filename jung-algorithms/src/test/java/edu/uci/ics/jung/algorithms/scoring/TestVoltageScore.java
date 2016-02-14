/**
 * Copyright (c) 2008, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Jul 14, 2008
 * 
 */
package edu.uci.ics.jung.algorithms.scoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.base.Functions;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

/**
 * @author jrtom
 *
 */
public class TestVoltageScore extends TestCase 
{
    protected Graph<Number,Number> g;
    
    @Override
    public void setUp() {
        g = new UndirectedSparseMultigraph<Number,Number>();
        for (int i = 0; i < 7; i++) {
        	g.addVertex(i);
        }

        int j = 0;
        g.addEdge(j++,0,1);
        g.addEdge(j++,0,2);
        g.addEdge(j++,1,3);
        g.addEdge(j++,2,3);
        g.addEdge(j++,3,4);
        g.addEdge(j++,3,5);
        g.addEdge(j++,4,6);
        g.addEdge(j++,5,6);
    }
    
    public final void testCalculateVoltagesSourceTarget() {
        VoltageScorer<Number,Number> vr = new VoltageScorer<Number,Number>(g, Functions.<Number>constant(1), 0, 6);
        double[] voltages = {1.0, 0.75, 0.75, 0.5, 0.25, 0.25, 0};
        
        vr.evaluate();
        for (int i = 0; i < 7; i++) {
            assertEquals(vr.getVertexScore(i), voltages[i], 0.01);
        }
    }
    
    public final void testCalculateVoltagesSourcesTargets()
    {
        Map<Number,Number> sources = new HashMap<Number,Number>();
        sources.put(0, new Double(1.0));
        sources.put(1, new Double(0.5));
        Set<Number> sinks = new HashSet<Number>();
        sinks.add(6);
        sinks.add(5);
        VoltageScorer<Number,Number> vr = 
        	new VoltageScorer<Number,Number>(g, Functions.constant(1), sources, sinks);
        double[] voltages = {1.0, 0.5, 0.66, 0.33, 0.16, 0, 0};
        
        vr.evaluate();
        for (int i = 0; i < 7; i++) {
            assertEquals(vr.getVertexScore(i), voltages[i], 0.01);
        }
    }
}
