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

import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger Info object.
 */
public class SwaggerInfo {
	
	private String title;
	private String description;
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
	 * Returns the value of the 'description' field.
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Assigns the value of the 'description' field.
	 *
	 * @param description  the field value to assign
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * Returns the <code>JsonObject</code> representation of this Swagger
	 * model element.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		if (title != null) {
			json.addProperty( "title", title );
		}
		if (description != null) {
			json.addProperty( "description", description );
		}
		if (termsOfService != null) {
			json.addProperty( "termsOfService", termsOfService );
		}
		if (contact != null) {
			json.add( "contact", contact.toJson() );
		}
		if (license != null) {
			json.add( "license", license.toJson() );
		}
		if (version != null) {
			json.addProperty( "version", version );
		}
		return json;
	}

}
