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
package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.model.TLFacet;

/**
 * Defines types of RAS operations for which WSDL operations may be generated, as well as the naming
 * scheme for those operations and their RQ/RS elements.
 * 
 * @author S. Livezey
 */
public enum RASOperationType {

    GET("Get"), CREATE("Create"), UPDATE("Update"), DELETE("Delete"), FIND("Find");

    private String operationName;

    /**
     * Constructor that specifies the name of the operation as it will appear in generated XML
     * schema docuemtns.
     * 
     * @param operationName
     *            the name of the operation
     */
    private RASOperationType(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Returns the name of the WSDL operation the specified business object.
     * 
     * @param targetFacet
     *            the target business object facet for the operation
     * @return String
     */
    public String getOperationName(TLFacet targetFacet) {
        StringBuilder opName = new StringBuilder(operationName).append("_").append(
                targetFacet.getOwningEntity().getLocalName());

        if (targetFacet.getFacetType().isContextual()) {
            opName.append("_").append(
                    targetFacet.getFacetType().getIdentityName(targetFacet.getContext(),
                            targetFacet.getLabel()));
        }
        return opName.toString();
    }

    /**
     * Returns the name of the request element for the operation type on the specified business
     * object.
     * 
     * @param targetFacet
     *            the target business object facet for the operation
     * @return String
     */
    public String getRequestElementName(TLFacet targetFacet) {
        return getOperationName(targetFacet) + "RQ";
    }

    /**
     * Returns the name of the response element for the operation type on the specified business
     * object.
     * 
     * @param targetFacet
     *            the target business object facet for the operation
     * @return String
     */
    public String getResponseElementName(TLFacet targetFacet) {
        return getOperationName(targetFacet) + "RS";
    }

}
