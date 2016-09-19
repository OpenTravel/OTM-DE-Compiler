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

package org.opentravel.schemacompiler.transform.util;

import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Visitor that attempts to resolve contextual facet owner references discovered in a
 * model after new libraries are loaded.
 */
class ContextualFacetResolutionVisitor extends ModelElementVisitorAdapter {
	
    private SymbolResolver symbolResolver;
    
    /**
     * Constructor that assigns the model being navigated.
     * 
     * @param model
     *            the model from which all entity names will be obtained
     */
    public ContextualFacetResolutionVisitor(TLModel model) {
        this.symbolResolver = new TLModelSymbolResolver(model);
    }

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitContextualFacet(org.opentravel.schemacompiler.model.TLContextualFacet)
	 */
	@Override
	public boolean visitContextualFacet(TLContextualFacet facet) {
        if ((facet.getOwningEntity() == null) && (facet.getOwningEntityName() != null)) {
            Object ref = symbolResolver.resolveEntity(facet.getOwningEntityName());
            TLFacetType facetType = facet.getFacetType();
            
            if (ref instanceof TLBusinessObject) {
            	TLBusinessObject facetOwner = (TLBusinessObject) ref;
            	
            	switch (facetType) {
            		case CUSTOM:
            			facetOwner.addCustomFacet( facet );
            			break;
            		case UPDATE:
            			facetOwner.addUpdateFacet( facet );
            			break;
            		case QUERY:
            		default:
            			facetOwner.addQueryFacet( facet );
            			break;
            	}
            } else if (ref instanceof TLChoiceObject) {
    			((TLChoiceObject) ref).addChoiceFacet( facet );
    			
            } else if (ref instanceof TLContextualFacet) {
            	((TLContextualFacet) ref).addChildFacet( facet );
            }
        }
		return true;
	}

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitUserDefinedLibrary(org.opentravel.schemacompiler.model.TLLibrary)
     */
    @Override
    public boolean visitUserDefinedLibrary(TLLibrary library) {
        symbolResolver.setPrefixResolver(new LibraryPrefixResolver(library));
        symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(library));
        return true;
    }

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
	 */
	@Override
	public boolean visitBuiltInLibrary(BuiltInLibrary library) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
	 */
	@Override
	public boolean visitLegacySchemaLibrary(XSDLibrary library) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
	 */
	@Override
	public boolean visitSimple(TLSimple simple) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(org.opentravel.schemacompiler.model.TLClosedEnumeration)
	 */
	@Override
	public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
	 */
	@Override
	public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitChoiceObject(org.opentravel.schemacompiler.model.TLChoiceObject)
	 */
	@Override
	public boolean visitChoiceObject(TLChoiceObject choiceObject) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(org.opentravel.schemacompiler.model.TLCoreObject)
	 */
	@Override
	public boolean visitCoreObject(TLCoreObject coreObject) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(org.opentravel.schemacompiler.model.TLBusinessObject)
	 */
	@Override
	public boolean visitBusinessObject(TLBusinessObject businessObject) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitService(org.opentravel.schemacompiler.model.TLService)
	 */
	@Override
	public boolean visitService(TLService service) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
	 */
	@Override
	public boolean visitResource(TLResource resource) {
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
	 */
	@Override
	public boolean visitExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
		return false;
	}
    
}
