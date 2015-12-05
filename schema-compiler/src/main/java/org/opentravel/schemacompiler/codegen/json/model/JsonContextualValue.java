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
package org.opentravel.schemacompiler.codegen.json.model;

import com.google.gson.JsonObject;

/**
 * Represents a single contextual value within the JSON schema documentation.
 */
public class JsonContextualValue {
	
	private String context;
	private String value;
	
	/**
	 * Returns the context name associated with the value.
	 *
	 * @return String
	 */
	public String getContext() {
		return context;
	}
	
	/**
	 * Assigns the context name associated with the value.
	 *
	 * @param context  the context name to assign
	 */
	public void setContext(String context) {
		this.context = context;
	}
	
	/**
	 * Returns the contextual value.
	 *
	 * @return String
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Assigns the contextual value.
	 *
	 * @param value  the contextual value to assign
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Returns the <code>JsonObject</code> representation of this contextual value.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject jsonObj = new JsonObject();
		
		jsonObj.addProperty( "context", context );
		jsonObj.addProperty( "value", value );
		return jsonObj;
	}
	
}
