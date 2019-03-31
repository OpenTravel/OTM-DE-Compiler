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
 * Library definition for closed enumeration types.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumeration extends TLAbstractEnumeration implements TLAttributeType {

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        StringBuilder identity = new StringBuilder();

        if (owningLibrary != null) {
            identity.append( owningLibrary.getValidationIdentity() ).append( " : " );
        }
        if (getName() == null) {
            identity.append( "[Unnamed Closed Enumeration Type]" );
        } else {
            identity.append( getName() );
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
     */
    @Override
    public XSDFacetProfile getXSDFacetProfile() {
        return XSDFacetProfile.FP_STRING;
    }

}
