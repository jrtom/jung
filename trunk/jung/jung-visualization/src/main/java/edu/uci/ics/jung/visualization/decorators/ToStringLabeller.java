/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
/*
 * Created on Apr 13, 2004
 */
package edu.uci.ics.jung.visualization.decorators;

import org.apache.commons.collections15.Transformer;



/**
 * Labels vertices by their toString. This class functions as a drop-in
 * replacement for the default StringLabeller method. This class does not
 * guarantee unique labels; or even consistent ones; as a result,
 * getVertexByLabel will always return NULL.
 * 
 * @author danyelf
 */
public class ToStringLabeller<V> implements Transformer<V,String> {

    /**
     * Retunrs v.toString()
     */
    public String transform(V v) {
        return v.toString();
    }

 }