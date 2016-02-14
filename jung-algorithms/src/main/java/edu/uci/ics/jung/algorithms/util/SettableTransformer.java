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

import com.google.common.base.Function;

/**
 * An interface for classes that can set the value to be returned (from <code>transform()</code>)
 * when invoked on a given input.
 * 
 * @author Joshua O'Madadhain
 */
public interface SettableTransformer<I, O> extends Function<I, O>
{
    /**
     * Sets the value (<code>output</code>) to be returned by a call to 
     * <code>transform(input)</code>).
     * @param input the value whose output value is being specified
     * @param output the output value for {@code input}
     */
    public void set(I input, O output);
}
