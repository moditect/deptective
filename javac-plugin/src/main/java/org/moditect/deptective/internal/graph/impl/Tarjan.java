/**
 *  Copyright 2019 The ModiTect authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.moditect.deptective.internal.graph.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.moditect.deptective.internal.graph.GraphUtils;
import org.moditect.deptective.internal.graph.INode;

/**
 */
public class Tarjan<T extends INode> {

  /** - */
  private int                     _index                       = 0;

  /** - */
  private ArrayList<Integer>      _stack                       = new ArrayList<Integer>();

  /** - */
  private List<List<T>>           _stronglyConnectedComponents = new ArrayList<List<T>>();

  /** - */
  int[]                           _vlowlink;

  /** - */
  int[]                           _vindex;

  /** - */
  private INode[] _artifacts;

  /**
   * @param artifacts
   * @return
   */
  public List<List<T>> detectStronglyConnectedComponents(Collection<? extends T> artifacts) {
    checkNotNull(artifacts);

    _artifacts = artifacts.toArray(new INode[0]);
    int[][] adjacencyList = GraphUtils.computeAdjacencyList(_artifacts);
    return executeTarjan(adjacencyList);
  }

  //
  private List<List<T>> executeTarjan(int[][] graph) {
    checkNotNull(graph);

    // clear
    _stronglyConnectedComponents.clear();
    _index = 0;
    _stack.clear();
    _vlowlink = new int[graph.length];
    _vindex = new int[graph.length];
    for (int i = 0; i < _vlowlink.length; i++) {
      _vlowlink[i] = -1;
      _vindex[i] = -1;
    }

    //
    for (int i = 0; i < graph.length; i++) {
      if (_vindex[i] == -1) {
        tarjan(i, graph);
      }
    }

    //
    return _stronglyConnectedComponents;
  }

  @SuppressWarnings("unchecked")
  private void tarjan(int v, int[][] graph) {
    checkNotNull(v);
    checkNotNull(graph);

    _vindex[v] = _index;
    _vlowlink[v] = _index;

    _index++;
    _stack.add(0, v);
    for (int n : graph[v]) {
      if (_vindex[n] == -1) {
        tarjan(n, graph);
        _vlowlink[v] = Math.min(_vlowlink[v], _vlowlink[n]);
      } else if (_stack.contains(n)) {
        _vlowlink[v] = Math.min(_vlowlink[v], _vindex[n]);
      }
    }
    if (_vlowlink[v] == _vindex[v]) {
      int n;
      ArrayList<T> component = new ArrayList<T>();
      do {
        n = _stack.remove(0);
        component.add((T) _artifacts[n]);
      } while (n != v);
      _stronglyConnectedComponents.add(component);
    }
  }
}
