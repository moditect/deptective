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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * http://dl.acm.org/citation.cfm?id=595057
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class FastFAS {

	/** the set of vertices */
	private Set<Integer> vertices;

	/** the adjacency matrix */
	int[][] adjacencyMatrix;

	/** the result list 's1' */
	private List<Integer> s1;

	/** the result list 's2' */
	private List<Integer> s2;

	/** the skipped edges */
	private List<Integer[]> skippedEdge;

	/**
	 * <p>
	 * Creates a new instance of type {@link FastFAS}.
	 * </p>
	 * 
	 * @param adjacencyMatrix the adjacency matrix
	 */
	public FastFAS(int[][] adjacencyMatrix) {
		this.adjacencyMatrix = checkNotNull(adjacencyMatrix);
	}

	/**
	 * <p>
	 * Returns the ordered sequence.
	 * </p>
	 * 
	 * @return the ordered sequence.
	 */
	public int[] getOrderedSequence() {

		// create the skipped edges list
		skippedEdge = new ArrayList<>();

		// create the vertices set
		vertices = new HashSet<Integer>();
		for (int i = 0; i < this.adjacencyMatrix.length; i++) {
			vertices.add(i);
		}

		// create the internal result lists
		s1 = new ArrayList<Integer>();
		s2 = new ArrayList<Integer>();

		// the main loop
		while (!vertices.isEmpty()) {
			if (findSink()) {
				continue;
			} else if (findSource()) {
				continue;
			} else if (findVertexToRemove()) {
				continue;
			}
		}

		// convert to result array
		return convertToArray(s1, s2);
	}
	
	public List<Integer[]> getSkippedEdge() {
		return skippedEdge;
	}

	public static int[] reverse(int[] sequence) {

		//
		checkNotNull(sequence);

		//
		int[] result = new int[sequence.length];
		for (int i = 0; i < sequence.length; i++) {
			result[sequence.length - (1 + i)] = sequence[i];
		}

		//
		return result;
	}

	/**
	 * <p>
	 * Tries to find and remove a sink.
	 * </p>
	 * 
	 * @return <code>true</code> if a sink was found and removed.
	 */
	private boolean findSink() {

		// initialize the sink
		int sink = -1;

		// try to find a sink...
		for (Integer i : vertices) {
			sink = i;
			for (Integer j : vertices) {
				if (i != j && this.adjacencyMatrix[i][j] != 0) {
					sink = -1;
					break;
				}
			}
			if (sink != -1) {
				break;
			}
		}

		// if a sink was found, remove it and return true...
		if (sink != -1) {
			vertices.remove(sink);
			s2.add(0, sink);
			return true;
		}
		// ...otherwise return false
		else {
			return false;
		}
	}

	/**
	 * <p>
	 * Tries to find and remove a source.
	 * </p>
	 * 
	 * @return <code>true</code> if a source was found and removed.
	 */
	private boolean findSource() {

		// initialize the source
		int source = -1;

		// try to find a source...
		for (Integer i : vertices) {
			source = i;
			for (Integer j : vertices) {
				if (i != j && this.adjacencyMatrix[j][i] != 0) {
					source = -1;
					break;
				}
			}
			if (source != -1) {
				break;
			}
		}

		// if a source was found, remove it and return true...
		if (source != -1) {
			vertices.remove(source);
			s1.add(source);
			return true;
		}
		// ...otherwise return false
		else {
			return false;
		}
	}

	/**
	 * <p>
	 * </p>
	 * 
	 * @return
	 */
	private boolean findVertexToRemove() {

		// initialize current maximum and vertex
		int currentMaximum = Integer.MIN_VALUE;
		int currentVertex = Integer.MIN_VALUE;

		// find the vertex with the highest maximum
		for (Integer vertex : vertices) {
			int delta = getDelta(vertex);
			if (currentVertex == Integer.MIN_VALUE || currentMaximum < delta) {
				currentMaximum = delta;
				currentVertex = vertex;
			}
		}
		
		// remove vertex and return true...
		vertices.remove(currentVertex);
		
		// 
		for (Integer j : vertices) {
			if (currentVertex != j && this.adjacencyMatrix[j][currentVertex] != 0) {
				skippedEdge.add(new Integer[]{j, currentVertex});	
			}
		}
		
		s1.add(currentVertex);
		return false;
	}

	/**
	 * <p>
	 * </p>
	 * 
	 * @param vertex
	 * @return
	 */
	private int getDelta(int vertex) {

		int in = 0;
		int out = 0;

		for (Integer j : vertices) {
			if (vertex != j) {
				in = in + this.adjacencyMatrix[j][vertex];
				out = out + this.adjacencyMatrix[vertex][j];
			}
		}

		//
		return out - in;
	}

	/**
	 * <p>
	 * Helper method. Concatenates the given lists and returns them as one array.
	 * </p>
	 * 
	 * @param s1 the list s1
	 * @param s2 the list s2
	 * @return the result array.
	 */
	private int[] convertToArray(List<Integer> s1, List<Integer> s2) {
		int[] result = new int[s1.size() + s2.size()];
		int index = 0;
		for (int i : s1) {
			result[index] = i;
			index++;
		}
		for (int i : s2) {
			result[index] = i;
			index++;
		}
		return result;
	}
}
