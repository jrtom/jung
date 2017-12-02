package edu.uci.ics.jung.visualization.properties;

import static edu.uci.ics.jung.visualization.layout.AWT.POINT_MODEL;

import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.model.AbstractLayoutModel;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.SpatialGridLayoutModel;
import edu.uci.ics.jung.visualization.layout.SpatialQuadTreeLayoutModel;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
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
  private static final String SPATIAL_SUPPORT = PREFIX + "spatialSupport";
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

      rc.setVertexFillPaintTransformer(
          new PickableVertexPaintTransformer(
              vv.getPickedVertexState(),
              new Color(Integer.getInteger(NODE_COLOR, 0xFF0000)),
              new Color(Integer.getInteger(PICKED_NODE_COLOR, 0x00FFFF))));

      rc.setEdgeDrawPaintTransformer(
          new PickableEdgePaintTransformer(
              vv.getPickedEdgeState(),
              new Color(Integer.getInteger(EDGE_COLOR, 0xFF0000)),
              new Color(Integer.getInteger(PICKED_EDGE_COLOR, 0x00FFFF))));

      rc.setVertexLabelDrawPaintTransformer(
          n -> new Color(Integer.getInteger(NODE_LABEL_COLOR, 0x000000)));

      int size = Integer.getInteger(NODE_SIZE, 12);

      vv.getRenderContext()
          .setVertexShapeTransformer(
              n -> getNodeShape(System.getProperty(NODE_SHAPE, "CIRCLE"), size));
      // change the default LayoutModel only if the user has set the property requesting it
      if (System.getProperty(SPATIAL_SUPPORT) != null) {
        LayoutModel layoutModel =
            createLayoutModel(vv.getModel().getNetwork(), vv.getModel().getLayoutSize());
        // be sure to connect the listener to the new LayoutModel so that animations are propogated to
        // the new LayoutModel
        if (layoutModel instanceof AbstractLayoutModel
            && vv.getModel() instanceof LayoutModel.ChangeListener) {
          ((AbstractLayoutModel) layoutModel)
              .addChangeListener((LayoutModel.ChangeListener) vv.getModel());
        }
        vv.getModel().setLayoutModel(layoutModel);
      }

      vv.getRenderer()
          .getVertexLabelRenderer()
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
  private Renderer.VertexLabel.Position getPosition(String position) {
    try {
      return Renderer.VertexLabel.Position.valueOf(position);
    } catch (Exception e) {
    }
    return Renderer.VertexLabel.Position.SE;
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
        rc.setEdgeShapeTransformer(EdgeShape.line());
        break;
      case "CUBIC_CURVE":
        rc.setEdgeShapeTransformer(EdgeShape.cubicCurve());
        break;
      case "ORTHOGONAL":
        rc.setEdgeShapeTransformer(EdgeShape.orthogonal());
        break;
      case "WEDGE":
        rc.setEdgeShapeTransformer(EdgeShape.wedge(10));
        break;
      case "QUAD_CURVE":
      default:
        rc.setEdgeShapeTransformer(EdgeShape.quadCurve());
        break;
    }
  }

  private VisualizationModel.SpatialSupport getSpatialSupportPreference() {
    String spatialSupportProperty = System.getProperty(SPATIAL_SUPPORT, "QUAD_TREE");
    try {
      return VisualizationModel.SpatialSupport.valueOf(spatialSupportProperty);
    } catch (IllegalArgumentException ex) {
      // the user set an unknown name
      // issue a warning because unlike colors and shapes, it is not immediately obvious what spatial
      // support is being used
      log.warn("Unknown ModelStructure type {} ignored.", spatialSupportProperty);
    }
    return VisualizationModel.SpatialSupport.QUAD_TREE;
  }

  private LayoutModel<N, Point2D> createLayoutModel(Network<N, E> network, Dimension layoutSize) {
    switch (getSpatialSupportPreference()) {
      case GRID:
        return SpatialGridLayoutModel.<N, Point2D>builder()
            .setGraph(network.asGraph())
            .setPointModel(POINT_MODEL)
            .setSize(layoutSize.width, layoutSize.height)
            .build();
      case QUAD_TREE:
        return SpatialQuadTreeLayoutModel.<N, Point2D>builder()
            .setGraph(network.asGraph())
            .setPointModel(POINT_MODEL)
            .setSize(layoutSize.width, layoutSize.height)
            .build();
      case NONE:
      default:
        return LoadingCacheLayoutModel.<N, Point2D>builder()
            .setGraph(network.asGraph())
            .setPointModel(POINT_MODEL)
            .setSize(layoutSize.width, layoutSize.height)
            .build();
    }
  }
}
