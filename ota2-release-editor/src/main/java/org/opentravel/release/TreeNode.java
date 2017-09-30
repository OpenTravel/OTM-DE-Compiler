/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.release;

import java.util.List;

/**
 * Represents a single node in the tree view for the selected library.
 */
public abstract class TreeNode<E> {
	
	private E entity;
	
	/**
	 * Constructor that specifies the OTM entity for this node.
	 * 
	 * @param entity  the OTM entity represented by this node
	 */
	public TreeNode(E entity) {
		this.entity = entity;
	}
	
	/**
	 * Returns the the OTM entity represented by this node.
	 *
	 * @return E
	 */
	public E getEntity() {
		return entity;
	}
	
	/**
	 * Returns the list of properties associated with the OTM entity
	 * associated with this node.
	 * 
	 * @return List<NodeProperty>
	 */
	public abstract List<NodeProperty> getProperties();
	
}
