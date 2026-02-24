package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer
import edu.uci.ics.jung.visualization.transform.LensTransformer
import edu.uci.ics.jung.visualization.transform.MagnifyTransformer
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer
import edu.uci.ics.jung.visualization.transform.shape.MagnifyShapeTransformer
import org.slf4j.LoggerFactory
import java.awt.Dimension
import java.awt.Shape
import java.awt.geom.Point2D

/**
 * @author Tom Nelson
 */
open class LensTransformSupport<N : Any, E : Any> : TransformSupport<N, E>() {

    /**
     * Overriden to apply lens effects to the transformation from view to layout coordinates
     *
     * @param vv
     * @param p
     * @return
     */
    override fun inverseTransform(vv: VisualizationServer<N, E>, p: Point2D): Point2D {
        var point = p
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        val viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW)
        val layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT)

        if (viewTransformer is LensTransformer) {
            val lensTransformer = viewTransformer
            val delegateTransformer = lensTransformer.delegate

            if (viewTransformer is MagnifyShapeTransformer) {
                val ht = MagnifyTransformer(lensTransformer.lens, layoutTransformer)
                point = delegateTransformer.inverseTransform(point)
                point = ht.inverseTransform(point)
            } else if (viewTransformer is HyperbolicShapeTransformer) {
                val ht = HyperbolicTransformer(lensTransformer.lens, layoutTransformer)
                point = delegateTransformer.inverseTransform(point)
                point = ht.inverseTransform(point)
            }
        } else {
            // the layoutTransformer may be a LensTransformer or not
            point = multiLayerTransformer.inverseTransform(point)
        }
        return point
    }

    override fun transform(vv: VisualizationServer<N, E>, shape: Shape): Shape {
        var result = shape
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        val viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW)
        val layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT)
        val model = vv.getModel()

        if (viewTransformer is LensTransformer) {
            result = multiLayerTransformer.transform(result)
        } else if (layoutTransformer is LensTransformer) {
            val layoutModel = model.getLayoutModel()
            val d = Dimension(layoutModel.width, layoutModel.height)
            val shapeChanger = HyperbolicShapeTransformer(d, viewTransformer)
            val lensTransformer = layoutTransformer as LensTransformer
            shapeChanger.lens.lensShape = lensTransformer.lens.lensShape
            val layoutDelegate = (layoutTransformer as MutableTransformerDecorator).delegate
            result = shapeChanger.transform(layoutDelegate.transform(result))
        } else {
            result = multiLayerTransformer.transform(Layer.LAYOUT, result)!!
        }
        return result
    }

    override fun transform(vv: VisualizationServer<N, E>, p: Point2D): Point2D {
        var point = p
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        val viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW)
        val layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT)
        val model = vv.getModel()

        if (viewTransformer is LensTransformer) {
            // use all layers
            point = multiLayerTransformer.transform(point)!!
        } else if (layoutTransformer is LensTransformer) {
            // apply the shape changer
            val layoutModel = model.getLayoutModel()
            val d = Dimension(layoutModel.width, layoutModel.height)
            val shapeChanger = HyperbolicShapeTransformer(d, viewTransformer)
            val lensTransformer = layoutTransformer as LensTransformer
            shapeChanger.lens.lensShape = lensTransformer.lens.lensShape
            val layoutDelegate = (layoutTransformer as MutableTransformerDecorator).delegate
            point = shapeChanger.transform(layoutDelegate.transform(point)!!)!!
        } else {
            // use the default
            point = multiLayerTransformer.transform(Layer.LAYOUT, point)!!
        }
        return point
    }

    /**
     * Overriden to perform lens effects when inverse transforming from view to layout.
     *
     * @param vv
     * @param shape
     * @return
     */
    override fun inverseTransform(vv: VisualizationServer<N, E>, shape: Shape): Shape {
        var result = shape
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        val viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW)
        val layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT)

        if (layoutTransformer is LensTransformer) {
            // apply the shape changer
            val layoutModel = vv.getModel().getLayoutModel()
            val d = Dimension(layoutModel.width, layoutModel.height)
            val shapeChanger = HyperbolicShapeTransformer(d, viewTransformer)
            val lensTransformer = layoutTransformer as LensTransformer
            shapeChanger.lens.lensShape = lensTransformer.lens.lensShape
            val layoutDelegate = (layoutTransformer as MutableTransformerDecorator).delegate
            result = layoutDelegate.inverseTransform(shapeChanger.inverseTransform(result))
        } else {
            // if the viewTransformer is either a LensTransformer or the default
            result = multiLayerTransformer.inverseTransform(result)
        }
        return result
    }

    companion object {
        private val log = LoggerFactory.getLogger(LensTransformSupport::class.java)
    }
}
