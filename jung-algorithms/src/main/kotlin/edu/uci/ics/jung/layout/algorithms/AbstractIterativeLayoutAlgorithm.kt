package edu.uci.ics.jung.layout.algorithms

import edu.uci.ics.jung.layout.model.LayoutModel
import java.util.Random
import org.slf4j.LoggerFactory

/**
 * For Iterative algorithms that perform delayed operations on a Thread, save off the layoutModel so
 * that it can be accessed by the threaded code. The layoutModel could be removed and instead passed
 * via all of the iterative methods (for example step(layoutModel) instead of step() )
 *
 * @author Tom Nelson
 */
abstract class AbstractIterativeLayoutAlgorithm<N : Any> : IterativeLayoutAlgorithm<N> {

  /**
   * because the IterativeLayoutAlgorithms use multithreading to continuously update node positions,
   * the layoutModel state is saved (during the visit method) so that it can be used continuously
   */
  protected open lateinit var layoutModel: LayoutModel<N>

  // both of these can be set at instance creation time
  protected var shouldPreRelax: Boolean = true
  protected var preRelaxDurationMs: Int = 500 // how long should the prerelax phase last?

  protected var random: Random = Random()

  fun setRandomSeed(randomSeed: Long) {
    this.random = Random(randomSeed)
  }

  // returns true iff prerelaxing happened
  final override fun preRelax(): Boolean {
    if (!shouldPreRelax) {
      return false
    }
    val timeNow = System.currentTimeMillis()
    while (System.currentTimeMillis() - timeNow < preRelaxDurationMs && !done()) {
      step()
    }
    return true
  }

  /**
   * because the IterativeLayoutAlgorithms use multithreading to continuously update node positions,
   * the layoutModel state is saved (during the visit method) so that it can be used continuously
   */
  override fun visit(layoutModel: LayoutModel<N>) {
    log.trace("visiting $layoutModel")
    this.layoutModel = layoutModel
  }

  companion object {
    private val log = LoggerFactory.getLogger(AbstractIterativeLayoutAlgorithm::class.java)
  }
}
