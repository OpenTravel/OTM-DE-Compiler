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
 * Data transfer object representing an OTM entity with one or more versions.
 */
@Entity
@Table( name = "entity" )
public class TREntity implements Serializable {
	
	private static final long serialVersionUID = 7428309685826326606L;

	@Id
	@Column( name = "id", nullable = false )
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id = -1L;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "library_id" )
	private TRLibrary library;
	
	@Column( name = "entity_name", nullable = false, length = 200 )
	private String entityName;
	
	@Column( name = "entity_type", nullable = false, length = 50 )
	private String entityType;
	
	@Column( name = "create_date", nullable = false )
	private Date createDate;
	
	@Column( name = "delete_date", nullable = true )
	private Date deleteDate;
	
	@OneToMany( mappedBy = "entity", fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE } )
	@OrderBy( "libraryVersion.majorVersion DESC, libraryVersion.minorVersion DESC, libraryVersion.patchVersion DESC" )
	private List<TREntityVersion> versions;

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
	 * Returns the value of the 'entityName' field.
	 *
	 * @return String
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * Assigns the value of the 'entityName' field.
	 *
	 * @param entityName  the field value to assign
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * Returns the value of the 'entityType' field.
	 *
	 * @return String
	 */
	public String getEntityType() {
		return entityType;
	}

	/**
	 * Assigns the value of the 'entityType' field.
	 *
	 * @param entityType  the field value to assign
	 */
	public void setEntityType(String entityType) {
		this.entityType = entityType;
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
	 * Returns the value of the 'deleteDate' field.
	 *
	 * @return Date
	 */
	public Date getDeleteDate() {
		return deleteDate;
	}

	/**
	 * Assigns the value of the 'deleteDate' field.
	 *
	 * @param deleteDate  the field value to assign
	 */
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	/**
	 * Returns the value of the 'versions' field.
	 *
	 * @return List<TREntityVersion>
	 */
	public List<TREntityVersion> getVersions() {
		return versions;
	}

	/**
	 * Assigns the value of the 'versions' field.
	 *
	 * @param versions  the field value to assign
	 */
	public void setVersions(List<TREntityVersion> versions) {
		this.versions = versions;
	}

}
