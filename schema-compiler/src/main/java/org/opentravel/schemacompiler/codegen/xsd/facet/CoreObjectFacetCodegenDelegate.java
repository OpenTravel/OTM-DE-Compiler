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
package org.opentravel.schemacompiler.codegen.xsd.facet;

import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Attribute;

/**
 * Base class for facet code generation delegates used to generate code artifacts for
 * <code>TLFacet</code model elements that are owned by <code>TLCoreObject</code> instances.
 * 
 * @author S. Livezey
 */
public abstract class CoreObjectFacetCodegenDelegate extends TLFacetCodegenDelegate {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet
     *            the source facet
     */
    public CoreObjectFacetCodegenDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
     */
    @Override
    public TLFacet getLocalBaseFacet() {
        return null;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
     */
    @Override
    public QName getExtensionPointElement() {
        return null;
    }
    
    /**
     * Returns true if role attributes should be created for this facet.  Default
     * value is true; sub-classes may override to supress the creation of role
     * attributes.
     * 
     * @return boolean
     */
    protected boolean createRoleAttributes() {
    	return true;
    }

    /**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createJaxbAttributes(java.util.List, java.util.List)
	 */
	@Override
	protected List<Annotated> createJaxbAttributes(List<TLAttribute> attributeList, List<TLIndicator> indicatorList) {
        List<Annotated> jaxbAttributes = super.createJaxbAttributes(attributeList, indicatorList);

        if (createRoleAttributes() && (getLocalBaseFacet() == null)) {
            TLCoreObject owner = (TLCoreObject) getSourceFacet().getOwningEntity();

            while (owner != null) {
                TLCoreObject ownerExtension = (TLCoreObject) FacetCodegenUtils
                        .getFacetOwnerExtension(owner);

                if (owner.getRoleEnumeration().getRoles().size() > 0) {
                    Attribute roleAttr = new Attribute();

                    if (ownerExtension != null) {
                        roleAttr.setName(
                        		XsdCodegenUtils.getRoleAttributeName( owner.getLocalName() ) );
                        
                    } else {
                        roleAttr.setName("role");
                    }
                    roleAttr.setType(new QName(owner.getNamespace(), owner.getRoleEnumeration()
                            .getLocalName() + "_Base"));
                    jaxbAttributes.add(roleAttr);
                }
                owner = ownerExtension;
            }
        }
        return jaxbAttributes;
    }
    
}
