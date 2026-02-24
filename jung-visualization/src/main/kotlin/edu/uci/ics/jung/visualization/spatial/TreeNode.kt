package edu.uci.ics.jung.visualization.spatial

import java.awt.geom.Rectangle2D

interface TreeNode {
  fun getBounds(): Rectangle2D
  fun getChildren(): Collection<TreeNode>?
}
