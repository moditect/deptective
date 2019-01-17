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

import org.moditect.deptective.internal.graph.IDependency;

public class TestDependency implements IDependency {

	private TestNode _from;
	
	private TestNode _to;
	
	private int aggregatdWeight;
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @param aggregatdWeight
	 */
	public TestDependency(TestNode from, TestNode to, int aggregatdWeight) {
		this._from = from;
		this._to = to;
		this.aggregatdWeight = aggregatdWeight;
		
		from.addOutgoingDependency(this);
	}

	@Override
	public TestNode getFrom() {
		return _from;
	}

	@Override
	public TestNode getTo() {
		return _to;
	}

	@Override
	public int getAggregatedWeight() {
		return aggregatdWeight;
	}

	@Override
	public String toString() {
		return "SimpleDependency [from=" + _from + ", to=" + _to + ", aggregatdWeight=" + aggregatdWeight + "]";
	}
}
