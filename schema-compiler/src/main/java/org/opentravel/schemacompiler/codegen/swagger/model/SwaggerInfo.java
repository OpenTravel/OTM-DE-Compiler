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
package org.opentravel.schemacompiler.codegen.swagger.model;

import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.model.JsonContextualValue;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentation;
import org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner;
import org.opentravel.schemacompiler.codegen.json.model.JsonLibraryInfo;

import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger Info object.
 */
public class SwaggerInfo implements JsonDocumentationOwner {
	
	private String title;
	private JsonLibraryInfo libraryInfo;
	private JsonDocumentation documentation;
	private String termsOfService;
	private SwaggerContact contact;
	private SwaggerLicense license;
	private String version;
	
	/**
	 * Returns the value of the 'title' field.
	 *
	 * @return String
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Assigns the value of the 'title' field.
	 *
	 * @param title  the field value to assign
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Returns the value of the 'libraryInfo' field.
	 *
	 * @return JsonLibraryInfo
	 */
	public JsonLibraryInfo getLibraryInfo() {
		return libraryInfo;
	}
	
	/**
	 * Assigns the value of the 'libraryInfo' field.
	 *
	 * @param libraryInfo  the field value to assign
	 */
	public void setLibraryInfo(JsonLibraryInfo libraryInfo) {
		this.libraryInfo = libraryInfo;
	}
	
	/**
	 * Returns the value of the 'documentation' field.
	 *
	 * @return JsonDocumentation
	 */
	public JsonDocumentation getDocumentation() {
		return documentation;
	}
	
	/**
	 * Assigns the value of the 'documentation' field.
	 *
	 * @param documentation  the field value to assign
	 */
	public void setDocumentation(JsonDocumentation documentation) {
		this.documentation = documentation;
	}
	
	/**
	 * Returns the value of the 'termsOfService' field.
	 *
	 * @return String
	 */
	public String getTermsOfService() {
		return termsOfService;
	}
	
	/**
	 * Assigns the value of the 'termsOfService' field.
	 *
	 * @param termsOfService  the field value to assign
	 */
	public void setTermsOfService(String termsOfService) {
		this.termsOfService = termsOfService;
	}
	
	/**
	 * Returns the value of the 'contact' field.
	 *
	 * @return SwaggerContact
	 */
	public SwaggerContact getContact() {
		return contact;
	}
	
	/**
	 * Assigns the value of the 'contact' field.
	 *
	 * @param contact  the field value to assign
	 */
	public void setContact(SwaggerContact contact) {
		this.contact = contact;
	}
	
	/**
	 * Returns the value of the 'license' field.
	 *
	 * @return SwaggerLicense
	 */
	public SwaggerLicense getLicense() {
		return license;
	}
	
	/**
	 * Assigns the value of the 'license' field.
	 *
	 * @param license  the field value to assign
	 */
	public void setLicense(SwaggerLicense license) {
		this.license = license;
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
	 * @see org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner#getEquivalentItems()
	 */
	@Override
	public List<JsonContextualValue> getEquivalentItems() {
		return Collections.emptyList();
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.json.model.JsonDocumentationOwner#getExampleItems()
	 */
	@Override
	public List<JsonContextualValue> getExampleItems() {
		return Collections.emptyList();
	}

	/**
	 * Returns the <code>JsonObject</code> representation of this Swagger
	 * model element.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		json.addProperty( "title", (title != null) ? title : "unknown" );
		
		if (libraryInfo != null) {
			json.add( "x-otm-library", libraryInfo.toJson() );
		}
		
		JsonSchemaCodegenUtils.createOtmAnnotations( json, this );
		
		if (termsOfService != null) {
			json.addProperty( "termsOfService", termsOfService );
		}
		if (contact != null) {
			json.add( "contact", contact.toJson() );
		}
		if (license != null) {
			json.add( "license", license.toJson() );
		}
		json.addProperty( "version", (version != null) ? version : "unknown" );
		return json;
	}

}
