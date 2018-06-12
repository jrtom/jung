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

import static edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;

import com.google.common.collect.Lists;
import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.util.Caching;
import edu.uci.ics.jung.layout.util.LayoutChangeListener;
import edu.uci.ics.jung.layout.util.LayoutEvent;
import edu.uci.ics.jung.layout.util.LayoutEventSupport;
import edu.uci.ics.jung.layout.util.LayoutNetworkEvent;
import edu.uci.ics.jung.visualization.annotations.AnnotationPaintable;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.control.TransformSupport;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.layout.BoundingRectangleCollector;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.properties.VisualizationViewerUI;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialRTree;
import edu.uci.ics.jung.visualization.spatial.rtree.QuadraticLeafSplitter;
import edu.uci.ics.jung.visualization.spatial.rtree.QuadraticSplitter;
import edu.uci.ics.jung.visualization.spatial.rtree.RStarLeafSplitter;
import edu.uci.ics.jung.visualization.spatial.rtree.RStarSplitter;
import edu.uci.ics.jung.visualization.spatial.rtree.SplitterContext;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.*;
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
    implements ChangeListener,
        ChangeEventSupport,
        VisualizationServer<N, E>,
        LayoutChangeListener<N> {

  static Logger log = LoggerFactory.getLogger(BasicVisualizationServer.class);

  protected ChangeEventSupport changeSupport = new DefaultChangeEventSupport(this);

  /** holds the state of this View */
  protected VisualizationModel<N, E> model;

  /** handles the actual drawing of graph elements */
  protected Renderer<N, E> renderer;

  /** rendering hints used in drawing. Anti-aliasing is on by default */
  protected Map<Key, Object> renderingHints = new HashMap<Key, Object>();

  /** holds the state of which nodes of the graph are currently 'picked' */
  protected PickedState<N> pickedNodeState;

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
  protected List<Paintable> preRenderers = new ArrayList<>();

  /**
   * a collection of user-implementable functions to render over the topology (after the graph is
   * rendered)
   */
  protected List<Paintable> postRenderers = new ArrayList<>();

  protected RenderContext<N, E> renderContext;

  protected TransformSupport<N, E> transformSupport = new TransformSupport();

  protected Spatial<N> nodeSpatial;

  protected Spatial<E> edgeSpatial;

  /**
   * @param network the network to render
   * @param layoutAlgorithm the algorithm to apply
   * @param preferredSize the size of the graph area
   */
  public BasicVisualizationServer(
      Network<N, E> network, LayoutAlgorithm<N> layoutAlgorithm, Dimension preferredSize) {
    this(new BaseVisualizationModel<N, E>(network, layoutAlgorithm, preferredSize), preferredSize);
  }

  /**
   * Create an instance with the specified model and view dimension.
   *
   * @param model the model to use
   * @param preferredSize initial preferred layoutSize of the view
   */
  public BasicVisualizationServer(VisualizationModel<N, E> model, Dimension preferredSize) {
    this.model = model;
    renderContext = new PluggableRenderContext<>(model.getNetwork());
    renderer = new BasicRenderer<>();
    createSpatialStuctures(model, renderContext);
    model.addChangeListener(this);
    model.addLayoutChangeListener(this);
    setDoubleBuffered(false);
    this.addComponentListener(new VisualizationListener(this));

    setPickSupport(new ShapePickSupport<>(this));
    setPickedNodeState(new MultiPickedState<>());
    setPickedEdgeState(new MultiPickedState<>());

    renderContext.setEdgeDrawPaintFunction(
        new PickableEdgePaintFunction<>(getPickedEdgeState(), Color.black, Color.cyan));
    renderContext.setNodeFillPaintFunction(
        new PickableNodePaintFunction<>(getPickedNodeState(), Color.red, Color.yellow));

    setPreferredSize(preferredSize);
    renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    renderContext.getMultiLayerTransformer().addChangeListener(this);
    try {
      VisualizationViewerUI.getInstance(this).parse();
    } catch (IOException e) {
      log.debug("Unable to read property files. Using defaults.");
    }
  }

  private void createSpatialStuctures(VisualizationModel model, RenderContext renderContext) {
    setNodeSpatial(
        new SpatialRTree.Nodes<N>(
            model,
            new BoundingRectangleCollector.Nodes<>(renderContext, model),
            SplitterContext.of(new RStarLeafSplitter(), new RStarSplitter())));
    setEdgeSpatial(
        new SpatialRTree.Edges<>(
            model,
            new BoundingRectangleCollector.Edges<E>(renderContext, model),
            SplitterContext.of(new QuadraticLeafSplitter<>(), new QuadraticSplitter<>())));
  }

  public Spatial<N> getNodeSpatial() {
    return nodeSpatial;
  }

  public void setNodeSpatial(Spatial<N> spatial) {

    if (this.nodeSpatial != null) {
      disconnectListeners(this.nodeSpatial);
    }
    this.nodeSpatial = spatial;

    boolean layoutModelRelaxing = model.getLayoutModel().isRelaxing();
    nodeSpatial.setActive(!layoutModelRelaxing);
    if (!layoutModelRelaxing) {
      nodeSpatial.recalculate();
    }
    connectListeners(spatial);
  }

  public Spatial<E> getEdgeSpatial() {
    return edgeSpatial;
  }

  public void setEdgeSpatial(Spatial<E> spatial) {

    if (this.edgeSpatial != null) {
      disconnectListeners(this.edgeSpatial);
    }
    this.edgeSpatial = spatial;

    boolean layoutModelRelaxing = model.getLayoutModel().isRelaxing();
    edgeSpatial.setActive(!layoutModelRelaxing);
    if (!layoutModelRelaxing) {
      edgeSpatial.recalculate();
    }
    connectListeners(edgeSpatial);
  }

  /**
   * hook up events so that when the VisualizationModel gets an event from the LayoutModel and fires
   * it, the Spatial will get the same event and know to update or recalculate its space
   *
   * @param spatial
   */
  private void connectListeners(Spatial<?> spatial) {
    if (model instanceof LayoutEventSupport && spatial instanceof LayoutChangeListener) {
      if (spatial instanceof LayoutChangeListener) {
        model.addLayoutChangeListener((LayoutChangeListener) spatial);
      }
    }
    // this one toggles active/inactive as the opposite of the LayoutModel's active/inactive state
    model.getLayoutModel().getLayoutStateChangeSupport().addLayoutStateChangeListener(spatial);
  }

  /**
   * disconnect listeners that will no longer be used
   *
   * @param spatial
   */
  private void disconnectListeners(Spatial<?> spatial) {
    if (model instanceof LayoutEventSupport) {
      if (spatial instanceof LayoutChangeListener) {
        model.removeLayoutChangeListener((LayoutChangeListener) spatial);
      }
    }
    if (model.getLayoutModel() instanceof LayoutEventSupport) {
      ((LayoutEventSupport) model.getLayoutModel())
          .removeLayoutChangeListener((LayoutChangeListener) spatial);
    }
    if (model.getLayoutModel() instanceof LayoutModel.ChangeSupport) {
      if (spatial instanceof LayoutModel.ChangeListener) {
        ((LayoutModel.ChangeSupport) model.getLayoutModel())
            .removeChangeListener((LayoutModel.ChangeListener) spatial);
      }
    }
    model.getLayoutModel().getLayoutStateChangeSupport().removeLayoutStateChangeListener(spatial);
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

  public VisualizationModel<N, E> getModel() {
    return model;
  }

  public void setModel(VisualizationModel<N, E> model) {
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

    AnnotationPaintable lowerAnnotationPaintable = null;

    if (log.isTraceEnabled()) {
      // when logging is set to trace, the grid will be drawn on the graph visualization
      addSpatialAnnotations(this.nodeSpatial, Color.blue);
      addSpatialAnnotations(this.edgeSpatial, Color.green);
    } else {
      removeSpatialAnnotations();
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

    renderer.render(renderContext, model, nodeSpatial, edgeSpatial);

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

  @Override
  public void layoutChanged(LayoutEvent<N> evt) {
    repaint();
  }

  @Override
  public void layoutChanged(LayoutNetworkEvent<N> evt) {
    repaint();
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
      preRenderers = new ArrayList<>();
    }
    preRenderers.add(paintable);
  }

  public void prependPreRenderPaintable(Paintable paintable) {
    if (preRenderers == null) {
      preRenderers = new ArrayList<>();
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
      postRenderers = new ArrayList<>();
    }
    postRenderers.add(paintable);
  }

  public void prependPostRenderPaintable(Paintable paintable) {
    if (postRenderers == null) {
      postRenderers = new ArrayList<>();
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

  public PickedState<N> getPickedNodeState() {
    return pickedNodeState;
  }

  public PickedState<E> getPickedEdgeState() {
    return pickedEdgeState;
  }

  public void setPickedNodeState(PickedState<N> pickedNodeState) {
    if (pickEventListener != null && this.pickedNodeState != null) {
      this.pickedNodeState.removeItemListener(pickEventListener);
    }
    this.pickedNodeState = pickedNodeState;
    this.renderContext.setPickedNodeState(pickedNodeState);
    if (pickEventListener == null) {
      pickEventListener = e -> repaint();
    }
    pickedNodeState.addItemListener(pickEventListener);
  }

  public void setPickedEdgeState(PickedState<E> pickedEdgeState) {
    if (pickEventListener != null && this.pickedEdgeState != null) {
      this.pickedEdgeState.removeItemListener(pickEventListener);
    }
    this.pickedEdgeState = pickedEdgeState;
    this.renderContext.setPickedEdgeState(pickedEdgeState);
    if (pickEventListener == null) {
      pickEventListener = e -> repaint();
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
    return new Point2D.Double(d.width / 2, d.height / 2);
  }

  public RenderContext<N, E> getRenderContext() {
    return renderContext;
  }

  public void setRenderContext(RenderContext<N, E> renderContext) {
    this.renderContext = renderContext;
  }

  private void addSpatialAnnotations(Spatial spatial, Color color) {
    if (spatial != null) {
      addPreRenderPaintable(new SpatialPaintable(spatial, color));
    }
  }

  private void removeSpatialAnnotations() {
    for (Iterator<Paintable> iterator = preRenderers.iterator(); iterator.hasNext(); ) {
      Paintable paintable = iterator.next();
      if (paintable instanceof BasicVisualizationServer.SpatialPaintable) {
        iterator.remove();
      }
    }
  }

  public TransformSupport<N, E> getTransformSupport() {
    return transformSupport;
  }

  public void setTransformSupport(TransformSupport<N, E> transformSupport) {
    this.transformSupport = transformSupport;
  }

  class SpatialPaintable<T> implements VisualizationServer.Paintable {

    Spatial<T> quadTree;
    Color color;

    public SpatialPaintable(Spatial<T> quadTree, Color color) {
      this.quadTree = quadTree;
      this.color = color;
    }

    public boolean useTransform() {
      return false;
    }

    public void paint(Graphics g) {
      Graphics2D g2d = (Graphics2D) g;
      Color oldColor = g2d.getColor();
      // gather all the grid shapes
      List<Shape> grid = Lists.newArrayList();
      grid = quadTree.getGrid();

      g2d.setColor(color);
      for (Shape r : grid) {
        Shape shape = transformSupport.transform(BasicVisualizationServer.this, r);
        g2d.draw(shape);
      }
      g2d.setColor(Color.red);

      for (Shape pickShape : quadTree.getPickShapes()) {
        if (pickShape != null) {
          Shape shape = transformSupport.transform(BasicVisualizationServer.this, pickShape);

          g2d.draw(shape);
        }
      }
      g2d.setColor(oldColor);
    }
  }
}
