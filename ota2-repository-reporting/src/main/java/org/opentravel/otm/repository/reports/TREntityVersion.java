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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Data transfer object representing a single version of an OTM entity.
 */
@Entity
@Table( name = "entity_version" )
public class TREntityVersion implements Serializable {
	
	private static final long serialVersionUID = -4138523307505635474L;

	@Id
	@Column( name = "id", nullable = false )
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id = -1L;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "entity_id" )
	private TREntity entity;
	
	@ManyToOne( fetch = FetchType.LAZY )
	@JoinColumn( name = "library_version_id" )
	private TRLibraryVersion libraryVersion;
	
	@Column( name = "create_date", nullable = false )
	private Date createDate;
	
	@Column( name = "delete_date", nullable = true )
	private Date deleteDate;
	
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
	 * Returns the value of the 'entity' field.
	 *
	 * @return TREntity
	 */
	public TREntity getEntity() {
		return entity;
	}

	/**
	 * Assigns the value of the 'entity' field.
	 *
	 * @param entity  the field value to assign
	 */
	public void setEntity(TREntity entity) {
		this.entity = entity;
	}

	/**
	 * Returns the value of the 'libraryVersion' field.
	 *
	 * @return TRLibraryVersion
	 */
	public TRLibraryVersion getLibraryVersion() {
		return libraryVersion;
	}

	/**
	 * Assigns the value of the 'libraryVersion' field.
	 *
	 * @param libraryVersion  the field value to assign
	 */
	public void setLibraryVersion(TRLibraryVersion libraryVersion) {
		this.libraryVersion = libraryVersion;
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

}
