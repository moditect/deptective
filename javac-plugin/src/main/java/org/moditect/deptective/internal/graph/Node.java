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
package org.moditect.deptective.internal.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.moditect.deptective.internal.graph.Dependency;
import org.moditect.deptective.internal.graph.Node;

/**
 * 
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public class Node {

	private Map<Node, Dependency> _outgoingDependencies;
	private String _id;

	public Node(String id) {
		super();
		this._id = id;
	}

	public String getId() {
		return _id;
	}

	public Dependency getOutgoingDependencyTo(Node node) {

		if (!hasOutgoingDependencies() || !_outgoingDependencies.containsKey(checkNotNull(node))) {
			return null;
		}

		return _outgoingDependencies.get(node);
	}

	public Set<Dependency> getOutgoingDependenciesTo(Collection<Node> nodes) {
		return checkNotNull(nodes).stream().map(node -> getOutgoingDependencyTo(node)).filter(dep -> dep != null)
				.collect(Collectors.toSet());
	}

	public boolean hasOutgoingDependencies() {
		return _outgoingDependencies != null && !_outgoingDependencies.isEmpty();
	}

	public void addOutgoingDependency(Dependency dependency) {
		checkNotNull(dependency);
		outgoingDependencies().put(dependency.getTo(), dependency);
	}

	@Override
	public String toString() {
		return "DefaultNode [_id=" + _id + "]";
	}

	private Map<Node, Dependency> outgoingDependencies() {

		//
		if (_outgoingDependencies == null) {
			_outgoingDependencies = new HashMap<>();
		}

		//
		return _outgoingDependencies;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}
}
