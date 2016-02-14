/*
 * Created on Jul 12, 2007
 *
 * Copyright (c) 2007, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring.util;

import java.util.Collection;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.scoring.HITS;

/**
 * Methods for assigning values (to be interpreted as prior probabilities) to vertices in the context
 * of random-walk-based scoring algorithms.
 */
public class ScoringUtils
{
    /**
     * Assigns a probability of 1/<code>roots.size()</code> to each of the elements of <code>roots</code>.
     * @param <V> the vertex type
     * @param roots the vertices to be assigned nonzero prior probabilities
     * @return a Function assigning a uniform prior to each element in {@code roots} 
     */
    public static <V> Function<V, Double> getUniformRootPrior(Collection<V> roots)
    {
        final Collection<V> inner_roots = roots;
        Function<V, Double> distribution = new Function<V, Double>()
        {
            public Double apply(V input)
            {
                if (inner_roots.contains(input))
                    return new Double(1.0 / inner_roots.size());
                else
                    return 0.0;
            }
        };
        
        return distribution;
    }
    
    /**
     * Returns a Function that hub and authority values of 1/<code>roots.size()</code> to each 
     * element of <code>roots</code>.
     * @param <V> the vertex type
     * @param roots the vertices to be assigned nonzero scores
     * @return a Function that assigns uniform prior hub/authority probabilities to each root
     */
    public static <V> Function<V, HITS.Scores> getHITSUniformRootPrior(Collection<V> roots)
    {
        final Collection<V> inner_roots = roots;
        Function<V, HITS.Scores> distribution = 
        	new Function<V, HITS.Scores>()
        {
            public HITS.Scores apply(V input)
            {
                if (inner_roots.contains(input))
                    return new HITS.Scores(1.0 / inner_roots.size(), 1.0 / inner_roots.size());
                else
                    return new HITS.Scores(0.0, 0.0);
            }
        };
        return distribution;
    }
}
