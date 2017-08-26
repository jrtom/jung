package edu.uci.ics.jung.visualization.renderers;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.layout.LayoutChangeListener;
import edu.uci.ics.jung.visualization.layout.LayoutEvent;
import edu.uci.ics.jung.visualization.layout.LayoutEventSupport;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CachingEdgeRenderer<V, E> extends BasicEdgeRenderer<V, E>
    implements ChangeListener, LayoutChangeListener<V> {

  protected Map<E, Shape> edgeShapeMap = new HashMap<E, Shape>();
  protected Set<E> dirtyEdges = new HashSet<E>();

  public CachingEdgeRenderer(Layout<V> layout, RenderContext<V, E> rc) {
    super(layout, rc);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public CachingEdgeRenderer(BasicVisualizationServer<V, E> vv) {
    super(vv.getGraphLayout(), vv.getRenderContext());
    vv.getRenderContext().getMultiLayerTransformer().addChangeListener(this);
    Layout<V> layout = vv.getGraphLayout();
    if (layout instanceof LayoutEventSupport) {
      ((LayoutEventSupport) layout).addLayoutChangeListener(this);
    }
  }
  /**
   * Draws the edge <code>e</code>, whose endpoints are at <code>(x1,y1)</code> and <code>(x2,y2)
   * </code>, on the graphics context <code>g</code>. The <code>Shape</code> provided by the <code>
   * EdgeShapeFunction</code> instance is scaled in the x-direction so that its width is equal to
   * the distance between <code>(x1,y1)</code> and <code>(x2,y2)</code>.
   */
  @Override
  protected void drawSimpleEdge(E e) {

    int[] coords = new int[4];
    boolean[] loop = new boolean[1];

    Shape edgeShape = edgeShapeMap.get(e);
    if (edgeShape == null || dirtyEdges.contains(e)) {
      edgeShape = prepareFinalEdgeShape(e, coords, loop);
      edgeShapeMap.put(e, edgeShape);
      dirtyEdges.remove(e);
    }

    int x1 = coords[0];
    int y1 = coords[1];
    int x2 = coords[2];
    int y2 = coords[3];
    boolean isLoop = loop[0];

    GraphicsDecorator g = renderContext.getGraphicsContext();
    Network<V, E> graph = renderContext.getNetwork();
    boolean edgeHit = true;
    boolean arrowHit = true;
    Rectangle deviceRectangle = null;
    JComponent vv = renderContext.getScreenDevice();
    if (vv != null) {
      Dimension d = vv.getSize();
      deviceRectangle = new Rectangle(0, 0, d.width, d.height);
    }
    MutableTransformer vt = renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW);
    if (vt instanceof LensTransformer) {
      vt = ((LensTransformer) vt).getDelegate();
    }
    edgeHit = vt.transform(edgeShape).intersects(deviceRectangle);

    if (edgeHit == true) {

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

        arrowHit =
            renderContext
                .getMultiLayerTransformer()
                .getTransformer(Layer.VIEW)
                .transform(destVertexShape)
                .intersects(deviceRectangle);
        if (arrowHit) {

          AffineTransform at =
              edgeArrowRenderingSupport.getArrowTransform(
                  renderContext, edgeShape, destVertexShape);
          if (at == null) {
            return;
          }
          Shape arrow = renderContext.getEdgeArrow();
          arrow = at.createTransformedShape(arrow);
          g.setPaint(renderContext.getArrowFillPaintTransformer().apply(e));
          g.fill(arrow);
          g.setPaint(renderContext.getArrowDrawPaintTransformer().apply(e));
          g.draw(arrow);
        }
        if (!graph.isDirected()) {
          Shape vertexShape =
              renderContext.getVertexShapeTransformer().apply(graph.incidentNodes(e).nodeU());
          xf = AffineTransform.getTranslateInstance(x1, y1);
          vertexShape = xf.createTransformedShape(vertexShape);

          arrowHit =
              renderContext
                  .getMultiLayerTransformer()
                  .getTransformer(Layer.VIEW)
                  .transform(vertexShape)
                  .intersects(deviceRectangle);

          if (arrowHit) {
            AffineTransform at =
                edgeArrowRenderingSupport.getReverseArrowTransform(
                    renderContext, edgeShape, vertexShape, !isLoop);
            if (at == null) {
              return;
            }
            Shape arrow = renderContext.getEdgeArrow();
            arrow = at.createTransformedShape(arrow);
            g.setPaint(renderContext.getArrowFillPaintTransformer().apply(e));
            g.fill(arrow);
            g.setPaint(renderContext.getArrowDrawPaintTransformer().apply(e));
            g.draw(arrow);
          }
        }
        // restore paint and stroke
        if (new_stroke != null) {
          g.setStroke(old_stroke);
        }
      }

      // restore old paint
      g.setPaint(oldPaint);
    }
  }

  @Override
  public void stateChanged(ChangeEvent evt) {
    //		System.err.println("got change event "+evt);
    edgeShapeMap.clear();
  }

  @Override
  public void layoutChanged(LayoutEvent<V> evt) {
    V v = evt.getVertex();
    Network<V, E> graph = renderContext.getNetwork();
    dirtyEdges.addAll(graph.incidentEdges(v));
  }
}
