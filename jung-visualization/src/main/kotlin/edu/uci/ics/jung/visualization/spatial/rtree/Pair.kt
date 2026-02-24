package edu.uci.ics.jung.visualization.spatial.rtree

import com.google.common.base.Preconditions
import org.slf4j.LoggerFactory

/**
 * A collection of two items. Used for pairs of lists during R*-Tree split
 *
 * @author Tom Nelson
 */
class Pair<T>(val left: T, val right: T) {

  init {
    Preconditions.checkArgument(left !== right, "Attempt to create pair with 2 equal elements")
  }

  override fun toString(): String = "Pair{left=$left, right=$right}"

  companion object {
    private val log = LoggerFactory.getLogger(Pair::class.java)

    @JvmStatic
    fun <T> of(left: T, right: T): Pair<T> = Pair(left, right)
  }
}
