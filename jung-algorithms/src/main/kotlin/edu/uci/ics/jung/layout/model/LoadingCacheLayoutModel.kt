package edu.uci.ics.jung.layout.model

import com.google.common.base.Preconditions.checkNotNull
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.graph.Graph
import edu.uci.ics.jung.layout.util.Caching
import java.util.function.Function
import org.slf4j.LoggerFactory

/**
 * A LayoutModel that uses a lazy cache for node locations (LoadingCache)
 *
 * @param N the node type
 * @author Tom Nelson
 */
open class LoadingCacheLayoutModel<N : Any> : AbstractLayoutModel<N>, LayoutModel<N>, Caching {

  protected var locationCache: LoadingCache<N, Point> =
    CacheBuilder.newBuilder().build(object : CacheLoader<N, Point>() {
      override fun load(key: N): Point = Point.ORIGIN
    })

  /**
   * a builder for LoadingCache instances
   *
   * @param N the node type
   * @param T the type of the superclass of the LayoutModel to be built
   */
  abstract class Builder<N : Any, T : LoadingCacheLayoutModel<N>> {
    var graph: Graph<N>? = null
    var width: Int = 0
    var height: Int = 0
    var locations: LoadingCache<N, Point> =
      CacheBuilder.newBuilder().build(object : CacheLoader<N, Point>() {
      override fun load(key: N): Point = Point.ORIGIN
    })

    /**
     * set the Graph to use for the LayoutModel
     *
     * @param graph
     * @return this builder for further use
     */
    fun setGraph(graph: Graph<N>): Builder<N, T> {
      this.graph = checkNotNull(graph)
      return this
    }

    /**
     * set the LayoutModel to copy with this builder
     *
     * @param layoutModel
     * @return this builder for further use
     */
    fun setLayoutModel(layoutModel: LayoutModel<N>): Builder<N, T> {
      this.width = layoutModel.width
      this.height = layoutModel.height
      return this
    }

    /**
     * sets the size that will be used for the LayoutModel
     *
     * @param width
     * @param height
     * @return the LayoutModel.Builder being built
     */
    fun setSize(width: Int, height: Int): Builder<N, T> {
      this.width = width
      this.height = height
      return this
    }

    /**
     * sets the initializer to use for new nodes
     *
     * @param initializer
     * @return the builder
     */
    fun setInitializer(initializer: Function<N, Point>): Builder<N, T> {
      val chain: Function<N, Point> = initializer.andThen { p -> Point.of(p.x, p.y) }
      this.locations = CacheBuilder.newBuilder().build(object : CacheLoader<N, Point>() {
        override fun load(key: N): Point = chain.apply(key)
      })
      return this
    }

    /**
     * build an instance of the requested LayoutModel of type T
     *
     * @return
     */
    abstract fun build(): T
  }

  protected constructor(builder: Builder<N, *>) : super(builder.graph!!, builder.width, builder.height) {
    this.locationCache = builder.locations
  }

  private constructor(other: LoadingCacheLayoutModel<N>) : super(other.graph, other.width, other.height)

  override fun setInitializer(initializer: Function<N, Point>) {
    val chain: Function<N, Point> = initializer.andThen { p -> Point.of(p.x, p.y) }
    this.locationCache = CacheBuilder.newBuilder().build(object : CacheLoader<N, Point>() {
      override fun load(key: N): Point = chain.apply(key)
    })
  }

  override var graph: Graph<N>
    get() = super.graph
    set(value) {
      super.graph = value
      changeSupport.fireChanged()
    }

  override fun set(node: N, location: Point) {
    if (!locked) {
      this.locationCache.put(node, location)
      super.set(node, location) // will fire events
    }
  }

  override fun set(node: N, x: Double, y: Double) {
    this.set(node, Point.of(x, y))
  }

  override fun get(node: N): Point {
    if (log.isTraceEnabled) {
      log.trace("${this.locationCache.getUnchecked(node)} gotten for $node")
    }
    return this.locationCache.getUnchecked(node)
  }

  override fun apply(node: N): Point = this.get(node)

  override fun clear() {
    this.locationCache = CacheBuilder.newBuilder().build(object : CacheLoader<N, Point>() {
      override fun load(key: N): Point = Point.ORIGIN
    })
  }

  companion object {
    private val log = LoggerFactory.getLogger(LoadingCacheLayoutModel::class.java)

    @JvmStatic
    fun <N : Any> builder(): Builder<N, *> {
      return object : Builder<N, LoadingCacheLayoutModel<N>>() {
        override fun build(): LoadingCacheLayoutModel<N> = LoadingCacheLayoutModel(this)
      }
    }

    @JvmStatic
    fun <N : Any> from(other: LoadingCacheLayoutModel<N>): LoadingCacheLayoutModel<N> =
      LoadingCacheLayoutModel(other)
  }
}
