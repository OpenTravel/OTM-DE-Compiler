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

package org.opentravel.schemacompiler.diff;

/**
 * Enumeration of the possible types of changes that can be discovered when comparing
 * two OTM fields.
 */
public enum FieldChangeType {
	
	MEMBER_TYPE_CHANGED,
	OWNING_FACET_CHANGED,
	TYPE_CHANGED,
	CARDINALITY_CHANGE,
	CHANGED_TO_MANDATORY,
	CHANGED_TO_OPTIONAL,
	CHANGED_TO_REFERENCE,
	CHANGED_TO_NON_REFERENCE,
	DOCUMENTATION_CHANGED,
	EQUIVALENT_ADDED,
	EQUIVALENT_DELETED,
	EXAMPLE_ADDED,
	EXAMPLE_DELETED,
	
}
