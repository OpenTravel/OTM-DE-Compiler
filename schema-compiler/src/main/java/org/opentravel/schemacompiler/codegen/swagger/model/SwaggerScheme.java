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

/**
 * Enumeration that specifies the transfer protocols supported for a Swagger
 * API specification document.  The schemes that are defined for this
 * enumeration are the only values allowed by the Swagger specification.
 */
public enum SwaggerScheme {
	
	HTTP( "http" ),
	HTTPS( "https" ),
	WS( "ws" ),
	WSS( "wss" );
	
	private String displayValue;
	
	/**
	 * Constructor that specifies the display value for the enumeration.
	 * 
	 * @param displayValue  the display value of the enumeration
	 */
	private SwaggerScheme(String displayValue) {
		this.displayValue = displayValue;
	}
	
	/**
	 * Returns the display value of the enumeration.
	 * 
	 * @return String
	 */
	public String getDisplayValue() {
		return displayValue;
	}
	
	/**
	 * Returns the correct enumeration for the given display value or null
	 * if an invalid scheme is provided.
	 * 
	 * @param displayValue  the display value of the scheme to return
	 * @return SwaggerScheme
	 */
	public static SwaggerScheme fromDisplayValue(String displayValue) {
		SwaggerScheme scheme = null;
		
		if (displayValue != null) {
			for (SwaggerScheme s : values()) {
				if (displayValue.equalsIgnoreCase( s.getDisplayValue() )) {
					scheme = s;
					break;
				}
			}
		}
		return scheme;
	}
	
}
