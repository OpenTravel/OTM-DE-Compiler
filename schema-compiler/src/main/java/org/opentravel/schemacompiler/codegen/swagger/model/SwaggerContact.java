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
 * Class that defines the meta-model for a Swagger Contact object.
 */
public class SwaggerContact {
	
	private String name;
	private String url;
	private String email;
	
	/**
	 * Returns the value of the 'name' field.
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Assigns the value of the 'name' field.
	 *
	 * @param name  the field value to assign
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the value of the 'url' field.
	 *
	 * @return String
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * Assigns the value of the 'url' field.
	 *
	 * @param url  the field value to assign
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Returns the value of the 'email' field.
	 *
	 * @return String
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Assigns the value of the 'email' field.
	 *
	 * @param email  the field value to assign
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Returns the <code>JsonObject</code> representation of this Swagger
	 * model element.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		
		if (name != null) {
			json.addProperty( "name", name );
		}
		if (url != null) {
			json.addProperty( "url", url );
		}
		if (email != null) {
			json.addProperty( "email", email );
		}
		return json;
	}

}
