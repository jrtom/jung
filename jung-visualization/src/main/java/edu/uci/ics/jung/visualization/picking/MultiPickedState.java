/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 * Created on Mar 28, 2005
 */
package edu.uci.ics.jung.visualization.picking;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Maintains the state of what has been 'picked' in the graph. The <code>Sets</code> are constructed
 * so that their iterators will traverse them in the order in which they are picked.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class MultiPickedState<T> extends AbstractPickedState<T> implements PickedState<T> {
  /** the 'picked' vertices */
  protected Set<T> picked = new LinkedHashSet<T>();

  public boolean pick(T v, boolean state) {
    boolean prior_state = this.picked.contains(v);
    if (state) {
      picked.add(v);
      if (prior_state == false) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, v, ItemEvent.SELECTED));
      }

    } else {
      picked.remove(v);
      if (prior_state == true) {
        fireItemStateChanged(
            new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, v, ItemEvent.DESELECTED));
      }
    }
    return prior_state;
  }

  public void clear() {
    Collection<T> unpicks = new ArrayList<T>(picked);
    for (T v : unpicks) {
      pick(v, false);
    }
    picked.clear();
  }

  public Set<T> getPicked() {
    return Collections.unmodifiableSet(picked);
  }

  public boolean isPicked(T e) {
    return picked.contains(e);
  }

  /** for the ItemSelectable interface contract */
  @SuppressWarnings("unchecked")
  public T[] getSelectedObjects() {
    List<T> list = new ArrayList<T>(picked);
    return (T[]) list.toArray();
  }
}
