package edu.uci.ics.jung.layout.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.util.Caching;
import java.util.Collection;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutModel that uses a lazy cache for node locations (LoadingCache)
 *
 * @param <N> the node type
 * @author Tom Nelson
 */
public class LoadingCacheLayoutModel<N> extends AbstractLayoutModel<N>
    implements LayoutModel<N>, Caching {

  private static final Logger log = LoggerFactory.getLogger(LoadingCacheLayoutModel.class);

  protected LoadingCache<N, Point> locations =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> Point.ORIGIN));

  @Override
  public Collection<ChangeListener> getChangeListeners() {
    return changeSupport.getChangeListeners();
  }

  /**
   * a builder for LoadingCache instances
   *
   * @param <N> the node type
   * @param <T> the type of the superclass of the LayoutModel to be built
   */
  public abstract static class Builder<N, T extends LoadingCacheLayoutModel<N>> {
    protected Graph<N> graph;
    protected int width;
    protected int height;

    protected LoadingCache<N, Point> locations =
        CacheBuilder.newBuilder().build(CacheLoader.from(() -> Point.ORIGIN));

    /**
     * set the Graph to use for the LayoutModel
     *
     * @param graph
     * @return this builder for further use
     */
    public LoadingCacheLayoutModel.Builder<N, T> setGraph(Graph<N> graph) {
      this.graph = checkNotNull(graph);
      return this;
    }

    /**
     * set the LayoutModel to copy with this builder
     *
     * @param layoutModel
     * @return this builder for further use
     */
    public LoadingCacheLayoutModel.Builder<N, T> setLayoutModel(LayoutModel<N> layoutModel) {
      this.width = layoutModel.getWidth();
      this.height = layoutModel.getHeight();
      return this;
    }

    /**
     * sets the size that will be used for the LayoutModel
     *
     * @param width
     * @param height
     * @return the LayoutModel.Builder being built
     */
    public LoadingCacheLayoutModel.Builder<N, T> setSize(int width, int height) {
      this.width = width;
      this.height = height;
      return this;
    }

    /**
     * sets the initializer to use for new nodes
     *
     * @param initializer
     * @return the builder
     */
    public LoadingCacheLayoutModel.Builder<N, T> setInitializer(Function<N, Point> initializer) {
      Function<N, Point> chain = initializer.andThen(p -> Point.of(p.x, p.y));
      this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
      return this;
    }

    /**
     * build an instance of the requested LayoutModel of type T
     *
     * @return
     */
    public abstract T build();
  }

  public static <N> Builder<N, ?> builder() {
    return new Builder<N, LoadingCacheLayoutModel<N>>() {
      @Override
      public LoadingCacheLayoutModel<N> build() {
        return new LoadingCacheLayoutModel<>(this);
      }
    };
  }

  public static <N, P> LoadingCacheLayoutModel<N> from(LoadingCacheLayoutModel<N> other) {
    return new LoadingCacheLayoutModel<>(other);
  }

  protected LoadingCacheLayoutModel(LoadingCacheLayoutModel.Builder<N, ?> builder) {
    super(builder.graph, builder.width, builder.height);
    this.locations = builder.locations;
  }

  private LoadingCacheLayoutModel(LoadingCacheLayoutModel<N> other) {
    super(other.graph, other.width, other.height);
  }

  public void setInitializer(Function<N, Point> initializer) {
    Function<N, Point> chain = initializer.andThen(p -> Point.of(p.x, p.y));
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain::apply));
  }

  @Override
  public void setGraph(Graph<N> graph) {
    super.setGraph(graph);
    changeSupport.fireChanged();
  }

  @Override
  public void set(N node, Point location) {
    if (!locked) {
      this.locations.put(node, location);
      super.set(node, location); // will fire events
    }
  }

  @Override
  public void set(N node, double x, double y) {
    this.set(node, Point.of(x, y));
  }

  @Override
  public Point get(N node) {
    if (log.isTraceEnabled()) {
      log.trace(this.locations.getUnchecked(node) + " gotten for " + node);
    }
    return this.locations.getUnchecked(node);
  }

  @Override
  public Point apply(N node) {
    return this.get(node);
  }

  @Override
  public void clear() {
    this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(() -> Point.ORIGIN));
  }
}
