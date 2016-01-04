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

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.ComplexType;
import org.w3._2001.xmlschema.Element;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of
 * <code>SHARED</code> and a facet owner of type <code>TLChoiceObject</code>.
 * 
 * @author S. Livezey
 */
public class ChoiceObjectSharedFacetCodegenDelegate extends ChoiceObjectFacetCodegenDelegate {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet
     *            the source facet
     */
    public ChoiceObjectSharedFacetCodegenDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }

	/**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#hasSubstitutionGroupElement()
     */
    @Override
    protected boolean hasSubstitutionGroupElement() {
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
     */
    @Override
    public TLFacet getLocalBaseFacet() {
        return null; // No base type for choice object shared facets
    }

    /**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createSubstitutionGroupElement(org.opentravel.schemacompiler.model.TLAlias)
	 */
	@Override
	protected Element createSubstitutionGroupElement(TLAlias ownerAlias) {
		Element sgElement = super.createSubstitutionGroupElement(ownerAlias);
		
		sgElement.setAbstract( true );
		return sgElement;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createType()
	 */
	@Override
	protected Annotated createType() {
		Annotated type = super.createType();
		
		if (type instanceof ComplexType) {
			((ComplexType) type).setAbstract( true );
		}
		return type;
	}

	/**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
     */
    @Override
    public QName getExtensionPointElement() {
        TLChoiceObject choiceObject = (TLChoiceObject) getSourceFacet().getOwningEntity();
        SchemaDependency extensionPoint = SchemaDependency.getExtensionPointElement();
        QName extensionPointQName;
        
        // If we have choice facets, we must use the extension point element that differentiates
        // the shared facet from the choice facet extension points.
        for (TLFacet descendantFacet : choiceObject.getChoiceFacets()) {
        	if (declaresOrInheritsFacetContent( descendantFacet )) {
        		extensionPoint = SchemaDependency.getExtensionPointElement();
        		break;
        	}
        }
        
        extensionPointQName = extensionPoint.toQName();
        addCompileTimeDependency(extensionPoint);
        return extensionPointQName;
    }

}
