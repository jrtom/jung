/*
 * Created on Sep 21, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.algorithms.util.MapSettableTransformer;
import edu.uci.ics.jung.algorithms.util.SettableTransformer;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads in data from a GraphML-formatted file and generates graphs based on that data. Currently
 * supports the following parts of the GraphML specification:
 *
 * <ul>
 *   <li>graphs and hypergraphs
 *   <li>directed and undirected edges
 *   <li>graph, node, edge <code>data</code>
 *   <li>graph, node, edge descriptions and <code>data</code> descriptions
 *   <li>node and edge IDs
 * </ul>
 *
 * Each of these is exposed via appropriate <code>get</code> methods.
 *
 * <p>Does not currently support nested graphs or ports.
 *
 * <p>Note that the user is responsible for supplying a graph <code>Factory</code> that can support
 * the edge types in the supplied GraphML file. If the graph generated by the <code>Factory</code>
 * is not compatible (for example: if the graph does not accept directed edges, and the GraphML file
 * contains a directed edge) then the results are graph-implementation-dependent.
 *
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
public class GraphMLReader<G extends MutableNetwork<N, E>, N, E> extends DefaultHandler {
  protected enum TagState {
    NO_TAG,
    NODE,
    EDGE,
    HYPEREDGE,
    ENDPOINT,
    GRAPH,
    DATA,
    KEY,
    DESC,
    DEFAULT_KEY,
    GRAPHML,
    OTHER
  }

  protected enum KeyType {
    NONE,
    NODE,
    EDGE,
    GRAPH,
    ALL
  }

  protected SAXParser saxp;
  protected boolean default_directed;
  protected G current_graph;
  protected N current_node;
  protected E current_edge;
  protected String current_key;
  protected LinkedList<TagState> current_states;
  protected BiMap<String, TagState> tag_state;
  protected Supplier<G> graph_factory;
  protected Supplier<N> node_factory;
  protected Supplier<E> edge_factory;
  protected BiMap<N, String> node_ids;
  protected BiMap<E, String> edge_ids;
  protected Map<String, GraphMLMetadata<G>> graph_metadata;
  protected Map<String, GraphMLMetadata<N>> node_metadata;
  protected Map<String, GraphMLMetadata<E>> edge_metadata;
  protected Map<N, String> node_desc;
  protected Map<E, String> edge_desc;
  protected Map<G, String> graph_desc;
  protected KeyType key_type;

  protected List<G> graphs;

  protected StringBuilder current_text = new StringBuilder();

  // TODO(jrtom): replace graph supplier with NetworkBuilder, or just provide another overload?

  /**
   * Creates a <code>GraphMLReader</code> instance with the specified node and edge factories.
   *
   * @param node_factory the node supplier to use to create node objects
   * @param edge_factory the edge supplier to use to create edge objects
   * @throws ParserConfigurationException if a SAX parser cannot be constructed
   * @throws SAXException if the SAX parser factory cannot be constructed
   */
  public GraphMLReader(Supplier<N> node_factory, Supplier<E> edge_factory)
      throws ParserConfigurationException, SAXException {
    current_node = null;
    current_edge = null;

    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxp = saxParserFactory.newSAXParser();

    current_states = new LinkedList<TagState>();

    tag_state = HashBiMap.<String, TagState>create();
    tag_state.put("node", TagState.NODE);
    tag_state.put("edge", TagState.EDGE);
    tag_state.put("hyperedge", TagState.HYPEREDGE);
    tag_state.put("endpoint", TagState.ENDPOINT);
    tag_state.put("graph", TagState.GRAPH);
    tag_state.put("data", TagState.DATA);
    tag_state.put("key", TagState.KEY);
    tag_state.put("desc", TagState.DESC);
    tag_state.put("default", TagState.DEFAULT_KEY);
    tag_state.put("graphml", TagState.GRAPHML);

    this.key_type = KeyType.NONE;

    this.node_factory = node_factory;
    this.edge_factory = edge_factory;
  }

  /**
   * Creates a <code>GraphMLReader</code> instance that assigns the node and edge <code>id</code>
   * strings to be the node and edge objects, as well as their IDs. Note that this requires that (a)
   * each edge have a valid ID, which is not normally a requirement for edges in GraphML, and (b)
   * that the node and edge types be assignment-compatible with <code>String</code>.
   *
   * @throws ParserConfigurationException if a SAX parser cannot be constructed
   * @throws SAXException if the SAX parser factory cannot be constructed
   */
  public GraphMLReader() throws ParserConfigurationException, SAXException {
    this(null, null);
  }

  /**
   * Returns a list of the graphs parsed from the specified reader, as created by the specified
   * Supplier.
   *
   * @param reader the source of the graph data in GraphML format
   * @param graph_factory used to build graph instances
   * @return the graphs parsed from the specified reader
   * @throws IOException if an error is encountered while parsing the graph
   */
  public List<G> loadMultiple(Reader reader, Supplier<G> graph_factory) throws IOException {
    this.graph_factory = graph_factory;
    initializeData();
    clearData();
    parse(reader);

    return graphs;
  }

  /**
   * Returns a list of the graphs parsed from the specified file, as created by the specified
   * Supplier.
   *
   * @param filename the source of the graph data in GraphML format
   * @param graph_factory used to build graph instances
   * @return the graphs parsed from the specified file
   * @throws IOException if an error is encountered while parsing the graph
   */
  public List<G> loadMultiple(String filename, Supplier<G> graph_factory) throws IOException {
    return loadMultiple(new FileReader(filename), graph_factory);
  }

  /**
   * Populates the specified graph with the data parsed from the reader.
   *
   * @param reader the source of the graph data in GraphML format
   * @param g the graph instance to populate
   * @throws IOException if an error is encountered while parsing the graph
   */
  public void load(Reader reader, G g) throws IOException {
    this.current_graph = g;
    this.graph_factory = null;
    initializeData();
    clearData();

    parse(reader);
  }

  /**
   * Populates the specified graph with the data parsed from the specified file.
   *
   * @param filename the source of the graph data in GraphML format
   * @param g the graph instance to populate
   * @throws IOException if an error is encountered while parsing the graph
   */
  public void load(String filename, G g) throws IOException {
    load(new FileReader(filename), g);
  }

  protected void clearData() {
    this.node_ids.clear();
    this.node_desc.clear();

    this.edge_ids.clear();
    this.edge_desc.clear();

    this.graph_desc.clear();
  }

  /**
   * This is separate from initialize() because these data structures are shared among all graphs
   * loaded (i.e., they're defined inside <code>graphml</code> rather than <code>graph</code>.
   */
  protected void initializeData() {
    this.node_ids = HashBiMap.<N, String>create();
    this.node_desc = new HashMap<N, String>();
    this.node_metadata = new HashMap<String, GraphMLMetadata<N>>();

    this.edge_ids = HashBiMap.<E, String>create();
    this.edge_desc = new HashMap<E, String>();
    this.edge_metadata = new HashMap<String, GraphMLMetadata<E>>();

    this.graph_desc = new HashMap<G, String>();
    this.graph_metadata = new HashMap<String, GraphMLMetadata<G>>();
  }

  protected void parse(Reader reader) throws IOException {
    try {
      saxp.parse(new InputSource(reader), this);
      reader.close();
    } catch (SAXException saxe) {
      throw new IOException(saxe.getMessage());
    }
  }

  @Override
  public void startElement(String uri, String name, String qName, Attributes atts)
      throws SAXNotSupportedException {
    String tag = qName.toLowerCase();
    TagState state = tag_state.get(tag);
    if (state == null) {
      state = TagState.OTHER;
    }

    switch (state) {
      case GRAPHML:
        break;

      case NODE:
        if (this.current_graph == null) {
          throw new SAXNotSupportedException("Graph must be defined prior to elements");
        }
        if (this.current_edge != null || this.current_node != null) {
          throw new SAXNotSupportedException("Nesting elements not supported");
        }

        createNode(atts);

        break;

      case ENDPOINT:
        if (this.current_graph == null) {
          throw new SAXNotSupportedException("Graph must be defined prior to elements");
        }
        if (this.current_edge == null) {
          throw new SAXNotSupportedException("No edge defined for endpoint");
        }
        if (this.current_states.getFirst() != TagState.HYPEREDGE) {
          throw new SAXNotSupportedException("Endpoints must be defined inside hyperedge");
        }
        Map<String, String> endpoint_atts = getAttributeMap(atts);
        String node = endpoint_atts.remove("node");
        if (node == null) {
          throw new SAXNotSupportedException("Endpoint must include an 'id' attribute");
        }
        N v = node_ids.inverse().get(node);
        if (v == null) {
          throw new SAXNotSupportedException("Endpoint refers to nonexistent node ID: " + node);
        }

        this.current_node = v;
        break;

      case EDGE:
        if (this.current_graph == null) {
          throw new SAXNotSupportedException("Graph must be defined prior to elements");
        }
        if (this.current_edge != null || this.current_node != null) {
          throw new SAXNotSupportedException("Nesting elements not supported");
        }

        createEdge(atts, state);
        break;

      case HYPEREDGE:
        throw new SAXNotSupportedException("Hyperedges not supported");

      case GRAPH:
        if (this.current_graph != null && graph_factory != null) {
          throw new SAXNotSupportedException("Nesting graphs not currently supported");
        }

        // graph Supplier is null if there's only one graph
        if (graph_factory != null) {
          current_graph = graph_factory.get();
        }

        // reset all non-key data structures (to avoid collisions between different graphs)
        clearData();

        // set up default direction of edges
        Map<String, String> graph_atts = getAttributeMap(atts);
        String default_direction = graph_atts.remove("edgedefault");
        if (default_direction == null) {
          throw new SAXNotSupportedException("All graphs must specify a default edge direction");
        }
        if (default_direction.equals("directed")) {
          this.default_directed = true;
        } else if (default_direction.equals("undirected")) {
          this.default_directed = false;
        } else {
          throw new SAXNotSupportedException(
              "Invalid or unrecognized default edge direction: " + default_direction);
        }

        // put remaining attribute/value pairs in graph_data
        addExtraData(graph_atts, graph_metadata, current_graph);

        break;

      case DATA:
        if (this.current_states.contains(TagState.DATA)) {
          throw new SAXNotSupportedException("Nested data not supported");
        }
        handleData(atts);
        break;

      case KEY:
        createKey(atts);
        break;

      default:
        break;
    }

    current_states.addFirst(state);
  }

  /**
   * @param <T>
   * @param atts
   * @param metadata_map
   * @param current_elt
   */
  private <T> void addExtraData(
      Map<String, String> atts, Map<String, GraphMLMetadata<T>> metadata_map, T current_elt) {
    // load in the default values; these override anything that might
    // be in the attribute map (because that's not really a proper
    // way to associate data)
    for (Map.Entry<String, GraphMLMetadata<T>> entry : metadata_map.entrySet()) {
      GraphMLMetadata<T> gmlm = entry.getValue();
      if (gmlm.default_value != null) {
        SettableTransformer<T, String> st = (SettableTransformer<T, String>) gmlm.transformer;
        st.set(current_elt, gmlm.default_value);
      }
    }

    // place remaining items in data
    for (Map.Entry<String, String> entry : atts.entrySet()) {
      String key = entry.getKey();
      GraphMLMetadata<T> key_data = metadata_map.get(key);
      SettableTransformer<T, String> st;
      if (key_data != null) {
        // if there's a default value, don't override it
        if (key_data.default_value != null) {
          continue;
        }
        st = (SettableTransformer<T, String>) key_data.transformer;
      } else {
        st = new MapSettableTransformer<T, String>(new HashMap<T, String>());
        key_data = new GraphMLMetadata<T>(null, null, st);
        metadata_map.put(key, key_data);
      }
      st.set(current_elt, entry.getValue());
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXNotSupportedException {
    this.current_text.append(new String(ch, start, length));
  }

  protected <T> void addDatum(Map<String, GraphMLMetadata<T>> metadata, T current_elt, String text)
      throws SAXNotSupportedException {
    if (metadata.containsKey(this.current_key)) {
      SettableTransformer<T, String> st =
          (SettableTransformer<T, String>) (metadata.get(this.current_key).transformer);
      st.set(current_elt, text);
    } else {
      throw new SAXNotSupportedException(
          "key " + this.current_key + " not valid for element " + current_elt);
    }
  }

  @Override
  public void endElement(String uri, String name, String qName) throws SAXNotSupportedException {
    String text = current_text.toString().trim();
    current_text.setLength(0);

    String tag = qName.toLowerCase();
    TagState state = tag_state.get(tag);
    if (state == null) {
      state = TagState.OTHER;
    }
    if (state == TagState.OTHER) {
      return;
    }

    if (state != current_states.getFirst()) {
      throw new SAXNotSupportedException(
          "Unbalanced tags: opened "
              + tag_state.inverse().get(current_states.getFirst())
              + ", closed "
              + tag);
    }

    switch (state) {
      case NODE:
      case ENDPOINT:
        current_node = null;
        break;

      case EDGE:
        current_edge = null;
        break;

      case HYPEREDGE:
        throw new SAXNotSupportedException("Hypergraphs not currently supported");

      case GRAPH:
        current_graph = null;
        break;

      case KEY:
        current_key = null;
        break;

      case DESC:
        switch (this.current_states.get(1)) { // go back one
          case GRAPH:
            graph_desc.put(current_graph, text);
            break;
          case NODE:
          case ENDPOINT:
            node_desc.put(current_node, text);
            break;
          case EDGE:
          case HYPEREDGE:
            edge_desc.put(current_edge, text);
            break;
          case DATA:
            switch (key_type) {
              case GRAPH:
                graph_metadata.get(current_key).description = text;
                break;
              case NODE:
                node_metadata.get(current_key).description = text;
                break;
              case EDGE:
                edge_metadata.get(current_key).description = text;
                break;
              case ALL:
                graph_metadata.get(current_key).description = text;
                node_metadata.get(current_key).description = text;
                edge_metadata.get(current_key).description = text;
                break;
              default:
                throw new SAXNotSupportedException(
                    "Invalid key type" + " specified for default: " + key_type);
            }

            break;
          default:
            break;
        }
        break;
      case DATA:
        this.key_type = KeyType.NONE;
        switch (this.current_states.get(1)) {
          case GRAPH:
            addDatum(graph_metadata, current_graph, text);
            break;
          case NODE:
          case ENDPOINT:
            addDatum(node_metadata, current_node, text);
            break;
          case EDGE:
          case HYPEREDGE:
            addDatum(edge_metadata, current_edge, text);
            break;
          default:
            break;
        }
        break;
      case DEFAULT_KEY:
        if (this.current_states.get(1) != TagState.KEY) {
          throw new SAXNotSupportedException(
              "'default' only defined in context of 'key' tag: "
                  + "stack: "
                  + current_states.toString());
        }

        switch (key_type) {
          case GRAPH:
            graph_metadata.get(current_key).default_value = text;
            break;
          case NODE:
            node_metadata.get(current_key).default_value = text;
            break;
          case EDGE:
            edge_metadata.get(current_key).default_value = text;
            break;
          case ALL:
            graph_metadata.get(current_key).default_value = text;
            node_metadata.get(current_key).default_value = text;
            edge_metadata.get(current_key).default_value = text;
            break;
          default:
            throw new SAXNotSupportedException(
                "Invalid key type" + " specified for default: " + key_type);
        }

        break;
      default:
        break;
    }

    current_states.removeFirst();
  }

  protected Map<String, String> getAttributeMap(Attributes atts) {
    Map<String, String> att_map = new HashMap<String, String>();
    for (int i = 0; i < atts.getLength(); i++) {
      att_map.put(atts.getQName(i), atts.getValue(i));
    }

    return att_map;
  }

  protected void handleData(Attributes atts) throws SAXNotSupportedException {
    switch (this.current_states.getFirst()) {
      case GRAPH:
        break;
      case NODE:
      case ENDPOINT:
        break;
      case EDGE:
        break;
      case HYPEREDGE:
        break;
      default:
        throw new SAXNotSupportedException(
            "'data' tag only defined "
                + "if immediately containing tag is 'graph', 'node', "
                + "'edge', or 'hyperedge'");
    }
    this.current_key = getAttributeMap(atts).get("key");
    if (this.current_key == null) {
      throw new SAXNotSupportedException("'data' tag requires a key specification");
    }
    if (this.current_key.equals("")) {
      throw new SAXNotSupportedException("'data' tag requires a non-empty key");
    }
    if (!getGraphMetadata().containsKey(this.current_key)
        && !getNodeMetadata().containsKey(this.current_key)
        && !getEdgeMetadata().containsKey(this.current_key)) {
      throw new SAXNotSupportedException(
          "'data' tag's key specification must reference a defined key");
    }
  }

  protected void createKey(Attributes atts) throws SAXNotSupportedException {
    Map<String, String> key_atts = getAttributeMap(atts);
    String id = key_atts.remove("id");
    String for_type = key_atts.remove("for");

    if (for_type == null || for_type.equals("") || for_type.equals("all")) {
      node_metadata.put(
          id,
          new GraphMLMetadata<N>(
              null, null, new MapSettableTransformer<N, String>(new HashMap<N, String>())));
      edge_metadata.put(
          id,
          new GraphMLMetadata<E>(
              null, null, new MapSettableTransformer<E, String>(new HashMap<E, String>())));
      graph_metadata.put(
          id,
          new GraphMLMetadata<G>(
              null, null, new MapSettableTransformer<G, String>(new HashMap<G, String>())));
      key_type = KeyType.ALL;
    } else {
      TagState type = tag_state.get(for_type);
      switch (type) {
        case NODE:
          node_metadata.put(
              id,
              new GraphMLMetadata<N>(
                  null, null, new MapSettableTransformer<N, String>(new HashMap<N, String>())));
          key_type = KeyType.NODE;
          break;
        case EDGE:
        case HYPEREDGE:
          edge_metadata.put(
              id,
              new GraphMLMetadata<E>(
                  null, null, new MapSettableTransformer<E, String>(new HashMap<E, String>())));
          key_type = KeyType.EDGE;
          break;
        case GRAPH:
          graph_metadata.put(
              id,
              new GraphMLMetadata<G>(
                  null, null, new MapSettableTransformer<G, String>(new HashMap<G, String>())));
          key_type = KeyType.GRAPH;
          break;
        default:
          throw new SAXNotSupportedException("Invalid metadata target type: " + for_type);
      }
    }

    this.current_key = id;
  }

  @SuppressWarnings("unchecked")
  protected void createNode(Attributes atts) throws SAXNotSupportedException {
    Map<String, String> node_atts = getAttributeMap(atts);
    String id = node_atts.remove("id");
    if (id == null) {
      throw new SAXNotSupportedException(
          "node attribute list missing " + "'id': " + atts.toString());
    }
    N v = node_ids.inverse().get(id);

    if (v == null) {
      if (node_factory != null) {
        v = node_factory.get();
      } else {
        v = (N) id;
      }
      node_ids.put(v, id);
      this.current_graph.addNode(v);

      // put remaining attribute/value pairs in node_data
      addExtraData(node_atts, node_metadata, v);
    } else {
      throw new SAXNotSupportedException(
          "Node id \"" + id + " is a duplicate of an existing node ID");
    }

    this.current_node = v;
  }

  @SuppressWarnings("unchecked")
  protected void createEdge(Attributes atts, TagState state) throws SAXNotSupportedException {
    Map<String, String> edge_atts = getAttributeMap(atts);

    String id = edge_atts.remove("id");
    E e;
    if (edge_factory != null) {
      e = edge_factory.get();
    } else if (id != null) {
      e = (E) id;
    } else {
      throw new IllegalArgumentException(
          "If no edge Supplier is supplied, " + "edge id may not be null: " + edge_atts);
    }

    if (id != null) {
      if (edge_ids.containsKey(e)) {
        throw new SAXNotSupportedException(
            "Edge id \"" + id + "\" is a duplicate of an existing edge ID");
      }
      edge_ids.put(e, id);
    }

    if (state == TagState.EDGE) {
      assignEdgeSourceTarget(e, atts, edge_atts); // , id);
    }

    // put remaining attribute/value pairs in edge_data
    addExtraData(edge_atts, edge_metadata, e);

    this.current_edge = e;
  }

  protected void assignEdgeSourceTarget(
      E e, Attributes atts, Map<String, String> edge_atts) // , String id)
      throws SAXNotSupportedException {
    String source_id = edge_atts.remove("source");
    if (source_id == null) {
      throw new SAXNotSupportedException(
          "edge attribute list missing " + "'source': " + atts.toString());
    }
    N source = node_ids.inverse().get(source_id);
    if (source == null) {
      throw new SAXNotSupportedException(
          "specified 'source' attribute " + "\"" + source_id + "\" does not match any node ID");
    }

    String target_id = edge_atts.remove("target");
    if (target_id == null) {
      throw new SAXNotSupportedException(
          "edge attribute list missing " + "'target': " + atts.toString());
    }
    N target = node_ids.inverse().get(target_id);
    if (target == null) {
      throw new SAXNotSupportedException(
          "specified 'target' attribute " + "\"" + target_id + "\" does not match any node ID");
    }

    String directed = edge_atts.remove("directed");
    if (directed != null) {
      boolean isDirected = directed.equals("true");
      boolean isUndirected = directed.equals("false");
      if (!isDirected && !isUndirected) {
        throw new SAXNotSupportedException(
            "Unrecognized edge direction specifier 'direction=\""
                + directed
                + "\"': "
                + "source: "
                + source_id
                + ", target: "
                + target_id);
      }
      if (isDirected != default_directed) {
        throw new SAXNotSupportedException(
            String.format(
                "Parser does not support graphs with directed and "
                    + "undirected edges; default direction: %b, edge direction: %b: ",
                default_directed, (isDirected ? isDirected : isUndirected)));
      }
    }
    current_graph.addEdge(source, target, e);
  }

  /**
   * @return a bidirectional map relating nodes and IDs.
   */
  public BiMap<N, String> getNodeIDs() {
    return node_ids;
  }

  /**
   * Returns a bidirectional map relating edges and IDs. This is not guaranteed to always be
   * populated (edge IDs are not required in GraphML files.
   *
   * @return a bidirectional map relating edges and IDs.
   */
  public BiMap<E, String> getEdgeIDs() {
    return edge_ids;
  }

  /**
   * @return a map from graph type name to type metadata
   */
  public Map<String, GraphMLMetadata<G>> getGraphMetadata() {
    return graph_metadata;
  }

  /**
   * @return a map from node type name to type metadata
   */
  public Map<String, GraphMLMetadata<N>> getNodeMetadata() {
    return node_metadata;
  }

  /**
   * @return a map from edge type name to type metadata
   */
  public Map<String, GraphMLMetadata<E>> getEdgeMetadata() {
    return edge_metadata;
  }

  /**
   * @return a map from graphs to graph descriptions
   */
  public Map<G, String> getGraphDescriptions() {
    return graph_desc;
  }

  /**
   * @return a map from nodes to node descriptions
   */
  public Map<N, String> getNodeDescriptions() {
    return node_desc;
  }

  /**
   * @return a map from edges to edge descriptions
   */
  public Map<E, String> getEdgeDescriptions() {
    return edge_desc;
  }
}
