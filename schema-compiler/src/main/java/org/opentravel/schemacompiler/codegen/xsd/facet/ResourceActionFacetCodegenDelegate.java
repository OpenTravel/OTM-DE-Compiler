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

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.w3._2001.xmlschema.Annotated;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of <code>ACTION</code>
 * and a facet owner of type <code>TLResource</code>.
 *
 * @author S. Livezey
 */
public class ResourceActionFacetCodegenDelegate extends FacetCodegenDelegate<TLActionFacet> {
	
    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet
     *            the source facet
     */
    public ResourceActionFacetCodegenDelegate(TLActionFacet sourceFacet) {
        super(sourceFacet);
    }

    /**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createType()
	 */
	@Override
	protected Annotated createType() {
		return null; // No type generated for action facet
	}

	/**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#hasContent()
     */
    @Override
    public boolean hasContent() {
    	TLActionFacet sourceFacet = getSourceFacet();
    	String xsdTypeName = XsdCodegenUtils.getGlobalTypeName( sourceFacet );
    	boolean contentExists;
    	
    	if (xsdTypeName != null) {
    		if (sourceFacet.getReferenceRepeat() > 1) {
    			contentExists = true;
    			
    		} else {
    	        contentExists = !PropertyCodegenUtils.getInheritedAttributes( sourceFacet ).isEmpty()
    	        		|| !PropertyCodegenUtils.getInheritedProperties( sourceFacet ).isEmpty()
    	        		|| !PropertyCodegenUtils.getInheritedIndicators( sourceFacet ).isEmpty();
    		}
    	} else {
    		contentExists = false;
    	}
        return contentExists;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
     */
    @Override
    public TLActionFacet getLocalBaseFacet() {
        return null; // No base type for action facets
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getSubstitutionGroup(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    protected QName getSubstitutionGroup(TLAlias facetAlias) {
        return null; // No substitution group for query facets
    }

    /**
     * If the source facet should support an extension point element, this method will return the
     * qualified name of the global extension point element to use in the type's definition. If
     * extensions are not supported for the facet, this method sould return null.
     * 
     * @return QName
     */
    public QName getExtensionPointElement() {
        SchemaDependency extensionPoint = SchemaDependency.getExtensionPointElement();
        QName extensionPointQName = extensionPoint.toQName();
        
        addCompileTimeDependency(extensionPoint);
        return extensionPointQName;
    }

}
