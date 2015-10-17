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
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLFacet;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of
 * <code>CHOICE</code> and a facet owner of type <code>TLChoiceObject</code>.
 * 
 * @author S. Livezey
 */
public class ChoiceObjectChoiceFacetCodegenDelegate extends ChoiceObjectFacetCodegenDelegate {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet
     *            the source facet
     */
    public ChoiceObjectChoiceFacetCodegenDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
     */
    @Override
    public TLFacet getLocalBaseFacet() {
        TLFacet sourceFacet = getSourceFacet();
        TLFacet baseFacet = null;

        if (sourceFacet.getOwningEntity() instanceof TLChoiceObject) {
            TLChoiceObject choiceObject = (TLChoiceObject) sourceFacet.getOwningEntity();
            
            baseFacet = choiceObject.getSharedFacet();
        }
        return baseFacet;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
     */
    @Override
    public QName getExtensionPointElement() {
    	TLBusinessObject bo = (TLBusinessObject) getSourceFacet().getOwningEntity();
        SchemaDependency extensionPoint;
        QName extensionPointQName;
        
        if (declaresOrInheritsFacetContent( bo.getSummaryFacet() )) {
        	// TODO: Need an ExtensionPoint_Shared element
        	extensionPoint = SchemaDependency.getExtensionPointCustomElement();
        	
        } else {
        	extensionPoint = SchemaDependency.getExtensionPointElement();
        }
        extensionPointQName = extensionPoint.toQName();
        addCompileTimeDependency(extensionPoint);
        return extensionPointQName;
    }

}