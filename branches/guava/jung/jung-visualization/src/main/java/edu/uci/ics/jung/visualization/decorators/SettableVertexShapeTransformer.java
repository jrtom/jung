/*
 * Created on Jul 18, 2004
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
public interface SettableVertexShapeTransformer<V> extends Transformer<V,Shape>
{
    public abstract void setSizeTransformer(Transformer<V,Integer> vsf);

    public abstract void setAspectRatioTransformer(Transformer<V,Float> varf);
}