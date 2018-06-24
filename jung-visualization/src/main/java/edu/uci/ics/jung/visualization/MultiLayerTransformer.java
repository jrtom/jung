package edu.uci.ics.jung.visualization;

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import java.awt.Shape;
import java.awt.geom.Point2D;

public interface MultiLayerTransformer
    extends BidirectionalTransformer, ShapeTransformer, ChangeEventSupport {

  enum Layer {
    LAYOUT,
    VIEW
  }

  void setTransformer(Layer layer, MutableTransformer Function);

  MutableTransformer getTransformer(Layer layer);

  Point2D inverseTransform(Layer layer, Point2D p);

  Point2D inverseTransform(Layer layer, double x, double y);

  Point2D transform(Layer layer, Point2D p);

  Point2D transform(Layer layer, double x, double y);

  Shape transform(Layer layer, Shape shape);

  Shape inverseTransform(Layer layer, Shape shape);

  void setToIdentity();
}
