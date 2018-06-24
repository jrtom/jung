/*
 * Created on Nov 3, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import com.google.common.base.Preconditions;
import java.util.function.Function;

/**
 * Provides node sizes that are spaced proportionally between min_size and max_size depending on
 *
 * @author Joshua O'Madadhain
 */
public class InterpolatingNodeSizeFunction<N> implements Function<N, Integer> {
  protected double min;
  protected double max;
  protected Function<N, ? extends Number> values;
  protected int min_size;
  protected int size_diff;

  public InterpolatingNodeSizeFunction(
      Function<N, ? extends Number> values, int min_size, int max_size) {
    super();
    Preconditions.checkArgument(min_size >= 0 && max_size >= 0, "sizes must be non-negative");
    Preconditions.checkArgument(min_size <= max_size, "min_size must be <= max_size");
    this.min = 0;
    this.max = 0;
    this.values = values;
    setMinSize(min_size);
    setMaxSize(max_size);
  }

  public Integer apply(N v) {
    Number n = values.apply(v);
    double value = min;
    if (n != null) {
      value = n.doubleValue();
    }
    min = Math.min(this.min, value);
    max = Math.max(this.max, value);

    if (min == max) {
      return min_size;
    }

    // interpolate between min and max sizes based on how big value is
    // with respect to min and max values
    return min_size + (int) (((value - min) / (max - min)) * size_diff);
  }

  public void setMinSize(int min_size) {
    this.min_size = min_size;
  }

  public void setMaxSize(int max_size) {
    this.size_diff = max_size - this.min_size;
  }
}
