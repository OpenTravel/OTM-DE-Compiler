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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opentravel.ns.ota2.release_v01_00.ReleaseStatus;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * An OTM release forms a stable baseline of OTM library content.  Releases and
 * individual release members can be assigned to a specific effective date.  OTM
 * libraries are divided into two categories - principal and referenced.  Principal
 * items are explicitly required for the release, and referenced items are those
 * libraries that must be included based on direct and indirect dependencies of
 * the principal items.
 */
public class Release {
	
	private static VersionScheme versionScheme;
	
	private URL releaseUrl;
	private String baseNamespace;
	private String name;
	private String version;
	private ReleaseStatus status;
	private String description;
	private List<ReleaseMember> principalMembers = new ArrayList<>();
	private List<ReleaseMember> referencedMembers = new ArrayList<>();
	private Date defaultEffectiveDate;
	private ReleaseCompileOptions compileOptions = new ReleaseCompileOptions();
	private Map<QName,QName> preferredFacets = new HashMap<>();
	
	/**
	 * Returns the URL location where this release's content is stored.
	 *
	 * @return URL
	 */
	public URL getReleaseUrl() {
		return releaseUrl;
	}
	
	/**
	 * Assigns the URL location where this release's content is stored.
	 *
	 * @param releaseUrl  the URL location to assign
	 */
	public void setReleaseUrl(URL releaseUrl) {
		this.releaseUrl = releaseUrl;
	}
	
	/**
	 * Returns the base namespace of the release.
	 *
	 * @return String
	 */
	public String getBaseNamespace() {
		return baseNamespace;
	}
	
	/**
	 * Assigns the base namespace of the release.
	 *
	 * @param baseNamespace  the base namespace URI to assign
	 */
	public void setBaseNamespace(String baseNamespace) {
		this.baseNamespace = baseNamespace;
	}
	
	/**
	 * The full namespace of the release (including the version identifier suffix).
	 * 
	 * @return String
	 */
	public String getNamespace() {
		return versionScheme.setVersionIdentifier( baseNamespace, version );
	}

	/**
	 * Returns the name of the release.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Assigns the name of the release.
	 *
	 * @param name  the name value to assign
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the version of the release.
	 *
	 * @return String
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Assigns the version of the release.
	 *
	 * @param version  the version identifier value to assign
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Returns the status of the release.
	 *
	 * @return ReleaseStatus
	 */
	public ReleaseStatus getStatus() {
		return status;
	}

	/**
	 * Assigns the status of the release.
	 *
	 * @param status  the status value to assign
	 */
	public void setStatus(ReleaseStatus status) {
		this.status = status;
	}

	/**
	 * Returns the description of the release.
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Assigns the description of the release.
	 *
	 * @param description  the description text to assign
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the principle members of the release.
	 *
	 * @return List<ReleaseMember>
	 */
	public List<ReleaseMember> getPrincipalMembers() {
		return principalMembers;
	}

	/**
	 * Returns the referenced members of the release.
	 *
	 * @return List<ReleaseMember>
	 */
	public List<ReleaseMember> getReferencedMembers() {
		return referencedMembers;
	}

	/**
	 * Returns an unmodifiable list of all principle and referenced members
	 * of this release.
	 *
	 * @return List<ReleaseMember>
	 */
	public List<ReleaseMember> getAllMembers() {
		List<ReleaseMember> allMembers = new ArrayList<>();
		
		allMembers.addAll( principalMembers );
		allMembers.addAll( referencedMembers );
		return Collections.unmodifiableList( allMembers );
	}
	
	/**
	 * Returns the default effective date of the release.
	 *
	 * @return Date
	 */
	public Date getDefaultEffectiveDate() {
		return defaultEffectiveDate;
	}

	/**
	 * Assigns the default effective date of the release.
	 *
	 * @param defaultEffectiveDate  the effective date to assign
	 */
	public void setDefaultEffectiveDate(Date defaultEffectiveDate) {
		this.defaultEffectiveDate = defaultEffectiveDate;
	}

	/**
	 * Returns the compiler options for the release.
	 *
	 * @return ReleaseCompileOptions
	 */
	public ReleaseCompileOptions getCompileOptions() {
		return compileOptions;
	}

	/**
	 * Assigns the compiler options for the release.
	 *
	 * @param compileOptions  the compiler options to assign
	 */
	public void setCompileOptions(ReleaseCompileOptions compileOptions) {
		this.compileOptions = compileOptions;
	}

	/**
	 * Returns the map of facet owner names to the names of their associated
	 * preferred facets.  This is used for example generation during the model
	 * compilation process.
	 *
	 * @return Map<QName,QName>
	 */
	public Map<QName, QName> getPreferredFacets() {
		return preferredFacets;
	}

	/**
	 * Assigns the value of the 'preferredFacets' field.
	 *
	 * @param preferredFacets  the field value to assign
	 */
	public void setPreferredFacets(Map<QName, QName> preferredFacets) {
		this.preferredFacets = preferredFacets;
	}
	
	/**
	 * Initialize the default version scheme.
	 */
	static {
		try {
			VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
			versionScheme = factory.getVersionScheme( factory.getDefaultVersionScheme() );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
