/*
 * Created on Jul 19, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.util;

import static edu.uci.ics.jung.layout.util.RandomLocationTransformer.Origin.NE;

import edu.uci.ics.jung.layout.model.PointModel;
import java.util.Date;
import java.util.Random;
import java.util.function.Function;

/**
 * Provides a random node location within the bounds of the width and height. This provides a random
 * location for unmapped nodes the first time they are accessed.
 *
 * <p><b>Note</b>: the generated values are not cached, so animate() will generate a new random
 * location for the passed node every time it is called. If you want a consistent value, wrap this
 * // * layout's generated values in a instance.
 *
 * @author Tom Nelson
 * @param <N> the node type
 */
public class RandomLocationTransformer<N, P> implements Function<N, P> {
  protected double width;
  protected double height;
  protected double depth;
  protected Random random;
  protected PointModel<P> pointModel;
  protected Origin origin = NE;

  public static enum Origin {
    NE,
    CENTER
  }

  /**
   * Creates an instance with the specified layoutSize which uses the current time as the random
   * seed.
   *
   * @param width, height the layoutSize of the layout area
   */
  public RandomLocationTransformer(
      PointModel<P> pointModel, double width, double height, double depth) {
    this(NE, pointModel, width, height, depth, new Date().getTime());
  }

  /**
   * Creates an instance with the specified layoutSize which uses the current time as the random
   * seed.
   *
   * @param width, height the layoutSize of the layout area
   */
  public RandomLocationTransformer(
      Origin origin, PointModel<P> pointModel, double width, double height, double depth) {
    this(origin, pointModel, width, height, depth, new Date().getTime());
  }
  //  /**
  //   * Creates an instance with the specified layoutSize which uses the current time as the random
  //   * seed.
  //   *
  //   * @param width, height the layoutSize of the layout area
  //   */
  //    public RandomLocationTransformer(PointModel<P> pointModel, double width, double height) {
  //    this(pointModel, width, height, 0, new Date().getTime());
  //  }

  /**
   * Creates an instance with the specified dimension and random seed.
   *
   * @param
   * @param seed the seed for the internal random number generator
   */
  public RandomLocationTransformer(
      PointModel<P> pointModel, double width, double height, double depth, long seed) {
    this(NE, pointModel, width, height, depth, seed);
    //    this.origin = origin;
    //    this.pointModel = pointModel;
    //    this.width = width;
    //    this.height = height;
    //    this.depth = depth;
    //    this.random = new Random(seed);
  }

  /**
   * Creates an instance with the specified dimension and random seed.
   *
   * @param
   * @param seed the seed for the internal random number generator
   */
  public RandomLocationTransformer(
      Origin origin,
      PointModel<P> pointModel,
      double width,
      double height,
      double depth,
      long seed) {
    this.origin = origin;
    this.pointModel = pointModel;
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.random = new Random(seed);
  }

  //  /**
  //   * Creates an instance with the specified dimension and random seed.
  //   *
  //   * @param
  //   * @param seed the seed for the internal random number generator
  //   */
  //  public RandomLocationTransformer(
  //      PointModel<P> pointModel, double width, double height, long seed) {
  //    this(pointModel, width, height, 0, seed);
  //  }

  private P applyNE(N node) {
    return pointModel.newPoint(
        random.nextDouble() * width, random.nextDouble() * height, random.nextDouble() * depth);
  }

  private P applyCenter(N node) {
    double radiusX = width / 2;
    double radiusY = height / 2;
    double radiusZ = depth / 2;
    return pointModel.newPoint(
        random.nextDouble() * width - radiusX,
        random.nextDouble() * height - radiusY,
        random.nextDouble() * depth - radiusZ);
  }

  @Override
  public P apply(N node) {
    if (this.origin == NE) {
      return applyNE(node);
    } else {
      return applyCenter(node);
    }
  }
}
