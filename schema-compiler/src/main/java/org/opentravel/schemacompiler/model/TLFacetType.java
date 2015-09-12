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
package org.opentravel.schemacompiler.model;

/**
 * Enumeration used to specify the type of a facet.
 * 
 * @author S. Livezey
 */
public enum TLFacetType {

    /** The ID facet of a business object. */
    ID("ID", false),

    /** The summary facet of a core or business object. */
    SUMMARY("Summary", false),

    /** The detail facet of a core or business object. */
    DETAIL("Detail", false),

    /** A custom facet of a business object. */
    CUSTOM("Custom", true),

    /** The simple facet of a core object. */
    SIMPLE("Simple", false),

    /** The query facet of a business object. */
    QUERY("Query", true),

    /** The request facet of a service operation. */
    REQUEST("RQ", false),

    /** The response facet of a service operation. */
    RESPONSE("RS", false),

    /** The notification facet of a service operation. */
    NOTIFICATION("Notif", false),

    /** The action facet of a REST resource. */
    ACTION("Action", false),

    /** The shared facet of a choice object. */
    SHARED("Shared", false),

    /** The choice facet of a choice object. */
    CHOICE("Choice", false);

    private String identityName;
    private boolean contextual;

    /**
     * Constructor that specifies the display name of the facet type.
     * 
     * @param identityName
     *            the identity string for the facet type (used for name-resolution purposes)
     * @param contextual
     *            indicates that the facet type should be considered contextual in nature
     */
    private TLFacetType(String identityName, boolean contextual) {
        this.identityName = identityName;
        this.contextual = contextual;
    }

    /**
     * Returns the identity name of the facet type.
     * 
     * @return String
     */
    public String getIdentityName() {
        return identityName;
    }

    /**
     * Returns the identity name of the facet type, or the given context/label values in the case of
     * a contextual facet.
     * 
     * @param facetContext
     *            the context value of a contextual facet
     * @param facetLabel
     *            the label value of a contextual facet
     * @return String
     */
    public String getIdentityName(String facetContext, String facetLabel) {
        StringBuilder identity = new StringBuilder();

        if (!contextual) {
            identity.append(identityName);

        } else {
            if (this == TLFacetType.QUERY) {
                identity.append(identityName);

                if ((facetLabel != null) && (facetLabel.length() > 0)) {
                    identity.append("_").append(facetLabel);

                } else if ((facetContext != null) && (facetContext.length() > 0)) {
                    identity.append("_").append(facetContext);
                }
            } else { // custom facet type
                if ((facetLabel != null) && (facetLabel.length() > 0)) {
                    identity.append(facetLabel);

                } else if ((facetContext != null) && (facetContext.length() > 0)) {
                    identity.append(facetContext);
                }
            }
        }
        return identity.toString();
    }

    /**
     * Returns true if the facet type should be considered contextual in nature.
     * 
     * @return boolean
     */
    public boolean isContextual() {
        return contextual;
    }

}
