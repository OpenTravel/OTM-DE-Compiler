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

package org.opentravel.schemacompiler.mvn;

/**
 * Specifies the key identity attributes of a managed OTM release.
 */
public class Release {
	
	private String baseNamespace;
	private String filename;
	private String version;
	
	/**
	 * Returns the base namespace of the managed OTM release.
	 *
	 * @return String
	 */
	public String getBaseNamespace() {
		return baseNamespace;
	}
	
	/**
	 * Assigns the base namespace of the managed OTM release.
	 *
	 * @param baseNamespace  the field value to assign
	 */
	public void setBaseNamespace(String baseNamespace) {
		this.baseNamespace = baseNamespace;
	}
	
	/**
	 * Returns the filename of the managed OTM release.
	 *
	 * @return String
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * Assigns the filename of the managed OTM release.
	 *
	 * @param filename  the field value to assign
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	/**
	 * Returns the version of the managed OTM release.
	 *
	 * @return String
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * Assigns the version of the managed OTM release.
	 *
	 * @param version  the field value to assign
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
}
