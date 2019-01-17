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

import java.util.Collection;
import java.util.Set;

/**
 * 
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public interface INode {

	/**
	 * 
	 * @param node
	 * @return
	 */
	IDependency getOutgoingDependencyTo(INode node);
	
	/**
	 * 
	 * @param nodes
	 * @return
	 */
	Set<IDependency> getOutgoingDependenciesTo(Collection<INode> nodes);

	/**
	 * 
	 * @return
	 */
	boolean hasOutgoingDependencies();
}
