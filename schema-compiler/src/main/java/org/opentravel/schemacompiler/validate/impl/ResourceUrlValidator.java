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
package org.opentravel.schemacompiler.validate.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.UrlValidator;

/**
 * Verifies the format of resource base URL's and path templates.
 */
public class ResourceUrlValidator extends UrlValidator {

	private static final long serialVersionUID = -3674643870101980887L;
	private static final Pattern pathParamPattern = Pattern.compile( "(?:\\{(.+?)\\})" );
	
	private boolean allowPathParams;
	
	/**
	 * Default constructor (path parameters not allowed).
	 */
	public ResourceUrlValidator() {
		this( false );
	}
	
	/**
	 * Constructor that indicates whether path parameters are allowed during validation.
	 * 
	 * @param allowPathParams  flag indicating whether to allow path parameters
	 */
	public ResourceUrlValidator(boolean allowPathParams) {
		this.allowPathParams = allowPathParams;
	}
	
	/**
	 * @see org.apache.commons.validator.routines.UrlValidator#isValidPath(java.lang.String)
	 */
	@Override
	public boolean isValidPath(String path) {
		String pathToCheck;
		
		if (allowPathParams && (path != null)) {
			pathToCheck = pathParamPattern.matcher( path ).replaceAll("a");
			
		} else {
			pathToCheck = path;
		}
		return super.isValidPath(pathToCheck) && (pathToCheck.indexOf('?') < 0);
	}

	/**
	 * @see org.apache.commons.validator.routines.UrlValidator#isValidQuery(java.lang.String)
	 */
	@Override
	protected boolean isValidQuery(String query) {
		return (query == null) || (query.length() == 0);
	}
	
	/**
	 * Returns the path parameters from the given URL.  If no path parameters are declared,
	 * an empty list will be returned.
	 * 
	 * @param url  the URL (or path fragment) to be analyzed
	 * @return List<String>
	 */
	public List<String> getPathParameters(String url) {
		List<String> pathParameters = new ArrayList<>();
		
		if (url != null) {
			Matcher m = pathParamPattern.matcher( url );
			
			while (m.find()) {
				pathParameters.add( m.group( 1 ) );
			}
		}
		return pathParameters;
	}
	
}
