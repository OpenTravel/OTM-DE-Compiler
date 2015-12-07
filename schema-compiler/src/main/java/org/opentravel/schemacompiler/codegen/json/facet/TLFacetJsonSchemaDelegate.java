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
package org.opentravel.schemacompiler.codegen.json.facet;

import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.CorrelatedCodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Base class for facet code generation delegates used to generate code artifacts for
 * <code>TLFacet</code> model elements.
 */
public abstract class TLFacetJsonSchemaDelegate extends FacetJsonSchemaDelegate<TLFacet> {
	
    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet  the source facet
     */
    public TLFacetJsonSchemaDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }
    
    /**
	 * @see org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate#generateArtifacts()
	 */
	@Override
	public CorrelatedCodegenArtifacts generateArtifacts() {
		CorrelatedCodegenArtifacts artifacts = super.generateArtifacts();
		
		for (TLAlias alias : getSourceFacet().getAliases()) {
			artifacts.addArtifact( alias, createDefinition( alias ) );
		}
		return artifacts;
	}

    /**
     * Creates the JSON definiton for the facet or the alias if one is specified.
     * 
     * @param alias  the facet alias for which to generate a definition
     * @return JsonSchemaNamedReference
     */
    protected JsonSchemaNamedReference createDefinition(TLAlias alias) {
    	JsonSchemaNamedReference definition;
    	
    	if (alias == null) {
    		definition = createDefinition();
    		
    	} else {
    		definition = new JsonSchemaNamedReference( getElementName( alias ),
    				new JsonSchemaReference( getSchemaReferencePath( getSourceFacet(), alias ) ) );
    	}
    	return definition;
    }
    
	/**
	 * @see org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegate#createDefinition()
	 */
	@Override
	protected JsonSchemaNamedReference createDefinition() {
		// TODO Implement the 'createDefinition()' method
		return null;
	}

	/**
     * Returns the name of the non-substitutable element used to represent the source facet or the
     * specified alias.
     * 
     * @param facetAlias  the alias of the source facet element being created (may be null)
     * @return String
     */
    protected final String getNonSubstitableElementName(TLAlias facetAlias) {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() ))
    			.getNonSubstitableElementName( facetAlias );
    }
    
    /**
     * Returns the facet instance that should serve as the base type for the source facet. In some
     * cases (business/core object extension), the facet returned by this method may have a
     * different owner than that of the source facet.
     * 
     * @return TLFacet
     */
    protected final TLFacet getBaseFacet() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getBaseFacet();
    }
    
    /**
     * Returns true if the source facet should have a non-substitutable facet in addition to the
     * substitutable one that is created by default.
     * 
     * @return boolean
     */
    protected final boolean hasNonSubstitutableElement() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).hasNonSubstitutableElement();
    }
    
    /**
     * If the source facet should support an extension point element, this method will return
     * the extension point property to use in the facet's JSON schema definition. If extensions
     * are not supported for the facet, this method will return null.
     * 
     * @return JsonSchemaNamedReference
     */
    protected final JsonSchemaNamedReference getExtensionPointProperty() {
    	QName extensionPointName = ((TLFacetCodegenDelegate)
    			xsdDelegateFactory.getDelegate( getSourceFacet() )).getExtensionPointElement();
    	JsonSchemaNamedReference extensionPointProperty = null;
    	
    	if (extensionPointName != null) {
    		String schemaPath = null;
    		
    		// Look through all of the schema dependencies to find the one that matches are extension
    		// point QName.  This is a bit inefficient, but it keeps us from having to replicate all
    		// of the extension point logic from the XSD facet delegates.
    		for (SchemaDependency dependency : SchemaDependency.getAllDependencies()) {
    			if ((extensionPointName.getNamespaceURI().equals( dependency.getSchemaDeclaration().getNamespace() ))
    					&& extensionPointName.getLocalPart().equals( dependency.getLocalName() )) {
    				CodeGenerationContext cgContext = getTransformerFactory().getContext().getCodegenContext();
    				String builtInLocation = XsdCodegenUtils.getBuiltInSchemaOutputLocation( cgContext );
    				String referencedFilename = dependency.getSchemaDeclaration().getFilename(
    						CodeGeneratorFactory.JSON_SCHEMA_TARGET_FORMAT );
    				
    				if (referencedFilename != null) {
    					schemaPath = builtInLocation + referencedFilename + "#/definitions/" + dependency.getLocalName();
    				}
    			}
    		}
    		extensionPointProperty = new JsonSchemaNamedReference(
    				extensionPointName.getLocalPart(),
    				new JsonSchemaReference(schemaPath) );
    	}
    	return extensionPointProperty;
    }
    
    /**
     * Returns the list of attributes to be generated for the source facet.
     * 
     * @return List<TLAttribute>
     */
    protected final List<TLAttribute> getAttributes() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getAttributes();
    }

    /**
     * Returns the list of elements (properties) to be generated for the source facet.
     * 
     * @return List<TLProperty>
     */
    protected final List<TLProperty> getElements() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getElements();
    }

    /**
     * Returns the list of indicators to be generated for the source facet.
     * 
     * @return List<TLIndicator>
     */
    protected final List<TLIndicator> getIndicators() {
    	return ((TLFacetCodegenDelegate) xsdDelegateFactory.getDelegate( getSourceFacet() )).getIndicators();
    }

}
