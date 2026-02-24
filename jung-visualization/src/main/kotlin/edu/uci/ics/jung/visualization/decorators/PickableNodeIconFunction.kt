/*
 * Created on Mar 10, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators

import com.google.common.base.Preconditions
import edu.uci.ics.jung.visualization.picking.PickedInfo
import java.util.function.Function
import javax.swing.Icon

/**
 * Supplies an Icon for each node according to the `Icon` parameters given in the
 * constructor, so that picked and non-picked nodes can be made to look different.
 */
open class PickableNodeIconFunction<N>(
    /** Specifies which nodes report as "picked". */
    protected val pi: PickedInfo<N>,
    /** `Icon` used to represent nodes. */
    protected val icon: Icon,
    /** `Icon` used to represent picked nodes. */
    protected val picked_icon: Icon
) : Function<N, Icon> {

    init {
        Preconditions.checkNotNull(pi)
        Preconditions.checkNotNull(icon)
        Preconditions.checkNotNull(picked_icon)
    }

    /** Returns the appropriate `Icon`, depending on picked state. */
    override fun apply(v: N): Icon =
        if (pi.isPicked(v)) picked_icon else icon
}
