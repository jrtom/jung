/*
 * Created on Aug 5, 2007
 *
 * Copyright (c) 2007, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.util;

import java.util.Map;


/**
 * A <code>SettableTransformer</code> that operates on an underlying <code>Map</code> instance.
 * Similar to <code>MapTransformer</code>.
 * 
 * @author Joshua O'Madadhain
 */
public class MapSettableTransformer<I, O> implements SettableTransformer<I, O>
{
    protected Map<I,O> map;
    
    /**
     * Creates an instance based on <code>m</code>.
     * @param m the map on which this instance is based
     */
    public MapSettableTransformer(Map<I,O> m)
    {
        this.map = m;
    }

    public O apply(I input)
    {
        return map.get(input);
    }

    public void set(I input, O output)
    {
        map.put(input, output);
    }
}
