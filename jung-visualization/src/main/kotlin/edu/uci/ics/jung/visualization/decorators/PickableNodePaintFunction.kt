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
import java.awt.Paint
import java.util.function.Function

/**
 * Paints each node according to the `Paint` parameters given in the constructor, so that
 * picked and non-picked nodes can be made to look different.
 */
open class PickableNodePaintFunction<N>(
    /** Specifies which nodes report as "picked". */
    protected val pi: PickedInfo<N>,
    /** `Paint` used to fill node shapes. */
    protected val fill_paint: Paint,
    /** `Paint` used to fill picked node shapes. */
    protected val picked_paint: Paint
) : Function<N, Paint> {

    init {
        Preconditions.checkNotNull(pi)
        Preconditions.checkNotNull(fill_paint)
        Preconditions.checkNotNull(picked_paint)
    }

    override fun apply(v: N): Paint =
        if (pi.isPicked(v)) picked_paint else fill_paint
}
