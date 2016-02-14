/*
 * Created on Jul 16, 2004
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
public class EllipseVertexShapeTransformer<V> extends AbstractVertexShapeTransformer<V>
	implements Function<V,Shape>
{
    public EllipseVertexShapeTransformer() 
    {
    }
    public EllipseVertexShapeTransformer(Function<V,Integer> vsf, Function<V,Float> varf)
    {
        super(vsf, varf);
    }
    
    public Shape apply(V v)
    {
        return factory.getEllipse(v);
    }
}
