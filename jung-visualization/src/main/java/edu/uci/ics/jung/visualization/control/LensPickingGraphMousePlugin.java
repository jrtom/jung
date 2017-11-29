package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MagnifyTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.MagnifyShapeTransformer;
import java.awt.*;
import java.awt.geom.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A subclass of PickingGraphMousePlugin that contains methods that are overridden to account for
 * the Lens effects that are in the view projection
 *
 * @author Tom Nelson
 */
public class LensPickingGraphMousePlugin<N, E> extends PickingGraphMousePlugin<N, E> {

  private static final Logger log = LoggerFactory.getLogger(LensPickingGraphMousePlugin.class);

  /**
   * Overriden to apply lens effects to the transformation from view to layout coordinates
   *
   * @param vv
   * @param p
   * @return
   */
  @Override
  protected Point2D inverseTransform(VisualizationViewer<N, E> vv, Point2D p) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);
    MutableTransformer layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT);

    if (viewTransformer instanceof LensTransformer) {
      LensTransformer lensTransformer = (LensTransformer) viewTransformer;
      MutableTransformer delegateTransformer = lensTransformer.getDelegate();

      if (viewTransformer instanceof MagnifyShapeTransformer) {
        MagnifyTransformer ht =
            new MagnifyTransformer(lensTransformer.getLens(), layoutTransformer);
        p = delegateTransformer.inverseTransform(p);
        p = ht.inverseTransform(p);
      } else if (viewTransformer instanceof HyperbolicShapeTransformer) {
        HyperbolicTransformer ht =
            new HyperbolicTransformer(lensTransformer.getLens(), layoutTransformer);
        log.info("made with magnification {}", lensTransformer.getLens().getMagnification());
        p = delegateTransformer.inverseTransform(p);
        p = ht.inverseTransform(p);
      }

    } else if (layoutTransformer instanceof LensTransformer) {

      p = multiLayerTransformer.inverseTransform(p);

    } else {
      p = multiLayerTransformer.inverseTransform(p);
    }
    return p;
  }

  /**
   * Overriden to perform lens effects when transforming from Layout to view. Used when projecting
   * the selection Lens (the rectangular area drawn with the mouse) back into the view.
   *
   * @param vv
   * @param shape
   * @return
   */
  @Override
  protected Shape transform(VisualizationViewer<N, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);
    MutableTransformer layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT);

    if (viewTransformer instanceof LensTransformer) {
      shape = multiLayerTransformer.inverseTransform(shape);
    } else if (layoutTransformer instanceof LensTransformer) {
      LayoutModel<N, Point2D> layoutModel = vv.getModel().getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      shape = layoutDelegate.inverseTransform(shapeChanger.inverseTransform(shape));
      log.info("made with magnification {}", lensTransformer.getLens().getMagnification());

    } else {
      shape = multiLayerTransformer.inverseTransform(shape);
    }
    return shape;
  }

  /**
   * Overriden to perform Lens effects when managing the picking Lens target shape (drawn with the
   * mouse) in both the layout and view coordinate systems
   *
   * @param vv
   * @param multiLayerTransformer
   * @param down
   * @param out
   */
  @Override
  protected void updatePickingTargets(
      VisualizationViewer vv,
      MultiLayerTransformer multiLayerTransformer,
      Point2D down,
      Point2D out) {
    viewRectangle.setFrameFromDiagonal(down, out);

    MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);
    MutableTransformer layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT);

    Shape shape = viewRectangle;
    if (viewTransformer instanceof LensTransformer) {
      layoutTargetShape = multiLayerTransformer.inverseTransform(shape);
    } else if (layoutTransformer instanceof LensTransformer) {
      LayoutModel<N, Point2D> layoutModel = vv.getModel().getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      layoutTargetShape = layoutDelegate.inverseTransform(shapeChanger.inverseTransform(shape));
      log.trace("made with magnification {}", lensTransformer.getLens().getMagnification());

    } else {
      layoutTargetShape = multiLayerTransformer.inverseTransform(shape);
    }

    if (log.isTraceEnabled()) {
      log.trace("viewRectangle {}", viewRectangle);
      log.trace("layoutTargetShape bounds {}", layoutTargetShape.getBounds());
    }
  }
}
