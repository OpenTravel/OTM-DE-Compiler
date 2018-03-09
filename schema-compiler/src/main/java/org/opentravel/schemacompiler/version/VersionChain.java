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

package org.opentravel.schemacompiler.version;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Container for a series of OTM model objects that are assigned the same base
 * namespace and name.
 *
 * @param <T>  the type of item managed by the chain
 */
public class VersionChain<T> {
	
	private String baseNS;
	private String name;
	private SortedSet<T> versions;
	
	/**
	 * Constructor that specifies the base namespace and item name for this chain.
	 * 
	 * @param baseNS  the base namespace of all items in the chain
	 * @param name  the name of all items in the chain
	 * @param comparator  used to sort items in ascending version order
	 */
	public VersionChain(String baseNS, String name, Comparator<T> comparator) {
		this.baseNS = baseNS;
		this.name = name;
		this.versions = new TreeSet<>( comparator );
	}

	/**
	 * Returns the base namespace of all items in the chain.
	 *
	 * @return String
	 */
	public String getBaseNS() {
		return baseNS;
	}

	/**
	 * Returns the name of all items in the chain.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns collection of item versions sorted in ascending version order.
	 *
	 * @return List<T>
	 */
	public List<T> getVersions() {
		return new ArrayList<>( versions );
	}
	
	/**
	 * Adds the given version to this chain.
	 * 
	 * @param version  the versioned item to add
	 */
	protected void addVersion(T version) {
		versions.add( version );
	}
	
	/**
	 * Returns the next version of the given item or null if no later version exists.
	 * 
	 * @param item  the item for which to return the next version
	 * @return T
	 * @throws IllegalArgumentException  thrown if the given item is not assigned to this version chain
	 */
	public T getNextVersion(T item) {
		List<T> versionList = new ArrayList<>( versions );
		int itemIdx = versionList.indexOf( item );
		T nextVersion = null;
		
		if (itemIdx < 0) {
			throw new IllegalArgumentException(
					"The given item is not assigned to this version chain");
		}
		if ((itemIdx + 1) < versionList.size()) {
			nextVersion = versionList.get( itemIdx + 1 );
		}
		return nextVersion;
	}
	
	/**
	 * Returns the previous version of the given item or null if no earlier version exists.
	 * 
	 * @param item  the item for which to return the previous version
	 * @return T
	 * @throws IllegalArgumentException  thrown if the given item is not assigned to this version chain
	 */
	public T getPreviousVersion(T item) {
		List<T> versionList = new ArrayList<>( versions );
		int itemIdx = versionList.indexOf( item );
		T nextVersion = null;
		
		if (itemIdx < 0) {
			throw new IllegalArgumentException(
					"The given item is not assigned to this version chain");
		}
		if (itemIdx > 0) {
			nextVersion = versionList.get( itemIdx - 1 );
		}
		return nextVersion;
	}
	
}
