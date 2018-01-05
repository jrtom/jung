/*
 * Copyright (C) 2014 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.jung.graph;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.Set;
import org.junit.Test;

// TODO: Consider merging AbstractCTreeTest into CTreeTest
public class CTreeTest extends AbstractCTreeTest {

  @Override
  public MutableCTree<Integer> createTree() {
    return TreeBuilder.builder().build();
  }

  @Override
  @Test
  public void nodes_checkReturnedSetMutability() {
    Set<Integer> nodes = tree.nodes();
    try {
      nodes.add(N2);
      fail(ERROR_MODIFIABLE_SET);
    } catch (UnsupportedOperationException e) {
      addNode(N1);
      assertThat(tree.nodes()).containsExactlyElementsIn(nodes);
    }
  }

  @Override
  @Test
  public void adjacentNodes_checkReturnedSetMutability() {
    addNode(N1);
    Set<Integer> adjacentNodes = tree.adjacentNodes(N1);
    try {
      adjacentNodes.add(N2);
      fail(ERROR_MODIFIABLE_SET);
    } catch (UnsupportedOperationException e) {
      putEdge(N1, N2);
      assertThat(tree.adjacentNodes(N1)).containsExactlyElementsIn(adjacentNodes);
    }
  }

  @Override
  @Test
  public void predecessors_checkReturnedSetMutability() {
    addNode(N2);
    Set<Integer> predecessors = tree.predecessors(N2);
    try {
      predecessors.add(N1);
      fail(ERROR_MODIFIABLE_SET);
    } catch (UnsupportedOperationException e) {
      // putting an edge like `putEdge(N1, N2)`, which would have been used to check that N2's
      // predecessors gets updated, cannot be done because CTree#putEdge cannot be used to
      // implicitly update the root of a tree.
    }
  }

  @Override
  @Test
  public void successors_checkReturnedSetMutability() {
    addNode(N1);
    Set<Integer> successors = tree.successors(N1);
    try {
      successors.add(N2);
      fail(ERROR_MODIFIABLE_SET);
    } catch (UnsupportedOperationException e) {
      putEdge(N1, N2);
      assertThat(successors).containsExactlyElementsIn(tree.successors(N1));
    }
  }
}
