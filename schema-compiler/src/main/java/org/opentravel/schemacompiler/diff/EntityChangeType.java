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
 * Enumeration of the possible types of changes that can be discovered when comparing two OTM entities.
 */
public enum EntityChangeType {

    ENTITY_TYPE_CHANGED,
    NAME_CHANGED,
    PARENT_TYPE_CHANGED,
    PARENT_TYPE_VERSION_CHANGED,
    EXTENSION_CHANGED,
    EXTENSION_VERSION_CHANGED,
    BASE_PAYLOAD_CHANGED,
    BASE_PAYLOAD_VERSION_CHANGED,
    REFERENCE_TYPE_CHANGED,
    REFERENCE_FACET_CHANGED,
    REFERENCE_REPEAT_CHANGED,
    SIMPLE_CORE_TYPE_CHANGED,
    SIMPLE_CORE_TYPE_VERSION_CHANGED,
    ALIAS_ADDED,
    ALIAS_DELETED,
    FACET_ADDED,
    FACET_DELETED,
    ROLE_ADDED,
    ROLE_DELETED,
    MEMBER_FIELD_ADDED,
    MEMBER_FIELD_DELETED,
    MEMBER_FIELD_CHANGED,
    ENUM_VALUE_ADDED,
    ENUM_VALUE_DELETED,
    CHANGED_TO_SIMPLE_LIST,
    CHANGED_TO_SIMPLE_NON_LIST,
    PATTERN_CONSTRAINT_CHANGED,
    MIN_LENGTH_CONSTRAINT_CHANGED,
    MAX_LENGTH_CONSTRAINT_CHANGED,
    FRACTION_DIGITS_CONSTRAINT_CHANGED,
    TOTAL_DIGITS_CONSTRAINT_CHANGED,
    MIN_INCLUSIVE_CONSTRAINT_CHANGED,
    MAX_INCLUSIVE_CONSTRAINT_CHANGED,
    MIN_EXCLUSIVE_CONSTRAINT_CHANGED,
    MAX_EXCLUSIVE_CONSTRAINT_CHANGED,
    DOCUMENTATION_CHANGED,
    EQUIVALENT_ADDED,
    EQUIVALENT_DELETED,
    EXAMPLE_ADDED,
    EXAMPLE_DELETED,

}
