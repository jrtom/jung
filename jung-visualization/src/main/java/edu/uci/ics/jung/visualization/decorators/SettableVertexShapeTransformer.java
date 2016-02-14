/*
 * Created on Jul 18, 2004
 *
 * Copyright (c) 2004, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import java.awt.Shape;

import com.google.common.base.Function;



/**
 * 
 * @author Joshua O'Madadhain
 */
public interface SettableVertexShapeTransformer<V> extends Function<V,Shape>
{
    public abstract void setSizeTransformer(Function<V,Integer> vsf);

    public abstract void setAspectRatioTransformer(Function<V,Float> varf);
}