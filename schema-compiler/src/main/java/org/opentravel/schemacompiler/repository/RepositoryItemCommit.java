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

import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryHistoryItemType;
import org.opentravel.schemacompiler.xml.XMLGregorianCalendarConverter;

/**
 * Represents a single commit or change in the update history of a repository item.
 * 
 * @author S. Livezey
 */
public class RepositoryItemCommit {
	
	private int commitNumber;
	private Date effectiveOn;
	private String user;
	private String remarks;
	
	/**
	 * Constructor that initializes this commit entry from its persistent representation.
	 * 
	 * @param historyItem  the persistent representation of this commit record
	 */
	public RepositoryItemCommit(LibraryHistoryItemType historyItem) {
		this.commitNumber = historyItem.getCommitNumber();
		this.effectiveOn = XMLGregorianCalendarConverter.toJavaDate( historyItem.getEffectiveOn() );
		this.user = historyItem.getUser();
		this.remarks = historyItem.getValue();
		
		// Ensure we have a trimmed, non-empty string for the remarks
		this.remarks = (this.remarks == null) ? "" : this.remarks.trim();
	}
	
	/**
	 * Returns the value of the 'commitNumber' field.
	 *
	 * @return int
	 */
	public int getCommitNumber() {
		return commitNumber;
	}
	
	/**
	 * Returns the value of the 'effectiveOn' field.
	 *
	 * @return Date
	 */
	public Date getEffectiveOn() {
		return effectiveOn;
	}
	
	/**
	 * Returns the value of the 'user' field.
	 *
	 * @return String
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * Returns the value of the 'remarks' field.
	 *
	 * @return String
	 */
	public String getRemarks() {
		return remarks;
	}
	
}
