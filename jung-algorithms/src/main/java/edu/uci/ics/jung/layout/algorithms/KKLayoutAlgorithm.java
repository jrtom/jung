/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
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
public class KKLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(KKLayoutAlgorithm.class);

  private double EPSILON = 0.1d;

  private int currentIteration;
  private int maxIterations = 2000;
  private String status = "KKLayout";

  private double L; // the ideal length of an edge
  private double K = 1; // arbitrary const number
  private double[][] dm; // distance matrix

  private boolean adjustForGravity = true;
  private boolean exchangenodes = true;

  private N[] nodes;
  private Point[] xydata;

  /** Retrieves graph distances between nodes of the visible graph */
  protected BiFunction<N, N, Number> distance;

  /**
   * The diameter of the visible graph. In other words, the maximum over all pairs of nodes of the
   * length of the shortest path between a and bf the visible graph.
   */
  protected double diameter;

  /** A multiplicative factor which partly specifies the "preferred" length of an edge (L). */
  private double length_factor = 0.9;

  /**
   * A multiplicative factor which specifies the fraction of the graph's diameter to be used as the
   * inter-node distance between disconnected nodes.
   */
  private double disconnected_multiplier = 0.5;

  public KKLayoutAlgorithm() {}

  public KKLayoutAlgorithm(Distance<N> distance) {
    this.distance = (x, y) -> distance.getDistance(x, y);
  }

  @Override
  public void visit(LayoutModel<N> layoutModel) {
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
  public void setLengthFactor(double length_factor) {
    this.length_factor = length_factor;
  }

  /**
   * @param disconnected_multiplier a multiplicative factor that specifies the fraction of the
   *     graph's diameter to be used as the inter-node distance between disconnected nodes
   */
  public void setDisconnectedDistanceMultiplier(double disconnected_multiplier) {
    this.disconnected_multiplier = disconnected_multiplier;
  }

  /**
   * @return a string with information about the current status of the algorithm.
   */
  public String getStatus() {
    return status + layoutModel.getWidth() + " " + layoutModel.getHeight();
  }

  public void setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  /**
   * @return true if the current iteration has passed the maximum count.
   */
  public boolean done() {
    if (currentIteration > maxIterations) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public void initialize() {
    currentIteration = 0;
    Graph<N> graph = layoutModel.getGraph();
    // KKLayoutAlgorithm will fail if all nodes start at the same location
    layoutModel.setInitializer(
        new RandomLocationTransformer<N>(
            layoutModel.getWidth(), layoutModel.getHeight(), graph.nodes().size()));
    if (graph != null && layoutModel != null) {

      double height = layoutModel.getHeight();
      double width = layoutModel.getWidth();

      int n = graph.nodes().size();
      dm = new double[n][n];
      nodes = (N[]) graph.nodes().toArray();
      xydata = new Point[n];

      // assign IDs to all visible nodes
      while (true) {
        try {
          int index = 0;
          for (N node : graph.nodes()) {
            Point xyd = layoutModel.apply(node);
            nodes[index] = node;
            xydata[index] = xyd;
            index++;
          }
          break;
        } catch (ConcurrentModificationException cme) {
        }
      }

      diameter = DistanceStatistics.<N>diameter(graph, distance, true);

      double L0 = Math.min(height, width);
      L = (L0 / diameter) * length_factor; // length_factor used to be hardcoded to 0.9
      // L = 0.75 * Math.sqrt(height * width / n);

      for (int i = 0; i < n - 1; i++) {
        for (int j = i + 1; j < n; j++) {
          Number d_ij = distance.apply(nodes[i], nodes[j]);
          log.trace("distance from " + i + " to " + j + " is " + d_ij);

          Number d_ji = distance.apply(nodes[j], nodes[i]);
          log.trace("distance from " + j + " to " + i + " is " + d_ji);

          double dist = diameter * disconnected_multiplier;
          log.trace("dist:" + dist);
          if (d_ij != null) {
            dist = Math.min(d_ij.doubleValue(), dist);
          }
          if (d_ji != null) {
            dist = Math.min(d_ji.doubleValue(), dist);
          }
          dm[i][j] = dm[j][i] = dist;
        }
      }
    }
  }

  @Override
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
      double deltam = calcDeltaM(i);

      if (maxDeltaM < deltam) {
        maxDeltaM = deltam;
        pm = i;
      }
    }
    if (pm == -1) {
      return;
    }

    for (int i = 0; i < 100; i++) {
      double[] dxy = calcDeltaXY(pm);
      xydata[pm] = Point.of(xydata[pm].x + dxy[0], xydata[pm].y + dxy[1]);
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
            double sx = xydata[i].x;
            double sy = xydata[i].y;
            xydata[i] = Point.of(xydata[j].x, xydata[j].y);
            xydata[j] = Point.of(sx, sy);
            return;
          }
        }
      }
    }
  }

  /** Shift all nodes so that the center of gravity is located at the center of the screen. */
  public void adjustForGravity() {
    double height = layoutModel.getHeight();
    double width = layoutModel.getWidth();
    double gx = 0;
    double gy = 0;
    for (int i = 0; i < xydata.length; i++) {
      gx += xydata[i].x;
      gy += xydata[i].y;
    }
    gx /= xydata.length;
    gy /= xydata.length;
    double diffx = width / 2 - gx;
    double diffy = height / 2 - gy;
    for (int i = 0; i < xydata.length; i++) {
      xydata[i] = xydata[i].add(diffx, diffy);
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
  private double[] calcDeltaXY(int m) {
    double dE_dxm = 0;
    double dE_dym = 0;
    double d2E_d2xm = 0;
    double d2E_dxmdym = 0;
    double d2E_dymdxm = 0;
    double d2E_d2ym = 0;

    for (int i = 0; i < nodes.length; i++) {
      if (i != m) {

        double dist = dm[m][i];
        double l_mi = L * dist;
        double k_mi = K / (dist * dist);
        double dx = xydata[m].x - xydata[i].x;
        double dy = xydata[m].y - xydata[i].y;
        double d = Math.sqrt(dx * dx + dy * dy);
        double ddd = d * d * d;

        dE_dxm += k_mi * (1 - l_mi / d) * dx;
        dE_dym += k_mi * (1 - l_mi / d) * dy;
        d2E_d2xm += k_mi * (1 - l_mi * dy * dy / ddd);
        d2E_dxmdym += k_mi * l_mi * dx * dy / ddd;
        d2E_d2ym += k_mi * (1 - l_mi * dx * dx / ddd);
      }
    }
    // d2E_dymdxm equals to d2E_dxmdym.
    d2E_dymdxm = d2E_dxmdym;

    double denomi = d2E_d2xm * d2E_d2ym - d2E_dxmdym * d2E_dymdxm;
    double deltaX = (d2E_dxmdym * dE_dym - d2E_d2ym * dE_dxm) / denomi;
    double deltaY = (d2E_dymdxm * dE_dxm - d2E_d2xm * dE_dym) / denomi;
    return new double[] {deltaX, deltaY};
  }

  /** Calculates the gradient of energy function at the node m. */
  private double calcDeltaM(int m) {
    double dEdxm = 0;
    double dEdym = 0;
    for (int i = 0; i < nodes.length; i++) {
      if (i != m) {
        double dist = dm[m][i];
        double l_mi = L * dist;
        double k_mi = K / (dist * dist);

        double dx = xydata[m].x - xydata[i].x;
        double dy = xydata[m].y - xydata[i].y;
        double d = Math.sqrt(dx * dx + dy * dy);

        double common = k_mi * (1 - l_mi / d);
        dEdxm += common * dx;
        dEdym += common * dy;
      }
    }
    return Math.sqrt(dEdxm * dEdxm + dEdym * dEdym);
  }

  /** Calculates the energy function E. */
  private double calcEnergy() {
    double energy = 0;
    for (int i = 0; i < nodes.length - 1; i++) {
      for (int j = i + 1; j < nodes.length; j++) {
        double dist = dm[i][j];
        double l_ij = L * dist;
        double k_ij = K / (dist * dist);
        double dx = xydata[i].x - xydata[j].x;
        double dy = xydata[i].y - xydata[j].y;
        double d = Math.sqrt(dx * dx + dy * dy);

        energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij - 2 * l_ij * d);
      }
    }
    return energy;
  }

  /** Calculates the energy function E as if positions of the specified nodes are exchanged. */
  private double calcEnergyIfExchanged(int p, int q) {
    if (p >= q) {
      throw new RuntimeException("p should be < q");
    }
    double energy = 0; // < 0
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
        double dx = xydata[ii].x - xydata[jj].x;
        double dy = xydata[ii].y - xydata[jj].y;
        double d = Math.sqrt(dx * dx + dy * dy);

        energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij - 2 * l_ij * d);
      }
    }
    return energy;
  }
}
