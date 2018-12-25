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

/**
 * Interface to be implemented by model objects that can represent a
 * named property of a JSON object.
 */
public interface JsonNamedProperty {
	
	/**
	 * Returns the name of the JSON property.
	 * 
	 * @return String
	 */
	public String getPropertyName();
	
	/**
	 * Returns the value of the JSON property.
	 * 
	 * @return JsonModelObject
	 */
	public JsonModelObject getPropertyValue();
	
}
