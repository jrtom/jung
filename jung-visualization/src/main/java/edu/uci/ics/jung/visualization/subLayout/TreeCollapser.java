/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.subLayout;

import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.util.TreeUtils;

public class TreeCollapser  {
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void collapse(Layout layout, Forest tree, Object subRoot) throws InstantiationException, IllegalAccessException {
        
    	// get a sub tree from subRoot
    	Forest subTree = TreeUtils.getSubTree(tree, subRoot);
    	Object parent = null;
    	Object edge = null;
    	if(tree.getPredecessorCount(subRoot) > 0) {
    		parent = tree.getPredecessors(subRoot).iterator().next();
    		edge = tree.getInEdges(subRoot).iterator().next();
    	}	
    	tree.removeVertex(subRoot);
    	if(parent != null) {
    		tree.addEdge(edge, parent, subTree);
    	} else {
    		tree.addVertex(subTree);
    	}
    	
    	layout.setLocation(subTree, (Point2D)layout.apply(subRoot));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void expand(Forest tree, Forest subTree) {

    	Object parent = null;
    	Object edge = null;
    	if(tree.getPredecessorCount(subTree) > 0) {
    		parent = tree.getPredecessors(subTree).iterator().next();
    		edge = tree.getInEdges(subTree).iterator().next();
    	}
    	tree.removeVertex(subTree);
    	TreeUtils.addSubTree(tree, subTree, parent, edge);
    }
}
