package edu.uci.ics.jung.visualization.control;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;
import java.util.function.Supplier;

public class SimpleEdgeSupport implements EdgeSupport {

  protected Point2D down;
  protected EdgeEffects edgeEffects;
  protected Supplier edgeFactory;
  protected Object startVertex;

  public SimpleEdgeSupport(Supplier edgeFactory) {
    this.edgeFactory = edgeFactory;
    this.edgeEffects = new CubicCurveEdgeEffects();
  }

  @Override
  public void startEdgeCreate(BasicVisualizationServer vv, Object startVertex, Point2D startPoint) {
    this.startVertex = startVertex;
    this.down = startPoint;
    this.edgeEffects.startEdgeEffects(vv, startPoint, startPoint);
    if (vv.getModel().getLayoutMediator().getNetwork().isDirected()) {
      this.edgeEffects.startArrowEffects(vv, startPoint, startPoint);
    }
    vv.repaint();
  }

  @Override
  public void midEdgeCreate(BasicVisualizationServer vv, Point2D midPoint) {
    if (startVertex != null) {
      this.edgeEffects.midEdgeEffects(vv, down, midPoint);
      if (vv.getModel().getLayoutMediator().getNetwork().isDirected()) {
        this.edgeEffects.midArrowEffects(vv, down, midPoint);
      }
      vv.repaint();
    }
  }

  @Override
  public void endEdgeCreate(BasicVisualizationServer vv, Object endVertex) {
    Preconditions.checkState(
        vv.getModel().getLayoutMediator().getNetwork() instanceof MutableNetwork<?, ?>,
        "graph must be mutable");
    if (startVertex != null) {
      MutableNetwork graph = (MutableNetwork) vv.getModel().getLayoutMediator().getNetwork();
      graph.addEdge(startVertex, endVertex, edgeFactory.get());
      vv.repaint();
    }
    startVertex = null;
    edgeEffects.endEdgeEffects(vv);
    edgeEffects.endArrowEffects(vv);
  }

  @Override
  public void abort(BasicVisualizationServer vv) {
    startVertex = null;
    edgeEffects.endEdgeEffects(vv);
    edgeEffects.endArrowEffects(vv);
    vv.repaint();
  }

  public EdgeEffects getEdgeEffects() {
    return edgeEffects;
  }

  public void setEdgeEffects(EdgeEffects edgeEffects) {
    this.edgeEffects = edgeEffects;
  }

  public Supplier getEdgeFactory() {
    return edgeFactory;
  }

  public void setEdgeFactory(Supplier edgeFactory) {
    this.edgeFactory = edgeFactory;
  }
}
