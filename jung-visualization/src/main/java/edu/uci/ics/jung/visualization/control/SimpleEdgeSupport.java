package edu.uci.ics.jung.visualization.control;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;
import java.util.function.Supplier;

public class SimpleEdgeSupport<V, E> implements EdgeSupport<V, E> {

  protected Point2D down;
  protected EdgeEffects<V, E> edgeEffects;
  protected Supplier<E> edgeFactory;
  protected V startVertex;

  public SimpleEdgeSupport(Supplier<E> edgeFactory) {
    this.edgeFactory = edgeFactory;
    this.edgeEffects = new CubicCurveEdgeEffects<V, E>();
  }

  @Override
  public void startEdgeCreate(
      BasicVisualizationServer<V, E> vv, V startVertex, Point2D startPoint) {
    this.startVertex = startVertex;
    this.down = startPoint;
    this.edgeEffects.startEdgeEffects(vv, startPoint, startPoint);
    if (vv.getModel().getNetwork().isDirected()) {
      this.edgeEffects.startArrowEffects(vv, startPoint, startPoint);
    }
    vv.repaint();
  }

  @Override
  public void midEdgeCreate(BasicVisualizationServer<V, E> vv, Point2D midPoint) {
    if (startVertex != null) {
      this.edgeEffects.midEdgeEffects(vv, down, midPoint);
      if (vv.getModel().getNetwork().isDirected()) {
        this.edgeEffects.midArrowEffects(vv, down, midPoint);
      }
      vv.repaint();
    }
  }

  @Override
  public void endEdgeCreate(BasicVisualizationServer<V, E> vv, V endVertex) {
    Preconditions.checkState(
        vv.getModel().getNetwork() instanceof MutableNetwork<?, ?>, "graph must be mutable");
    if (startVertex != null) {
      MutableNetwork<V, E> graph = (MutableNetwork<V, E>) vv.getModel().getNetwork();
      graph.addEdge(startVertex, endVertex, edgeFactory.get());
      vv.repaint();
    }
    startVertex = null;
    edgeEffects.endEdgeEffects(vv);
    edgeEffects.endArrowEffects(vv);
  }

  public EdgeEffects<V, E> getEdgeEffects() {
    return edgeEffects;
  }

  public void setEdgeEffects(EdgeEffects<V, E> edgeEffects) {
    this.edgeEffects = edgeEffects;
  }

  public Supplier<E> getEdgeFactory() {
    return edgeFactory;
  }

  public void setEdgeFactory(Supplier<E> edgeFactory) {
    this.edgeFactory = edgeFactory;
  }
}
