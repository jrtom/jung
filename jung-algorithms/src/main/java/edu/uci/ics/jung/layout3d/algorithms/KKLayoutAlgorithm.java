/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout3d.algorithms;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.algorithms.AbstractIterativeLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import java.util.ConcurrentModificationException;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Kamada-Kawai algorithm for node layout. Does not respect filter calls, and
 * sometimes crashes when the view changes to it.
 *
 * @see "Tomihisa Kamada and Satoru Kawai: An algorithm for drawing general indirect graphs.
 *     Information Processing Letters 31(1):7-15, 1989"
 * @see "Tomihisa Kamada: On visualization of abstract objects and relations. Ph.D. dissertation,
 *     Dept. of Information Science, Univ. of Tokyo, Dec. 1988."
 * @author Masanori Harada
 * @author Tom Nelson
 */
public class KKLayoutAlgorithm<N, P> extends AbstractIterativeLayoutAlgorithm<N, P>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(KKLayoutAlgorithm.class);

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
  private P[] xydata;

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

  public KKLayoutAlgorithm() {}

  public KKLayoutAlgorithm(Distance<N> distance) {
    this.distance = (x, y) -> distance.getDistance(x, y);
  }

  @Override
  public void visit(LayoutModel<N, P> layoutModel) {
    super.visit(layoutModel);

    Graph<N> graph = layoutModel.getGraph();
    if (graph != null) {
      Distance distance = new UnweightedShortestPath<N>(graph);
      this.distance = (x, y) -> distance.getDistance(x, y);
    }
    initialize();
  }

  /**
   * @param length_factor a multiplicative factor which partially specifies the preferred length of
   *     an edge
   */
  public void setLengthFactor(float length_factor) {
    this.length_factor = length_factor;
  }

  /**
   * // * @param disconnected_multiplier a multiplicative factor that specifies the fraction of the
   * graph's diameter to be used as the inter-node distance between disconnected nodes float public
   * void setDisconnectedDistanceMultiplier(double disconnected_multiplier) {
   * this.disconnected_multiplier = disconnected_multiplier; }
   *
   * <p>/** @return a string with information about the current status of the algorithm.
   */
  public String getStatus() {
    return status + layoutModel.getWidth() + " " + layoutModel.getHeight();
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
      log.trace("is done");
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public void initialize() {
    currentIteration = 0;
    Graph<N> graph = layoutModel.getGraph();
    // KKLayoutAlgorithm will fail if all vertices start at the same location
    layoutModel.setInitializer(
        new RandomLocationTransformer<N, P>(
            RandomLocationTransformer.Origin.CENTER,
            layoutModel.getPointModel(),
            layoutModel.getWidth(),
            layoutModel.getHeight(),
            layoutModel.getDepth(),
            graph.nodes().size()));
    if (graph != null && layoutModel != null) {

      float height = layoutModel.getHeight();
      float width = layoutModel.getWidth();
      float depth = layoutModel.getDepth();

      int n = graph.nodes().size();
      dm = new float[n][n];
      nodes = (N[]) graph.nodes().toArray();
      xydata = (P[]) new Object[n];

      // assign IDs to all visible nodes
      while (true) {
        try {
          int index = 0;
          for (N node : graph.nodes()) {
            P xyd = layoutModel.apply(node);
            nodes[index] = node;
            xydata[index] = xyd;
            index++;
          }
          break;
        } catch (ConcurrentModificationException cme) {
        }
      }

      diameter = (float) DistanceStatistics.<N>diameter(graph, distance, true);

      float L0 = Math.min(height, width);
      L = (L0 / diameter) * length_factor; // length_factor used to be hardcoded to 0.9
      //L = 0.75 * Math.sqrt(height * width / n);

      for (int i = 0; i < n - 1; i++) {
        for (int j = i + 1; j < n; j++) {
          Number d_ij = distance.apply(nodes[i], nodes[j]);
          log.trace("distance from " + i + " to " + j + " is " + d_ij);

          Number d_ji = distance.apply(nodes[j], nodes[i]);
          log.trace("distance from " + j + " to " + i + " is " + d_ji);

          float dist = diameter * disconnected_multiplier;
          log.trace("dist:" + dist);
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
    Graph<N> graph = layoutModel.getGraph();
    currentIteration++;
    double energy = calcEnergy();
    status =
        "Kamada-Kawai N="
            + graph.nodes().size()
            + "("
            + graph.nodes().size()
            + ")"
            + " IT: "
            + currentIteration
            + " E="
            + energy;

    int n = graph.nodes().size();
    if (n == 0) {
      return;
    }

    double maxDeltaM = 0;
    int pm = -1; // the node having max deltaM
    for (int i = 0; i < n; i++) {
      if (layoutModel.isLocked(nodes[i])) {
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
      pointModel.setLocation(
          xydata[pm],
          pointModel.getX(xydata[pm]) + dxy[0],
          pointModel.getY(xydata[pm]) + dxy[1],
          pointModel.getZ(xydata[pm]) + dxy[2]);

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
        if (layoutModel.isLocked(nodes[i])) {
          continue;
        }
        for (int j = i + 1; j < n; j++) {
          if (layoutModel.isLocked(nodes[j])) {
            continue;
          }
          double xenergy = calcEnergyIfExchanged(i, j);
          if (energy > xenergy) {
            double sx = pointModel.getX(xydata[i]);
            double sy = pointModel.getY(xydata[i]);
            double sz = pointModel.getZ(xydata[i]);
            pointModel.setLocation(xydata[i], xydata[j]);
            pointModel.setLocation(xydata[j], sx, sy, sz);
            return;
          }
        }
      }
    }
  }

  /** Shift all nodes so that the center of gravity is located at the center of the screen. */
  public void adjustForGravity() {
    float gx = 0;
    float gy = 0;
    float gz = 0;
    for (int i = 0; i < xydata.length; i++) {
      gx += pointModel.getX(xydata[i]);
      gy += pointModel.getY(xydata[i]);
      gz += pointModel.getZ(xydata[i]);
    }
    gx /= xydata.length;
    gy /= xydata.length;
    gz /= xydata.length;
    // move the center to the origin
    double diffx = 0 - gx;
    double diffy = 0 - gy;
    double diffz = 0 - gz;
    for (int i = 0; i < xydata.length; i++) {
      pointModel.setLocation(
          xydata[i],
          pointModel.getX(xydata[i]) + diffx,
          pointModel.getY(xydata[i]) + diffy,
          pointModel.getZ(xydata[i]) + diffz);
      layoutModel.set(nodes[i], xydata[i]);
    }
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
    float d2E_dzmdxm = 0;

    //    float d2E_dymdzm = 0;

    float d2E_d2ym = 0;
    //    float d2E_d2zm = 0;

    for (int i = 0; i < nodes.length; i++) {
      if (i != m) {

        float dist = dm[m][i];
        float l_mi = L * dist;
        float k_mi = K / (dist * dist);
        float dx = (float) (pointModel.getX(xydata[m]) - pointModel.getX(xydata[i]));
        float dy = (float) (pointModel.getY(xydata[m]) - pointModel.getY(xydata[i]));
        float dz = (float) (pointModel.getZ(xydata[m]) - pointModel.getZ(xydata[i]));
        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float ddd = d * d * d;

        dE_dxm += k_mi * (1 - l_mi / d) * dx;
        dE_dym += k_mi * (1 - l_mi / d) * dy;
        dE_dzm += k_mi * (1 - l_mi / d) * dz;

        d2E_d2xm += k_mi * (1 - l_mi * dy * dy / ddd);
        d2E_dxmdym += k_mi * l_mi * dx * dy / ddd;
        d2E_d2ym += k_mi * (1 - l_mi * dz * dz / ddd);
        //        d2E_dymdzm += k_mi * l_mi * dy * dz / ddd;

        //        d2E_d2zm += k_mi * (1 - l_mi * dx * dx / ddd);
        d2E_dzmdxm += k_mi * l_mi * dz * dx / ddd;
      }
    }

    d2E_dymdxm = d2E_dxmdym;

    float denomi = d2E_d2xm * d2E_d2ym - d2E_dxmdym * d2E_dymdxm;
    float deltaX = (d2E_dxmdym * dE_dym - d2E_d2ym * dE_dxm) / denomi;
    float deltaY = (d2E_dymdxm * dE_dxm - d2E_d2xm * dE_dym) / denomi;
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

        double dx = pointModel.getX(xydata[m]) - pointModel.getX(xydata[i]);
        double dy = pointModel.getY(xydata[m]) - pointModel.getY(xydata[i]);
        double dz = pointModel.getZ(xydata[m]) - pointModel.getZ(xydata[i]);
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
        double dist = dm[i][j];
        double l_ij = L * dist;
        double k_ij = K / (dist * dist);
        double dx = pointModel.getX(xydata[i]) - pointModel.getX(xydata[j]);
        double dy = pointModel.getY(xydata[i]) - pointModel.getY(xydata[j]);
        double dz = pointModel.getZ(xydata[i]) - pointModel.getZ(xydata[j]);

        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
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

        double dist = dm[i][j];
        double l_ij = L * dist;
        double k_ij = K / (dist * dist);
        double dx = pointModel.getX(xydata[ii]) - pointModel.getX(xydata[jj]);
        double dy = pointModel.getY(xydata[ii]) - pointModel.getY(xydata[jj]);
        double dz = pointModel.getZ(xydata[ii]) - pointModel.getZ(xydata[jj]);

        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
        energy += k_ij / 2 * (dx * dx + dy * dy + dz * dz + l_ij * l_ij - 2 * l_ij * d);
      }
    }
    return energy;
  }

  public void reset() {
    currentIteration = 0;
  }
}
