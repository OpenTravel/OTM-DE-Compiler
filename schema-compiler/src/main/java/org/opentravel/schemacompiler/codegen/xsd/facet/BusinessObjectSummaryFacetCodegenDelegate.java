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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of
 * <code>SUMMARY</code> and a facet owner of type <code>TLBusinessObject</code>.
 * 
 * @author S. Livezey
 */
public class BusinessObjectSummaryFacetCodegenDelegate extends BusinessObjectFacetCodegenDelegate {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet
     *            the source facet
     */
    public BusinessObjectSummaryFacetCodegenDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#hasNonSubstitutableElement()
     */
    @Override
    protected boolean hasNonSubstitutableElement() {
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
     */
    @Override
    public TLFacet getLocalBaseFacet() {
        TLFacet sourceFacet = getSourceFacet();
        TLFacet baseFacet = null;

        if (sourceFacet.getOwningEntity() instanceof TLBusinessObject) {
            FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory(
                    transformerContext);
            TLBusinessObject businessObject = (TLBusinessObject) sourceFacet.getOwningEntity();
            TLFacet parentFacet = businessObject.getIdFacet();

            if (factory.getDelegate(parentFacet).hasContent()) {
                baseFacet = parentFacet;
            }
        }
        return baseFacet;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
     */
    @Override
    public QName getExtensionPointElement() {
        SchemaDependency extensionPoint = SchemaDependency.getExtensionPointElement();
        List<TLFacet> descendantFacets = getDescendantFacets();
        QName extensionPointQName;
        
        // If we have multiple layers of inheritance below the ID facet, we must use the extension
        // point element that differentiates the summary facet from the custom/detail facet extension
        // points.
        for (TLFacet descendantFacet : descendantFacets) {
        	if (declaresOrInheritsFacetContent( descendantFacet )) {
        		extensionPoint = SchemaDependency.getExtensionPointSummaryElement();
        		break;
        	}
        }
        
        extensionPointQName = extensionPoint.toQName();
        addCompileTimeDependency(extensionPoint);
        return extensionPointQName;
    }
    
    /**
     * Returns the inheritance descendants of the current source facet.  This includes the detail facet and all
     * custom facets, including "ghost facets" inherited from extended business objects.
     * 
     * @return List<TLFacet>
     */
    private List<TLFacet> getDescendantFacets() {
    	TLBusinessObject bo = (TLBusinessObject) getSourceFacet().getOwningEntity();
    	List<TLFacet> descendants = new ArrayList<>();
    	
    	descendants.add( bo.getDetailFacet() );
    	descendants.addAll( bo.getCustomFacets() );
    	descendants.addAll( FacetCodegenUtils.findGhostFacets( bo, TLFacetType.CUSTOM ) );
    	return descendants;
    }
    
}
