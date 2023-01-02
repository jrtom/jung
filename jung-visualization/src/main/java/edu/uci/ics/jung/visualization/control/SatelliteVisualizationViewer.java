/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 15, 2005
 */

package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

/**
 * A VisualizationViewer that can act as a satellite view for another (master) VisualizationViewer.
 * In this view, the full graph is always visible and all mouse actions affect the graph in the
 * master view.
 *
 * <p>A rectangular shape in the satellite view shows the visible bounds of the master view.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class SatelliteVisualizationViewer<N, E> extends VisualizationViewer<N, E> {

  /** the master VisualizationViewer that this is a satellite view for */
  protected VisualizationViewer<N, E> master;

  /**
   * @param master the master VisualizationViewer for which this is a satellite view
   * @param preferredSize the specified layoutSize of the component
   */
  public SatelliteVisualizationViewer(VisualizationViewer<N, E> master, Dimension preferredSize) {
    super(master.getModel(), preferredSize);
    this.master = master;

    // create a graph mouse with custom plugins to affect the master view
    ModalGraphMouse gm = new ModalSatelliteGraphMouse();
    setGraphMouse(gm);

    // this adds the Lens to the satellite view
    addPreRenderPaintable(new ViewLens<N, E>(this, master));

    // get a copy of the current layout transform
    // it may have been scaled to fit the graph
    AffineTransform modelLayoutTransform =
        new AffineTransform(
            master
                .getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .getTransform());

    // I want no layout transformations in the satellite view
    // this resets the auto-scaling that occurs in the super constructor
    getRenderContext()
        .getMultiLayerTransformer()
        .setTransformer(Layer.LAYOUT, new MutableAffineTransformer(modelLayoutTransform));

    // make sure the satellite listens for changes in the master
    master.addChangeListener(this);

    // share the picked state of the master
    setPickedNodeState(master.getPickedNodeState());
    setPickedEdgeState(master.getPickedEdgeState());
    setNodeSpatial(new Spatial.NoOp.Node(model.getLayoutModel()));
    setEdgeSpatial(new Spatial.NoOp.Edge(model));
  }

  /**
   * override to not use the spatial data structure, as this view will always show the entire graph
   *
   * @param g2d
   */
  @Override
  protected void renderGraph(Graphics2D g2d) {
    if (renderContext.getGraphicsContext() == null) {
      renderContext.setGraphicsContext(new GraphicsDecorator(g2d));
    } else {
      renderContext.getGraphicsContext().setDelegate(g2d);
    }
    renderContext.setScreenDevice(this);
    LayoutModel<N> layoutModel = getModel().getLayoutModel();

    g2d.setRenderingHints(renderingHints);

    // the layoutSize of the VisualizationViewer
    Dimension d = getSize();

    // clear the offscreen image
    g2d.setColor(getBackground());
    g2d.fillRect(0, 0, d.width, d.height);

    AffineTransform oldXform = g2d.getTransform();
    AffineTransform newXform = new AffineTransform(oldXform);
    newXform.concatenate(
        renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW).getTransform());

    g2d.setTransform(newXform);

    // if there are  preRenderers set, paint them
    for (Paintable paintable : preRenderers) {

      if (paintable.useTransform()) {
        paintable.paint(g2d);
      } else {
        g2d.setTransform(oldXform);
        paintable.paint(g2d);
        g2d.setTransform(newXform);
      }
    }

    renderer.render(renderContext, model);

    // if there are postRenderers set, do it
    for (Paintable paintable : postRenderers) {

      if (paintable.useTransform()) {
        paintable.paint(g2d);
      } else {
        g2d.setTransform(oldXform);
        paintable.paint(g2d);
        g2d.setTransform(newXform);
      }
    }
    g2d.setTransform(oldXform);
  }

  /**
   * @return Returns the master.
   */
  public VisualizationViewer<N, E> getMaster() {
    return master;
  }

  /**
   * A four-sided shape that represents the visible part of the master view and is drawn in the
   * satellite view
   *
   * @author Tom Nelson
   */
  static class ViewLens<N, E> implements Paintable {

    VisualizationViewer<N, E> master;
    VisualizationViewer<N, E> vv;

    public ViewLens(VisualizationViewer<N, E> vv, VisualizationViewer<N, E> master) {
      this.vv = vv;
      this.master = master;
    }

    public void paint(Graphics g) {
      ShapeTransformer masterViewTransformer =
          master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
      ShapeTransformer masterLayoutTransformer =
          master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
      ShapeTransformer vvLayoutTransformer =
          vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

      Shape lens = master.getBounds();
      lens = masterViewTransformer.inverseTransform(lens);
      lens = masterLayoutTransformer.inverseTransform(lens);
      lens = vvLayoutTransformer.transform(lens);
      Graphics2D g2d = (Graphics2D) g;
      Color old = g.getColor();
      Color lensColor = master.getBackground();
      vv.setBackground(lensColor.darker());
      g.setColor(lensColor);
      g2d.fill(lens);
      g.setColor(Color.gray);
      g2d.draw(lens);
      g.setColor(old);
    }

    public boolean useTransform() {
      return true;
    }
  }
}
