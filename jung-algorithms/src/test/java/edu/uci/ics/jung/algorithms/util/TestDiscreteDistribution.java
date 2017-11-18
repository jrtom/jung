package edu.uci.ics.jung.algorithms.util;

import static org.junit.Assert.assertArrayEquals;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestDiscreteDistribution {

  // TODO: Migrate to assertThrows when JUnit 4.13 or JUnit 5 is adopted
  @Rule public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void mean_collectionOfSingleDoubleArray() {
    double[] expected = new double[] {3};
    double[] actual = DiscreteDistribution.mean(ImmutableList.of(new double[] {1, 2, 3, 4, 5}));
    assertArrayEquals(expected, actual, 1.0e-10);
  }

  @Test
  public void mean_arrayOfSingleDoubleArray() {
    double[] expected = new double[] {3};
    double[] actual = DiscreteDistribution.mean(new double[][] {{1, 2, 3, 4, 5}});
    assertArrayEquals(expected, actual, 1.0e-10);
  }

  @Test
  public void mean_collectionOfManyDoubleArrays() {
    double[] expected = new double[] {3, 4, 5};
    double[] actual =
        DiscreteDistribution.mean(
            ImmutableList.of(
                new double[] {1, 2, 3, 4, 5},
                new double[] {0, 2, 4, 6, 8},
                new double[] {0, 0.5, 5, 9.5, 10}));
    assertArrayEquals(expected, actual, 1.0e-10);
  }

  @Test
  public void mean_arrayOfManyDoubleArrays() {
    double[] expected = new double[] {3, 4, 5};
    double[] actual =
        DiscreteDistribution.mean(
            new double[][] {
              {1, 2, 3, 4, 5}, //
              {0, 2, 4, 6, 8},
              {0, 0.5, 5, 9.5, 10}
            });
    assertArrayEquals(expected, actual, 1.0e-10);
  }

  @Test
  public void mean_emptyCollectionOfDoubleArrays_throwsIllegalArgumentException() {
    expectedException.expect(IllegalArgumentException.class);
    DiscreteDistribution.mean(ImmutableList.of());
  }

  @Test
  public void mean_emptyArrayOfDoubleArrays_throwsIllegalArgumentException() {
    expectedException.expect(IllegalArgumentException.class);
    DiscreteDistribution.mean(new double[0][0]);
  }

  @Test
  public void mean_collectionOfSingleEmptyDoubleArray_throwsIllegalArgumentException() {
    expectedException.expect(IllegalArgumentException.class);
    DiscreteDistribution.mean(ImmutableList.of(new double[0]));
  }

  @Test
  public void mean_arrayOfSingleEmptyDoubleArray_throwsIllegalArgumentException() {
    expectedException.expect(IllegalArgumentException.class);
    DiscreteDistribution.mean(new double[][] {new double[0]});
  }

  @Test
  public void mean_collectionOfManyEmptyDoubleArrays_throwsIllegalArgumentException() {
    expectedException.expect(IllegalArgumentException.class);
    DiscreteDistribution.mean(ImmutableList.of(new double[0], new double[0], new double[0]));
  }

  @Test
  public void mean_arrayOfManyEmptyDoubleArrays_throwsIllegalArgumentException() {
    expectedException.expect(IllegalArgumentException.class);
    DiscreteDistribution.mean(new double[][] {new double[0], new double[0], new double[0]});
  }

  @Test
  public void mean_collectionWithDifferingLengthArrays_throwsIllegalArgumentException() {
    expectedException.expect(IllegalArgumentException.class);
    DiscreteDistribution.mean(ImmutableList.of(new double[0], new double[0], new double[] {1}));
  }

  @Test
  public void mean_arrayWithDifferingLengthArrays_throwsIllegalArgumentException() {
    expectedException.expect(IllegalArgumentException.class);
    DiscreteDistribution.mean(new double[][] {new double[0], new double[0], new double[] {1}});
  }

  @Test
  public void mean_nullCollection_throwsNullPointerException() {
    expectedException.expect(NullPointerException.class);
    DiscreteDistribution.mean((Collection<double[]>) null);
  }

  @Test
  public void mean_nullArray_throwsNullPointerException() {
    expectedException.expect(NullPointerException.class);
    DiscreteDistribution.mean((double[][]) null);
  }

  @Test
  public void mean_collectionWithNull_throwsNullPointerException() {
    expectedException.expect(NullPointerException.class);
    DiscreteDistribution.mean(Collections.singleton(null));
  }
}
