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

import com.google.common.collect.Multimap;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialGrid;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.Caching;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
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
public class BasicVisualizationServer extends JPanel
    implements ChangeListener, ChangeEventSupport, VisualizationServer {

  static Logger log = LoggerFactory.getLogger(BasicVisualizationServer.class);

  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);

  /** holds the state of this View */
  protected VisualizationModel model;

  /** handles the actual drawing of graph elements */
  protected Renderer renderer;

  /** rendering hints used in drawing. Anti-aliasing is on by default */
  protected Map<Key, Object> renderingHints = new HashMap<Key, Object>();

  /** holds the state of which vertices of the graph are currently 'picked' */
  protected PickedState pickedVertexState;

  /** holds the state of which edges of the graph are currently 'picked' */
  protected PickedState pickedEdgeState;

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

  protected RenderContext renderContext;

  /**
   * Create an instance with the specified Layout.
   *
   * @param layout The Layout to apply, with its associated Graph
   */
  public BasicVisualizationServer(Network network, Layout layout) {
    this(new DefaultVisualizationModel(network, layout));
  }

  /**
   * Create an instance with the specified Layout and view dimension.
   *
   * @param layout The Layout to apply, with its associated Graph
   * @param preferredSize the preferred size of this View
   */
  public BasicVisualizationServer(Network network, Layout layout, Dimension preferredSize) {
    this(new DefaultVisualizationModel(network, layout, preferredSize), preferredSize);
  }

  /**
   * Create an instance with the specified model and a default dimension (600x600).
   *
   * @param model the model to use
   */
  public BasicVisualizationServer(VisualizationModel model) {
    this(model, new Dimension(600, 600));
  }

  /**
   * Create an instance with the specified model and view dimension.
   *
   * @param model the model to use
   * @param preferredSize initial preferred size of the view
   */
  public BasicVisualizationServer(VisualizationModel model, Dimension preferredSize) {
    this.model = model;
    renderContext = new PluggableRenderContext(model.getLayoutMediator().getNetwork());
    renderer = new BasicRenderer();
    model.addChangeListener(this);
    setDoubleBuffered(false);
    this.addComponentListener(new VisualizationListener(this));

    setPickSupport(new ShapePickSupport(this));
    setPickedVertexState(new MultiPickedState());
    setPickedEdgeState(new MultiPickedState());

    renderContext.setEdgeDrawPaintTransformer(
        new PickableEdgePaintTransformer(getPickedEdgeState(), Color.black, Color.cyan));
    renderContext.setVertexFillPaintTransformer(
        new PickableVertexPaintTransformer(getPickedVertexState(), Color.red, Color.yellow));

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
   * Always sanity-check getSize so that we don't use a value that is improbable
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
   * correct size.
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

  public VisualizationModel getModel() {
    return model;
  }

  public void setModel(VisualizationModel model) {
    this.model = model;
  }

  public void stateChanged(ChangeEvent e) {
    repaint();
    fireStateChanged();
  }

  public void setRenderer(Renderer r) {
    this.renderer = r;
    repaint();
  }

  public Renderer getRenderer() {
    return renderer;
  }

  public void setLayoutMediator(LayoutMediator layoutMediator, Dimension d) {
    model.setLayoutMediator(layoutMediator, d);
  }

  public void setLayoutMediator(LayoutMediator layoutMediator) {
    if (log.isDebugEnabled()) {
      log.debug("setLayoutMediator to " + layoutMediator);
    }
    Dimension viewSize = getPreferredSize();
    if (this.isShowing()) {
      viewSize = getSize();
    }
    this.setLayoutMediator(layoutMediator, viewSize);
  }

  public void scaleToLayout(ScalingControl scaler) {
    Dimension vd = getPreferredSize();
    if (this.isShowing()) {
      vd = getSize();
    }
    Dimension ld = getGraphLayout().getSize();
    if (vd.equals(ld) == false) {
      scaler.scale(this, (float) (vd.getWidth() / ld.getWidth()), new Point2D.Double());
    }
  }

  public Layout getGraphLayout() {
    return model.getLayoutMediator().getLayout();
  }

  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    if (aFlag == true) {
      Dimension d = this.getSize();
      if (d.width <= 0 || d.height <= 0) {
        d = this.getPreferredSize();
      }
      model.getLayoutMediator().getLayout().setSize(d);
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

  protected Spatial getSpatial(Graphics2D g2d, Layout<Object> layout, Dimension d)
      throws NoninvertibleTransformException {

    AffineTransform spatialTransform = new AffineTransform(g2d.getTransform());

    spatialTransform.concatenate(
        renderContext.getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getTransform());
    spatialTransform.concatenate(
        renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW).getTransform());

    spatialTransform = spatialTransform.createInverse();

    Dimension layoutSize = model.getLayoutMediator().getLayout().getSize();
    AffineTransform layoutTransform =
        renderContext.getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getTransform();
    Rectangle2D layoutRectangle = new Rectangle2D.Double(0, 0, layoutSize.width, layoutSize.height);
    Shape transformedLayoutShape = layoutTransform.createTransformedShape(layoutRectangle);
    Point2D viewOriginOnLayout = new Point2D.Double();
    Point2D viewExtremeOnLayout = new Point2D.Double();

    layoutTransform.inverseTransform(new Point2D.Double(0, 0), viewOriginOnLayout);
    layoutTransform.inverseTransform(
        new Point2D.Double(getSize().width, getSize().height), viewExtremeOnLayout);

    Rectangle2D viewProjection = new Rectangle2D.Double();
    viewProjection.setFrameFromDiagonal(viewOriginOnLayout, viewExtremeOnLayout);

    Rectangle2D union = new Rectangle2D.Double();
    Rectangle2D.union(transformedLayoutShape.getBounds(), viewProjection, union);

    if (log.isDebugEnabled()) {
      log.debug(
          "the union of "
              + transformedLayoutShape.getBounds()
              + " and "
              + viewProjection.getBounds()
              + " is "
              + union.getBounds());
    }

    SpatialGrid spatial = new SpatialGrid(union.getBounds(), 20, 20);
    Multimap<Integer, Object> spatialMap = spatial.getMap();
    Network graph = model.getLayoutMediator().getNetwork();
    for (Object node : graph.nodes()) {
      spatialMap.put(spatial.getBoxNumberFromLocation(layout.apply(node)), node);
    }

    Rectangle2D shape = new Rectangle2D.Double(0, 0, (double) d.width, (double) d.height);

    Shape visibleShape = spatialTransform.createTransformedShape(shape);

    Rectangle visibleRectangle = visibleShape.getBounds();
    if (log.isDebugEnabled()) {
      log.debug("visibleRectangle:" + visibleRectangle.getBounds());
    }
    spatial.setVisibleArea(visibleRectangle);
    return spatial;
  }

  protected void renderGraph(Graphics2D g2d) {
    if (renderContext.getGraphicsContext() == null) {
      renderContext.setGraphicsContext(new GraphicsDecorator(g2d));
    } else {
      renderContext.getGraphicsContext().setDelegate(g2d);
    }
    renderContext.setScreenDevice(this);
    Layout layout = model.getLayoutMediator().getLayout();

    g2d.setRenderingHints(renderingHints);

    // the size of the VisualizationViewer
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

    if (layout instanceof Caching) {
      ((Caching) layout).clear();
    }

    try {
      Spatial spatial = getSpatial(g2d, layout, d);
      renderer.render(renderContext, model.getLayoutMediator(), spatial);
    } catch (NoninvertibleTransformException ex) {
      log.debug("Possible problem with transform. Failover to not use Spatial data structure.", ex);
      renderer.render(renderContext, model.getLayoutMediator());
    }

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
   * VisualizationListener reacts to changes in the size of the VisualizationViewer. When the size
   * changes, it ensures that the offscreen image is sized properly. If the layout is locked to this
   * view size, then the layout is also resized to be the same as the view size.
   */
  protected class VisualizationListener extends ComponentAdapter {
    protected BasicVisualizationServer vv;

    public VisualizationListener(BasicVisualizationServer vv) {
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

  public PickedState getPickedVertexState() {
    return pickedVertexState;
  }

  public PickedState getPickedEdgeState() {
    return pickedEdgeState;
  }

  public void setPickedVertexState(PickedState pickedVertexState) {
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

  public void setPickedEdgeState(PickedState pickedEdgeState) {
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

  public NetworkElementAccessor getPickSupport() {
    return renderContext.getPickSupport();
  }

  public void setPickSupport(NetworkElementAccessor pickSupport) {
    renderContext.setPickSupport(pickSupport);
  }

  public Point2D getCenter() {
    Dimension d = getSize();
    return new Point2D.Float(d.width / 2, d.height / 2);
  }

  public RenderContext getRenderContext() {
    return renderContext;
  }

  public void setRenderContext(RenderContext renderContext) {
    this.renderContext = renderContext;
  }
}
