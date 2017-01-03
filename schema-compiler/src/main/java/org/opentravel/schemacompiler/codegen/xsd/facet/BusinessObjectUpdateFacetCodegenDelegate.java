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

import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.Documentation;

/**
 * Code generation delegate for <code>TLFacet</code> instances with a facet type of <code>QUERY</code>
 * and a facet owner of type <code>TLBusinessObject</code>.
 * 
 * @author S. Livezey
 */
public class BusinessObjectUpdateFacetCodegenDelegate extends BusinessObjectFacetCodegenDelegate {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet
     *            the source facet
     */
    public BusinessObjectUpdateFacetCodegenDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }

    /**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createJaxbAttributes(java.util.List, java.util.List)
	 */
	@Override
	protected List<Annotated> createJaxbAttributes(List<TLAttribute> attributeList, List<TLIndicator> indicatorList) {
		List<TLProperty> elementList = getElements();
		List<Annotated> jaxbAttributes = new ArrayList<>();
		
		// Add 'update' indicators for optional elements and attributes
		for (TLAttribute attribute : attributeList) {
			if (!attribute.isMandatory()) {
				String indicatorName = XsdCodegenUtils.getUpdateIndicatorName( attribute );
				String fieldName = attribute.getName();
				
				if (attribute.isReference()) {
					QName elementName = XsdCodegenUtils.getGlobalElementName( attribute.getType() );
					
					if (elementName != null) {
						fieldName = elementName.getLocalPart();
					}
				}
				addUpdateIndicator( indicatorName, fieldName, jaxbAttributes );
			}
		}
		
		for (TLProperty element : elementList) {
			if (!element.isMandatory()) {
				String indicatorName = XsdCodegenUtils.getUpdateIndicatorName( element );
				QName elementName = XsdCodegenUtils.getGlobalElementName( element.getType() );
				String fieldName = (elementName != null) ? elementName.getLocalPart() : element.getName();
				
				addUpdateIndicator( indicatorName, fieldName, jaxbAttributes );
			}
		}
		
		// Add all of the standard attributes/indicators defined in the model
		jaxbAttributes.addAll( super.createJaxbAttributes( attributeList, indicatorList ) );
		
		return jaxbAttributes;
	}
	
	/**
	 * Adds an 'update' indicator for the optional field with the given name.
	 * 
	 * @param indicatorName  the name of the update indicator
	 * @param fieldName  the name of the optional attribute or element
	 * @param jaxbAttributes  the list of JAXB attributes to which the new indicator will be added
	 */
	protected void addUpdateIndicator(String indicatorName, String fieldName, List<Annotated> jaxbAttributes) {
		if ((indicatorName != null) && (indicatorName.length() > 0)) {
	        Attribute updateAttr = new Attribute();
	        Annotation attrDoc = new Annotation();
            Documentation desc = new Documentation();
	        
	        updateAttr.setName( indicatorName );
	        updateAttr.setType( XsdCodegenUtils.XSD_BOOLEAN_TYPE );
	        updateAttr.setUse( "optional" );
            desc.getContent().add( "Indicates whether an update to the '" + fieldName + "' field has been supplied." );
	        attrDoc.getAppinfoOrDocumentation().add( desc );
	        updateAttr.setAnnotation( attrDoc );
	        
	        jaxbAttributes.add( updateAttr );
		}
	}

	/**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#hasContent()
     */
    @Override
    public boolean hasContent() {
        return getSourceFacet().declaresContent() || (getBaseFacet() != null);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
     */
    @Override
    public TLFacet getLocalBaseFacet() {
        FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory(transformerContext);
        TLFacet sourceFacet = getSourceFacet();
        TLFacetOwner facetOwner = sourceFacet.getOwningEntity();
        TLFacet baseFacet = null;

        while ((baseFacet == null) && (facetOwner instanceof TLContextualFacet)) {
        	TLContextualFacet owningFacet = (TLContextualFacet) facetOwner;
        	
        	if (factory.getDelegate(owningFacet).hasContent()) {
        		baseFacet = owningFacet;
        		
        	} else {
        		facetOwner = owningFacet.getOwningEntity();
        	}
        }
        return baseFacet;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getSubstitutionGroup(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    protected QName getSubstitutionGroup(TLAlias facetAlias) {
    	TLContextualFacet facet = (TLContextualFacet) getSourceFacet();
    	QName subGrp = null;
    	
    	if (facet.getOwningEntity() instanceof TLContextualFacet) {
    		while (facet.getOwningEntity() instanceof TLContextualFacet) {
    			facet = (TLContextualFacet) facet.getOwningEntity();
    		}
    		subGrp = XsdCodegenUtils.getGlobalElementName( facet );
    	}
    	return subGrp;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
     */
    @Override
    public QName getExtensionPointElement() {
    	TLFacetOwner facetOwner = getSourceFacet().getOwningEntity();
        QName extensionPointQName;
    	
        if (facetOwner instanceof TLBusinessObject) {
            SchemaDependency extensionPoint = SchemaDependency.getExtensionPointElement();
            
            extensionPointQName = extensionPoint.toQName();
            addCompileTimeDependency(extensionPoint);
            
        } else {
        	extensionPointQName = null;
        }
        return extensionPointQName;
    }

}
