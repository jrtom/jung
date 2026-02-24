/*
 * Created on May 3, 2004
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

import com.google.common.base.Preconditions
import com.google.common.graph.MutableNetwork
import edu.uci.ics.jung.algorithms.util.MapSettableTransformer
import edu.uci.ics.jung.algorithms.util.SettableTransformer
import java.awt.geom.Point2D
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.util.ArrayList
import java.util.HashMap
import java.util.StringTokenizer
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * Reads a `Graph` from a Pajek NET formatted source.
 *
 * If the edge constraints specify that the graph is strictly undirected, and an "*Arcs" section
 * is encountered, or if the edge constraints specify that the graph is strictly directed, and an
 * "*Edges" section is encountered, an `IllegalArgumentException` is thrown.
 *
 * If the edge constraints do not permit parallel edges, only the first encountered of a set of
 * parallel edges will be read; subsequent edges in that set will be ignored.
 *
 * More restrictive edge constraints will cause nodes to be generated that are more time- and
 * space-efficient.
 *
 * At the moment, only supports the part of the specification that defines:
 *
 * - node ids (each must have a value from 1 to n, where n is the number of nodes)
 * - node labels (must be in quotes if interrupted by whitespace)
 * - directed edge connections (single or list)
 * - undirected edge connections (single or list)
 * - edge weights (not compatible with edges specified in list form)
 *   **note**: this version of PajekNetReader does not support multiple edge weights, as
 *   PajekNetFile does; this behavior is consistent with the NET format.
 * - node locations (x and y; z coordinate is ignored)
 *
 * Here is an example format for a directed graph without edge weights and edges specified in
 * list form:
 * ```
 * *nodes [# of nodes]
 * 1 "a"
 * 2 "b"
 * 3 "c"
 * *arcslist
 * 1 2 3
 * 2 3
 * ```
 *
 * Here is an example format for an undirected graph with edge weights and edges specified in
 * non-list form:
 * ```
 * *nodes [# of nodes]
 * 1 "a"
 * 2 "b"
 * 3 "c"
 * *edges
 * 1 2 0.1
 * 1 3 0.9
 * 2 3 1.0
 * ```
 *
 * @author Joshua O'Madadhain
 * @see [Pajek Manual](http://vlado.fmf.uni-lj.si/pub/networks/pajek/doc/pajekman.pdf)
 * @author Tom Nelson - converted to jung2
 */
open class PajekNetReader<G : MutableNetwork<N, E>, N : Any, E : Any> {
  internal var node_factory: Supplier<N>?
  internal var edge_factory: Supplier<E>

  /** The map for node labels (if any) created by this class. */
  internal var node_labels: SettableTransformer<N, String> =
    MapSettableTransformer<N, String>(HashMap())

  /** The map for node locations (if any) defined by this class. */
  internal var node_locations: SettableTransformer<N, Point2D> =
    MapSettableTransformer<N, Point2D>(HashMap())

  internal var edge_weights: SettableTransformer<E, Number>? =
    MapSettableTransformer<E, Number>(HashMap())

  /**
   * Creates a PajekNetReader instance with the specified node and edge factories.
   *
   * @param node_factory the Supplier to use to create node objects
   * @param edge_factory the Supplier to use to create edge objects
   */
  constructor(node_factory: Supplier<N>?, edge_factory: Supplier<E>) {
    this.node_factory = node_factory
    this.edge_factory = edge_factory
  }

  /**
   * Creates a PajekNetReader instance with the specified edge Supplier, and whose node objects
   * correspond to the integer IDs assigned in the file. Note that this requires `V` to
   * be assignment-compatible with an `Integer` value.
   *
   * @param edge_factory the Supplier to use to create edge objects
   */
  constructor(edge_factory: Supplier<E>) : this(null, edge_factory)

  /**
   * Returns the graph created by parsing the specified file, as created by the specified Supplier.
   *
   * @param filename the file from which the graph is to be read
   * @param graph_factory used to provide a graph instance
   * @return a graph parsed from the specified file
   * @throws IOException if the graph cannot be loaded
   */
  @Throws(IOException::class)
  fun load(filename: String, graph_factory: Supplier<out G>): G {
    return load(FileReader(filename), graph_factory.get())
  }

  /**
   * Returns the graph created by parsing the specified reader, as created by the specified
   * Supplier.
   *
   * @param reader the reader instance from which the graph is to be read
   * @param graph_factory used to provide a graph instance
   * @return a graph parsed from the specified reader
   * @throws IOException if the graph cannot be loaded
   */
  @Throws(IOException::class)
  fun load(reader: Reader, graph_factory: Supplier<out G>): G {
    return load(reader, graph_factory.get())
  }

  /**
   * Returns the graph created by parsing the specified file, by populating the specified graph.
   *
   * @param filename the file from which the graph is to be read
   * @param g the graph instance to populate
   * @return a graph parsed from the specified file
   * @throws IOException if the graph cannot be loaded
   */
  @Throws(IOException::class)
  fun load(filename: String, g: G): G {
    return load(FileReader(filename), g)
  }

  /**
   * Populates the graph `g` with the graph represented by the Pajek-format data supplied
   * by `reader`. Stores edge weights, if any, according to `nev` (if
   * non-null).
   *
   * Any existing nodes/edges of `g`, if any, are unaffected.
   *
   * The edge data are filtered according to `g`'s constraints, if any; thus, if
   * `g` only accepts directed edges, any undirected edges in the input are ignored.
   *
   * @param reader the reader from which the graph is to be read
   * @param g the graph instance to populate
   * @return a graph parsed from the specified reader
   * @throws IOException if the graph cannot be loaded
   */
  @Throws(IOException::class)
  fun load(reader: Reader, g: G): G {
    Preconditions.checkNotNull(g)
    val br = BufferedReader(reader)

    // ignore everything until we see '*Nodes'
    var curLine = skip(br, v_pred)

    if (curLine == null) { // no nodes in the graph; return empty graph
      return g
    }

    // create appropriate number of nodes
    var st = StringTokenizer(curLine)
    st.nextToken() // skip past "*nodes"
    val num_nodes = Integer.parseInt(st.nextToken())
    var id: MutableList<N>? = null
    if (node_factory != null) {
      for (i in 1..num_nodes) {
        g.addNode(node_factory!!.get())
      }
      id = ArrayList<N>(g.nodes())
    }

    // read nodes until we see any Pajek format tag ('*...')
    curLine = null
    while (br.ready()) {
      curLine = br.readLine()
      if (curLine == null || t_pred.test(curLine)) {
        break
      }
      if (curLine == "") { // skip blank lines
        continue
      }

      try {
        readNode(curLine, id, num_nodes)
      } catch (iae: IllegalArgumentException) {
        br.close()
        reader.close()
        throw iae
      }
    }

    // skip over the intermediate stuff (if any)
    // and read the next arcs/edges section that we find
    curLine = readArcsOrEdges(curLine, br, g, id, edge_factory)

    // ditto
    readArcsOrEdges(curLine, br, g, id, edge_factory)

    br.close()
    reader.close()

    return g
  }

  /**
   * Parses `curLine` as a reference to a node, and optionally assigns label and location
   * information.
   */
  @Suppress("UNCHECKED_CAST", "DEPRECATION")
  private fun readNode(curLine: String, id: List<N>?, num_nodes: Int) {
    val v: N
    var parts: Array<String>? = null
    var coord_idx = -1 // index of first coordinate in parts; -1 indicates no coordinates found
    val index: String
    var label: String? = null
    // if there are quote marks on this line, split on them; label is surrounded by them
    if (curLine.indexOf('"') != -1) {
      val initial_split = curLine.trim().split("\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      // if there are any quote marks, there should be exactly 2
      Preconditions.checkArgument(
        initial_split.size == 2 || initial_split.size == 3,
        "Unbalanced (or too many) quote marks in $curLine"
      )
      index = initial_split[0].trim()
      label = initial_split[1].trim()
      if (initial_split.size == 3) {
        parts = initial_split[2].trim().split("\\s+".toRegex()).toTypedArray()
      }
      coord_idx = 0
    } else { // no quote marks, but are there coordinates?
      parts = curLine.trim().split("\\s+".toRegex()).toTypedArray()
      index = parts[0]
      when (parts.size) {
        1 -> {} // just the ID; nothing to do, continue
        2 -> label = parts[1] // just the ID and a label
        3 -> coord_idx = 1 // ID, no label, coordinates
        else -> coord_idx = 2 // ID, label, (x,y) coordinates, maybe some other stuff
      }
    }
    val v_id = Integer.parseInt(index) - 1 // go from 1-based to 0-based index
    Preconditions.checkArgument(v_id >= 0 && v_id < num_nodes)
    if (id != null) {
      v = id[v_id]
    } else {
      v = Integer(v_id) as N
    }
    // only attach the label if there's one to attach
    if (label != null && label.isNotEmpty()) {
      node_labels.set(v, label)
    }

    // parse the rest of the line
    if (coord_idx != -1
      && parts != null
      && parts.size >= coord_idx + 2
    ) {
      val x = java.lang.Double.parseDouble(parts[coord_idx])
      val y = java.lang.Double.parseDouble(parts[coord_idx + 1])
      node_locations.set(v, Point2D.Double(x, y))
    }
  }

  @Suppress("UNCHECKED_CAST", "DEPRECATION")
  @Throws(IOException::class)
  private fun readArcsOrEdges(
    curLine: String?,
    br: BufferedReader,
    g: MutableNetwork<N, E>,
    id: List<N>?,
    edge_factory: Supplier<E>
  ): String? {
    var nextLine = curLine

    // in case we're not there yet (i.e., format tag isn't arcs or edges)
    if (nextLine == null || !c_pred.test(nextLine)) {
      nextLine = skip(br, c_pred)
    }

    if (nextLine == null) {
      return nextLine
    }

    var reading_arcs = false
    var reading_edges = false
    if (a_pred.test(nextLine)) {
      Preconditions.checkState(
        g.isDirected, "Supplied undirected-only graph cannot be populated with directed edges"
      )
      reading_arcs = true
    }
    if (e_pred.test(nextLine)) {
      Preconditions.checkState(
        !g.isDirected,
        "Supplied directed-only graph cannot be populated with undirected edges"
      )
      reading_edges = true
    }

    if (!(reading_arcs || reading_edges)) {
      return nextLine
    }

    val is_list = l_pred.test(nextLine)

    while (br.ready()) {
      nextLine = br.readLine()
      if (nextLine == null || t_pred.test(nextLine)) {
        break
      }
      if (curLine == "") { // skip blank lines
        continue
      }

      val st = StringTokenizer(nextLine!!.trim())

      val vid1 = Integer.parseInt(st.nextToken()) - 1
      val v1: N
      if (id != null) {
        v1 = id[vid1]
      } else {
        v1 = Integer(vid1) as N
      }

      if (is_list) { // one source, multiple destinations
        do {
          createAddEdge(st, v1, g, id, edge_factory)
        } while (st.hasMoreTokens())
      } else { // one source, one destination, at most one weight
        val e = createAddEdge(st, v1, g, id, edge_factory)
        // get the edge weight if we care
        if (edge_weights != null && st.hasMoreTokens()) {
          edge_weights!!.set(e, java.lang.Float(st.nextToken()))
        }
      }
    }
    return nextLine
  }

  @Suppress("UNCHECKED_CAST", "DEPRECATION")
  internal open fun createAddEdge(
    st: StringTokenizer,
    v1: N,
    g: MutableNetwork<N, E>,
    id: List<N>?,
    edge_factory: Supplier<E>
  ): E {
    val vid2 = Integer.parseInt(st.nextToken()) - 1
    val v2: N
    if (id != null) {
      v2 = id[vid2]
    } else {
      v2 = Integer(vid2) as N
    }
    val e = edge_factory.get()

    // don't error-check this: let the graph implementation do whatever it's going to do
    // (add the edge, replace the existing edge, throw an exception--depends on the graph
    // implementation)
    g.addEdge(v1, v2, e)
    return e
  }

  /**
   * Returns the first line read from `br` for which `p` returns `true`,
   * or `null` if there is no such line.
   *
   * @param br the reader from which the graph is being read
   * @param p predicate specifying what line to accept
   * @return the first line from [br] that matches [p], or null
   * @throws IOException if an error is encountered while reading from [br]
   */
  @Throws(IOException::class)
  internal open fun skip(br: BufferedReader, p: Predicate<String>): String? {
    while (br.ready()) {
      var curLine = br.readLine() ?: break
      curLine = curLine.trim()
      if (p.test(curLine)) {
        return curLine
      }
    }
    return null
  }

  /**
   * A Predicate which evaluates to `true` if the argument starts with the
   * constructor-specified String.
   *
   * @author Joshua O'Madadhain
   */
  internal open class StartsWithPredicate(private val tag: String) : Predicate<String> {
    override fun test(str: String): Boolean {
      return str.lowercase().startsWith(tag)
    }
  }

  /**
   * A Predicate which evaluates to `true` if the argument ends with the string "list".
   *
   * @author Joshua O'Madadhain
   */
  internal open class ListTagPred protected constructor() : Predicate<String> {
    override fun test(s: String): Boolean {
      return s.lowercase().endsWith("list")
    }

    companion object {
      @JvmStatic
      internal fun getInstance(): ListTagPred = ListTagPred()
    }
  }

  /**
   * @return the nodeLocationTransformer
   */
  fun getNodeLocationTransformer(): SettableTransformer<N, Point2D> = node_locations

  /**
   * Provides a Function which will be used to write out the node locations.
   *
   * @param node_locations a container for the node locations
   */
  fun setNodeLocationTransformer(node_locations: SettableTransformer<N, Point2D>) {
    this.node_locations = node_locations
  }

  /**
   * @return a mapping from nodes to their labels
   */
  fun getNodeLabeller(): SettableTransformer<N, String> = node_labels

  /**
   * Provides a Function which will be used to write out the node labels.
   *
   * @param node_labels a container for the node labels
   */
  fun setNodeLabeller(node_labels: SettableTransformer<N, String>) {
    this.node_labels = node_labels
  }

  /**
   * @return a mapping from edges to their weights
   */
  fun getEdgeWeightTransformer(): SettableTransformer<E, Number>? = edge_weights

  /**
   * Provides a Function which will be used to write out edge weights.
   *
   * @param edge_weights a container for the edge weights
   */
  fun setEdgeWeightTransformer(edge_weights: SettableTransformer<E, Number>) {
    this.edge_weights = edge_weights
  }

  companion object {
    /** Used to specify whether the most recently read line is a Pajek-specific tag. */
    private val v_pred: Predicate<String> = StartsWithPredicate("*nodes")
    private val a_pred: Predicate<String> = StartsWithPredicate("*arcs")
    private val e_pred: Predicate<String> = StartsWithPredicate("*edges")
    private val t_pred: Predicate<String> = StartsWithPredicate("*")
    private val c_pred: Predicate<String> = a_pred.or(e_pred)
    internal val l_pred: Predicate<String> = ListTagPred.getInstance()
  }
}
