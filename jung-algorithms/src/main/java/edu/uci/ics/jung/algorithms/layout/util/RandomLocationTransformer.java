/*
 * Created on Jul 19, 2005
 *
 * Copyright (c) 2005, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.layout.util;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Date;
import java.util.Random;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.StaticLayout;

/**
 * Provides a random vertex location within the bounds of the Dimension property.
 * This provides a random location for unmapped vertices
 * the first time they are accessed.
 * 
 * <p><b>Note</b>: the generated values are not cached, so apply() will generate a new random
 * location for the passed vertex every time it is called.  If you want a consistent value,
 * wrap this layout's generated values in a {@link StaticLayout} instance.
 * 
 * @author Tom Nelson
 *
 * @param <V> the vertex type
 */
public class RandomLocationTransformer<V> implements Function<V,Point2D> {
	Dimension d;
	Random random;
    
    /**
     * Creates an instance with the specified size which uses the current time 
     * as the random seed.
     * @param d the size of the layout area
     */
    public RandomLocationTransformer(Dimension d) {
    	this(d, new Date().getTime());
    }
    
    /**
     * Creates an instance with the specified dimension and random seed.
     * @param d the size of the layout area
     * @param seed the seed for the internal random number generator
     */
    public RandomLocationTransformer(final Dimension d, long seed) {
    	this.d = d;
    	this.random = new Random(seed);
    }
    
    public Point2D apply(V v) {
        return new Point2D.Double(random.nextDouble() * d.width, random.nextDouble() * d.height);
    }
}
