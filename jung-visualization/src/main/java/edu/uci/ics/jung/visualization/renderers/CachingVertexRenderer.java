package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.algorithms.layout.LayoutChangeListener;
import edu.uci.ics.jung.algorithms.layout.LayoutEvent;
import edu.uci.ics.jung.algorithms.layout.LayoutEventSupport;
import edu.uci.ics.jung.algorithms.layout.LayoutNetworkEvent;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CachingVertexRenderer<N, E> extends BasicVertexRenderer<N, E>
    implements ChangeListener, LayoutChangeListener<N, Point2D> {

  protected Map<N, Shape> vertexShapeMap = new HashMap<>();

  protected Set<N> dirtyVertices = new HashSet<>();

  @SuppressWarnings({"rawtypes", "unchecked"})
  public CachingVertexRenderer(BasicVisualizationServer<N, E> vv) {
    vv.getRenderContext().getMultiLayerTransformer().addChangeListener(this);
    VisualizationModel<N, E, Point2D> visualizationModel = vv.getModel();
    if (visualizationModel instanceof LayoutEventSupport) {
      ((LayoutEventSupport) visualizationModel).addLayoutChangeListener(this);
    }
  }

  /** Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>. */
  protected void paintIconForVertex(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E, Point2D> visualizationModel,
      N v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();

    int[] coords = new int[2];
    Shape shape = vertexShapeMap.get(v);
    if (shape == null || dirtyVertices.contains(v)) {
      shape = prepareFinalVertexShape(renderContext, visualizationModel, v, coords);
      vertexShapeMap.put(v, shape);
      dirtyVertices.remove(v);
    }
    if (renderContext.getVertexIconTransformer() != null) {
      Icon icon = renderContext.getVertexIconTransformer().apply(v);
      if (icon != null) {

        g.draw(icon, renderContext.getScreenDevice(), shape, coords[0], coords[1]);

      } else {
        paintShapeForVertex(renderContext, visualizationModel, v, shape);
      }
    } else {
      paintShapeForVertex(renderContext, visualizationModel, v, shape);
    }
  }

  public void stateChanged(ChangeEvent evt) {
    vertexShapeMap.clear();
  }

  public void layoutChanged(LayoutEvent<N, Point2D> evt) {
    this.dirtyVertices.add(evt.getNode());
  }

  public void layoutChanged(LayoutNetworkEvent<N, Point2D> evt) {
    this.dirtyVertices.add(evt.getNode());
  }
}
