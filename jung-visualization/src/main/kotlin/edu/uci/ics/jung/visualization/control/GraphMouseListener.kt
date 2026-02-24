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
 * Created on Feb 17, 2004
 */
package edu.uci.ics.jung.visualization.control

import java.awt.event.MouseEvent

/**
 * This interface allows users to register listeners to register to receive node clicks.
 *
 * @author danyelf
 */
interface GraphMouseListener<N> {

    fun graphClicked(v: N, me: MouseEvent)

    fun graphPressed(v: N, me: MouseEvent)

    fun graphReleased(v: N, me: MouseEvent)
}
