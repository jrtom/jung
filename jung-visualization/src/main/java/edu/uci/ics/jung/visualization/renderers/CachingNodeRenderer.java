package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.layout.util.LayoutChangeListener;
import edu.uci.ics.jung.layout.util.LayoutEvent;
import edu.uci.ics.jung.layout.util.LayoutEventSupport;
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Shape;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CachingNodeRenderer<N, E> extends BasicNodeRenderer<N, E>
    implements ChangeListener, LayoutChangeListener<N> {

  protected Map<N, Shape> nodeShapeMap = new HashMap<>();

  protected Set<N> dirtyNodes = new HashSet<>();

  @SuppressWarnings({"rawtypes", "unchecked"})
  public CachingNodeRenderer(BasicVisualizationServer<N, E> vv) {
    vv.getRenderContext().getMultiLayerTransformer().addChangeListener(this);
    VisualizationModel<N, E> visualizationModel = vv.getModel();
    if (visualizationModel instanceof LayoutEventSupport) {
      ((LayoutEventSupport) visualizationModel).addLayoutChangeListener(this);
    }
  }

  /** Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>. */
  protected void paintIconForNode(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();

    int[] coords = new int[2];
    Shape shape = nodeShapeMap.get(v);
    if (shape == null || dirtyNodes.contains(v)) {
      shape = prepareFinalNodeShape(renderContext, visualizationModel, v, coords);
      nodeShapeMap.put(v, shape);
      dirtyNodes.remove(v);
    }
    if (renderContext.getNodeIconFunction() != null) {
      Icon icon = renderContext.getNodeIconFunction().apply(v);
      if (icon != null) {

        g.draw(icon, renderContext.getScreenDevice(), shape, coords[0], coords[1]);

      } else {
        paintShapeForNode(renderContext, visualizationModel, v, shape);
      }
    } else {
      paintShapeForNode(renderContext, visualizationModel, v, shape);
    }
  }

  public void stateChanged(ChangeEvent evt) {
    nodeShapeMap.clear();
  }

  public void layoutChanged(LayoutEvent<N> evt) {
    this.dirtyNodes.add(evt.getNode());
  }

  public void layoutChanged(LayoutNetworkEvent<N> evt) {
    this.dirtyNodes.add(evt.getNode());
  }
}
