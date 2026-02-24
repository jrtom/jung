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
package edu.uci.ics.jung.algorithms.util

import com.google.common.collect.Iterators
import java.util.AbstractCollection
import java.util.Comparator
import java.util.NoSuchElementException
import java.util.Queue

/**
 * An array-based binary heap implementation of a priority queue, which also provides efficient
 * `update()` and `contains` operations. It contains extra infrastructure (a
 * hash table) to keep track of the position of each element in the array; thus, if the key value of
 * an element changes, it may be "resubmitted" to the heap via `update` so that the heap
 * can reposition it efficiently, as necessary.
 *
 * @author Joshua O'Madadhain
 */
class MapBinaryHeap<T : Any> : AbstractCollection<T>, Queue<T> {

  private val heap: MutableList<T> = ArrayList() // holds the heap as an implicit binary tree
  private val objectIndices: MutableMap<T, Int> = HashMap() // maps each object in the heap to its index in the heap
  private var comp: Comparator<in T>

  /**
   * Creates a `MapBinaryHeap` whose heap ordering is based on the ordering of the
   * elements specified by `comp`.
   *
   * @param comp the comparator to use to order elements in the heap
   */
  constructor(comp: Comparator<T>) {
    this.comp = comp
    clear()
  }

  /**
   * Creates a `MapBinaryHeap` whose heap ordering will be based on the *natural
   * ordering* of the elements, which must be `Comparable`.
   */
  constructor() {
    this.comp = ComparableComparator()
    clear()
  }

  /**
   * Creates a `MapBinaryHeap` based on the specified collection whose heap ordering will
   * be based on the *natural ordering* of the elements, which must be `Comparable`.
   *
   * @param c the collection of `Comparable` elements to add to the heap
   */
  constructor(c: Collection<T>) : this() {
    addAll(c)
  }

  /**
   * Creates a `MapBinaryHeap` based on the specified collection whose heap ordering is
   * based on the ordering of the elements specified by `comp`.
   *
   * @param c the collection of elements to add to the heap
   * @param comp the comparator to use for items in [c]
   */
  constructor(c: Collection<T>, comp: Comparator<T>) : this(comp) {
    addAll(c)
  }

  /**
   * @see Collection.clear
   */
  override fun clear() {
    objectIndices.clear()
    heap.clear()
  }

  /** Inserts `o` into this collection. */
  override fun add(element: T): Boolean {
    val lastIndex = heap.size
    heap.add(element)
    percolateUp(lastIndex, element)
    return true
  }

  /**
   * Returns `true` if this collection contains no elements, and `false`
   * otherwise.
   */
  override fun isEmpty(): Boolean = heap.isEmpty()

  /** Returns the element at the top of the heap; does not alter the heap. */
  override fun peek(): T? = if (heap.isNotEmpty()) heap[TOP] else null

  /** Returns the size of the heap. */
  override val size: Int
    get() = heap.size

  /**
   * Informs the heap that this object's internal key value has been updated, and that its place in
   * the heap may need to be shifted (up or down).
   *
   * @param o the object whose key value has been updated
   */
  fun update(o: T) {
    // Since we don't know whether the key value increased or
    // decreased, we just percolate up followed by percolating down;
    // one of the two will have no effect.

    val cur = objectIndices[o]!! // current index
    val newIdx = percolateUp(cur, o)
    percolateDown(newIdx)
  }

  override fun contains(element: T): Boolean = objectIndices.containsKey(element)

  /**
   * Moves the element at position `cur` closer to the bottom of the heap, or returns if
   * no further motion is necessary. Calls itself recursively if further motion is possible.
   */
  private fun percolateDown(cur: Int) {
    val left = lChild(cur)
    val right = rChild(cur)
    var smallest: Int

    smallest = if (left < heap.size && comp.compare(heap[left], heap[cur]) < 0) {
      left
    } else {
      cur
    }

    if (right < heap.size && comp.compare(heap[right], heap[smallest]) < 0) {
      smallest = right
    }

    if (cur != smallest) {
      swap(cur, smallest)
      percolateDown(smallest)
    }
  }

  /**
   * Moves the element `o` at position `cur` as high as it can go in the heap.
   * Returns the new position of the element in the heap.
   */
  private fun percolateUp(cur: Int, o: T): Int {
    var i = cur

    while (i > TOP && comp.compare(heap[parent(i)], o) > 0) {
      val parentElt = heap[parent(i)]
      heap[i] = parentElt
      objectIndices[parentElt] = i // reset index to i (new location)
      i = parent(i)
    }

    // place object in heap at appropriate place
    objectIndices[o] = i
    heap[i] = o

    return i
  }

  /** Returns the index of the left child of the element at index `i` of the heap. */
  private fun lChild(i: Int): Int = (i shl 1) + 1

  /** Returns the index of the right child of the element at index `i` of the heap. */
  private fun rChild(i: Int): Int = (i shl 1) + 2

  /** Returns the index of the parent of the element at index `i` of the heap. */
  private fun parent(i: Int): Int = (i - 1) shr 1

  /**
   * Swaps the positions of the elements at indices `i` and `j` of the heap.
   */
  private fun swap(i: Int, j: Int) {
    val iElt = heap[i]
    val jElt = heap[j]

    heap[i] = jElt
    objectIndices[jElt] = i

    heap[j] = iElt
    objectIndices[iElt] = j
  }

  /**
   * Comparator used if none is specified in the constructor. Used instead of
   * [Comparator.naturalOrder] or equivalent due to incompatible generics.
   *
   * @author Joshua O'Madadhain
   */
  private inner class ComparableComparator : Comparator<T> {
    /**
     * @see Comparator.compare
     */
    @Suppress("UNCHECKED_CAST")
    override fun compare(arg0: T, arg1: T): Int =
      (arg0 as Comparable<T>).compareTo(arg1)
  }

  /** Returns an `Iterator` that does not support modification of the heap. */
  override fun iterator(): MutableIterator<T> =
    Iterators.unmodifiableIterator(heap.iterator())

  /**
   * This data structure does not support the removal of arbitrary elements.
   *
   * @throws UnsupportedOperationException this operation is not supported
   */
  override fun remove(element: T): Boolean {
    throw UnsupportedOperationException()
  }

  /**
   * This data structure does not support the removal of arbitrary elements.
   *
   * @throws UnsupportedOperationException this operation is not supported
   */
  override fun removeAll(elements: Collection<T>): Boolean {
    throw UnsupportedOperationException()
  }

  /**
   * This data structure does not support the removal of arbitrary elements.
   *
   * @throws UnsupportedOperationException this operation is not supported
   */
  override fun retainAll(elements: Collection<T>): Boolean {
    throw UnsupportedOperationException()
  }

  override fun element(): T {
    return this.peek() ?: throw NoSuchElementException()
  }

  override fun offer(e: T): Boolean = add(e)

  override fun poll(): T? {
    val top = this.peek()
    if (top != null) {
      val lastIndex = heap.size - 1
      val bottomElt = heap[lastIndex]
      heap[TOP] = bottomElt
      objectIndices[bottomElt] = TOP

      heap.removeAt(lastIndex) // remove the last element
      if (heap.size > 1) {
        percolateDown(TOP)
      }

      objectIndices.remove(top)
    }
    return top
  }

  override fun remove(): T {
    return this.poll() ?: throw NoSuchElementException()
  }

  companion object {
    private const val TOP = 0 // the index of the top of the heap
  }
}
