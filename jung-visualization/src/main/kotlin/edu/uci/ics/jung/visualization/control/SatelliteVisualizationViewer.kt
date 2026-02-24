/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 15, 2005
 */

package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationModel
import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.spatial.Spatial
import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.Key
import java.awt.Shape
import java.awt.geom.AffineTransform

/**
 * A VisualizationViewer that can act as a satellite view for another (master) VisualizationViewer.
 * In this view, the full graph is always visible and all mouse actions affect the graph in the
 * master view.
 *
 * A rectangular shape in the satellite view shows the visible bounds of the master view.
 *
 * @author Tom Nelson
 */
open class SatelliteVisualizationViewer<N : Any, E : Any>(
    /** the master VisualizationViewer that this is a satellite view for */
    val master: VisualizationViewer<N, E>,
    preferredSize: Dimension
) : VisualizationViewer<N, E>(master.getModel(), preferredSize) {

    init {
        // create a graph mouse with custom plugins to affect the master view
        val gm: ModalGraphMouse = ModalSatelliteGraphMouse()
        setGraphMouse(gm)

        // this adds the Lens to the satellite view
        addPreRenderPaintable(ViewLens(this, master))

        // get a copy of the current layout transform
        // it may have been scaled to fit the graph
        val modelLayoutTransform = AffineTransform(
            master.getRenderContext().getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT).getTransform()
        )

        // I want no layout transformations in the satellite view
        // this resets the auto-scaling that occurs in the super constructor
        getRenderContext().getMultiLayerTransformer()
            .setTransformer(Layer.LAYOUT, MutableAffineTransformer(modelLayoutTransform))

        // make sure the satellite listens for changes in the master
        master.addChangeListener(this)

        // share the picked state of the master
        setPickedNodeState(master.getPickedNodeState())
        setPickedEdgeState(master.getPickedEdgeState())
        @Suppress("UNCHECKED_CAST")
        setNodeSpatial(Spatial.NoOp.Node<N>(getModel().getLayoutModel() as LayoutModel<N>))
        @Suppress("UNCHECKED_CAST")
        setEdgeSpatial(Spatial.NoOp.Edge<E, N>(getModel() as VisualizationModel<N, E>))
    }

    @Suppress("UNCHECKED_CAST")
    override fun setRenderingHints(renderingHints: Map<Key, Any>) {
        super.setRenderingHints(renderingHints)
    }

    /**
     * override to not use the spatial data structure, as this view will always show the entire graph
     *
     * @param g2d
     */
    override fun renderGraph(g2d: Graphics2D) {
        val rc = getRenderContext()
        if (rc.getGraphicsContext() == null) {
            rc.setGraphicsContext(GraphicsDecorator(g2d))
        } else {
            rc.getGraphicsContext()!!.setDelegate(g2d)
        }
        rc.setScreenDevice(this)
        val layoutModel = getModel().getLayoutModel()

        g2d.setRenderingHints(getRenderingHints())

        // the layoutSize of the VisualizationViewer
        val d = size

        // clear the offscreen image
        g2d.color = background
        g2d.fillRect(0, 0, d.width, d.height)

        val oldXform = g2d.transform
        val newXform = AffineTransform(oldXform)
        newXform.concatenate(
            rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).getTransform()
        )

        g2d.transform = newXform

        // if there are preRenderers set, paint them
        for (paintable in preRenderers) {
            if (paintable.useTransform()) {
                paintable.paint(g2d)
            } else {
                g2d.transform = oldXform
                paintable.paint(g2d)
                g2d.transform = newXform
            }
        }

        getRenderer().render(rc, getModel())

        // if there are postRenderers set, do it
        for (paintable in postRenderers) {
            if (paintable.useTransform()) {
                paintable.paint(g2d)
            } else {
                g2d.transform = oldXform
                paintable.paint(g2d)
                g2d.transform = newXform
            }
        }
        g2d.transform = oldXform
    }

    /**
     * A four-sided shape that represents the visible part of the master view and is drawn in the
     * satellite view
     *
     * @author Tom Nelson
     */
    internal class ViewLens<N : Any, E : Any>(
        private val vv: VisualizationViewer<N, E>,
        private val master: VisualizationViewer<N, E>
    ) : VisualizationServer.Paintable {

        override fun paint(g: Graphics) {
            val masterViewTransformer =
                master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
            val masterLayoutTransformer =
                master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
            val vvLayoutTransformer =
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)

            var lens: Shape = master.bounds
            lens = masterViewTransformer.inverseTransform(lens)
            lens = masterLayoutTransformer.inverseTransform(lens)
            val lensShape = vvLayoutTransformer.transform(lens)
            val g2d = g as Graphics2D
            val old = g.getColor()
            val lensColor = master.background
            vv.background = lensColor.darker()
            g.setColor(lensColor)
            g2d.fill(lensShape)
            g.setColor(Color.gray)
            g2d.draw(lensShape)
            g.setColor(old)
        }

        override fun useTransform(): Boolean {
            return true
        }
    }
}
