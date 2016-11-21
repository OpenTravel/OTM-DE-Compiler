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

package org.opentravel.schemacompiler.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides the commit history for a repository item.
 * 
 * @author S. Livezey
 */
public class RepositoryItemHistory {
	
	private RepositoryItem item;
	private List<RepositoryItemCommit> commitHistory = new ArrayList<>();
	
	/**
	 * Constructor that initializes the <code>RepositoryItem</code> to which
	 * this history applies.
	 * 
	 * @param item  the repository item to which this history applies
	 */
	public RepositoryItemHistory(RepositoryItem item) {
		this.item = item;
	}
	
	/**
	 * Returns the <code>RepositoryItem</code> to which this history applies.
	 * 
	 * @return RepositoryItem
	 */
	public RepositoryItem getRepositoryItem() {
		return item;
	}
	
	/**
	 * Returns the detailed commit history for this repository item.
	 *
	 * @return List<RepositoryItemCommit>
	 */
	public List<RepositoryItemCommit> getCommitHistory() {
		return commitHistory;
	}
	
	/**
	 * Assigns the detailed commit history for this repository item.
	 *
	 * @param commitHistory  the field value to assign
	 */
	public void setCommitHistory(List<RepositoryItemCommit> commitHistory) {
		this.commitHistory = Collections.unmodifiableList(
				(commitHistory == null) ? new ArrayList<RepositoryItemCommit>() : commitHistory );
	}
	
}
