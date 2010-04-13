/*
 * Created on Feb 16, 2009
 *
 * Copyright (c) 2009, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import java.text.NumberFormat;

import org.apache.commons.collections15.Transformer;

/**
 * Transforms inputs to String representations by chaining an input 
 * {@code Number}-generating {@code Transformer} with an internal 
 * {@code NumberFormat} instance.
 * @author Joshua O'Madadhain
 */
public class NumberFormattingTransformer<T> implements Transformer<T, String>
{
    private Transformer<T, ? extends Number> values;
    private NumberFormat formatter = NumberFormat.getInstance();
    
    public NumberFormattingTransformer(Transformer<T, ? extends Number> values)
    {
        this.values = values;
    }

    /**
     * Returns a formatted string for the input.
     */
    public String transform(T input)
    {
        return formatter.format(values.transform(input));
    }

}
