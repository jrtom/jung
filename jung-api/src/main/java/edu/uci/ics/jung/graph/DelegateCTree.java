/*
 * Created on Feb 12, 2017
 *
 * Copyright (c) 2017, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;

class DelegateCTree<N> implements MutableCTree<N> {
	private final MutableGraph<N> delegate;
	private final Map<N, Integer> depths;
	private Optional<Integer> height;
	private Optional<N> root;
	
	DelegateCTree(MutableGraph<N> graph, Optional<N> root) {
		this.delegate = graph;
		this.depths = new HashMap<N, Integer>();
		if (root.isPresent()) {
			this.addNode(root.get());
		}
	}
	
	@Override
	public Optional<N> root() {
		return root;
	}

	@Override
	public Optional<N> predecessor(N node) {
		Set<N> predecessors = delegate.predecessors(node);
		Preconditions.checkState(predecessors.size() <= 1);
		return predecessors.isEmpty()
				? Optional.<N>empty()
				: Optional.of(Iterables.getOnlyElement(predecessors));
	}

	@Override
	public int depth(N node) {
		Preconditions.checkArgument(delegate.nodes().contains(node));
		return depths.get(node);
	}

	@Override
	public Optional<Integer> height() {
		if (!root.isPresent()) {
			return Optional.empty();
		}
		// Calculate the height
		if (!height.isPresent()) {
			calculateHeight();
		}
		return height;
	}

	@Override
	public boolean isDirected() {
		return true;
	}

	@Override
	public boolean allowsSelfLoops() {
		return false;
	}

	@Override
	public Set<N> adjacentNodes(N node) {
		return delegate.adjacentNodes(node);
	}

	@Override
	public int degree(N node) {
		return delegate.degree(node);
	}

	@Override
	public int inDegree(N node) {
		return delegate.inDegree(node);
	}

	@Override
	public ElementOrder<N> nodeOrder() {
		return delegate.nodeOrder();
	}

	@Override
	public Set<N> nodes() {
		return delegate.nodes();
	}

	@Override
	public int outDegree(N node) {
		return delegate.outDegree(node);
	}

	@Override
	public Set<N> predecessors(N node) {
		return delegate.predecessors(node);
	}

	@Override
	public Set<N> successors(N node) {
		return delegate.successors(node);
	}

	@Override
	public Set<EndpointPair<N>> edges() {
		return delegate.edges();
	}

	@Override
	public boolean addNode(N node) {
		if (root().equals(node)) {
			return false;
		}
		Preconditions.checkArgument(nodes().isEmpty());
		delegate.addNode(node);
		this.root = Optional.of(node);
		setDepth(node, null);
		return true;
	}

	@Override
	public boolean putEdge(N nodeU, N nodeV) {
		if (nodes().isEmpty()) {
			this.addNode(nodeU);  // set the root
		} else {
			Preconditions.checkArgument(nodes().contains(nodeU));
			if (successors(nodeU).contains(nodeV)) {
				return false;  // edge is already present; no-op
			}
			// verify that nodeV is not in the tree
			Preconditions.checkArgument(!nodes().contains(nodeV));
		}
		setDepth(nodeV, nodeU);
		return delegate.putEdge(nodeU, nodeV);
	}

	private void setDepth(N node, N parent) {
		if (parent == null) {  // root: both depth and height are 0
			depths.put(node, 0);
			height = Optional.of(0);
		} else {
			int nodeDepth = depths.get(parent) + 1;
			height = Optional.of(Math.max(nodeDepth, height.get()));
		}
	}

	private int calculateHeight() {
		// This method is only called when the root is present, so we don't need to check for that.
		int currentHeight = 0;
		List<N> currentLevel = new ArrayList<N>();
		List<N> nextLevel;
		currentLevel.addAll(successors(root.get()));
		while (!currentLevel.isEmpty()) {
			nextLevel = new ArrayList<N>();
			currentHeight++;  // there's at least one node in the current level
			for (N node : currentLevel) {
				nextLevel.addAll(successors(node));
			}
			currentLevel = nextLevel;
		}
		return currentHeight;
	}
	
	@Override
	public boolean removeNode(N node) {
		if (!nodes().contains(node)) {
			return false;
		}
		for (N nodeToRemove : Graphs.reachableNodes(delegate, node)) {
			delegate.removeNode(nodeToRemove);
			depths.remove(nodeToRemove);
		}
		// Reset the height, since we don't know how it was affected by removing the subtree.
		this.height = Optional.empty();
		return true;
	}

	@Override
	public boolean removeEdge(N nodeU, N nodeV) {
		delegate.removeEdge(nodeU, nodeV);
		return this.removeNode(nodeV);
	}
}
