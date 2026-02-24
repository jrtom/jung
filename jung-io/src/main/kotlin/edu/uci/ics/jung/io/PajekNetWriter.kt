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
package edu.uci.ics.jung.io

import com.google.common.graph.Network
import java.awt.geom.Point2D
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.Writer
import java.util.ArrayList
import java.util.function.Function

/**
 * Writes graphs in the Pajek NET format.
 *
 * Labels for nodes, edge weights, and node locations may each optionally be specified. Note that
 * node location coordinates must be normalized to the interval [0, 1] on each axis in order to
 * conform to the Pajek specification.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson - converted to jung2
 */
class PajekNetWriter<N, E> {

  /**
   * Saves the graph to the specified file.
   *
   * @param g the graph to be saved
   * @param filename the filename of the file to write the graph to
   * @param vs mapping from nodes to labels
   * @param nev mapping from edges to weights
   * @param vld mapping from nodes to locations
   * @throws IOException if the graph cannot be saved
   */
  @Throws(IOException::class)
  fun save(
    g: Network<N, E>,
    filename: String,
    vs: Function<N, String>?,
    nev: Function<E, Number>?,
    vld: Function<N, Point2D>?
  ) {
    save(g, FileWriter(filename), vs, nev, vld)
  }

  /**
   * Saves the graph to the specified file.
   *
   * @param g the graph to be saved
   * @param filename the filename of the file to write the graph to
   * @param vs mapping from nodes to labels
   * @param nev mapping from edges to weights
   * @throws IOException if the graph cannot be saved
   */
  @Throws(IOException::class)
  fun save(
    g: Network<N, E>,
    filename: String,
    vs: Function<N, String>?,
    nev: Function<E, Number>?
  ) {
    save(g, FileWriter(filename), vs, nev, null)
  }

  /**
   * Saves the graph to the specified file. No node labels are written, and the edge weights are
   * written as 1.0.
   *
   * @param g the graph to be saved
   * @param filename the filename of the file to write the graph to
   * @throws IOException if the graph cannot be saved
   */
  @Throws(IOException::class)
  fun save(g: Network<N, E>, filename: String) {
    save(g, filename, null, null, null)
  }

  /**
   * Saves the graph to the specified writer. No node labels are written, and the edge weights are
   * written as 1.0.
   *
   * @param g the graph to be saved
   * @param w the writer instance to write the graph to
   * @throws IOException if the graph cannot be saved
   */
  @Throws(IOException::class)
  fun save(g: Network<N, E>, w: Writer) {
    save(g, w, null, null, null)
  }

  /**
   * Saves the graph to the specified writer.
   *
   * @param g the graph to be saved
   * @param w the writer instance to write the graph to
   * @param vs mapping from nodes to labels
   * @param nev mapping from edges to weights
   * @throws IOException if the graph cannot be saved
   */
  @Throws(IOException::class)
  fun save(
    g: Network<N, E>,
    w: Writer,
    vs: Function<N, String>?,
    nev: Function<E, Number>?
  ) {
    save(g, w, vs, nev, null)
  }

  /**
   * Saves the graph to the specified writer.
   *
   * @param graph the graph to be saved
   * @param w the writer instance to write the graph to
   * @param vs mapping from nodes to labels (no labels are written if null)
   * @param nev mapping from edges to weights (defaults to weights of 1.0 if null)
   * @param vld mapping from nodes to locations (no locations are written if null)
   * @throws IOException if the graph cannot be saved
   */
  @Throws(IOException::class)
  fun save(
    graph: Network<N, E>,
    w: Writer,
    vs: Function<N, String>?,
    nev: Function<E, Number>?,
    vld: Function<N, Point2D>?
  ) {
    val writer = BufferedWriter(w)
    val effectiveNev = nev ?: Function<E, Number> { 1 }
    writer.write("*Nodes ${graph.nodes().size}")
    writer.newLine()

    val id = ArrayList<N>(graph.nodes())
    for (currentNode in graph.nodes()) {
      // convert from 0-based to 1-based index
      val v_id = id.indexOf(currentNode) + 1
      writer.write("$v_id")
      if (vs != null) {
        val label = vs.apply(currentNode)
        if (label != null) {
          writer.write(" \"$label\"")
        }
      }
      if (vld != null) {
        val location = vld.apply(currentNode)
        if (location != null) {
          writer.write(" ${location.x} ${location.y} 0.0")
        }
      }
      writer.newLine()
    }

    writer.write(if (graph.isDirected) "*Arcs" else "*Edges")
    writer.newLine()

    for (e in graph.edges()) {
      val endpoints = graph.incidentNodes(e)
      // convert from 0-based to 1-based index
      val nodeU_id = id.indexOf(endpoints.nodeU()) + 1
      val nodeV_id = id.indexOf(endpoints.nodeV()) + 1
      val weight = effectiveNev.apply(e).toFloat()
      writer.write("$nodeU_id $nodeV_id $weight")
      writer.newLine()
    }
    writer.close()
  }
}
