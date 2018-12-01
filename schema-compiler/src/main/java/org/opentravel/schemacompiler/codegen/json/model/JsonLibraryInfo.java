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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonObject;

/**
 * Provides JSON schema documentation about the OTM library from which the schema
 * was generated.
 */
public class JsonLibraryInfo {
	
	private DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
	// 2015-12-01T17:20:42.144-06:00
	
    private String projectName;
    private String resourceName;
    private String libraryName;
    private String libraryVersion;
    private String libraryStatus;
    private String sourceFile;
    private String compilerVersion;
    private Date compileDate;
    
	/**
	 * Returns the name of the OTM project being compiled.
	 *
	 * @return String
	 */
	public String getProjectName() {
		return projectName;
	}
	
	/**
	 * Assigns the name of the OTM project being compiled.
	 *
	 * @param projectName  the OTM project name to assign
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	/**
	 * Returns the value of the 'resourceName' field.
	 *
	 * @return String
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * Assigns the value of the 'resourceName' field.
	 *
	 * @param resourceName  the field value to assign
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * Returns the name of the OTM library being compiled.
	 *
	 * @return String
	 */
	public String getLibraryName() {
		return libraryName;
	}
	
	/**
	 * Assigns the name of the OTM library being compiled.
	 *
	 * @param libraryName  the OTM library name to assign
	 */
	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}
	
	/**
	 * Returns the version of the OTM library being compiled.
	 *
	 * @return String
	 */
	public String getLibraryVersion() {
		return libraryVersion;
	}
	/**
	 * Assigns the version of the OTM library being compiled.
	 *
	 * @param libraryVersion  the version identifier to assign
	 */
	public void setLibraryVersion(String libraryVersion) {
		this.libraryVersion = libraryVersion;
	}
	
	/**
	 * Returns the status of the OTM library being compiled.
	 *
	 * @return String
	 */
	public String getLibraryStatus() {
		return libraryStatus;
	}
	
	/**
	 * Assigns the status of the OTM library being compiled.
	 *
	 * @param libraryStatus  the library status to assign
	 */
	public void setLibraryStatus(String libraryStatus) {
		this.libraryStatus = libraryStatus;
	}
	
	/**
	 * Returns the name of the OTM source file being compiled.
	 *
	 * @return String
	 */
	public String getSourceFile() {
		return sourceFile;
	}
	
	/**
	 * Assigns the name of the OTM source file being compiled.
	 *
	 * @param sourceFile  the source filename to assign
	 */
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	/**
	 * Returns the version of the OTM compiler that generated the JSON schema.
	 *
	 * @return String
	 */
	public String getCompilerVersion() {
		return compilerVersion;
	}
	
	/**
	 * Assigns the version of the OTM compiler that generated the JSON schema.
	 *
	 * @param compilerVersion  the OTM compiler version to assign
	 */
	public void setCompilerVersion(String compilerVersion) {
		this.compilerVersion = compilerVersion;
	}
	
	/**
	 * Returns the date/time that the JSON schema was generated.
	 *
	 * @return Date
	 */
	public Date getCompileDate() {
		return compileDate;
	}
	
	/**
	 * Assigns the date/time that the JSON schema was generated.
	 *
	 * @param compileDate  the date/time value to assign
	 */
	public void setCompileDate(Date compileDate) {
		this.compileDate = compileDate;
	}
    
	/**
	 * Returns this instance as a <code>JsonObject</code>.
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJson() {
		JsonObject libraryInfo = new JsonObject();
		
		if (projectName != null) {
			libraryInfo.addProperty( "ProjectName", projectName );
		}
		if (resourceName != null) {
			libraryInfo.addProperty( "ResourceName", resourceName );
		}
		if (libraryName != null) {
			libraryInfo.addProperty( "LibraryName", libraryName );
		}
		if (libraryVersion != null) {
			libraryInfo.addProperty( "LibraryVersion", libraryVersion );
		}
		if (libraryStatus != null) {
			libraryInfo.addProperty( "LibraryStatus", libraryStatus );
		}
		if (sourceFile != null) {
			libraryInfo.addProperty( "SourceFile", sourceFile );
		}
		if (compilerVersion != null) {
			libraryInfo.addProperty( "CompilerVersion", compilerVersion );
		}
		if (compileDate != null) {
			libraryInfo.addProperty( "CompileDate", dateFormat.format( compileDate ) );
		}
		return libraryInfo;
	}
	
}
