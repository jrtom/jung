package edu.uci.ics.jung.graph;

import junit.framework.TestCase;
import edu.uci.ics.jung.graph.util.TreeUtils;

public abstract class AbstractTreeUtilsTest extends TestCase {
	
	protected Tree<String,Integer> tree;

	public void testRemove() {
		try {
			TreeUtils.getSubTree(tree, "C0");
			tree.removeVertex("C0");
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testAdd() {
		try {
			Forest<String,Integer> subTree = TreeUtils.getSubTree(tree, "C0");
			Integer edge = tree.getInEdges("C0").iterator().next();
			String parent = tree.getPredecessors("C0").iterator().next();
			tree.removeVertex("C0");
			
			TreeUtils.addSubTree(tree, subTree, parent, edge);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

}
