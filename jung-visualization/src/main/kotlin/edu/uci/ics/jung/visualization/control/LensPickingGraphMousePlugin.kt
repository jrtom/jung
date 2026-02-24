package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.VisualizationViewer
import org.slf4j.LoggerFactory
import java.awt.Shape
import java.awt.geom.Point2D

/**
 * A subclass of PickingGraphMousePlugin that contains methods that are overridden to account for
 * the Lens effects that are in the view projection
 *
 * @author Tom Nelson
 */
open class LensPickingGraphMousePlugin<N : Any, E : Any> : PickingGraphMousePlugin<N, E>() {

    protected var transformSupport: TransformSupport<*, *> = LensTransformSupport<Any, Any>()

    /**
     * Overriden to apply lens effects to the transformation from view to layout coordinates
     *
     * @param vv
     * @param p
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    override fun inverseTransform(vv: VisualizationViewer<N, E>, p: Point2D): Point2D {
        return (transformSupport as TransformSupport<N, E>).inverseTransform(vv, p)
    }

    /**
     * Overriden to perform lens effects when transforming from Layout to view. Used when projecting
     * the selection Lens (the rectangular area drawn with the mouse) back into the view.
     *
     * @param vv
     * @param shape
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    override fun transform(vv: VisualizationViewer<N, E>, shape: Shape): Shape {
        return (transformSupport as TransformSupport<N, E>).transform(vv, shape)
    }

    /**
     * Overriden to perform Lens effects when managing the picking Lens target shape (drawn with the
     * mouse) in both the layout and view coordinate systems
     *
     * @param vv
     * @param multiLayerTransformer
     * @param down
     * @param out
     */
    @Suppress("UNCHECKED_CAST")
    override fun updatePickingTargets(
        vv: VisualizationViewer<*, *>,
        multiLayerTransformer: MultiLayerTransformer,
        down: Point2D,
        out: Point2D
    ) {
        viewRectangle.setFrameFromDiagonal(down, out)
        layoutTargetShape = (transformSupport as TransformSupport<Any, Any>).inverseTransform(vv as VisualizationViewer<Any, Any>, viewRectangle)
    }

    companion object {
        private val log = LoggerFactory.getLogger(LensPickingGraphMousePlugin::class.java)
    }
}
