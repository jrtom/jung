package edu.uci.ics.jung.algorithms.scoring

import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils
import junit.framework.TestCase

class TestKStepMarkov : TestCase() {
  lateinit var mGraph: MutableNetwork<Number, Number>
  lateinit var mTransitionMatrix: Array<DoubleArray>
  var edgeWeights: MutableMap<Number, Number> = HashMap()

  override fun setUp() {
    mGraph = NetworkBuilder.directed().allowsParallelEdges(true).build()
    mTransitionMatrix = arrayOf(
        doubleArrayOf(0.0, 0.5, 0.5),
        doubleArrayOf(1.0 / 3.0, 0.0, 2.0 / 3.0),
        doubleArrayOf(1.0 / 3.0, 2.0 / 3.0, 0.0)
    )

    for (i in mTransitionMatrix.indices) {
      mGraph.addNode(i)
    }

    for (i in mTransitionMatrix.indices) {
      for (j in mTransitionMatrix[i].indices) {
        if (mTransitionMatrix[i][j] > 0) {
          val edge = i * mTransitionMatrix.size + j
          mGraph.addEdge(i, j, edge)
          edgeWeights[edge] = mTransitionMatrix[i][j]
        }
      }
    }
  }

  // TODO(jrtom): this isn't actually testing anything
  fun testRanker() {

    val priors = HashSet<Number>()
    priors.add(1)
    priors.add(2)
    val ranker = KStepMarkov<Number, Number>(
        mGraph, { e -> edgeWeights[e]!! }, ScoringUtils.getUniformRootPrior(priors), 2)

    for (i in 0 until 10) {
      ranker.step()
    }
  }
}
