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
package org.opentravel.exampleupgrade;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.example.ExtensionPointRegistry;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.visitor.AbstractNavigator;

/**
 * Navigator that traverses OTM entities and entity members in such a way that
 * each member is visited in the order that they occur in generated schemas.  The
 * field members that are traversed for facet owners include inherited members
 * as well as declared fields.
 */
public class UpgradeModelNavigator extends AbstractNavigator<NamedEntity>{
	
	private ExtensionPointRegistry extensionPointRegistry;
	private UpgradeModelVisitor visitor;
	
    /**
     * Constructor that initializes the visitor to be notified when model elements are encountered
     * during navigation.
     * 
     * @param visitor  the visitor to be notified when model elements are encountered
     * @param model  the model containing all of the entities to be visited
     */
	public UpgradeModelNavigator(UpgradeModelVisitor visitor, TLModel model) {
		super(visitor);
		this.visitor = visitor;
		extensionPointRegistry = new ExtensionPointRegistry( model );
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.AbstractNavigator#navigate(java.lang.Object)
	 */
	@Override
	public void navigate(NamedEntity target) {
        if (target instanceof TLValueWithAttributes) {
            navigateValueWithAttributes((TLValueWithAttributes) target);

        } else if (target instanceof TLFacet) {
            navigateFacet((TLFacet) target);

        } else if (target instanceof TLActionFacet) {
            navigateActionFacet((TLActionFacet) target);

        } else if (target instanceof TLListFacet) {
            navigateListFacet((TLListFacet) target);

        } else if (target instanceof TLAlias) {
            navigateAlias((TLAlias) target);

        } else if (target instanceof TLExtensionPointFacet) {
            navigateExtensionPointFacet((TLExtensionPointFacet) target);

        } else if (target instanceof TLAttribute) {
            navigateAttribute((TLAttribute) target);

        } else if (target instanceof TLProperty) {
            navigateElement((TLProperty) target);

        } else if (target instanceof TLIndicator) {
            navigateIndicator((TLIndicator) target);
        }
	}

    /**
     * Called when a <code>TLValueWithAttributes</code> instance is encountered during model
     * navigation.
     * 
     * @param valueWithAttributes  the simple entity to visit and navigate
     */
    public void navigateValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if (canVisit(valueWithAttributes) && visitor.visitValueWithAttributes(valueWithAttributes)) {
            for (TLAttribute attribute : valueWithAttributes.getAttributes()) {
                navigateAttribute(attribute);
            }
        }
        addVisitedNode(valueWithAttributes);
    }

    /**
     * Called when a <code>TLExtensionPointFacet</code> instance is encountered during model
     * navigation.
     * 
     * @param extensionPointFacet  the extension point facet entity to visit and navigate
     */
    public void navigateExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
        if (canVisit(extensionPointFacet) && visitor.visitExtensionPointFacet(extensionPointFacet)) {

            for (TLAttribute attribute : extensionPointFacet.getAttributes()) {
                navigateAttribute(attribute);
            }
            for (TLProperty element : extensionPointFacet.getElements()) {
                navigateElement(element);
            }
            for (TLIndicator indicator : extensionPointFacet.getIndicators()) {
                navigateIndicator(indicator);
            }
            visitor.visitExtensionPointEnd(extensionPointFacet);
        }
    }

    /**
     * Called when a <code>TLFacet</code> instance is encountered during model navigation.
     * 
     * @param facet  the facet entity to visit and navigate
     */
    public void navigateFacet(TLFacet facet) {
        if (canVisit(facet) && visitor.visitFacet(facet)) {
        	navigateFacetMembers(facet);
        }
        addVisitedNode(facet);
    }

    /**
     * Called when a <code>TLListFacet</code> instance is encountered during model navigation.
     * 
     * @param listFacet  the list facet entity to visit and navigate
     */
    public void navigateListFacet(TLListFacet listFacet) {
        if (canVisit(listFacet) && visitor.visitListFacet(listFacet)) {
			TLAbstractFacet itemFacet = listFacet.getItemFacet();
			
			if (itemFacet instanceof TLFacet) {
				navigateFacetMembers( (TLFacet) itemFacet );
			}
			// TODO: What about the role attribute?
        }
        addVisitedNode(listFacet);
    }

    /**
     * Called when a <code>TLAlias</code> instance is encountered during model navigation.
     * 
     * @param alias  the alias entity to visit and navigate
     */
    public void navigateAlias(TLAlias alias) {
        if (canVisit(alias)) {
			TLAliasOwner aliasOwner = alias.getOwningEntity();
			
            visitor.visitAlias(alias);
            
			if (aliasOwner instanceof TLFacet) {
				navigateFacetMembers( (TLFacet) aliasOwner );
				
			} else if (aliasOwner instanceof TLListFacet) {
				TLAbstractFacet itemFacet = ((TLListFacet) aliasOwner).getItemFacet();
				
				if (itemFacet instanceof TLFacet) {
					navigateFacetMembers( (TLFacet) itemFacet );
				}
				
			} else if (aliasOwner instanceof TLComplexTypeBase) {
    			navigateFacetMembers( getPreferredFacet( (TLComplexTypeBase) aliasOwner ) );
				
			} else {
				throw new IllegalArgumentException("Unexpected alias owner type.");
			}
        }
        addVisitedNode(alias);
    }

    /**
     * Called when a <code>TLActionFacet</code> instance is encountered during model navigation.
     * 
     * @param actionFacet  the action facet entity to visit and navigate
     */
    public void navigateActionFacet(TLActionFacet actionFacet) {
    	NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( actionFacet );
    	
    	if (payloadType instanceof TLActionFacet) {
            if (canVisit(actionFacet)) {
            	TLProperty boElement = ResourceCodegenUtils.createBusinessObjectElement(actionFacet, null);
            	NamedEntity basePayload = actionFacet.getBasePayload();
            	TLFacet payloadFacet = null;
            	
            	if (basePayload instanceof TLCoreObject) {
            		payloadFacet = ((TLCoreObject) basePayload).getSummaryFacet();
                    
            	} else if (basePayload instanceof TLChoiceObject) {
            		payloadFacet = ((TLChoiceObject) basePayload).getSharedFacet();
            	}
            	
            	visitor.visitActionFacet(actionFacet);
                addVisitedNode(actionFacet);
            	
                if (boElement != null) {
                	navigateElement( boElement );
                }
            	if (payloadFacet != null) {
                	navigateFacetMembers(payloadFacet);
            	}
            }
    	}
    }
    
    /**
     * Navigates the member fields of the given facet.
     * 
     * @param facet  the facet whose members are to be navigated
     */
    private void navigateFacetMembers(TLFacet facet) {
        Map<TLFacetType, List<TLExtensionPointFacet>> facetExtensionsByType =
        		extensionPointRegistry.getExtensionPoints( facet );
        Set<TLFacetType> processedExtensionPointTypes = new HashSet<TLFacetType>();
        String previousFacetIdentity = null;

        // Start by navigating attributes and indicators for this facet
        for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes(facet)) {
            navigateAttribute(attribute);
        }
        for (TLIndicator indicator : PropertyCodegenUtils.getInheritedIndicators(facet)) {
        	if (!indicator.isPublishAsElement()) {
                navigateIndicator(indicator);
        	}
        }

        // Navigate the elements (properties) and extension points for this facet
        for (TLModelElement elementItem : PropertyCodegenUtils.getElementSequence(facet)) {
        	if (elementItem instanceof TLProperty) {
        		TLProperty element = (TLProperty) elementItem;
                TLFacet currentFacet = (TLFacet) element.getOwner();
                String currentFacetIdentity = extensionPointRegistry.getFacetIdentity( currentFacet );
                
                // Before navigating the element itself, check to see if we need to insert any extension
                // point facets
                if (!currentFacetIdentity.equals( previousFacetIdentity )) {
                    List<TLFacet> facetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy(currentFacet);

                    // Ignore the last element in the facet hierarchy list since it is always the
                    // current facet we are processing
                    for (int i = 0; i < (facetHierarchy.size() - 1); i++) {
                        TLFacet hFacet = facetHierarchy.get(i);

                        if (!processedExtensionPointTypes.contains(hFacet.getFacetType())) {
                        	if (extensionPointRegistry.hasExtensionPoint( hFacet )) {
                                navigateExtensionPoint(hFacet, facetExtensionsByType.get(hFacet.getFacetType()));
                        	}
                            processedExtensionPointTypes.add(hFacet.getFacetType());
                        }
                    }
                    previousFacetIdentity = currentFacetIdentity;
                }

                // Navigate the example content for the current element
                navigateElement(element);
                
        	} else if (elementItem instanceof TLIndicator) {
                navigateIndicator( (TLIndicator) elementItem );
        	}
        }
        
        // Wrap up by checking for any extension points for the current facet (take into account
        // that the facet may not contain any properties and therefore may not have checked for
        // extension points yet).
        List<TLFacet> facetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy(facet);

        for (TLFacet hFacet : facetHierarchy) {
            if (!processedExtensionPointTypes.contains(hFacet.getFacetType())) {
            	if (extensionPointRegistry.hasExtensionPoint( hFacet )) {
            		navigateExtensionPoint(hFacet, facetExtensionsByType.get(hFacet.getFacetType()));
            	}
            }
        }
    }
    
    /**
     * Navigates the specified extensions of the facet.
     * 
     * @param facet
     *            the facet whose extension points are to be navigated
     * @param facetExtensions
     *            the list of extension points for the facet
     */
    private void navigateExtensionPoint(TLFacet facet, List<TLExtensionPointFacet> facetExtensions) {
        if ((facetExtensions != null) && !facetExtensions.isEmpty()) {
            for (TLExtensionPointFacet xpFacet : facetExtensions) {
                navigateExtensionPointFacet(xpFacet);
            }
        }
    }

    /**
     * Called when a <code>TLAttribute</code> instance is encountered during model navigation.
     * 
     * @param attribute  the attribute entity to visit and navigate
     */
    public void navigateAttribute(TLAttribute attribute) {
        if (canVisit(attribute) && visitor.visitAttribute(attribute)) {
        	// No further navigation required
        }
        addVisitedNode(attribute);
    }

    /**
     * Called when a <code>TLProperty</code> instance is encountered during model navigation.
     * 
     * @param element  the element entity to visit and navigate
     */
    public void navigateElement(TLProperty element) {
        if (canVisit(element) && visitor.visitElement(element)) {
        	TLPropertyType elementType = element.getType();
        	QName elementName = XsdCodegenUtils.getGlobalElementName( elementType );
        	
        	if (elementName != null) {
        		if (elementType instanceof TLFacet) {
        			navigateFacet( (TLFacet) elementType );
        			
        		} else if (elementType instanceof TLListFacet) {
        			navigateListFacet( (TLListFacet) elementType );
        			
        		} else if (elementType instanceof TLAlias) {
        			navigateAlias( (TLAlias) elementType );
        			
        		} else if (elementType instanceof TLComplexTypeBase) {
        			navigateFacetMembers( getPreferredFacet( (TLComplexTypeBase) elementType ) );
        			
        		} else {
        			throw new IllegalArgumentException("Unexpected OTM element type");
        		}
        		
        	} else if (elementType instanceof TLValueWithAttributes) {
    			navigateValueWithAttributes( (TLValueWithAttributes) elementType );
        	}
        	visitor.visitElementEnd(element);
        }
        addVisitedNode(element);
    }
    
    /**
     * Returns the preferred facet for the given OTM entity based on the visitor's
     * current position in the DOM tree.
     * 
     * @param otmEntity  the OTM entity for which to return the preferred facet
     * @return TLFacet
     */
    private TLFacet getPreferredFacet(TLComplexTypeBase otmEntity) {
    	TLFacet preferredFacet;
    	
    	// TODO: Rework preferred facet logic
		// In these cases, we will need to do some sort of lookahead on the DOM tree to find
		// out which facet should be chosen.  This will probably need to be delegated to the
		// listener since the navigator has no visibility to the DOM tree.
    	
    	if (otmEntity instanceof TLBusinessObject) {
    		preferredFacet = ((TLBusinessObject) otmEntity).getSummaryFacet();
    		
    	} else if (otmEntity instanceof TLChoiceObject) {
    		preferredFacet = ((TLChoiceObject) otmEntity).getSharedFacet();
    		
    	} else if (otmEntity instanceof TLCoreObject) {
    		preferredFacet = ((TLCoreObject) otmEntity).getSummaryFacet();
    		
    	} else {
    		throw new IllegalArgumentException("Unrecognized complex entity type.");
    	}
    	return preferredFacet;
    }

    /**
     * Called when a <code>TLIndicator</code> instance is encountered during model navigation.
     * 
     * @param indicator  the indicator entity to visit and navigate
     */
    public void navigateIndicator(TLIndicator indicator) {
        if (canVisit(indicator) && visitor.visitIndicator(indicator)) {
        	// No further navigation required
        }
        addVisitedNode(indicator);
    }

	/**
	 * @see org.opentravel.schemacompiler.visitor.AbstractNavigator#navigateLibrary(org.opentravel.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public void navigateLibrary(AbstractLibrary library) {
		throw new UnsupportedOperationException("Libraries not supported by this navigator.");
	}
	
}
