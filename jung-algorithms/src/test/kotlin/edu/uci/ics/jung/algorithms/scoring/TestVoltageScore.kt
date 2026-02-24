/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jul 14, 2008
 */
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import junit.framework.TestCase

/**
 * @author jrtom
 */
class TestVoltageScore : TestCase() {
  protected var g: MutableNetwork<Number, Number>? = null

  // TODO:
  // * test multiple sources/targets
  // * test weighted edges
  // * test exceptional cases

  fun testDirectedVoltagesSourceTarget() {
    g = NetworkBuilder.directed().build()

    var j = 0
    g!!.addEdge(0, 1, j++)
    g!!.addEdge(1, 2, j++)
    g!!.addEdge(2, 3, j++)
    g!!.addEdge(2, 4, j++)
    g!!.addEdge(3, 4, j++)
    g!!.addEdge(4, 1, j++)
    g!!.addEdge(4, 0, j++)

    val vr = VoltageScorer<Number, Number>(g!!, { _: Number -> 1 }, 0, 3)
    val voltages = doubleArrayOf(1.0, 2.0 / 3, 2.0 / 3, 0.0, 1.0 / 3)

    vr.evaluate()
    checkVoltages(vr, voltages)
  }

  fun testUndirectedSourceTarget() {
    g = NetworkBuilder.undirected().build()
    var j = 0
    g!!.addEdge(0, 1, j++)
    g!!.addEdge(0, 2, j++)
    g!!.addEdge(1, 3, j++)
    g!!.addEdge(2, 3, j++)
    g!!.addEdge(3, 4, j++)
    g!!.addEdge(3, 5, j++)
    g!!.addEdge(4, 6, j++)
    g!!.addEdge(5, 6, j++)
    val vr = VoltageScorer<Number, Number>(g!!, { _: Number -> 1 }, 0, 6)
    val voltages = doubleArrayOf(1.0, 0.75, 0.75, 0.5, 0.25, 0.25, 0.0)

    vr.evaluate()
    checkVoltages(vr, voltages)
  }

  companion object {
    private fun checkVoltages(vr: VoltageScorer<Number, Number>, voltages: DoubleArray) {
      assertEquals(vr.nodeScores().size, voltages.size)
      println("scores: " + vr.nodeScores())
      println("voltages: " + voltages.toString())
      for (i in voltages.indices) {
        assertEquals(vr.getNodeScore(i), voltages[i], 0.01)
      }
    }
  }
}
