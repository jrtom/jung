/*
 * Created on May 4, 2004
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io;

import com.google.common.base.Function;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes graphs in the Pajek NET format.
 *
 * <p>Labels for vertices, edge weights, and vertex locations may each optionally be specified. Note
 * that vertex location coordinates must be normalized to the interval [0, 1] on each axis in order
 * to conform to the Pajek specification.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson - converted to jung2
 */
public class PajekNetWriter<V, E> {
  /** Creates a new instance. */
  public PajekNetWriter() {}

  /**
   * Saves the graph to the specified file.
   *
   * @param g the graph to be saved
   * @param filename the filename of the file to write the graph to
   * @param vs mapping from vertices to labels
   * @param nev mapping from edges to weights
   * @param vld mapping from vertices to locations
   * @throws IOException if the graph cannot be saved
   */
  public void save(
      Network<V, E> g,
      String filename,
      Function<V, String> vs,
      Function<E, Number> nev,
      Function<V, Point2D> vld)
      throws IOException {
    save(g, new FileWriter(filename), vs, nev, vld);
  }

  /**
   * Saves the graph to the specified file.
   *
   * @param g the graph to be saved
   * @param filename the filename of the file to write the graph to
   * @param vs mapping from vertices to labels
   * @param nev mapping from edges to weights
   * @throws IOException if the graph cannot be saved
   */
  public void save(
      Network<V, E> g, String filename, Function<V, String> vs, Function<E, Number> nev)
      throws IOException {
    save(g, new FileWriter(filename), vs, nev, null);
  }

  /**
   * Saves the graph to the specified file. No vertex labels are written, and the edge weights are
   * written as 1.0.
   *
   * @param g the graph to be saved
   * @param filename the filename of the file to write the graph to
   * @throws IOException if the graph cannot be saved
   */
  public void save(Network<V, E> g, String filename) throws IOException {
    save(g, filename, null, null, null);
  }

  /**
   * Saves the graph to the specified writer. No vertex labels are written, and the edge weights are
   * written as 1.0.
   *
   * @param g the graph to be saved
   * @param w the writer instance to write the graph to
   * @throws IOException if the graph cannot be saved
   */
  public void save(Network<V, E> g, Writer w) throws IOException {
    save(g, w, null, null, null);
  }

  /**
   * Saves the graph to the specified writer.
   *
   * @param g the graph to be saved
   * @param w the writer instance to write the graph to
   * @param vs mapping from vertices to labels
   * @param nev mapping from edges to weights
   * @throws IOException if the graph cannot be saved
   */
  public void save(Network<V, E> g, Writer w, Function<V, String> vs, Function<E, Number> nev)
      throws IOException {
    save(g, w, vs, nev, null);
  }

  /**
   * Saves the graph to the specified writer.
   *
   * @param graph the graph to be saved
   * @param w the writer instance to write the graph to
   * @param vs mapping from vertices to labels (no labels are written if null)
   * @param nev mapping from edges to weights (defaults to weights of 1.0 if null)
   * @param vld mapping from vertices to locations (no locations are written if null)
   * @throws IOException if the graph cannot be saved
   */
  public void save(
      Network<V, E> graph,
      Writer w,
      Function<V, String> vs,
      Function<E, Number> nev,
      Function<V, Point2D> vld)
      throws IOException {
    /*
     * TODO: Changes we might want to make:
     * - optionally writing out in list form
     */

    BufferedWriter writer = new BufferedWriter(w);
    if (nev == null) {
      nev =
          new Function<E, Number>() {
            public Number apply(E e) {
              return 1;
            }
          };
    }
    writer.write("*Vertices " + graph.nodes().size());
    writer.newLine();

    List<V> id = new ArrayList<V>(graph.nodes());
    for (V currentVertex : graph.nodes()) {
      // convert from 0-based to 1-based index
      int v_id = id.indexOf(currentVertex) + 1;
      writer.write("" + v_id);
      if (vs != null) {
        String label = vs.apply(currentVertex);
        if (label != null) {
          writer.write(" \"" + label + "\"");
        }
      }
      if (vld != null) {
        Point2D location = vld.apply(currentVertex);
        if (location != null) {
          writer.write(" " + location.getX() + " " + location.getY() + " 0.0");
        }
      }
      writer.newLine();
    }

    writer.write(graph.isDirected() ? "*Arcs" : "*Edges");
    writer.newLine();

    for (E e : graph.edges()) {
      EndpointPair<V> endpoints = graph.incidentNodes(e);
      // convert from 0-based to 1-based index
      int nodeU_id = id.indexOf(endpoints.nodeU()) + 1;
      int nodeV_id = id.indexOf(endpoints.nodeV()) + 1;
      float weight = nev.apply(e).floatValue();
      writer.write(nodeU_id + " " + nodeV_id + " " + weight);
      writer.newLine();
    }
    writer.close();
  }
}
