/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 *
 * Created on Oct 29, 2003
 */
package edu.uci.ics.jung.algorithms.util;

import com.google.common.collect.Iterators;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * An array-based binary heap implementation of a priority queue, which also provides efficient
 * <code>update()</code> and <code>contains</code> operations. It contains extra infrastructure (a
 * hash table) to keep track of the position of each element in the array; thus, if the key value of
 * an element changes, it may be "resubmitted" to the heap via <code>update</code> so that the heap
 * can reposition it efficiently, as necessary.
 *
 * @author Joshua O'Madadhain
 */
public class MapBinaryHeap<T> extends AbstractCollection<T> implements Queue<T> {
  private List<T> heap = new ArrayList<>(); // holds the heap as an implicit binary tree
  private Map<T, Integer> objectIndices =
      new HashMap<>(); // maps each object in the heap to its index in the heap
  private Comparator<? super T> comp;
  private static final int TOP = 0; // the index of the top of the heap

  /**
   * Creates a <code>MapBinaryHeap</code> whose heap ordering is based on the ordering of the
   * elements specified by <code>comp</code>.
   *
   * @param comp the comparator to use to order elements in the heap
   */
  public MapBinaryHeap(Comparator<T> comp) {
    initialize(comp);
  }

  /**
   * Creates a <code>MapBinaryHeap</code> whose heap ordering will be based on the <i>natural
   * ordering</i> of the elements, which must be <code>Comparable</code>.
   */
  public MapBinaryHeap() {
    initialize(new ComparableComparator());
  }

  /**
   * Creates a <code>MapBinaryHeap</code> based on the specified collection whose heap ordering will
   * be based on the <i>natural ordering</i> of the elements, which must be <code>Comparable</code>.
   *
   * @param c the collection of {@code Comparable} elements to add to the heap
   */
  public MapBinaryHeap(Collection<T> c) {
    this();
    addAll(c);
  }

  /**
   * Creates a <code>MapBinaryHeap</code> based on the specified collection whose heap ordering is
   * based on the ordering of the elements specified by <code>c</code>.
   *
   * @param c the collection of elements to add to the heap
   * @param comp the comparator to use for items in {@code c}
   */
  public MapBinaryHeap(Collection<T> c, Comparator<T> comp) {
    this(comp);
    addAll(c);
  }

  private void initialize(Comparator<T> comp) {
    this.comp = comp;
    clear();
  }

  /** @see Collection#clear() */
  @Override
  public void clear() {
    objectIndices.clear();
    heap.clear();
  }

  /** Inserts <code>o</code> into this collection. */
  @Override
  public boolean add(T o) {
    int lastIndex = heap.size();
    heap.add(o);
    percolateUp(lastIndex, o);
    return true;
  }

  /**
   * Returns <code>true</code> if this collection contains no elements, and <code>false</code>
   * otherwise.
   */
  @Override
  public boolean isEmpty() {
    return heap.isEmpty();
  }

  /** Returns the element at the top of the heap; does not alter the heap. */
  public T peek() {
    if (heap.size() > 0) {
      return heap.get(TOP);
    } else {
      return null;
    }
  }

  /** Returns the size of the heap. */
  @Override
  public int size() {
    return heap.size();
  }

  /**
   * Informs the heap that this object's internal key value has been updated, and that its place in
   * the heap may need to be shifted (up or down).
   *
   * @param o the object whose key value has been updated
   */
  public void update(T o) {
    // Since we don't know whether the key value increased or
    // decreased, we just percolate up followed by percolating down;
    // one of the two will have no effect.

    int cur = objectIndices.get(o); // current index
    int newIdx = percolateUp(cur, o);
    percolateDown(newIdx);
  }

  @Override
  public boolean contains(Object o) {
    return objectIndices.containsKey(o);
  }

  /**
   * Moves the element at position <code>cur</code> closer to the bottom of the heap, or returns if
   * no further motion is necessary. Calls itself recursively if further motion is possible.
   */
  private void percolateDown(int cur) {
    int left = lChild(cur);
    int right = rChild(cur);
    int smallest;

    if ((left < heap.size()) && (comp.compare(heap.get(left), heap.get(cur)) < 0)) {
      smallest = left;
    } else {
      smallest = cur;
    }

    if ((right < heap.size()) && (comp.compare(heap.get(right), heap.get(smallest)) < 0)) {
      smallest = right;
    }

    if (cur != smallest) {
      swap(cur, smallest);
      percolateDown(smallest);
    }
  }

  /**
   * Moves the element <code>o</code> at position <code>cur</code> as high as it can go in the heap.
   * Returns the new position of the element in the heap.
   */
  private int percolateUp(int cur, T o) {
    int i = cur;

    while ((i > TOP) && (comp.compare(heap.get(parent(i)), o) > 0)) {
      T parentElt = heap.get(parent(i));
      heap.set(i, parentElt);
      objectIndices.put(parentElt, i); // reset index to i (new location)
      i = parent(i);
    }

    // place object in heap at appropriate place
    objectIndices.put(o, i);
    heap.set(i, o);

    return i;
  }

  /** Returns the index of the left child of the element at index <code>i</code> of the heap. */
  private int lChild(int i) {
    return (i << 1) + 1;
  }

  /** Returns the index of the right child of the element at index <code>i</code> of the heap. */
  private int rChild(int i) {
    return (i << 1) + 2;
  }

  /** Returns the index of the parent of the element at index <code>i</code> of the heap. */
  private int parent(int i) {
    return (i - 1) >> 1;
  }

  /**
   * Swaps the positions of the elements at indices <code>i</code> and <code>j</code> of the heap.
   */
  private void swap(int i, int j) {
    T iElt = heap.get(i);
    T jElt = heap.get(j);

    heap.set(i, jElt);
    objectIndices.put(jElt, i);

    heap.set(j, iElt);
    objectIndices.put(iElt, j);
  }

  /**
   * Comparator used if none is specified in the constructor. Used instead of {@link
   * Comparator#naturalOrder()} or equivalent due to incompatible generics.
   *
   * @author Joshua O'Madadhain
   */
  private class ComparableComparator implements Comparator<T> {
    /** @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
    @SuppressWarnings("unchecked")
    public int compare(T arg0, T arg1) {
      return ((Comparable<T>) arg0).compareTo(arg1);
    }
  }

  /** Returns an <code>Iterator</code> that does not support modification of the heap. */
  @Override
  public Iterator<T> iterator() {
    return Iterators.unmodifiableIterator(heap.iterator());
  }

  /**
   * This data structure does not support the removal of arbitrary elements.
   *
   * @throws UnsupportedOperationException this operation is not supported
   */
  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  /**
   * This data structure does not support the removal of arbitrary elements.
   *
   * @throws UnsupportedOperationException this operation is not supported
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * This data structure does not support the removal of arbitrary elements.
   *
   * @throws UnsupportedOperationException this operation is not supported
   */
  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T element() {
    T top = this.peek();
    if (top == null) {
      throw new NoSuchElementException();
    }
    return top;
  }

  public boolean offer(T o) {
    return add(o);
  }

  @Override
  public T poll() {
    T top = this.peek();
    if (top != null) {
      int lastIndex = heap.size() - 1;
      T bottom_elt = heap.get(lastIndex);
      heap.set(TOP, bottom_elt);
      objectIndices.put(bottom_elt, TOP);

      heap.remove(lastIndex); // remove the last element
      if (heap.size() > 1) {
        percolateDown(TOP);
      }

      objectIndices.remove(top);
    }
    return top;
  }

  @Override
  public T remove() {
    T top = this.poll();
    if (top == null) {
      throw new NoSuchElementException();
    }
    return top;
  }
}
