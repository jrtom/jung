package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer
import org.slf4j.LoggerFactory
import java.awt.Shape
import java.awt.geom.Point2D

/**
 * @author Tom Nelson
 */
open class TransformSupport<N : Any, E : Any> : MutableAffineTransformer() {

    /**
     * Overriden to apply lens effects to the transformation from view to layout coordinates
     *
     * @param vv
     * @param p
     * @return
     */
    open fun inverseTransform(vv: VisualizationServer<N, E>, p: Point2D): Point2D {
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        return multiLayerTransformer.inverseTransform(p)
    }

    /**
     * Overriden to perform lens effects when transforming from Layout to view. Used when projecting
     * the selection Lens (the rectangular area drawn with the mouse) back into the view.
     *
     * @param vv
     * @param p
     * @return
     */
    open fun transform(vv: VisualizationServer<N, E>, p: Point2D): Point2D {
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        return multiLayerTransformer.transform(p)!!
    }

    /**
     * Overriden to perform lens effects when transforming from Layout to view. Used when projecting
     * the selection Lens (the rectangular area drawn with the mouse) back into the view.
     *
     * @param vv
     * @param shape
     * @return
     */
    open fun transform(vv: VisualizationServer<N, E>, shape: Shape): Shape {
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        return multiLayerTransformer.transform(shape)
    }

    /**
     * Overriden to perform lens effects when inverse transforming from view to layout.
     *
     * @param vv
     * @param shape
     * @return
     */
    open fun inverseTransform(vv: VisualizationServer<N, E>, shape: Shape): Shape {
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        return multiLayerTransformer.inverseTransform(shape)
    }

    companion object {
        private val log = LoggerFactory.getLogger(TransformSupport::class.java)
    }
}
