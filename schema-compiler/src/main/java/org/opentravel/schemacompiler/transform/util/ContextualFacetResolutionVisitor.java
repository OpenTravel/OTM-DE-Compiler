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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Visitor that attempts to resolve contextual facet owner references discovered in a
 * model after new libraries are loaded.
 */
class ContextualFacetResolutionVisitor extends ModelElementVisitorAdapter {
	
	private List<TLContextualFacet> unresolvedFacets = new ArrayList<>();
	private SymbolTable symbolTable;
    private SymbolResolver symbolResolver;
    
    /**
     * Constructor that assigns the model being navigated.
     * 
     * @param model  the model from which all entity names will be obtained
     */
    private ContextualFacetResolutionVisitor(TLModel model) {
    	this.symbolTable = SymbolTableFactory.newSymbolTableFromModel( model );
        this.symbolResolver = new TLModelSymbolResolver( symbolTable );
    }
    
    /**
     * Visits and resolves the contextual facets owners within the given model.  This is
     * necessary as a first-pass during model resolution since the qualified name of the
     * contextual facets is composed of the owner name.  Therefore, it is necessary to
     * resolve the facet owners and rebuid the symbol table prior to resolving other
     * entity references.
     * 
     * @param model  the model for which to resolve contextual facet owners
     */
    public static void resolveReferences(TLModel model) {
    	ContextualFacetResolutionVisitor visitor = new ContextualFacetResolutionVisitor( model );
    	
        ModelNavigator.navigate( model, visitor );
        visitor.handleUnresolvedFacets();
    }
    
    /**
     * Attempts to resolve as many unresolved facet owner references as possible.
     */
    private void handleUnresolvedFacets() {
    	int lastCount = 0;
    	
    	// Continue making passes through the list until no more entities can be resolved
    	while (unresolvedFacets.size() != lastCount) {
    		Iterator<TLContextualFacet> iterator = unresolvedFacets.iterator();
    		lastCount = unresolvedFacets.size();
    		
    		while (iterator.hasNext()) {
    			TLContextualFacet facet = iterator.next();
    			
    			if (resolveFacetOwner( facet )) {
    				iterator.remove();
    			}
    		}
    	}
    }
    
    /**
     * Attempts to resolve the given facet's owner reference.  If the reference
     * was resolved successfully, this method will return true.
     * 
     * @param facet  the facet whose owner reference is to be resolved
     * @return boolean
     */
    private boolean resolveFacetOwner(TLContextualFacet facet) {
    	boolean isResolved = false;
    	
        if ((facet.getOwningEntity() == null) && (facet.getOwningEntityName() != null)) {
        	// Attempt to resolve the facet using its 'owningEntityName' field
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
            
            // If the facet was resolved successfully, add its name to the symbol table
            if (facet.getOwningEntity() != null) {
            	symbolTable.addEntity( facet.getNamespace(), facet.getLocalName(), facet );
            	isResolved = true;
            }
        }
        return isResolved;
    }

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitContextualFacet(org.opentravel.schemacompiler.model.TLContextualFacet)
	 */
	@Override
	public boolean visitContextualFacet(TLContextualFacet facet) {
    	resolveFacetOwner( facet );
    	
    	// If we were unable to resolve the facet's owner reference on this first-pass,
    	// save it so we can process it later
        if ((facet.getOwningEntity() == null) && (facet.getOwningEntityName() != null)) {
    		unresolvedFacets.add( facet );
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
