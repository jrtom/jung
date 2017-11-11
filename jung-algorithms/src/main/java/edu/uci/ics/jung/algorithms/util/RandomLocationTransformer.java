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
package edu.uci.ics.jung.algorithms.util;

import edu.uci.ics.jung.algorithms.layout.DomainModel;
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
  protected Random random;
  protected DomainModel<P> domainModel;

  /**
   * Creates an instance with the specified layoutSize which uses the current time as the random
   * seed.
   *
   * @param width, height the layoutSize of the layout area
   */
  public RandomLocationTransformer(DomainModel<P> domainModel, double width, double height) {
    this(domainModel, width, height, new Date().getTime());
  }

  /**
   * Creates an instance with the specified dimension and random seed.
   *
   * @param
   * @param seed the seed for the internal random number generator
   */
  public RandomLocationTransformer(
      DomainModel<P> domainModel, double width, double height, long seed) {
    this.domainModel = domainModel;
    this.width = width;
    this.height = height;
    this.random = new Random(seed);
  }

  public P apply(N node) {
    return domainModel.newPoint(random.nextDouble() * width, random.nextDouble() * height);
  }
}
