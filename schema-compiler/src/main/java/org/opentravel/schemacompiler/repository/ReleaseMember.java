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

import java.util.Date;

/**
 * Specifies a single library member of an OTM release.  All release members are
 * required to be managed in an OTM repository.  In addition to specifying the
 * library itself, each release member can specify an effective date based on the
 * library's commit history in the repository.
 */
public class ReleaseMember {
	
	private RepositoryItem repositoryItem;
	private Date effectiveDate;
	
	/**
	 * Returns the repository item that is a member of the OTM release.
	 *
	 * @return RepositoryItem
	 */
	public RepositoryItem getRepositoryItem() {
		return repositoryItem;
	}
	
	/**
	 * Assigns the repository item that is a member of the OTM release.
	 *
	 * @param repositoryItem  the repository item to assign
	 */
	public void setRepositoryItem(RepositoryItem repositoryItem) {
		this.repositoryItem = repositoryItem;
	}
	
	/**
	 * Returns the effective date of the repository item for the release.
	 *
	 * @return Date
	 */
	public Date getEffectiveDate() {
		return effectiveDate;
	}
	
	/**
	 * Assigns the effective date of the repository item for the release.
	 *
	 * @param effectiveDate  the effective date to assign
	 */
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	
}
