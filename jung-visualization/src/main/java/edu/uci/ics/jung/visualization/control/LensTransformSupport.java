package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
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

/** @author Tom Nelson */
public class LensTransformSupport<N, E> extends TransformSupport<N, E> {

  private static final Logger log = LoggerFactory.getLogger(LensTransformSupport.class);

  /**
   * Overriden to apply lens effects to the transformation from view to layout coordinates
   *
   * @param vv
   * @param p
   * @return
   */
  @Override
  public Point2D inverseTransform(VisualizationServer<N, E> vv, Point2D p) {
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
        p = delegateTransformer.inverseTransform(p);
        p = ht.inverseTransform(p);
      }

    } else {
      // the layoutTransformer may be a LensTransformer or not
      p = multiLayerTransformer.inverseTransform(p);
    }
    return p;
  }

  public Shape transform(VisualizationServer<N, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);
    MutableTransformer layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT);
    VisualizationModel<N, E> model = vv.getModel();

    if (viewTransformer instanceof LensTransformer) {
      shape = multiLayerTransformer.transform(shape);
    } else if (layoutTransformer instanceof LensTransformer) {
      LayoutModel<N> layoutModel = model.getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      shape = shapeChanger.transform(layoutDelegate.transform(shape));
    } else {
      shape = multiLayerTransformer.transform(Layer.LAYOUT, shape);
    }
    return shape;
  }

  public Point2D transform(VisualizationServer<N, E> vv, Point2D p) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);
    MutableTransformer layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT);
    VisualizationModel<N, E> model = vv.getModel();

    if (viewTransformer instanceof LensTransformer) {
      // use all layers
      p = multiLayerTransformer.transform(p);
    } else if (layoutTransformer instanceof LensTransformer) {
      // apply the shape changer
      LayoutModel<N> layoutModel = model.getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      p = shapeChanger.transform(layoutDelegate.transform(p));
    } else {
      // use the default
      p = multiLayerTransformer.transform(Layer.LAYOUT, p);
    }
    return p;
  }

  /**
   * Overriden to perform lens effects when inverse transforming from view to layout.
   *
   * @param vv
   * @param shape
   * @return
   */
  @Override
  public Shape inverseTransform(VisualizationServer<N, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    MutableTransformer viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW);
    MutableTransformer layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT);

    if (layoutTransformer instanceof LensTransformer) {
      // apply the shape changer
      LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();
      Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
      HyperbolicShapeTransformer shapeChanger = new HyperbolicShapeTransformer(d, viewTransformer);
      LensTransformer lensTransformer = (LensTransformer) layoutTransformer;
      shapeChanger.getLens().setLensShape(lensTransformer.getLens().getLensShape());
      MutableTransformer layoutDelegate =
          ((MutableTransformerDecorator) layoutTransformer).getDelegate();
      shape = layoutDelegate.inverseTransform(shapeChanger.inverseTransform(shape));
    } else {
      // if the viewTransformer is either a LensTransformer or the default
      shape = multiLayerTransformer.inverseTransform(shape);
    }
    return shape;
  }
}
