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

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.visualization.util.VertexShapeFactory;



/**
 * 
 * @author Joshua O'Madadhain
 */
public abstract class AbstractVertexShapeTransformer<V> implements SettableVertexShapeTransformer<V>
{
    protected Transformer<V,Integer> vsf;
    protected Transformer<V,Float> varf;
    protected VertexShapeFactory<V> factory;
    public final static int DEFAULT_SIZE = 8;
    public final static float DEFAULT_ASPECT_RATIO = 1.0f;
    
    public AbstractVertexShapeTransformer(Transformer<V,Integer> vsf, Transformer<V,Float> varf)
    {
        this.vsf = vsf;
        this.varf = varf;
        factory = new VertexShapeFactory<V>(vsf, varf);
    }

    @SuppressWarnings("unchecked")
	public AbstractVertexShapeTransformer()
    {
        this(new ConstantTransformer(DEFAULT_SIZE), 
                new ConstantTransformer(DEFAULT_ASPECT_RATIO));
    }
    
    public void setSizeTransformer(Transformer<V,Integer> vsf)
    {
        this.vsf = vsf;
        factory = new VertexShapeFactory<V>(vsf, varf);
    }
    
    public void setAspectRatioTransformer(Transformer<V,Float> varf)
    {
        this.varf = varf;
        factory = new VertexShapeFactory<V>(vsf, varf);
    }
}
