/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.layout3d;
/*
 * This source is under the same license with JUNG.
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.util.ConcurrentModificationException;
import java.util.function.BiFunction;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3f;

/**
 * Implements the Kamada-Kawai algorithm for node layout. Does not respect filter calls, and
 * sometimes crashes when the view changes to it.
 *
 * @see "Tomihisa Kamada and Satoru Kawai: An algorithm for drawing general indirect graphs.
 *     Information Processing Letters 31(1):7-15, 1989"
 * @see "Tomihisa Kamada: On visualization of abstract objects and relations. Ph.D. dissertation,
 *     Dept. of Information Science, Univ. of Tokyo, Dec. 1988."
 * @author Masanori Harada
 */
public class KKLayout<N, E> extends AbstractLayout<N, E> implements IterativeContext {

  private double EPSILON = 0.1d;

  private int currentIteration;
  private int maxIterations = 2000;
  private String status = "KKLayout";

  private float L; // the ideal length of an edge
  private float K = 1; // arbitrary const number
  private float[][] dm; // distance matrix

  private boolean adjustForGravity = true;
  private boolean exchangenodes = true;

  private N[] nodes;
  private Point3f[] xyzdata;

  //  private final Graph<N> graph;

  /** Retrieves graph distances between nodes of the visible graph */
  protected BiFunction<N, N, Number> distance;

  /**
   * The diameter of the visible graph. In other words, the maximum over all pairs of nodes of the
   * length of the shortest path between a and bf the visible graph.
   */
  protected float diameter;

  /** A multiplicative factor which partly specifies the "preferred" length of an edge (L). */
  private float length_factor = 0.9f;

  /**
   * A multiplicative factor which specifies the fraction of the graph's diameter to be used as the
   * inter-node distance between disconnected nodes.
   */
  private float disconnected_multiplier = 0.5f;

  public KKLayout(Network<N, E> g) {
    this(g, new UnweightedShortestPath<N>(g.asGraph()));
  }

  /**
   * Creates an instance for the specified graph and distance metric.
   *
   * @param g the graph on which the layout algorithm is to operate
   * @param distance specifies the distance between pairs of nodes
   */
  public KKLayout(Network<N, E> g, Distance<N> distance) {
    super(g);
    //    this.graph = g;
    this.distance = (x, y) -> distance.getDistance(x, y);
  }

  /**
   * @param length_factor a multiplicative factor which partially specifies the preferred length of
   *     an edge
   */
  public void setLengthFactor(float length_factor) {
    this.length_factor = length_factor;
  }

  /**
   * @param disconnected_multiplier a multiplicative factor that specifies the fraction of the
   *     graph's diameter to be used as the inter-node distance between disconnected nodes
   */
  public void setDisconnectedDistanceMultiplier(float disconnected_multiplier) {
    this.disconnected_multiplier = disconnected_multiplier;
  }

  /** @return a string with information about the current status of the algorithm. */
  public String getStatus() {
    return status + this.getSize();
  }

  public void setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  /** @return true */
  public boolean isIncremental() {
    return true;
  }

  /** @return true if the current iteration has passed the maximum count. */
  public boolean done() {
    if (currentIteration > maxIterations) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public void initialize() {
    currentIteration = 0;

    BoundingSphere size = getSize();
    if (network != null && size != null) {

      float radius = (float) size.getRadius();

      int n = network.nodes().size();
      dm = new float[n][n];
      nodes = (N[]) network.nodes().toArray();
      xyzdata = new Point3f[n];

      // assign IDs to all visible nodes
      while (true) {
        try {
          int index = 0;
          for (N node : network.nodes()) {
            Point3f xyzd = apply(node);
            nodes[index] = node;
            xyzdata[index] = xyzd;
            index++;
          }
          break;
        } catch (ConcurrentModificationException cme) {
        }
      }

      diameter = (float) DistanceStatistics.<N>diameter(network.asGraph(), distance, true);

      float L0 = radius * 2;
      L = (L0 / diameter) * length_factor; // length_factor used to be hardcoded to 0.9
      //L = 0.75 * Math.sqrt(height * width / n);

      for (int i = 0; i < n - 1; i++) {
        for (int j = i + 1; j < n; j++) {
          Number d_ij = distance.apply(nodes[i], nodes[j]);
          Number d_ji = distance.apply(nodes[j], nodes[i]);
          float dist = diameter * disconnected_multiplier;
          if (d_ij != null) {
            dist = Math.min(d_ij.floatValue(), dist);
          }
          if (d_ji != null) {
            dist = Math.min(d_ji.floatValue(), dist);
          }
          dm[i][j] = dm[j][i] = dist;
        }
      }
    }
  }

  public void step() {
    try {
      currentIteration++;
      float energy = calcEnergy();
      status =
          "Kamada-Kawai N="
              + network.nodes().size()
              + "("
              + network.nodes().size()
              + ")"
              + " IT: "
              + currentIteration
              + " E="
              + energy;

      int n = network.nodes().size();
      if (n == 0) {
        return;
      }

      float maxDeltaM = 0;
      int pm = -1; // the node having max deltaM
      for (int i = 0; i < n; i++) {
        if (isLocked(nodes[i])) {
          continue;
        }
        float deltam = calcDeltaM(i);

        if (maxDeltaM < deltam) {
          maxDeltaM = deltam;
          pm = i;
        }
      }
      if (pm == -1) {
        return;
      }

      for (int i = 0; i < 100; i++) {
        float[] dxy = calcDeltaXY(pm);
        xyzdata[pm].set(
            xyzdata[pm].getX() + dxy[0], xyzdata[pm].getY() + dxy[1], xyzdata[pm].getZ() + dxy[2]);

        double deltam = calcDeltaM(pm);
        if (deltam < EPSILON) {
          break;
        }
      }

      if (adjustForGravity) {
        adjustForGravity();
      }

      if (exchangenodes && maxDeltaM < EPSILON) {
        energy = calcEnergy();
        for (int i = 0; i < n - 1; i++) {
          if (isLocked(nodes[i])) {
            continue;
          }
          for (int j = i + 1; j < n; j++) {
            if (isLocked(nodes[j])) {
              continue;
            }
            double xenergy = calcEnergyIfExchanged(i, j);
            if (energy > xenergy) {
              float sx = xyzdata[i].getX();
              float sy = xyzdata[i].getY();
              float sz = xyzdata[i].getZ();

              xyzdata[i].set(xyzdata[j]);
              xyzdata[j].set(sx, sy, sz);
              return;
            }
          }
        }
      }
    } finally {
      //			fireStateChanged();
    }
  }

  /** Shift all nodes so that the center of gravity is located at the center of the screen. */
  public void adjustForGravity() {
    float center = 0;
    float gx = 0;
    float gy = 0;
    float gz = 0;
    for (int i = 0; i < xyzdata.length; i++) {
      gx += xyzdata[i].getX();
      gy += xyzdata[i].getY();
      gz += xyzdata[i].getZ();
    }
    gx /= xyzdata.length;
    gy /= xyzdata.length;
    gz /= xyzdata.length;
    float diffx = center - gx;
    float diffy = center - gy;
    float diffz = center - gz;
    for (int i = 0; i < xyzdata.length; i++) {
      xyzdata[i].set(
          xyzdata[i].getX() + diffx, xyzdata[i].getY() + diffy, xyzdata[i].getZ() + diffz);
    }
  }

  @Override
  public void setSize(BoundingSphere size) {
    if (initialized == false) {
      setInitializer(new RandomLocationTransformer<N>(size));
    }
    super.setSize(size);
  }

  public void setAdjustForGravity(boolean on) {
    adjustForGravity = on;
  }

  public boolean getAdjustForGravity() {
    return adjustForGravity;
  }

  /**
   * Enable or disable the local minimum escape technique by exchanging nodes.
   *
   * @param on iff the local minimum escape technique is to be enabled
   */
  public void setExchangenodes(boolean on) {
    exchangenodes = on;
  }

  public boolean getExchangenodes() {
    return exchangenodes;
  }

  /** Determines a step to new position of the node m. */
  private float[] calcDeltaXY(int m) {
    float dE_dxm = 0;
    float dE_dym = 0;
    float dE_dzm = 0;
    float d2E_d2xm = 0;
    float d2E_dxmdym = 0;
    float d2E_dymdxm = 0;

    float d2E_dxmdzm = 0;
    float d2E_dzmdxm = 0;
    float d2E_dymdzm = 0;
    float d2E_dzmdym = 0;

    float d2E_d2ym = 0;
    float d2E_d2zm = 0;

    for (int i = 0; i < nodes.length; i++) {
      if (i != m) {

        float dist = dm[m][i];
        float l_mi = L * dist;
        float k_mi = K / (dist * dist);
        float dx = xyzdata[m].getX() - xyzdata[i].getX();
        float dy = xyzdata[m].getY() - xyzdata[i].getY();
        float dz = xyzdata[m].getZ() - xyzdata[i].getZ();

        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float ddd = d * d * d;

        dE_dxm += k_mi * (1 - l_mi / d) * dx;
        dE_dym += k_mi * (1 - l_mi / d) * dy;
        dE_dzm += k_mi * (1 - l_mi / d) * dz;

        d2E_d2xm += k_mi * (1 - l_mi * dy * dy / ddd);
        d2E_dxmdym += k_mi * l_mi * dx * dy / ddd;
        d2E_d2ym += k_mi * (1 - l_mi * dz * dz / ddd);
        d2E_dymdzm += k_mi * l_mi * dy * dz / ddd;

        d2E_d2zm += k_mi * (1 - l_mi * dx * dx / ddd);
        d2E_dzmdxm += k_mi * l_mi * dz * dx / ddd;
      }
    }
    // d2E_dymdxm equals to d2E_dxmdym.
    d2E_dymdxm = d2E_dxmdym;

    float denomi = d2E_d2xm * d2E_d2ym - d2E_dxmdym * d2E_dymdxm;
    float deltaX = (d2E_dxmdym * dE_dym - d2E_d2ym * dE_dxm) / denomi;
    float deltaY = (d2E_dymdzm * dE_dzm - d2E_d2zm * dE_dym) / denomi;
    float deltaZ = (d2E_dzmdxm * dE_dxm - d2E_d2xm * dE_dzm) / denomi;

    return new float[] {deltaX, deltaY, deltaZ};
  }

  /** Calculates the gradient of energy function at the node m. */
  private float calcDeltaM(int m) {
    float dEdxm = 0;
    float dEdym = 0;
    float dEdzm = 0;
    for (int i = 0; i < nodes.length; i++) {
      if (i != m) {
        float dist = dm[m][i];
        float l_mi = L * dist;
        float k_mi = K / (dist * dist);

        float dx = xyzdata[m].getX() - xyzdata[i].getX();
        float dy = xyzdata[m].getY() - xyzdata[i].getY();
        float dz = xyzdata[m].getZ() - xyzdata[i].getZ();
        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        float common = k_mi * (1 - l_mi / d);
        dEdxm += common * dx;
        dEdym += common * dy;
        dEdzm += common * dz;
      }
    }
    return (float) Math.sqrt(dEdxm * dEdxm + dEdym * dEdym + dEdzm * dEdzm);
  }

  /** Calculates the energy function E. */
  private float calcEnergy() {
    float energy = 0;
    for (int i = 0; i < nodes.length - 1; i++) {
      for (int j = i + 1; j < nodes.length; j++) {
        float dist = dm[i][j];
        float l_ij = L * dist;
        float k_ij = K / (dist * dist);
        float dx = xyzdata[i].getX() - xyzdata[j].getX();
        float dy = xyzdata[i].getY() - xyzdata[j].getY();
        float dz = xyzdata[i].getZ() - xyzdata[j].getZ();
        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        energy += k_ij / 2 * (dx * dx + dy * dy + dz * dz + l_ij * l_ij - 2 * l_ij * d);
      }
    }
    return energy;
  }

  /** Calculates the energy function E as if positions of the specified nodes are exchanged. */
  private float calcEnergyIfExchanged(int p, int q) {
    if (p >= q) {
      throw new RuntimeException("p should be < q");
    }
    float energy = 0; // < 0
    for (int i = 0; i < nodes.length - 1; i++) {
      for (int j = i + 1; j < nodes.length; j++) {
        int ii = i;
        int jj = j;
        if (i == p) {
          ii = q;
        }
        if (j == q) {
          jj = p;
        }

        float dist = dm[i][j];
        float l_ij = L * dist;
        float k_ij = K / (dist * dist);
        float dx = xyzdata[ii].getX() - xyzdata[jj].getX();
        float dy = xyzdata[ii].getY() - xyzdata[jj].getY();
        float dz = xyzdata[ii].getZ() - xyzdata[jj].getZ();
        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        energy += k_ij / 2 * (dx * dx + dy * dy + dz * dz + l_ij * l_ij - 2 * l_ij * d);
      }
    }
    return energy;
  }

  public void reset() {
    currentIteration = 0;
  }
}
