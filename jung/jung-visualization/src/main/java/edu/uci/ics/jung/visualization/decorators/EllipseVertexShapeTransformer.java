/*
 * Created on Jul 16, 2004
 *
 * Copyright (c) 2004, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import java.awt.Shape;

import org.apache.commons.collections15.Transformer;

/**
 * 
 * @author Joshua O'Madadhain
 */
public class EllipseVertexShapeTransformer<V> extends AbstractVertexShapeTransformer<V>
	implements Transformer<V,Shape>
{
    public EllipseVertexShapeTransformer() 
    {
    }
    public EllipseVertexShapeTransformer(Transformer<V,Integer> vsf, Transformer<V,Float> varf)
    {
        super(vsf, varf);
    }
    
    public Shape transform(V v)
    {
        return factory.getEllipse(v);
    }
}
