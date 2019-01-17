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
package org.moditect.deptective.internal.graph.fwk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.moditect.deptective.internal.graph.IDependency;
import org.moditect.deptective.internal.graph.INode;

/**
 * 
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public class TestNode implements INode {

	//
	private Map<INode, IDependency> _outgoingDependencies;

	//
	private String _id;

	/**
	 * 
	 * @param id
	 */
	public TestNode(String id) {
		super();
		this._id = id;
	}

	/**
	 * 
	 * @return
	 */
	public String getId() {
		return _id;
	}

	@Override
	public IDependency getOutgoingDependencyTo(INode node) {

		//
		if (!hasOutgoingDependencies() || !_outgoingDependencies.containsKey(checkNotNull(node))) {
			return null;
		}

		//
		return _outgoingDependencies.get(node);
	}

	/**
	 * 
	 */
	@Override
	public Set<IDependency> getOutgoingDependenciesTo(Collection<INode> nodes) {
		return checkNotNull(nodes).stream().map(node -> getOutgoingDependencyTo(node)).filter(dep -> dep != null)
				.collect(Collectors.toSet());
	}

	/**
	 * 
	 */
	@Override
	public boolean hasOutgoingDependencies() {
		return _outgoingDependencies != null && !_outgoingDependencies.isEmpty();
	}

	/**
	 * 
	 * @param target
	 */
	public void addOutgoingDependency(TestDependency dependency) {
		checkNotNull(dependency);
		outgoingDependencies().put(dependency.getTo(), dependency);
	}

	@Override
	public String toString() {
		return "DummyNode [_id=" + _id + "]";
	}

	/**
	 * 
	 * @return
	 */
	private Map<INode, IDependency> outgoingDependencies() {

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
		TestNode other = (TestNode) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		return true;
	}
}
