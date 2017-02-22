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

package org.opentravel.exampleupgrade;

/**
 * Used to specify the type of match between an existing example DOM element
 * and an associated OTM model entity or field.
 */
public enum ExampleMatchType {
	
	/**
	 * Indicates that an exact match was found between the OTM entity/field and the
	 * DOM element in the existing DOM document.  Exact matches exist under
	 * the following conditions:
	 * <ul>
	 *   <li>XML Elements - both the namespace and local names of the DOM element exactly match that of the OTM model.</li>
	 *   <li>JSON Elements - the local name of the DOM element matches that of the OTM model.</li>
	 *   <li>XML/JSON Attributes &amp; Indicators - the name of the attribute or indicator matches that of the OTM model.</li>
	 * </ul>
	 */
	EXACT,
	
	/**
	 * Indicates that an partial match was found between the OTM entity/field and the
	 * DOM element in the existing DOM document.  Partial matches exist under the
	 * following conditions:
	 * <ul>
	 *   <li>XML Elements - the base namespaces (version independent) and local names of the DOM element exactly match that of the OTM model.</li>
	 *   <li>JSON Elements - due to the lack of a namespace designation, a partial match for JSON examples is not possible</li>
	 *   <li>XML/JSON Attributes &amp; Indicators - the name of the attribute or indicator matches that of the OTM model.</li>
	 * </ul>
	 */
	PARTIAL,
	
	/**
	 * Indicates that no associated DOM element could be identified for the OTM model
	 * element/field. 
	 */
	NONE
	
}
