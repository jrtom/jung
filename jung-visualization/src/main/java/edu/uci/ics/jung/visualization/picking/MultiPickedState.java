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
public class MultiPickedState extends AbstractPickedState implements PickedState {
  /** the 'picked' vertices */
  protected Set<Object> picked = new LinkedHashSet<Object>();

  public boolean pick(Object v, boolean state) {
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
    Collection<Object> unpicks = new ArrayList<Object>(picked);
    for (Object v : unpicks) {
      pick(v, false);
    }
    picked.clear();
  }

  public Set<Object> getPicked() {
    return Collections.unmodifiableSet(picked);
  }

  public boolean isPicked(Object e) {
    return picked.contains(e);
  }

  /** for the ItemSelectable interface contract */
  @SuppressWarnings("unchecked")
  public Object[] getSelectedObjects() {
    List<Object> list = new ArrayList<Object>(picked);
    return (Object[]) list.toArray();
  }
}
