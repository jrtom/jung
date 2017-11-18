/*
\* Copyright (c) 2003, The JUNG Authors
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.util.Caching;
import edu.uci.ics.jung.visualization.annotations.Annotation;
import edu.uci.ics.jung.visualization.annotations.AnnotationPaintable;
import edu.uci.ics.jung.visualization.annotations.AnnotationRenderer;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialGrid;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that maintains many of the details necessary for creating visualizations of graphs. This
 * is the old VisualizationViewer without tooltips and mouse behaviors. Its purpose is to be a base
 * class that can also be used on the server side of a multi-tiered application.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 * @author Danyel Fisher
 */
@SuppressWarnings("serial")
public class BasicVisualizationServer<N, E> extends JPanel
    implements ChangeListener, ChangeEventSupport, VisualizationServer<N, E> {

  static Logger log = LoggerFactory.getLogger(BasicVisualizationServer.class);

  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);

  /** holds the state of this View */
  protected VisualizationModel<N, E, Point2D> model;

  /** handles the actual drawing of graph elements */
  protected Renderer<N, E> renderer;

  /** rendering hints used in drawing. Anti-aliasing is on by default */
  protected Map<Key, Object> renderingHints = new HashMap<Key, Object>();

  /** holds the state of which vertices of the graph are currently 'picked' */
  protected PickedState<N> pickedVertexState;

  /** holds the state of which edges of the graph are currently 'picked' */
  protected PickedState<E> pickedEdgeState;

  /**
   * a listener used to cause pick events to result in repaints, even if they come from another view
   */
  protected ItemListener pickEventListener;

  /** an offscreen image to render the graph Used if doubleBuffered is set to true */
  protected BufferedImage offscreen;

  /** graphics context for the offscreen image Used if doubleBuffered is set to true */
  protected Graphics2D offscreenG2d;

  /** user-settable choice to use the offscreen image or not. 'false' by default */
  protected boolean doubleBuffered;

  /**
   * a collection of user-implementable functions to render under the topology (before the graph is
   * rendered)
   */
  protected List<Paintable> preRenderers = new ArrayList<Paintable>();

  /**
   * a collection of user-implementable functions to render over the topology (after the graph is
   * rendered)
   */
  protected List<Paintable> postRenderers = new ArrayList<Paintable>();

  protected RenderContext<N, E> renderContext;

  /**
   * Create an instance with the specified Layout.
   *
   * @param
   */
  public BasicVisualizationServer(
      Network<N, E> network, LayoutAlgorithm<N, Point2D> layoutAlgorithm) {
    this(new BaseVisualizationModel<N, E>(network, layoutAlgorithm), DEFAULT_SIZE);
  }

  /**
   * Create an instance with the specified Layout and view dimension.
   *
   * @param
   * @param preferredSize the preferred layoutSize of this View
   */
  public BasicVisualizationServer(
      Network<N, E> network, LayoutAlgorithm<N, Point2D> layoutAlgorithm, Dimension preferredSize) {
    this(new BaseVisualizationModel<N, E>(network, layoutAlgorithm, preferredSize), preferredSize);
  }

  /**
   * Create an instance with the specified model and a default dimension (600x600).
   *
   * @param model the model to use
   */
  public BasicVisualizationServer(VisualizationModel<N, E, Point2D> model) {
    this(model, DEFAULT_SIZE);
  }

  /**
   * Create an instance with the specified model and view dimension.
   *
   * @param model the model to use
   * @param preferredSize initial preferred layoutSize of the view
   */
  public BasicVisualizationServer(
      VisualizationModel<N, E, Point2D> model, Dimension preferredSize) {
    this.model = model;
    renderContext = new PluggableRenderContext<N, E>(model.getNetwork());
    renderer = new BasicRenderer<N, E>();
    model.addChangeListener(this);
    setDoubleBuffered(false);
    this.addComponentListener(new VisualizationListener(this));

    setPickSupport(new ShapePickSupport<N, E>(this));
    setPickedVertexState(new MultiPickedState<N>());
    setPickedEdgeState(new MultiPickedState<E>());

    renderContext.setEdgeDrawPaintTransformer(
        new PickableEdgePaintTransformer<E>(getPickedEdgeState(), Color.black, Color.cyan));
    renderContext.setVertexFillPaintTransformer(
        new PickableVertexPaintTransformer<N>(getPickedVertexState(), Color.red, Color.yellow));

    setPreferredSize(preferredSize);
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    renderContext.getMultiLayerTransformer().addChangeListener(this);
  }

  @Override
  public void setDoubleBuffered(boolean doubleBuffered) {
    this.doubleBuffered = doubleBuffered;
  }

  @Override
  public boolean isDoubleBuffered() {
    return doubleBuffered;
  }

  /**
   * Always sanity-check getLayoutSize so that we don't use a value that is improbable
   *
   * @see java.awt.Component#getSize()
   */
  @Override
  public Dimension getSize() {
    Dimension d = super.getSize();
    if (d.width <= 0 || d.height <= 0) {
      d = getPreferredSize();
    }
    return d;
  }

  /**
   * Ensure that, if doubleBuffering is enabled, the offscreen image buffer exists and is the
   * correct layoutSize.
   *
   * @param d the expected Dimension of the offscreen buffer
   */
  protected void checkOffscreenImage(Dimension d) {
    if (doubleBuffered) {
      if (offscreen == null
          || offscreen.getWidth() != d.width
          || offscreen.getHeight() != d.height) {
        offscreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        offscreenG2d = offscreen.createGraphics();
      }
    }
  }

  public VisualizationModel<N, E, Point2D> getModel() {
    return model;
  }

  public void setModel(VisualizationModel<N, E, Point2D> model) {
    this.model = model;
  }

  public void stateChanged(ChangeEvent e) {
    repaint();
    fireStateChanged();
  }

  public void setRenderer(Renderer<N, E> r) {
    this.renderer = r;
    repaint();
  }

  public Renderer<N, E> getRenderer() {
    return renderer;
  }

  public void scaleToLayout(ScalingControl scaler) {
    Dimension vd = getPreferredSize();
    if (this.isShowing()) {
      vd = getSize();
    }
    Dimension ld = model.getLayoutSize();
    if (vd.equals(ld) == false) {
      scaler.scale(this, (float) (vd.getWidth() / ld.getWidth()), new Point2D.Double());
    }
  }

  public Map<Key, Object> getRenderingHints() {
    return renderingHints;
  }

  public void setRenderingHints(Map<Key, Object> renderingHints) {
    this.renderingHints = renderingHints;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;
    if (doubleBuffered) {
      checkOffscreenImage(getSize());
      renderGraph(offscreenG2d);
      g2d.drawImage(offscreen, null, 0, 0);
    } else {
      renderGraph(g2d);
    }
  }

  public Shape viewOnLayout() {
    Dimension d = this.getSize();
    MultiLayerTransformer vt = renderContext.getMultiLayerTransformer();
    Shape s = new Rectangle2D.Double(0, 0, d.width, d.height);
    return vt.inverseTransform(s);
  }

  protected void renderGraph(Graphics2D g2d) {
    if (renderContext.getGraphicsContext() == null) {
      renderContext.setGraphicsContext(new GraphicsDecorator(g2d));
    } else {
      renderContext.getGraphicsContext().setDelegate(g2d);
    }
    renderContext.setScreenDevice(this);

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

    Spatial spatial = model.getSpatial();

    AnnotationPaintable lowerAnnotationPaintable = null;
    // when logging is set to trace, the grid will be drawn on the graph visualization
    if (log.isTraceEnabled()) {
      AnnotationRenderer annotationRenderer = new AnnotationRenderer();
      lowerAnnotationPaintable = new AnnotationPaintable(renderContext, annotationRenderer);
      if (spatial != null) {
        List<Rectangle2D> grid = ((SpatialGrid) spatial).getGrid();
        int num = 0;
        for (Rectangle2D r : grid) {
          Point2D p =
              this.getRenderContext()
                  .getMultiLayerTransformer()
                  .inverseTransform(new Point2D.Double(r.getX(), r.getY()));
          Annotation<Shape> annotation =
              new Annotation<Shape>(r, Annotation.Layer.LOWER, Color.BLACK, false, p);
          lowerAnnotationPaintable.add(annotation);
          String label = num + ":" + spatial.getMap().get(num);
          int stringWidth = g2d.getFontMetrics().stringWidth(label);
          Point2D center =
              new Point2D.Double(
                  r.getX() + (r.getWidth() - stringWidth) / 2, r.getY() + r.getHeight() / 2);

          Annotation<String> annotation2 =
              new Annotation<String>(label, Annotation.Layer.LOWER, Color.BLACK, false, center);
          lowerAnnotationPaintable.add(annotation2);
          num++;
        }
        this.addPreRenderPaintable(lowerAnnotationPaintable);
      }
    }

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
    if (lowerAnnotationPaintable != null) {
      this.removePreRenderPaintable(lowerAnnotationPaintable);
    }

    if (model instanceof Caching) {
      ((Caching) model).clear();
    }

    renderer.render(renderContext, model, spatial);

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
   * VisualizationListener reacts to changes in the layoutSize of the VisualizationViewer. When the
   * layoutSize changes, it ensures that the offscreen image is sized properly. If the layout is
   * locked to this view layoutSize, then the layout is also resized to be the same as the view
   * layoutSize.
   */
  protected class VisualizationListener extends ComponentAdapter {
    protected BasicVisualizationServer<N, E> vv;

    public VisualizationListener(BasicVisualizationServer<N, E> vv) {
      this.vv = vv;
    }

    /** create a new offscreen image for the graph whenever the window is resied */
    @Override
    public void componentResized(ComponentEvent e) {
      Dimension d = vv.getSize();
      if (d.width <= 0 || d.height <= 0) {
        return;
      }
      checkOffscreenImage(d);
      repaint();
    }
  }

  public void addPreRenderPaintable(Paintable paintable) {
    if (preRenderers == null) {
      preRenderers = new ArrayList<Paintable>();
    }
    preRenderers.add(paintable);
  }

  public void prependPreRenderPaintable(Paintable paintable) {
    if (preRenderers == null) {
      preRenderers = new ArrayList<Paintable>();
    }
    preRenderers.add(0, paintable);
  }

  public void removePreRenderPaintable(Paintable paintable) {
    if (preRenderers != null) {
      preRenderers.remove(paintable);
    }
  }

  public void addPostRenderPaintable(Paintable paintable) {
    if (postRenderers == null) {
      postRenderers = new ArrayList<Paintable>();
    }
    postRenderers.add(paintable);
  }

  public void prependPostRenderPaintable(Paintable paintable) {
    if (postRenderers == null) {
      postRenderers = new ArrayList<Paintable>();
    }
    postRenderers.add(0, paintable);
  }

  public void removePostRenderPaintable(Paintable paintable) {
    if (postRenderers != null) {
      postRenderers.remove(paintable);
    }
  }

  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }

  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
  }

  public ChangeListener[] getChangeListeners() {
    return changeSupport.getChangeListeners();
  }

  public void fireStateChanged() {
    changeSupport.fireStateChanged();
  }

  public PickedState<N> getPickedVertexState() {
    return pickedVertexState;
  }

  public PickedState<E> getPickedEdgeState() {
    return pickedEdgeState;
  }

  public void setPickedVertexState(PickedState<N> pickedVertexState) {
    if (pickEventListener != null && this.pickedVertexState != null) {
      this.pickedVertexState.removeItemListener(pickEventListener);
    }
    this.pickedVertexState = pickedVertexState;
    this.renderContext.setPickedVertexState(pickedVertexState);
    if (pickEventListener == null) {
      pickEventListener =
          new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
              repaint();
            }
          };
    }
    pickedVertexState.addItemListener(pickEventListener);
  }

  public void setPickedEdgeState(PickedState<E> pickedEdgeState) {
    if (pickEventListener != null && this.pickedEdgeState != null) {
      this.pickedEdgeState.removeItemListener(pickEventListener);
    }
    this.pickedEdgeState = pickedEdgeState;
    this.renderContext.setPickedEdgeState(pickedEdgeState);
    if (pickEventListener == null) {
      pickEventListener =
          new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
              repaint();
            }
          };
    }
    pickedEdgeState.addItemListener(pickEventListener);
  }

  public NetworkElementAccessor<N, E> getPickSupport() {
    return renderContext.getPickSupport();
  }

  public void setPickSupport(NetworkElementAccessor<N, E> pickSupport) {
    renderContext.setPickSupport(pickSupport);
  }

  public Point2D getCenter() {
    Dimension d = getSize();
    return new Point2D.Float(d.width / 2, d.height / 2);
  }

  public RenderContext<N, E> getRenderContext() {
    return renderContext;
  }

  public void setRenderContext(RenderContext<N, E> renderContext) {
    this.renderContext = renderContext;
  }
}
