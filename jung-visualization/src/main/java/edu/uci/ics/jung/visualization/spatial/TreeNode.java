package edu.uci.ics.jung.visualization.spatial;

import java.awt.geom.Rectangle2D;
import java.util.Collection;

public interface TreeNode {

  Rectangle2D getBounds();

  Collection<? extends TreeNode> getChildren();
}
