package edu.uci.ics.jung.visualization.properties;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.layout.BoundingRectangleCollector;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialGrid;
import edu.uci.ics.jung.visualization.spatial.SpatialQuadTree;
import edu.uci.ics.jung.visualization.spatial.SpatialRTree;
import edu.uci.ics.jung.visualization.spatial.rtree.QuadraticLeafSplitter;
import edu.uci.ics.jung.visualization.spatial.rtree.QuadraticSplitter;
import edu.uci.ics.jung.visualization.spatial.rtree.RStarLeafSplitter;
import edu.uci.ics.jung.visualization.spatial.rtree.RStarSplitter;
import edu.uci.ics.jung.visualization.spatial.rtree.SplitterContext;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisualizationViewerUI<N, E> {

  private static final Logger log = LoggerFactory.getLogger(VisualizationViewerUI.class);

  private static final String PROPERTIES_FILE_NAME = "jung.properties";

  private static final String PREFIX = "jung.";
  private static final String NODE_SHAPE = PREFIX + "nodeShape";
  private static final String NODE_SIZE = PREFIX + "nodeSize";
  private static final String EDGE_SHAPE = PREFIX + "edgeShape";
  private static final String NODE_COLOR = PREFIX + "nodeColor";
  private static final String PICKED_NODE_COLOR = PREFIX + "pickedNodeColor";
  private static final String EDGE_COLOR = PREFIX + "edgeColor";
  private static final String PICKED_EDGE_COLOR = PREFIX + "pickedEdgeColor";
  private static final String ARROW_STYLE = PREFIX + "arrowStyle";
  private static final String NODE_SPATIAL_SUPPORT = PREFIX + "nodeSpatialSupport";
  private static final String EDGE_SPATIAL_SUPPORT = PREFIX + "edgeSpatialSupport";
  private static final String NODE_LABEL_POSITION = PREFIX + "nodeLabelPosition";
  private static final String NODE_LABEL_COLOR = PREFIX + "nodeLabelColor";

  VisualizationServer<N, E> vv;

  public static VisualizationViewerUI getInstance(VisualizationServer vv) {
    return new VisualizationViewerUI(vv);
  }

  VisualizationViewerUI(VisualizationServer<N, E> vv) {
    this.vv = vv;
  }

  private boolean loadFromAppName() {
    try {
      String launchProgram = System.getProperty("sun.java.command");
      if (launchProgram != null && !launchProgram.isEmpty()) {
        launchProgram =
            "/" + launchProgram.substring(launchProgram.lastIndexOf('.') + 1) + ".properties";
        InputStream stream = getClass().getResourceAsStream(launchProgram);
        System.getProperties().load(stream);
        return true;
      }
    } catch (Exception ex) {
    }
    return false;
  }

  private boolean loadFromDefault() {
    try {
      InputStream stream = getClass().getResourceAsStream("/" + PROPERTIES_FILE_NAME);
      System.getProperties().load(stream);
      return true;
    } catch (Exception ex) {
    }
    return false;
  }

  /**
   * parse the properties file and set values or defaults
   *
   * @throws IOException
   */
  public void parse() throws IOException {
    if (loadFromAppName() || loadFromDefault()) {

      RenderContext rc = vv.getRenderContext();

      setEdgeShape(System.getProperty(EDGE_SHAPE, "QUAD_CURVE"));

      rc.setNodeFillPaintFunction(
          new PickableNodePaintFunction(
              vv.getPickedNodeState(),
              new Color(Integer.getInteger(NODE_COLOR, 0xFF0000)),
              new Color(Integer.getInteger(PICKED_NODE_COLOR, 0x00FFFF))));

      rc.setEdgeDrawPaintFunction(
          new PickableEdgePaintFunction(
              vv.getPickedEdgeState(),
              new Color(Integer.getInteger(EDGE_COLOR, 0xFF0000)),
              new Color(Integer.getInteger(PICKED_EDGE_COLOR, 0x00FFFF))));

      rc.setNodeLabelDrawPaintFunction(
          n -> new Color(Integer.getInteger(NODE_LABEL_COLOR, 0x000000)));

      int size = Integer.getInteger(NODE_SIZE, 12);

      vv.getRenderContext()
          .setNodeShapeFunction(n -> getNodeShape(System.getProperty(NODE_SHAPE, "CIRCLE"), size));

      // only set if the property is requested
      if (System.getProperty(NODE_SPATIAL_SUPPORT) != null) {

        Spatial<N> spatial = createNodeSpatial(vv);
        if (spatial != null) {
          vv.setNodeSpatial(spatial);
        }
      }
      // only set if the property is requested
      if (System.getProperty(EDGE_SPATIAL_SUPPORT) != null) {

        Spatial<E> spatial = createEdgeSpatial(vv);
        if (spatial != null) {
          vv.setEdgeSpatial(spatial);
        }
      }

      vv.getRenderer()
          .getNodeLabelRenderer()
          .setPosition(getPosition(System.getProperty(NODE_LABEL_POSITION, "SE")));
    }
  }

  private Shape getNodeShape(String shape, int size) {
    switch (shape) {
      case "SQUARE":
        return new Rectangle2D.Float(-size / 2.f, -size / 2.f, size, size);
      case "CIRCLE":
      default:
        return new Ellipse2D.Float(-size / 2.f, -size / 2.f, size, size);
    }
  }

  /**
   * parse out the node label position
   *
   * @param position
   * @return
   */
  private Renderer.NodeLabel.Position getPosition(String position) {
    try {
      return Renderer.NodeLabel.Position.valueOf(position);
    } catch (Exception e) {
    }
    return Renderer.NodeLabel.Position.SE;
  }

  /**
   * parse out the edge shape
   *
   * @param edgeShape
   */
  private void setEdgeShape(String edgeShape) {
    RenderContext rc = vv.getRenderContext();
    switch (edgeShape) {
      case "LINE":
        rc.setEdgeShapeFunction(EdgeShape.line());
        break;
      case "CUBIC_CURVE":
        rc.setEdgeShapeFunction(EdgeShape.cubicCurve());
        break;
      case "ORTHOGONAL":
        rc.setEdgeShapeFunction(EdgeShape.orthogonal());
        break;
      case "WEDGE":
        rc.setEdgeShapeFunction(EdgeShape.wedge(10));
        break;
      case "QUAD_CURVE":
      default:
        rc.setEdgeShapeFunction(EdgeShape.quadCurve());
        break;
    }
  }

  private VisualizationModel.SpatialSupport getNodeSpatialSupportPreference() {
    String spatialSupportProperty = System.getProperty(NODE_SPATIAL_SUPPORT, "RTREE");
    try {
      return VisualizationModel.SpatialSupport.valueOf(spatialSupportProperty);
    } catch (IllegalArgumentException ex) {
      // the user set an unknown name
      // issue a warning because unlike colors and shapes, it is not immediately obvious what
      // spatial
      // support is being used
      log.warn("Unknown ModelStructure type {} ignored.", spatialSupportProperty);
    }
    return VisualizationModel.SpatialSupport.QUADTREE;
  }

  private VisualizationModel.SpatialSupport getEdgeSpatialSupportPreference() {
    String spatialSupportProperty = System.getProperty(EDGE_SPATIAL_SUPPORT, "RTREE");
    try {
      return VisualizationModel.SpatialSupport.valueOf(spatialSupportProperty);
    } catch (IllegalArgumentException ex) {
      // the user set an unknown name
      // issue a warning because unlike colors and shapes, it is not immediately obvious what
      // spatial
      // support is being used
      log.warn("Unknown ModelStructure type {} ignored.", spatialSupportProperty);
    }
    return VisualizationModel.SpatialSupport.NONE;
  }

  private Spatial<N> createNodeSpatial(VisualizationServer<N, E> visualizationServer) {
    switch (getNodeSpatialSupportPreference()) {
      case RTREE:
        return new SpatialRTree.Nodes<>(
            visualizationServer.getModel(),
            new BoundingRectangleCollector.Nodes<>(
                visualizationServer.getRenderContext(), visualizationServer.getModel()),
            SplitterContext.of(new RStarLeafSplitter<>(), new RStarSplitter<>()));
      case GRID:
        return new SpatialGrid<>(visualizationServer.getModel().getLayoutModel());
      case QUADTREE:
        return new SpatialQuadTree<>(visualizationServer.getModel().getLayoutModel());
      case NONE:
      default:
        return new Spatial.NoOp.Node<N>(visualizationServer.getModel().getLayoutModel());
    }
  }

  private Spatial<E> createEdgeSpatial(VisualizationServer<N, E> visualizationServer) {
    switch (getEdgeSpatialSupportPreference()) {
      case RTREE:
        return new SpatialRTree.Edges<E, N>(
            visualizationServer.getModel(),
            new BoundingRectangleCollector.Edges<>(
                visualizationServer.getRenderContext(), visualizationServer.getModel()),
            SplitterContext.of(new QuadraticLeafSplitter(), new QuadraticSplitter()));
      case NONE:
      default:
        return new Spatial.NoOp.Edge<E, N>(visualizationServer.getModel());
    }
  }
}
