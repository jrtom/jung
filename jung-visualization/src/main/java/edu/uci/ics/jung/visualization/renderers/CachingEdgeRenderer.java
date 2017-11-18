package edu.uci.ics.jung.visualization.renderers;

import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.util.LayoutChangeListener;
import edu.uci.ics.jung.layout.util.LayoutEvent;
import edu.uci.ics.jung.layout.util.LayoutEventSupport;
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CachingEdgeRenderer<N, E> extends BasicEdgeRenderer<N, E>
    implements ChangeListener, LayoutChangeListener<N, Point2D> {

  protected Map<E, Shape> edgeShapeMap = new HashMap<E, Shape>();
  protected Set dirtyEdges = new HashSet<>();

  @SuppressWarnings({"rawtypes", "unchecked"})
  public CachingEdgeRenderer(BasicVisualizationServer<N, E> vv) {
    vv.getRenderContext().getMultiLayerTransformer().addChangeListener(this);
    LayoutModel<N, Point2D> layoutModel = vv.getModel().getLayoutModel();
    if (layoutModel instanceof LayoutEventSupport) {
      ((LayoutEventSupport) layoutModel).addLayoutChangeListener(this);
    }
  }
  /**
   * Draws the edge <code>e</code>, whose endpoints are at <code>(x1,y1)</code> and <code>(x2,y2)
   * </code>, on the graphics context <code>g</code>. The <code>Shape</code> provided by the <code>
   * EdgeShapeFunction</code> instance is scaled in the x-direction so that its width is equal to
   * the distance between <code>(x1,y1)</code> and <code>(x2,y2)</code>.
   */
  @Override
  protected void drawSimpleEdge(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E, Point2D> visualizationModel,
      E e) {

    int[] coords = new int[4];
    boolean[] loop = new boolean[1];

    Shape edgeShape = edgeShapeMap.get(e);
    if (edgeShape == null || dirtyEdges.contains(e)) {
      edgeShape = prepareFinalEdgeShape(renderContext, visualizationModel, e, coords, loop);
      edgeShapeMap.put(e, edgeShape);
      dirtyEdges.remove(e);
    }

    int x1 = coords[0];
    int y1 = coords[1];
    int x2 = coords[2];
    int y2 = coords[3];
    boolean isLoop = loop[0];

    GraphicsDecorator g = renderContext.getGraphicsContext();
    Network<N, E> graph = visualizationModel.getNetwork();

    Paint oldPaint = g.getPaint();

    // get Paints for filling and drawing
    // (filling is done first so that drawing and label use same Paint)
    Paint fill_paint = renderContext.getEdgeFillPaintTransformer().apply(e);
    if (fill_paint != null) {
      g.setPaint(fill_paint);
      g.fill(edgeShape);
    }
    Paint draw_paint = renderContext.getEdgeDrawPaintTransformer().apply(e);
    if (draw_paint != null) {
      g.setPaint(draw_paint);
      g.draw(edgeShape);
    }

    float scalex = (float) g.getTransform().getScaleX();
    float scaley = (float) g.getTransform().getScaleY();
    // see if arrows are too small to bother drawing
    if (scalex < .3 || scaley < .3) {
      return;
    }

    if (renderContext.renderEdgeArrow()) {

      Stroke new_stroke = renderContext.getEdgeArrowStrokeTransformer().apply(e);
      Stroke old_stroke = g.getStroke();
      if (new_stroke != null) {
        g.setStroke(new_stroke);
      }

      Shape destVertexShape =
          renderContext.getVertexShapeTransformer().apply(graph.incidentNodes(e).nodeV());

      AffineTransform xf = AffineTransform.getTranslateInstance(x2, y2);
      destVertexShape = xf.createTransformedShape(destVertexShape);

      AffineTransform at =
          edgeArrowRenderingSupport.getArrowTransform(renderContext, edgeShape, destVertexShape);
      if (at == null) {
        return;
      }
      Shape arrow = renderContext.getEdgeArrow();
      arrow = at.createTransformedShape(arrow);
      g.setPaint(renderContext.getArrowFillPaintTransformer().apply(e));
      g.fill(arrow);
      g.setPaint(renderContext.getArrowDrawPaintTransformer().apply(e));
      g.draw(arrow);

      if (!graph.isDirected()) {
        Shape vertexShape =
            renderContext.getVertexShapeTransformer().apply(graph.incidentNodes(e).nodeU());
        xf = AffineTransform.getTranslateInstance(x1, y1);
        vertexShape = xf.createTransformedShape(vertexShape);

        at =
            edgeArrowRenderingSupport.getReverseArrowTransform(
                renderContext, edgeShape, vertexShape, !isLoop);
        if (at == null) {
          return;
        }
        arrow = renderContext.getEdgeArrow();
        arrow = at.createTransformedShape(arrow);
        g.setPaint(renderContext.getArrowFillPaintTransformer().apply(e));
        g.fill(arrow);
        g.setPaint(renderContext.getArrowDrawPaintTransformer().apply(e));
        g.draw(arrow);
      }
      // restore paint and stroke
      if (new_stroke != null) {
        g.setStroke(old_stroke);
      }
    }

    // restore old paint
    g.setPaint(oldPaint);
  }

  @Override
  public void stateChanged(ChangeEvent evt) {
    edgeShapeMap.clear();
  }

  //  @Override
  public void layoutChanged(LayoutNetworkEvent<N, Point2D> evt) {
    N node = evt.getNode();
    Network<N, ?> network = evt.getNetwork();
    dirtyEdges.addAll(network.incidentEdges(node));
  }

  public void layoutChanged(LayoutEvent<N, Point2D> evt) {
    System.err.println("FIX ME");
  }
}
