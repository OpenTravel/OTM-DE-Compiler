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
package org.opentravel.schemacompiler.ioc;

/**
 * Specifies the location and file format for a <code>SchemaDeclaration</code>.
 */
public class SchemaLocation {
	
	private String format;
	private String location;
	
	/**
	 * Default constructor.
	 */
	public SchemaLocation() {}
	
	/**
	 * Full constructor.
	 * 
	 * @param format  the file format of the schema
	 * @param location  the location of the schema
	 */
	public SchemaLocation(String format, String location) {
		this.format = format;
		this.location = location;
	}
	
	/**
	 * Returns the file format of the schema.  As a convention, this value should match one
	 * of the target format constants defined in the <code>CodeGeneratorFactory</code> class.
	 *
	 * @see org.opentravel.schemacompiler.codegen.CodeGeneratorFactory
	 * @return String
	 */
	public String getFormat() {
		return format;
	}
	
	/**
	 * Assigns the file format of the schema.  As a convention, this value should match one
	 * of the target format constants defined in the <code>CodeGeneratorFactory</code> class.
	 *
	 * @see org.opentravel.schemacompiler.codegen.CodeGeneratorFactory
	 * @param format  the schema file format value to assign
	 */
	public void setFormat(String format) {
		this.format = format;
	}
	
	/**
	 * Returns the location of the schema.
	 *
	 * @return String
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * Assigns the location of the schema.
	 *
	 * @param location  the schema location value to assign
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	
}
