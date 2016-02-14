/*
 * Copyright (c) 2003, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Apr 13, 2004
 */
package edu.uci.ics.jung.visualization.decorators;

import com.google.common.base.Function;



/**
 * Labels vertices by their toString. This class functions as a drop-in
 * replacement for the default StringLabeller method. This class does not
 * guarantee unique labels; or even consistent ones.
 * 
 * @author danyelf
 */
public class ToStringLabeller implements Function<Object, String> {

    /**
     * @return o.toString()
     */
    public String apply(Object o) {
        return o.toString();
    }

 }