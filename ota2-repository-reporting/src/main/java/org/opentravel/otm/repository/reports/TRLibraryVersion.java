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
package org.opentravel.otm.repository.reports;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * Data transfer object representing a single version of an OTM library.
 */
@Entity
@Table( name = "library_version" )
public class TRLibraryVersion implements Serializable {
	
	private static final long serialVersionUID = -3832326093552995268L;
	
	@Id
	@Column( name = "id", nullable = false )
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id = -1L;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "library_id" )
	private TRLibrary library;
	
	@Column( name = "version_identifier", nullable = false, length = 50 )
	private String version;
	
	@Column( name = "major_version", nullable = false )
	private int majorVersion;
	
	@Column( name = "minor_version", nullable = false )
	private int minorVersion;
	
	@Column( name = "patch_version", nullable = false )
	private int patchVersion;
	
	@Column( name = "create_date", nullable = false )
	private Date createDate;
	
	@OneToMany( mappedBy = "libraryVersion", fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE } )
	@OrderBy( "commitNumber DESC" )
	private List<TRLibraryCommit> commits;

	/**
	 * Returns the value of the 'id' field.
	 *
	 * @return long
	 */
	public long getId() {
		return id;
	}

	/**
	 * Assigns the value of the 'id' field.
	 *
	 * @param id  the field value to assign
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the value of the 'library' field.
	 *
	 * @return TRLibrary
	 */
	public TRLibrary getLibrary() {
		return library;
	}

	/**
	 * Assigns the value of the 'library' field.
	 *
	 * @param library  the field value to assign
	 */
	public void setLibrary(TRLibrary library) {
		this.library = library;
	}

	/**
	 * Returns the value of the 'version' field.
	 *
	 * @return String
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Assigns the value of the 'version' field.
	 *
	 * @param version  the field value to assign
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Returns the value of the 'majorVersion' field.
	 *
	 * @return int
	 */
	public int getMajorVersion() {
		return majorVersion;
	}

	/**
	 * Assigns the value of the 'majorVersion' field.
	 *
	 * @param majorVersion  the field value to assign
	 */
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	/**
	 * Returns the value of the 'minorVersion' field.
	 *
	 * @return int
	 */
	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * Assigns the value of the 'minorVersion' field.
	 *
	 * @param minorVersion  the field value to assign
	 */
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	/**
	 * Returns the value of the 'patchVersion' field.
	 *
	 * @return int
	 */
	public int getPatchVersion() {
		return patchVersion;
	}

	/**
	 * Assigns the value of the 'patchVersion' field.
	 *
	 * @param patchVersion  the field value to assign
	 */
	public void setPatchVersion(int patchVersion) {
		this.patchVersion = patchVersion;
	}

	/**
	 * Returns the value of the 'createDate' field.
	 *
	 * @return Date
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * Assigns the value of the 'createDate' field.
	 *
	 * @param createDate  the field value to assign
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * Returns the value of the 'commits' field.
	 *
	 * @return List<TRLibraryCommit>
	 */
	public List<TRLibraryCommit> getCommits() {
		return commits;
	}

	/**
	 * Assigns the value of the 'commits' field.
	 *
	 * @param commits  the field value to assign
	 */
	public void setCommits(List<TRLibraryCommit> commits) {
		this.commits = commits;
	}
	
}
