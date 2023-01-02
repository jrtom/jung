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
package edu.uci.ics.jung.io;

import com.google.common.base.Preconditions;
import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.algorithms.util.MapSettableTransformer;
import edu.uci.ics.jung.algorithms.util.SettableTransformer;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Reads a <code>Graph</code> from a Pajek NET formatted source.
 *
 * <p>If the edge constraints specify that the graph is strictly undirected, and an "*Arcs" section
 * is encountered, or if the edge constraints specify that the graph is strictly directed, and an
 * "*Edges" section is encountered, an <code>IllegalArgumentException</code> is thrown.
 *
 * <p>If the edge constraints do not permit parallel edges, only the first encountered of a set of
 * parallel edges will be read; subsequent edges in that set will be ignored.
 *
 * <p>More restrictive edge constraints will cause nodes to be generated that are more time- and
 * space-efficient.
 *
 * <p>At the moment, only supports the part of the specification that defines:
 *
 * <ul>
 *   <li>node ids (each must have a value from 1 to n, where n is the number of nodes)
 *   <li>node labels (must be in quotes if interrupted by whitespace)
 *   <li>directed edge connections (single or list)
 *   <li>undirected edge connections (single or list)
 *   <li>edge weights (not compatible with edges specified in list form) <br>
 *       <b>note</b>: this version of PajekNetReader does not support multiple edge weights, as
 *       PajekNetFile does; this behavior is consistent with the NET format.
 *   <li>node locations (x and y; z coordinate is ignored)
 * </ul>
 *
 * <p>Here is an example format for a directed graph without edge weights and edges specified in
 * list form: <br>
 *
 * <pre>
 * *nodes [# of nodes]
 * 1 "a"
 * 2 "b"
 * 3 "c"
 * *arcslist
 * 1 2 3
 * 2 3
 * </pre>
 *
 * Here is an example format for an undirected graph with edge weights and edges specified in
 * non-list form: <br>
 *
 * <pre>
 * *nodes [# of nodes]
 * 1 "a"
 * 2 "b"
 * 3 "c"
 * *edges
 * 1 2 0.1
 * 1 3 0.9
 * 2 3 1.0
 * </pre>
 *
 * @author Joshua O'Madadhain
 * @see "'Pajek - Program for Analysis and Visualization of Large Networks', Vladimir Batagelj and
 *     Andrej Mrvar, http://vlado.fmf.uni-lj.si/pub/networks/pajek/doc/pajekman.pdf"
 * @author Tom Nelson - converted to jung2
 */
public class PajekNetReader<G extends MutableNetwork<N, E>, N, E> {
  protected Supplier<N> node_factory;
  protected Supplier<E> edge_factory;

  /** The map for node labels (if any) created by this class. */
  protected SettableTransformer<N, String> node_labels =
      new MapSettableTransformer<N, String>(new HashMap<N, String>());

  /** The map for node locations (if any) defined by this class. */
  protected SettableTransformer<N, Point2D> node_locations =
      new MapSettableTransformer<N, Point2D>(new HashMap<N, Point2D>());

  protected SettableTransformer<E, Number> edge_weights =
      new MapSettableTransformer<E, Number>(new HashMap<E, Number>());

  /** Used to specify whether the most recently read line is a Pajek-specific tag. */
  private static final Predicate<String> v_pred = new StartsWithPredicate("*nodes");

  private static final Predicate<String> a_pred = new StartsWithPredicate("*arcs");
  private static final Predicate<String> e_pred = new StartsWithPredicate("*edges");
  private static final Predicate<String> t_pred = new StartsWithPredicate("*");
  private static final Predicate<String> c_pred = a_pred.or(e_pred);
  protected static final Predicate<String> l_pred = ListTagPred.getInstance();

  /**
   * Creates a PajekNetReader instance with the specified node and edge factories.
   *
   * @param node_factory the Supplier to use to create node objects
   * @param edge_factory the Supplier to use to create edge objects
   */
  public PajekNetReader(Supplier<N> node_factory, Supplier<E> edge_factory) {
    this.node_factory = node_factory;
    this.edge_factory = edge_factory;
  }

  /**
   * Creates a PajekNetReader instance with the specified edge Supplier, and whose node objects
   * correspond to the integer IDs assigned in the file. Note that this requires <code>V</code> to
   * be assignment-compatible with an <code>Integer</code> value.
   *
   * @param edge_factory the Supplier to use to create edge objects
   */
  public PajekNetReader(Supplier<E> edge_factory) {
    this(null, edge_factory);
  }

  /**
   * Returns the graph created by parsing the specified file, as created by the specified Supplier.
   *
   * @param filename the file from which the graph is to be read
   * @param graph_factory used to provide a graph instance
   * @return a graph parsed from the specified file
   * @throws IOException if the graph cannot be loaded
   */
  public G load(String filename, Supplier<? extends G> graph_factory) throws IOException {
    return load(new FileReader(filename), graph_factory.get());
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
  public G load(Reader reader, Supplier<? extends G> graph_factory) throws IOException {
    return load(reader, graph_factory.get());
  }

  /**
   * Returns the graph created by parsing the specified file, by populating the specified graph.
   *
   * @param filename the file from which the graph is to be read
   * @param g the graph instance to populate
   * @return a graph parsed from the specified file
   * @throws IOException if the graph cannot be loaded
   */
  public G load(String filename, G g) throws IOException {
    return load(new FileReader(filename), g);
  }

  /**
   * Populates the graph <code>g</code> with the graph represented by the Pajek-format data supplied
   * by <code>reader</code>. Stores edge weights, if any, according to <code>nev</code> (if
   * non-null).
   *
   * <p>Any existing nodes/edges of <code>g</code>, if any, are unaffected.
   *
   * <p>The edge data are filtered according to <code>g</code>'s constraints, if any; thus, if
   * <code>g</code> only accepts directed edges, any undirected edges in the input are ignored.
   *
   * @param reader the reader from which the graph is to be read
   * @param g the graph instance to populate
   * @return a graph parsed from the specified reader
   * @throws IOException if the graph cannot be loaded
   */
  public G load(Reader reader, G g) throws IOException {
    Preconditions.checkNotNull(g);
    BufferedReader br = new BufferedReader(reader);

    // ignore everything until we see '*Nodes'
    String curLine = skip(br, v_pred);

    if (curLine == null) { // no nodes in the graph; return empty graph
      return g;
    }

    // create appropriate number of nodes
    StringTokenizer st = new StringTokenizer(curLine);
    st.nextToken(); // skip past "*nodes";
    int num_nodes = Integer.parseInt(st.nextToken());
    List<N> id = null;
    // TODO: under what circumstances (if any) is it reasonable for node_factory to be null?
    if (node_factory != null) {
      for (int i = 1; i <= num_nodes; i++) {
        g.addNode(node_factory.get());
      }
      id = new ArrayList<N>(g.nodes());
    }

    // read nodes until we see any Pajek format tag ('*...')
    curLine = null;
    while (br.ready()) {
      curLine = br.readLine();
      if (curLine == null || t_pred.test(curLine)) {
        break;
      }
      if (curLine == "") { // skip blank lines
        continue;
      }

      try {
        readNode(curLine, id, num_nodes);
      } catch (IllegalArgumentException iae) {
        br.close();
        reader.close();
        throw iae;
      }
    }

    // skip over the intermediate stuff (if any)
    // and read the next arcs/edges section that we find
    curLine = readArcsOrEdges(curLine, br, g, id, edge_factory);

    // ditto
    readArcsOrEdges(curLine, br, g, id, edge_factory);

    br.close();
    reader.close();

    return g;
  }

  /**
   * Parses <code>curLine</code> as a reference to a node, and optionally assigns label and location
   * information.
   */
  @SuppressWarnings("unchecked")
  private void readNode(String curLine, List<N> id, int num_nodes) {
    N v;
    String[] parts = null;
    int coord_idx = -1; // index of first coordinate in parts; -1 indicates no coordinates found
    String index;
    String label = null;
    // if there are quote marks on this line, split on them; label is surrounded by them
    if (curLine.indexOf('"') != -1) {
      String[] initial_split = curLine.trim().split("\"");
      // if there are any quote marks, there should be exactly 2
      Preconditions.checkArgument(
          initial_split.length == 2 || initial_split.length == 3,
          "Unbalanced (or too many) quote marks in " + curLine);
      index = initial_split[0].trim();
      label = initial_split[1].trim();
      if (initial_split.length == 3) {
        parts = initial_split[2].trim().split("\\s+", -1);
      }
      coord_idx = 0;
    } else { // no quote marks, but are there coordinates?
      parts = curLine.trim().split("\\s+", -1);
      index = parts[0];
      switch (parts.length) {
        case 1: // just the ID; nothing to do, continue
          break;
        case 2: // just the ID and a label
          label = parts[1];
          break;
        case 3: // ID, no label, coordinates
          coord_idx = 1;
          break;
        default: // ID, label, (x,y) coordinates, maybe some other stuff
          coord_idx = 2;
          break;
      }
    }
    int v_id = Integer.parseInt(index) - 1; // go from 1-based to 0-based index
    Preconditions.checkArgument(v_id >= 0 && v_id < num_nodes);
    if (id != null) {
      v = id.get(v_id);
    } else {
      v = (N) (new Integer(v_id));
    }
    // only attach the label if there's one to attach
    if (label != null && label.length() > 0 && node_labels != null) {
      node_labels.set(v, label);
    }

    // parse the rest of the line
    if (coord_idx != -1
        && parts != null
        && parts.length >= coord_idx + 2
        && node_locations != null) {
      double x = Double.parseDouble(parts[coord_idx]);
      double y = Double.parseDouble(parts[coord_idx + 1]);
      node_locations.set(v, new Point2D.Double(x, y));
    }
  }

  @SuppressWarnings("unchecked")
  private String readArcsOrEdges(
      String curLine,
      BufferedReader br,
      MutableNetwork<N, E> g,
      List<N> id,
      Supplier<E> edge_factory)
      throws IOException {
    String nextLine = curLine;

    // in case we're not there yet (i.e., format tag isn't arcs or edges)
    if (!c_pred.test(curLine)) {
      nextLine = skip(br, c_pred);
    }

    boolean reading_arcs = false;
    boolean reading_edges = false;
    if (a_pred.test(nextLine)) {
      Preconditions.checkState(
          g.isDirected(), "Supplied undirected-only graph cannot be populated with directed edges");
      reading_arcs = true;
    }
    if (e_pred.test(nextLine)) {
      Preconditions.checkState(
          !g.isDirected(),
          "Supplied directed-only graph cannot be populated with undirected edges");
      reading_edges = true;
    }

    if (!(reading_arcs || reading_edges)) {
      return nextLine;
    }

    boolean is_list = l_pred.test(nextLine);

    while (br.ready()) {
      nextLine = br.readLine();
      if (nextLine == null || t_pred.test(nextLine)) {
        break;
      }
      if (curLine == "") { // skip blank lines
        continue;
      }

      StringTokenizer st = new StringTokenizer(nextLine.trim());

      int vid1 = Integer.parseInt(st.nextToken()) - 1;
      // FIXME: check for vid < 0
      N v1;
      if (id != null) {
        v1 = id.get(vid1);
      } else {
        // TODO: wat (look for other (N) casts also)
        v1 = (N) new Integer(vid1);
      }

      if (is_list) { // one source, multiple destinations
        do {
          createAddEdge(st, v1, g, id, edge_factory);
        } while (st.hasMoreTokens());
      } else { // one source, one destination, at most one weight
        E e = createAddEdge(st, v1, g, id, edge_factory);
        // get the edge weight if we care
        if (edge_weights != null && st.hasMoreTokens()) {
          edge_weights.set(e, new Float(st.nextToken()));
        }
      }
    }
    return nextLine;
  }

  @SuppressWarnings("unchecked")
  protected E createAddEdge(
      StringTokenizer st, N v1, MutableNetwork<N, E> g, List<N> id, Supplier<E> edge_factory) {
    int vid2 = Integer.parseInt(st.nextToken()) - 1;
    N v2;
    if (id != null) {
      v2 = id.get(vid2);
    } else {
      v2 = (N) new Integer(vid2);
    }
    E e = edge_factory.get();

    // don't error-check this: let the graph implementation do whatever it's going to do
    // (add the edge, replace the existing edge, throw an exception--depends on the graph
    // implementation)
    g.addEdge(v1, v2, e);
    return e;
  }

  /**
   * Returns the first line read from <code>br</code> for which <code>p</code> returns <code>true
   * </code>, or <code>null</code> if there is no such line.
   *
   * @param br the reader from which the graph is being read
   * @param p predicate specifying what line to accept
   * @return the first line from {@code br} that matches {@code p}, or null
   * @throws IOException if an error is encountered while reading from {@code br}
   */
  protected String skip(BufferedReader br, Predicate<String> p) throws IOException {
    while (br.ready()) {
      String curLine = br.readLine();
      if (curLine == null) {
        break;
      }
      curLine = curLine.trim();
      if (p.test(curLine)) {
        return curLine;
      }
    }
    return null;
  }

  /**
   * A Predicate which evaluates to <code>true</code> if the argument starts with the
   * constructor-specified String.
   *
   * @author Joshua O'Madadhain
   */
  protected static class StartsWithPredicate implements Predicate<String> {
    private String tag;

    protected StartsWithPredicate(String s) {
      this.tag = s;
    }

    public boolean test(String str) {
      return (str != null && str.toLowerCase().startsWith(tag));
    }
  }

  /**
   * A Predicate which evaluates to <code>true</code> if the argument ends with the string "list".
   *
   * @author Joshua O'Madadhain
   */
  protected static class ListTagPred implements Predicate<String> {
    protected static ListTagPred instance;

    protected ListTagPred() {}

    protected static ListTagPred getInstance() {
      if (instance == null) {
        instance = new ListTagPred();
      }
      return instance;
    }

    public boolean test(String s) {
      return (s != null && s.toLowerCase().endsWith("list"));
    }
  }

  /**
   * @return the nodeLocationTransformer
   */
  public SettableTransformer<N, Point2D> getNodeLocationTransformer() {
    return node_locations;
  }

  /**
   * Provides a Function which will be used to write out the node locations.
   *
   * @param node_locations a container for the node locations
   */
  public void setNodeLocationTransformer(SettableTransformer<N, Point2D> node_locations) {
    this.node_locations = node_locations;
  }

  /**
   * @return a mapping from nodes to their labels
   */
  public SettableTransformer<N, String> getNodeLabeller() {
    return node_labels;
  }

  /**
   * Provides a Function which will be used to write out the node labels.
   *
   * @param node_labels a container for the node labels
   */
  public void setNodeLabeller(SettableTransformer<N, String> node_labels) {
    this.node_labels = node_labels;
  }

  /**
   * @return a mapping from edges to their weights
   */
  public SettableTransformer<E, Number> getEdgeWeightTransformer() {
    return edge_weights;
  }

  /**
   * Provides a Function which will be used to write out edge weights.
   *
   * @param edge_weights a container for the edge weights
   */
  public void setEdgeWeightTransformer(SettableTransformer<E, Number> edge_weights) {
    this.edge_weights = edge_weights;
  }
}
