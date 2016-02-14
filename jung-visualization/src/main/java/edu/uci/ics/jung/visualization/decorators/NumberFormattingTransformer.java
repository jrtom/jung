/*
 * Created on Feb 16, 2009
 *
 * Copyright (c) 2009, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import java.text.NumberFormat;

import com.google.common.base.Function;

/**
 * Transforms inputs to String representations by chaining an input 
 * {@code Number}-generating {@code Function} with an internal 
 * {@code NumberFormat} instance.
 * @author Joshua O'Madadhain
 */
public class NumberFormattingTransformer<T> implements Function<T, String>
{
    private Function<T, ? extends Number> values;
    private NumberFormat formatter = NumberFormat.getInstance();
    
    public NumberFormattingTransformer(Function<T, ? extends Number> values)
    {
        this.values = values;
    }

    /**
     * Returns a formatted string for the input.
     */
    public String apply(T input)
    {
        return formatter.format(values.apply(input));
    }

}
