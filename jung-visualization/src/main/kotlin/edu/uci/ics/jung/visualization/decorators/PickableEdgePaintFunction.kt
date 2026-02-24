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
 * Paints each edge according to the `Paint` parameters given in the constructor, so that
 * picked and non-picked edges can be made to look different.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
open class PickableEdgePaintFunction<E>(
    /** Specifies which nodes report as "picked". */
    protected val pi: PickedInfo<E>,
    /** `Paint` used to draw edge shapes. */
    protected val draw_paint: Paint,
    /** `Paint` used to draw picked edge shapes. */
    protected val picked_paint: Paint
) : Function<E, Paint> {

    init {
        Preconditions.checkNotNull(pi)
        Preconditions.checkNotNull(draw_paint)
        Preconditions.checkNotNull(picked_paint)
    }

    override fun apply(e: E): Paint =
        if (pi.isPicked(e)) picked_paint else draw_paint
}
