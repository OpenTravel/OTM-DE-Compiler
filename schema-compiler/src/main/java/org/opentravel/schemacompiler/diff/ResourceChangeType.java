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
 * Enumeration of the possible types of changes that can be discovered when comparing two OTM resources.
 */
public enum ResourceChangeType {

    // TLResource changes
    NAME_CHANGED,
    DOCUMENTATION_CHANGED,
    BASE_PATH_CHANGED,
    ABSTRACT_IND_CHANGED,
    FIRST_CLASS_IND_CHANGED,
    EXTENSION_CHANGED,
    EXTENSION_VERSION_CHANGED,
    BUSINESS_OBJECT_REF_CHANGED,
    BUSINESS_OBJECT_REF_VERSION_CHANGED,
    PARENT_REF_ADDED,
    PARENT_REF_DELETED,
    PARENT_REF_CHANGED,
    PARAM_GROUP_ADDED,
    PARAM_GROUP_DELETED,
    PARAM_GROUP_CHANGED,
    ACTION_FACET_ADDED,
    ACTION_FACET_DELETED,
    ACTION_FACET_CHANGED,
    ACTION_ADDED,
    ACTION_DELETED,
    ACTION_CHANGED,

    // TLResourceParentRef changes
    PATH_TEMPLATE_CHANGED,
    PARENT_PARAM_GROUP_CHANGED,

    // TLParamGroup changes
    FACET_REF_CHANGED,
    PARAMETER_ADDED,
    PARAMETER_CHANGED,
    PARAMETER_DELETED,

    // TLParameter changes
    LOCATION_CHANGED,
    EQUIVALENT_ADDED,
    EQUIVALENT_DELETED,
    EXAMPLE_ADDED,
    EXAMPLE_DELETED,

    // TLActionFacet changes (included under EntityChangeType)

    // TLAction changes
    COMMON_ACTION_IND_CHANGED,
    REQUEST_METHOD_CHANGED,
    REQUEST_PARAM_GROUP_CHANGED,
    REQUEST_PATH_TEMPLATE_CHANGED,
    REQUEST_PAYLOAD_TYPE_CHANGED,
    REQUEST_PAYLOAD_TYPE_VERSION_CHANGED,
    REQUEST_MIME_TYPE_ADDED,
    REQUEST_MIME_TYPE_DELETED,
    RESPONSE_ADDED,
    RESPONSE_DELETED,
    RESPONSE_CHANGED,

    // TLActionResponse changes
    STATUS_CODE_ADDED,
    STATUS_CODE_DELETED,
    RESPONSE_PAYLOAD_TYPE_CHANGED,
    RESPONSE_PAYLOAD_TYPE_VERSION_CHANGED,
    RESPONSE_MIME_TYPE_ADDED,
    RESPONSE_MIME_TYPE_DELETED,

}
