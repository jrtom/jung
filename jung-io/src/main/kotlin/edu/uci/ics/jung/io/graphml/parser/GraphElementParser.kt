/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io.graphml.parser

import com.google.common.graph.MutableNetwork
import edu.uci.ics.jung.io.GraphIOException
import edu.uci.ics.jung.io.graphml.DataMetadata
import edu.uci.ics.jung.io.graphml.EdgeMetadata
import edu.uci.ics.jung.io.graphml.ExceptionConverter
import edu.uci.ics.jung.io.graphml.GraphMLConstants
import edu.uci.ics.jung.io.graphml.GraphMetadata
import edu.uci.ics.jung.io.graphml.GraphMetadata.EdgeDefault
import edu.uci.ics.jung.io.graphml.NodeMetadata
import java.util.LinkedList
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.EndElement
import javax.xml.stream.events.StartElement

/**
 * Parses graph elements.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class GraphElementParser<G : MutableNetwork<N, E>, N, E>(
  parserContext: ParserContext<G, N, E>
) : AbstractElementParser<G, N, E>(parserContext) {

  @Throws(GraphIOException::class)
  override fun parse(xmlEventReader: XMLEventReader, start: StartElement): GraphMetadata? {
    try {
      // Create the new graph.
      val graphMetadata = GraphMetadata()

      // Parse the attributes.
      @Suppress("UNCHECKED_CAST")
      val iterator = start.attributes as Iterator<Attribute>
      while (iterator.hasNext()) {
        val attribute = iterator.next()
        val name = attribute.name.localPart
        val value = attribute.value
        if (graphMetadata.id == null && GraphMLConstants.ID_NAME == name) {
          graphMetadata.id = value
        } else if (graphMetadata.edgeDefault == null &&
          GraphMLConstants.EDGEDEFAULT_NAME == name
        ) {
          graphMetadata.edgeDefault =
            if (GraphMLConstants.DIRECTED_NAME == value) EdgeDefault.DIRECTED
            else EdgeDefault.UNDIRECTED
        } else {
          graphMetadata.setProperty(name, value)
        }
      }

      // Make sure the graphdefault has been set.
      if (graphMetadata.edgeDefault == null) {
        throw GraphIOException("Element 'graph' is missing attribute 'edgedefault'")
      }

      val idToNodeMap = HashMap<String, N>()
      val edgeMetadata: MutableCollection<EdgeMetadata> = LinkedList()

      while (xmlEventReader.hasNext()) {
        val event = xmlEventReader.nextEvent()
        if (event.isStartElement) {
          val element = event as StartElement
          val name = element.name.localPart
          if (GraphMLConstants.DESC_NAME == name) {
            // Parse the description and set it in the graph.
            val desc = getParser(name).parse(xmlEventReader, element) as String
            graphMetadata.description = desc
          } else if (GraphMLConstants.DATA_NAME == name) {
            // Parse the data element and store the property in the graph.
            val data = getParser(name).parse(xmlEventReader, element) as DataMetadata
            graphMetadata.addData(data)
          } else if (GraphMLConstants.NODE_NAME == name) {
            // Parse the node metadata
            val metadata = getParser(name).parse(xmlEventReader, element) as NodeMetadata

            // Create the node object and store it in the metadata
            val node = parserContext.createNode(metadata)
            metadata.node = node
            idToNodeMap[metadata.id!!] = node

            // Add it to the graph
            graphMetadata.addNodeMetadata(node as Any, metadata)
          } else if (GraphMLConstants.EDGE_NAME == name) {
            // Parse the edge metadata
            val metadata = getParser(name).parse(xmlEventReader, element) as EdgeMetadata

            // Set the directed property if not overridden.
            if (metadata.isDirected == null) {
              metadata.isDirected = graphMetadata.edgeDefault == EdgeDefault.DIRECTED
            }

            // Create the edge object and store it in the metadata
            val edge = parserContext.createEdge(metadata)
            edgeMetadata.add(metadata)
            metadata.edge = edge

            // Add it to the graph.
            graphMetadata.addEdgeMetadata(edge as Any, metadata)
          } else {
            // Treat anything else as unknown
            getUnknownParser().parse(xmlEventReader, element)
          }
        }
        if (event.isEndElement) {
          val end = event as EndElement
          verifyMatch(start, end)
          break
        }
      }

      // Apply the keys to this object.
      applyKeys(graphMetadata)

      // Create the graph object and store it in the metadata
      val graph = parserContext.createGraph(graphMetadata)
      graphMetadata.graph = graph

      // Add all of the nodes to the graph object.
      addNodesToGraph(graph, idToNodeMap.values)

      // Add the edges to the graph object.
      addEdgesToGraph(graph, edgeMetadata, idToNodeMap)

      return graphMetadata
    } catch (e: Exception) {
      ExceptionConverter.convert(e)
    }

    return null
  }

  private fun addNodesToGraph(graph: G, nodes: Collection<N>) {
    for (node in nodes) {
      graph.addNode(node)
    }
  }

  @Suppress("UNCHECKED_CAST")
  @Throws(GraphIOException::class)
  private fun addEdgesToGraph(
    graph: G,
    metadata: Collection<EdgeMetadata>,
    idToNodeMap: Map<String, N>
  ) {
    for (emd in metadata) {
      // Get the edge out of the metadata
      val edge = emd.edge as E

      // Get the nodes.
      val source = idToNodeMap[emd.source]
      val target = idToNodeMap[emd.target]
      if (source == null || target == null) {
        throw GraphIOException(
          "edge references undefined source or target node. " +
            "Source: " + emd.source + ", Target: " + emd.target
        )
      }

      // Add it to the graph.
      graph.addEdge(source, target, edge)
    }
  }
}
