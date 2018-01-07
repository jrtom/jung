package edu.uci.ics.jung.visualization.spatial.rtree;

/**
 * a container for the functions that support R-Tree and R*-Tree
 *
 * @param <T> the type of element in the RTree
 * @author Tom Nelson
 */
public class SplitterContext<T> {

  public final LeafSplitter<T> leafSplitter;
  public final Splitter<T> splitter;

  public static <T> SplitterContext<T> of(LeafSplitter<T> leafSplitter, Splitter<T> splitter) {
    return new SplitterContext<>(leafSplitter, splitter);
  }

  private SplitterContext(LeafSplitter<T> leafSplitter, Splitter<T> splitter) {
    this.leafSplitter = leafSplitter;
    this.splitter = splitter;
  }
}
