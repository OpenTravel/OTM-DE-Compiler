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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * Data transfer object representing an OTM library with one or more versions.
 */
@Entity
@Table( name = "library" )
public class TRLibrary implements Serializable {
	
	private static final long serialVersionUID = -6719356561574789203L;

	@Id
	@Column( name = "id", nullable = false )
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id = -1L;
	
	@Column( name = "library_name", nullable = false, length = 50 )
	private String libraryName;
	
	@Column( name = "base_namespace", nullable = false, length = 200 )
	private String baseNamespace;
	
	@Column( name = "create_date", nullable = false )
	private Date createDate;
	
	@OneToMany( mappedBy = "library", fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE } )
	@OrderBy( "majorVersion DESC, minorVersion DESC, patchVersion DESC" )
	private List<TRLibraryVersion> versions;

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
	 * Returns the value of the 'libraryName' field.
	 *
	 * @return String
	 */
	public String getLibraryName() {
		return libraryName;
	}

	/**
	 * Assigns the value of the 'libraryName' field.
	 *
	 * @param libraryName  the field value to assign
	 */
	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	/**
	 * Returns the value of the 'baseNamespace' field.
	 *
	 * @return String
	 */
	public String getBaseNamespace() {
		return baseNamespace;
	}

	/**
	 * Assigns the value of the 'baseNamespace' field.
	 *
	 * @param baseNamespace  the field value to assign
	 */
	public void setBaseNamespace(String baseNamespace) {
		this.baseNamespace = baseNamespace;
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
	 * Returns the value of the 'versions' field.
	 *
	 * @return List<TRLibraryVersion>
	 */
	public List<TRLibraryVersion> getVersions() {
		return versions;
	}

	/**
	 * Assigns the value of the 'versions' field.
	 *
	 * @param versions  the field value to assign
	 */
	public void setVersions(List<TRLibraryVersion> versions) {
		this.versions = versions;
	}
	
}
