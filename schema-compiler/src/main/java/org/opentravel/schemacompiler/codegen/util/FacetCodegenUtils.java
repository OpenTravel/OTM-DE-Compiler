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
package org.opentravel.schemacompiler.codegen.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.OperationType;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.visitor.ModelElementVisitor;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Static utility methods used during the generation of code output for facets.
 * 
 * @author S. Livezey
 */
public class FacetCodegenUtils {
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private FacetCodegenUtils() {}
	
	/**
	 * For contextual facets, this method will return the value of the 'name' field.  For
	 * non-contextual facets, null will be returned.
	 * 
	 * @param facet  the facet for which to return a name
	 * @return String
	 */
	public static String getFacetName(TLFacet facet) {
		return (facet instanceof TLContextualFacet) ? ((TLContextualFacet) facet).getName() : null;
	}
	
    /**
     * Returns all of the facets from the specified owner of the requested type. For non-contextual
     * facet types (e.g. summary or ID), the list that is returned will contain a single element.
     * For contextual facet types (e.g. query or custom) a list of zero or more matching facets will
     * be returned, regardless of their context or label values.
     * 
     * @param owner
     *            the facet owner from which to return member facets
     * @param facetType
     *            the type of member facets to return
     * @return List<TLFacet>
     */
    public static List<TLFacet> getAllFacetsOfType(TLFacetOwner owner, TLFacetType facetType) {
        List<TLFacet> facetList = new ArrayList<>();

        if (facetType.isContextual()) {
            if (owner instanceof TLBusinessObject) {
                switch (facetType) {
                    case CUSTOM:
                        facetList.addAll(((TLBusinessObject) owner).getCustomFacets());
                        break;
                    case QUERY:
                        facetList.addAll(((TLBusinessObject) owner).getQueryFacets());
                        break;
                    case UPDATE:
                        facetList.addAll(((TLBusinessObject) owner).getUpdateFacets());
                        break;
					default:
						break;
                }
            } else if (owner instanceof TLChoiceObject) {
            	if (facetType == TLFacetType.CHOICE) {
            		facetList.addAll(((TLChoiceObject) owner).getChoiceFacets());
            	}
            } else if (owner instanceof TLContextualFacet) {
            	TLContextualFacet owningFacet = (TLContextualFacet) owner;
            	
            	if (owningFacet.getFacetType() == facetType) {
            		facetList.addAll( owningFacet.getChildFacets() );
            	}
            }
        } else {
            facetList.add(getFacetOfType(owner, facetType));
        }
        return facetList;
    }

    /**
     * Returns a facet of the specified type from the given owner. If no such facet is available
     * from the owner, this method will return null.
     * 
     * <p>
     * NOTE: This method assumes null values for the facet's context and label when looking up
     * contextual facets.
     * 
     * @param owner  the facet owner from which to return a member facet
     * @param facetType  the type of member facet to return
     * @return TLFacet
     */
    public static TLFacet getFacetOfType(TLFacetOwner owner, TLFacetType facetType) {
        return getFacetOfType(owner, facetType, null);
    }

    /**
     * Returns a facet of the specified type from the given owner. If no such facet is available
     * from the owner, this method will return null.
     * 
     * @param owner  the facet owner from which to return a member facet
     * @param facetType  the type of member facet to return
     * @param facetContext  the context ID of the facet to return (only used for contextual facets)
     * @param facetLabel  the label of the facet to return (only used for contextual or action facets)
     * @return TLFacet
     * @deprecated Use the {@link #getFacetOfType(TLFacetOwner, TLFacetType, String)} method instead
     */
    @Deprecated
    public static TLFacet getFacetOfType(TLFacetOwner owner, TLFacetType facetType,
            String facetContext, String facetLabel) {
    	return getFacetOfType( owner, facetType, facetLabel );
    }
    
    /**
     * Returns a facet of the specified type from the given owner. If no such facet is available
     * from the owner, this method will return null.
     * 
     * @param owner  the facet owner from which to return a member facet
     * @param facetType  the type of member facet to return
     * @param facetName  the name of the contextual facet to return (only used for contextual facets)
     * @return TLFacet
     */
    public static TLFacet getFacetOfType(TLFacetOwner owner, TLFacetType facetType, String facetName) {
        TLFacet memberFacet;

        if (owner instanceof TLBusinessObject) {
            TLBusinessObject boOwner = (TLBusinessObject) owner;

            switch (facetType) {
                case ID:
                    memberFacet = boOwner.getIdFacet();
                    break;
                case SUMMARY:
                    memberFacet = boOwner.getSummaryFacet();
                    break;
                case DETAIL:
                    memberFacet = boOwner.getDetailFacet();
                    break;
                case CUSTOM:
                    memberFacet = findContextualFacet(boOwner.getCustomFacets(), facetName);
                    break;
                case QUERY:
                    memberFacet = findContextualFacet(boOwner.getQueryFacets(), facetName);
                    break;
                case UPDATE:
                    memberFacet = findContextualFacet(boOwner.getUpdateFacets(), facetName);
                    break;
				default:
					memberFacet = null;
					break;
            }
        } else if (owner instanceof TLCoreObject) {
            TLCoreObject coreOwner = (TLCoreObject) owner;

            switch (facetType) {
            // NOTE: We are looking for a TLFacet, so the core's simple facet is not considered
                case SUMMARY:
                    memberFacet = coreOwner.getSummaryFacet();
                    break;
                case DETAIL:
                    memberFacet = coreOwner.getDetailFacet();
                    break;
				default:
					memberFacet = null;
					break;
            }
        } else if (owner instanceof TLChoiceObject) {
        	TLChoiceObject choiceOwner = (TLChoiceObject) owner;
        	
        	switch (facetType) {
        		case SHARED:
        			memberFacet = choiceOwner.getSharedFacet();
        			break;
        		case CHOICE:
        			memberFacet = findContextualFacet(choiceOwner.getChoiceFacets(), facetName);
        			break;
				default:
					memberFacet = null;
					break;
        	}
        } else if (owner instanceof TLContextualFacet) {
        	TLContextualFacet owningFacet = (TLContextualFacet) owner;
        	
        	if (owningFacet.getFacetType() == facetType) {
        		memberFacet = findContextualFacet(owningFacet.getChildFacets(), facetName);
        	} else {
        		memberFacet = null;
        	}
        } else if (owner instanceof TLOperation) {
            TLOperation opOwner = (TLOperation) owner;

            switch (facetType) {
                case REQUEST:
                    memberFacet = opOwner.getRequest();
                    break;
                case RESPONSE:
                    memberFacet = opOwner.getResponse();
                    break;
                case NOTIFICATION:
                    memberFacet = opOwner.getNotification();
                    break;
				default:
					memberFacet = null;
					break;
            }
        } else {
        	memberFacet = null;
        }
        return memberFacet;
    }

    /**
     * Returns the contextual facet from the list with the specified name. If no such facet exists,
     * this method will return null.
     * 
     * @param facetList  the list of facets from which to select a member
     * @param facetName  the name of the contextual facet to return
     * @return TLContextualFacet
     */
    private static TLContextualFacet findContextualFacet(List<TLContextualFacet> facetList, String facetName) {
    	TLContextualFacet memberFacet = null;
    	
    	if (facetName != null) {
            for (TLContextualFacet facet : facetList) {
                if (facetName.equals(facet.getName())) {
                    memberFacet = facet;
                    break;
                }
            }
    	}
        return memberFacet;
    }

    /**
     * Returns true if the given facet owner is an extensible business object, core, or operation
     * entity.
     * 
     * @param owner
     *            the facet owner to analyze
     * @return boolean
     */
    public static boolean isExtensible(TLFacetOwner owner) {
        boolean result = false;

        if (owner instanceof TLBusinessObject) {
            result = !((TLBusinessObject) owner).isNotExtendable();

        } else if (owner instanceof TLCoreObject) {
            result = !((TLCoreObject) owner).isNotExtendable();

        } else if (owner instanceof TLChoiceObject) {
            result = !((TLChoiceObject) owner).isNotExtendable();
            
        } else if (owner instanceof TLContextualFacet) {
            result = !((TLContextualFacet) owner).isNotExtendable();
            
        } else if (owner instanceof TLOperation) {
            result = !((TLOperation) owner).isNotExtendable();
        }
        return result;
    }
    
    /**
     * Returns the top-level facet owner that is not a <code>TLContextualFacet</code>.
     * 
     * @param facet  the facet for which to return the top-level owner
     * @return TLFacetOwner
     */
    public static TLFacetOwner getTopLevelOwner(TLFacet facet) {
    	Set<TLFacetOwner> visitedOwners = new HashSet<>();
    	TLFacetOwner owner = facet.getOwningEntity();
    	
    	while (owner instanceof TLContextualFacet) {
    		if (visitedOwners.contains( owner )) {
    			owner = null;
    			break;
    		}
    		visitedOwners.add( owner );
    		owner = ((TLContextualFacet) owner).getOwningEntity();
    	}
    	return owner;
    }

    /**
     * Returns the <code>TLFacetOwner</code> instance that is extended by the facet owner that is
     * passed to this method.  If the owner does not extend another model entity, this method will
     * return null.
     * 
     * @param owner  the facet owner for which to return the extended entity
     * @return TLFacetOwner
     */
    public static TLFacetOwner getFacetOwnerExtension(TLFacetOwner owner) {
        TLFacetOwner result = null;

        if (owner instanceof TLExtensionOwner) {
            TLExtension extension = ((TLExtensionOwner) owner).getExtension();
            NamedEntity extendedEntity = (extension == null) ? null : extension.getExtendsEntity();

            if (extendedEntity instanceof TLFacetOwner) {
                // This should always be true, but check the type before assigning - just in case
                result = (TLFacetOwner) extendedEntity;
            }
            
        } else if (owner instanceof TLContextualFacet) {
        	TLContextualFacet cfOwner = (TLContextualFacet) owner;
        	Set<TLContextualFacet> visitedFacets = new HashSet<>();
        	TLFacetType facetType = cfOwner.getFacetType();
        	List<String> cfNamePath = new ArrayList<>();
        	TLFacetOwner baseOwner = null;
        	
        	while (cfOwner != null) {
        		cfNamePath.add(0, cfOwner.getName());
        		
        		if (cfOwner.getOwningEntity() instanceof TLContextualFacet) {
        			TLContextualFacet owningFacet = (TLContextualFacet) cfOwner.getOwningEntity();
        			
        			if (!visitedFacets.contains( owningFacet )) {
        				visitedFacets.add( owningFacet );
            			cfOwner = owningFacet;
        			} else {
        				cfOwner = null;
        			}
        			
        		} else {
            		baseOwner = cfOwner.getOwningEntity();
        			cfOwner = null;
        		}
        	}
        	
        	while ((result == null) && (baseOwner instanceof TLExtensionOwner)) {
        		baseOwner = getFacetOwnerExtension( baseOwner );
        		result = getContextualFacet( baseOwner, facetType, cfNamePath );
        	}
        }
        return result;
    }
    
    /**
     * Returns the contextual facet that conforms with the specified path relative to the
     * given owner.  If no such facet exists, this method will return null.
     * 
     * @param owner  the base owner from which the path originates
     * @param facetType  the type of the contextual facet to return
     * @param namePath  the list of names to follow
     * @return TLContextualFacet
     */
    private static TLContextualFacet getContextualFacet(TLFacetOwner owner, TLFacetType facetType,
    		List<String> namePath) {
    	TLFacetOwner currentOwner = owner;
    	TLContextualFacet facet = null;
    	
    	for (String facetName : namePath) {
    		currentOwner = facet = (TLContextualFacet) getFacetOfType( currentOwner, facetType, facetName );
    	}
    	return facet;
    }

    /**
     * Returns the inheritance hierarchy for facets with the same owner. For EXAMPLE, passing the
     * detail facet of a business object to this method would result in a list containing the ID,
     * summary, and detail facets from the original facet's owner. The hierarchy that is returned is
     * sorted from the highest level of the inheritance hierarchy to the lowest. At a minimum, the
     * facet that is passed to this method is guranteed to be a member (and the last element of) of
     * the resulting list.
     * 
     * @param facet
     *            the facet instance for which to return the hierarchy
     * @return List<TLFacet>
     */
    public static List<TLFacet> getLocalFacetHierarchy(TLFacet facet) {
        List<TLFacet> localHierarchy = new ArrayList<>();
        
        getLocalFacetHierarchy( facet, localHierarchy, new HashSet<TLFacet>() );
        return localHierarchy;
    }

    /**
     * Recursive companion method that protects against infinite loops due to circular references.
     * 
     * @param facet  the facet instance for which to return the hierarchy
     * @param localHierarchy  the local facet hierarchy being constructed
     * @param visitedFacets  the collection of facets that have already been visited
     * @return List<TLFacet>
     */
    private static void getLocalFacetHierarchy(TLFacet facet, List<TLFacet> localHierarchy, Set<TLFacet> visitedFacets) {
    	if (!visitedFacets.contains( facet )) {
    		visitedFacets.add( facet );
    		
            TLFacetOwner facetOwner = facet.getOwningEntity();

            localHierarchy.add(facet); // start by including the facet that was passed to this method

            if (facetOwner instanceof TLBusinessObject) {
                switch (facet.getFacetType()) {
                    case DETAIL:
                    case CUSTOM:
                        localHierarchy.add(0, getFacetOfType(facetOwner, TLFacetType.SUMMARY));
                        localHierarchy.add(0, getFacetOfType(facetOwner, TLFacetType.ID));
                        break;
                    case SUMMARY:
                        localHierarchy.add(0, getFacetOfType(facetOwner, TLFacetType.ID));
                        break;
    				default:
    					break;
                }
            } else if (facetOwner instanceof TLCoreObject) {
                if (facet.getFacetType() == TLFacetType.DETAIL) {
                    localHierarchy.add(0, getFacetOfType(facetOwner, TLFacetType.SUMMARY));
                }
            } else if (facetOwner instanceof TLChoiceObject) {
                if (facet.getFacetType() == TLFacetType.CHOICE) {
                    localHierarchy.add(0, getFacetOfType(facetOwner, TLFacetType.SHARED));
                }
            } else if (facetOwner instanceof TLContextualFacet) {
            	List<TLFacet> nestedHierarchy = new ArrayList<>();
            	
            	getLocalFacetHierarchy( (TLContextualFacet) facetOwner, nestedHierarchy, visitedFacets );
            	localHierarchy.addAll( 0, nestedHierarchy );
            	
            } else if (facetOwner instanceof TLOperation) {
                // No detail hierarchy within an operation's facets
            }
    	}
    }
    
	/**
	 * Returns the list of available facets for the substitution group.
	 * 
	 * @param businessObject  the business object for which to return available facets
	 * @return List<TLFacet>
	 */
	public static List<TLFacet> getAvailableFacets(TLComplexTypeBase entity) {
		List<TLFacet> facetList = new ArrayList<>();
		
		if (entity instanceof TLBusinessObject) {
			TLBusinessObject businessObject = (TLBusinessObject) entity;
			
			addIfContentExists( businessObject.getIdFacet(), facetList );
			addIfContentExists( businessObject.getSummaryFacet(), facetList );
			addIfContentExists( businessObject.getDetailFacet(), facetList );
			
			addContextualFacets( businessObject.getCustomFacets(), facetList, new HashSet<TLContextualFacet>() );
			addContextualFacets( findGhostFacets( businessObject, TLFacetType.CUSTOM ),
					facetList, new HashSet<TLContextualFacet>() );
			
		} else if (entity instanceof TLChoiceObject) {
			TLChoiceObject choiceObject = (TLChoiceObject) entity;
			
			addContextualFacets( choiceObject.getChoiceFacets(), facetList, new HashSet<TLContextualFacet>() );
			addContextualFacets( findGhostFacets( choiceObject, TLFacetType.CHOICE ),
					facetList, new HashSet<TLContextualFacet>() );
			
		} else if (entity instanceof TLCoreObject) {
			TLCoreObject coreObject = (TLCoreObject) entity;
			
			addIfContentExists( coreObject.getSummaryFacet(), facetList );
			addIfContentExists( coreObject.getDetailFacet(), facetList );
		}
		return facetList;
	}
	
	/**
	 * Returns the available facet aliases for the given parent object alias.  The alias
	 * passed to this method must be a direct alias of a core, choice, or business object
	 * (i.e. not a facet alias).
	 * 
	 * @param alias  the alias for which to return all available facet aliases
	 * @return List<TLAlias>
	 */
	public static List<TLAlias> getAvailableFacetAliases(TLAlias alias) {
		if (alias.getOwningEntity() instanceof TLComplexTypeBase) {
			List<TLFacet> availableFacets = getAvailableFacets( (TLComplexTypeBase) alias.getOwningEntity() );
			List<TLAlias> availableAliases = new ArrayList<>();
			
			for (TLFacet facet : availableFacets) {
				TLAlias facetAlias;
				
				if (facet instanceof TLContextualFacet) {
					facetAlias = AliasCodegenUtils.getFacetAlias( alias, facet.getFacetType(),
							((TLContextualFacet) facet).getName() );
					
				} else {
					facetAlias = AliasCodegenUtils.getFacetAlias( alias, facet.getFacetType() );
				}
				
				if (facetAlias != null) {
					availableAliases.add( facetAlias );
				}
			}
			return availableAliases;
			
		} else {
			throw new IllegalArgumentException("Invalid alias type (must be a parent object alias).");
		}
	}
	
	/**
	 * Returns the list of available facets for the operation.
	 * 
	 * @param operation  the operation for which to return available facets
	 * @return List<TLFacet>
	 */
	public static List<TLFacet> getAvailableFacets(TLOperation operation) {
		List<TLFacet> facetList = new ArrayList<>();
		
		addIfContentExists(operation.getRequest(), facetList);
		addIfContentExists(operation.getResponse(), facetList);
		addIfContentExists(operation.getNotification(), facetList);
		return facetList;
	}
	
	/**
	 * Returns the list of all available child facets for the given contextual facet.
	 * 
	 * @param facet  the contextual facet for which to return available child facets
	 * @return List<TLFacet>
	 */
	public static List<TLFacet> getAvailableFacets(TLContextualFacet facet) {
		List<TLFacet> facetList = new ArrayList<>();
		
		addContextualFacets( Arrays.asList( facet ), facetList, new HashSet<TLContextualFacet>() );
		return facetList;
	}
	
	/**
	 * Recursive method that adds the list of child facets to the list of contextual facets.
	 * 
	 * @param facetsToAdd  the list of facets to add
	 * @param contextualFacets  the final list of contextual facets being assembled
	 * @param visitedFacets  collection of facets already visited (prevents infinite loops)
	 */
	private static void addContextualFacets(List<TLContextualFacet> facetsToAdd,
			List<TLFacet> contextualFacets, Set<TLContextualFacet> visitedFacets) {
		for (TLContextualFacet facet : facetsToAdd) {
			if (!visitedFacets.contains( facet )) {
				visitedFacets.add( facet );
				addIfContentExists( facet, contextualFacets );
				addContextualFacets( facet.getChildFacets(), contextualFacets, visitedFacets );
				addContextualFacets( FacetCodegenUtils.findGhostFacets( facet, facet.getFacetType() ),
						contextualFacets, visitedFacets );
			}
		}
	}
	
	/**
	 * If the given facet declares or inherits fields, this method will add
	 * it to the list provided.
	 * 
	 * @param facet  the facet to verify and add
	 * @param facetList  the list of facets to which the given one may be appended
	 */
	private static void addIfContentExists(TLFacet facet, List<TLFacet> facetList) {
		if (new FacetCodegenDelegateFactory(null).getDelegate(facet).hasContent()) {
			facetList.add(facet);
		}
	}
	
    /**
     * Returns a list of "ghost facets" for the given owner. A ghost facet occurs when a contextual
     * facet (e.g. custom or query) with a certain context/label combination is declared on an
     * ancestor entity that is extended by the given 'facetOwner' but no corresponding entity is
     * declared on the 'facetOwner' itself. In these cases, we must compile XSD artifacts for an
     * inherited "ghost facet", even though it was not explicitly declared in the model.
     * 
     * @param facetOwner
     *            the facet owner for which to return "ghost facets"
     * @param facetType
     *            the type of ghost facets to retrieve
     * @return List<TLContextualFacet>
     */
    public static List<TLContextualFacet> findGhostFacets(TLFacetOwner facetOwner, TLFacetType facetType) {
        Set<String> inheritedFacetNames = new HashSet<>();
        List<TLContextualFacet> inheritedFacets = new ArrayList<>();
        TLFacetOwner extendedOwner = getFacetOwnerExtension(facetOwner);
        Set<TLFacetOwner> visitedOwners = new HashSet<>();

        // Find all of the inherited facets of the specified facet type
        while (extendedOwner != null) {
            List<TLFacet> facetList = getAllFacetsOfType(extendedOwner, facetType);
            
            for (TLFacet f : facetList) {
            	if (f instanceof TLContextualFacet) {
            		TLContextualFacet facet = (TLContextualFacet) f;
                    String facetKey = facetType.getIdentityName(facet.getName());

                    if (!inheritedFacetNames.contains(facetKey)) {
                        inheritedFacetNames.add(facetKey);
                        inheritedFacets.add(facet);
                    }
            	}
            }
            visitedOwners.add(extendedOwner);
            extendedOwner = getFacetOwnerExtension(extendedOwner);

            if (visitedOwners.contains(extendedOwner)) {
                break; // exit if we encounter a circular reference
            }
        }

        // At this point, the values in the 'inheritedFacets' map contains the set of uniquely-named
        // facets that are declared by the facetOwner's ancestors. Any ancestor facets that do not
        // have a matching item that is explicitly declared by the facetOwner should be considered a
        // ghost facet.
        List<TLContextualFacet> ghostFacets = new ArrayList<>();

        for (TLContextualFacet inheritedFacet : inheritedFacets) {
        	TLContextualFacet declaredFacet = (TLContextualFacet) getFacetOfType(
        			facetOwner, facetType, inheritedFacet.getName());

            if (declaredFacet == null) {
            	TLContextualFacet ghostFacet = new TLContextualFacet();
            	
            	if (inheritedFacet.isLocalFacet()) {
                    ghostFacet.setOwningLibrary(facetOwner.getOwningLibrary());
            		
            	} else {
                    ghostFacet.setOwningLibrary(inheritedFacet.getOwningLibrary());
            	}
                ghostFacet.setFacetType(facetType);
                ghostFacet.setName(inheritedFacet.getName());
                ghostFacet.setOwningEntity(facetOwner);
                ghostFacets.add(ghostFacet);
            }
        }
        return ghostFacets;
    }

    /**
     * Returns a list of "ghost facets" for the given resource. A ghost facet occurs when an action
     * facet with a certain name is declared on an ancestor resource that is extended by the given
     * 'resource' but no corresponding entity is declared on the 'resource' itself. In these cases,
     * we must compile XSD artifacts for an inherited "ghost facet", even though it was not explicitly
     * declared in the model.
     * 
     * @param resource  the resource for which to return "ghost facets"
     * @return List<TLActionFacet>
     */
    public static List<TLActionFacet> findGhostFacets(TLResource resource) {
        Set<String> inheritedFacetNames = new HashSet<>();
        List<TLActionFacet> inheritedFacets = new ArrayList<>();
        TLResource extendedResource = ResourceCodegenUtils.getExtendedResource(resource);
        Set<TLResource> visitedOwners = new HashSet<>();

        // Find all of the inherited facets of the specified facet type
        while (extendedResource != null) {
            List<TLActionFacet> facetList = extendedResource.getActionFacets();

            for (TLActionFacet facet : facetList) {
            	if (ResourceCodegenUtils.isTemplateActionFacet( facet )) {
            		continue; // Skip template action facets
            	}
                String facetKey = facet.getName();

                if (!inheritedFacetNames.contains(facetKey)) {
                    inheritedFacetNames.add(facetKey);
                    inheritedFacets.add(facet);
                }
            }
            visitedOwners.add(extendedResource);
            extendedResource = ResourceCodegenUtils.getExtendedResource(extendedResource);

            if (visitedOwners.contains(extendedResource)) {
                break; // exit if we encounter a circular reference
            }
        }

        // At this point, the values in the 'inheritedFacets' map contains the set of uniquely-named
        // facets that are declared by the facetOwner's ancestors. Any ancestor facets that do not
        // have a matching item that is explicitly declared by the facetOwner should be considered a
        // ghost facet.
        List<TLActionFacet> ghostFacets = new ArrayList<>();
    	
        for (TLActionFacet inheritedFacet : inheritedFacets) {
        	TLActionFacet declaredFacet = resource.getActionFacet(inheritedFacet.getName());

            if (declaredFacet == null) {
            	TLActionFacet ghostFacet = new TLActionFacet();

                ghostFacet.setOwningResource(resource);
                ghostFacet.setName(inheritedFacet.getName());
                ghostFacet.setReferenceType(inheritedFacet.getReferenceType());
                ghostFacet.setReferenceRepeat(inheritedFacet.getReferenceRepeat());
                ghostFacet.setReferenceFacetName(inheritedFacet.getReferenceFacetName());
                ghostFacet.setBasePayload(inheritedFacet.getBasePayload());
                ghostFacets.add(ghostFacet);
            }
        }
        return ghostFacets;
    }
    
    /**
     * Returns the list of all non-local ghost facets that should be included in the code
     * generation output for the given library.  The list of facets returned by this method
     * includes any nested ghost facets that might exist in the facet hierarchy.
     * 
     * @param library  the library for which to return non-local ghost-facets
     * @return List<TLContextualFacet>
     */
    public static List<TLContextualFacet> findNonLocalGhostFacets(TLLibrary library) {
    	List<TLContextualFacet> nonLocalFacets = new ArrayList<>();
    	List<TLFacetOwner> topLevelOwners = new ArrayList<>();
    	Map<TLFacetOwner,List<TLFacetOwner>> extensionRegistry;
    	
    	// Construct the collection top-level facet owners
    	for (TLContextualFacet facet : library.getContextualFacetTypes()) {
    		if (!facet.isLocalFacet()) {
    			TLFacetOwner owner = getTopLevelOwner( facet );
    			
    			if (!topLevelOwners.contains( owner )) {
    				topLevelOwners.add( owner );
    			}
    		}
    	}
    	extensionRegistry = buildExtensionRegistry( library.getOwningModel() );
    	
    	// Navigate through all of the extending entities to search for ghost facets
    	// that should be generated in our starting library
    	for (TLFacetOwner originalOwner : topLevelOwners) {
    		findNonLocalGhostFacets( originalOwner, library, nonLocalFacets,
    				extensionRegistry, new HashSet<TLFacetOwner>() );
    	}
    	return nonLocalFacets;
    }
    
    /**
     * Recursive routine that navigate through all of the extending entities to search
     * for ghost facets that should be generated in our starting library.
     * 
     * @param owner  the facet owner for which to navigate the extension hierarchy
     * @param originalLibrary  the original library for which non-local ghost facets should be identified
     * @param nonLocalFacets  the list of non-local facets that have been collected
     * @param extensionRegistry  the registry of extended-to-extending entities
     * @param visitedOwners  the list of visited owners (protection against circular references)
     */
    private static void findNonLocalGhostFacets(TLFacetOwner owner, TLLibrary originalLibrary,
    		List<TLContextualFacet> nonLocalFacets, Map<TLFacetOwner,List<TLFacetOwner>> extensionRegistry,
    		Set<TLFacetOwner> visitedOwners) {
    	if (!visitedOwners.contains( owner )) {
    		List<TLFacetOwner> extendingOwners = extensionRegistry.get( owner );
    		
    		visitedOwners.add( owner );
    		
    		if (extendingOwners != null) {
        		for (TLFacetOwner extendingOwner : extendingOwners) {
                	findNonLocalGhostFacets( extendingOwner, originalLibrary, nonLocalFacets );
                	findNonLocalGhostFacets( extendingOwner, originalLibrary, nonLocalFacets, extensionRegistry, visitedOwners );
        		}
    		}
    	}
    }
    
    /**
     * Recursively collects all non-local ghost facets for the given owner that should be generated
     * in the original library.
     * 
     * @param owner  the owner for which to collect non-local contextual facets
     * @param originalLibrary  the original library for which non-local ghost facets should be identified
     * @param nonLocalFacets  the list of non-local facets that have been collected
     */
    private static void findNonLocalGhostFacets(TLFacetOwner owner, TLLibrary originalLibrary,
    		List<TLContextualFacet> nonLocalFacets) {
    	for (TLFacetType facetType : getFacetTypes( owner )) {
        	List<TLContextualFacet> ghostFacets = findGhostFacets( owner, facetType );
    		
        	for (TLFacet realFacet : getAllFacetsOfType( owner, facetType )) {
        		if (realFacet instanceof TLContextualFacet) {
            		findNonLocalGhostFacets( (TLContextualFacet) realFacet, originalLibrary, nonLocalFacets );
        		}
        	}
        	for (TLContextualFacet ghostFacet : ghostFacets) {
        		if (!ghostFacet.isLocalFacet() && (ghostFacet.getOwningLibrary() == originalLibrary)) {
        			nonLocalFacets.add( ghostFacet );
        		}
        		findNonLocalGhostFacets( ghostFacet, originalLibrary, nonLocalFacets );
        	}
    	}
    }
    
    /**
     * Returns the list of facet types that are applicable to the given owner.
     * 
     * @param owner  the owner for which to return a list of facet types
     * @return TLFacetType[]
     */
    private static TLFacetType[] getFacetTypes(TLFacetOwner owner) {
    	TLFacetType[] typeList;
    	
    	if (owner instanceof TLBusinessObject) {
    		typeList = new TLFacetType[] { TLFacetType.CUSTOM, TLFacetType.QUERY, TLFacetType.UPDATE };
    		
    	} else if (owner instanceof TLChoiceObject) {
    		typeList = new TLFacetType[] { TLFacetType.CHOICE };
    		
    	} else if (owner instanceof TLContextualFacet) {
    		typeList = new TLFacetType[] { ((TLContextualFacet) owner).getFacetType() };
    		
    	} else {
    		typeList = new TLFacetType[] { TLFacetType.CUSTOM, TLFacetType.QUERY, TLFacetType.UPDATE };
    	}
    	return typeList;
    }
    
    /**
     * Constructs a map of all extension relationships within the given OTM model.  The
     * values in the resulting map represent all entities which extend the facet-owner key.
     * 
     * @param model  the model for which to build the extension registry
     * @return Map<TLFacetOwner,List<TLFacetOwner>>
     */
    private static Map<TLFacetOwner,List<TLFacetOwner>> buildExtensionRegistry(TLModel model) {
    	final Map<TLFacetOwner,List<TLFacetOwner>> registry = new HashMap<>();
    	ModelElementVisitor visitor = new ModelElementVisitorAdapter() {
    		@Override public boolean visitExtension(TLExtension extension) {
				TLExtensionOwner extendingEntity = extension.getOwner();
				NamedEntity extendedEntity = extension.getExtendsEntity();
				
				if ((extendingEntity instanceof TLFacetOwner) && (extendedEntity instanceof TLFacetOwner)) {
					List<TLFacetOwner> extendingList = registry.get( extendedEntity );
					
					if (extendingList == null) {
						extendingList = new ArrayList<>();
						registry.put( (TLFacetOwner) extendedEntity, extendingList );
					}
					extendingList.add( (TLFacetOwner) extendingEntity );
				}
				return true;
			}
		};
		
		ModelNavigator.navigate( model, visitor );
    	return registry;
    }
    
    /**
     * Returns the type of operation based on the configuration of the request, response,
     * and/or notification facets.
     * 
     * @param operation  the operation to be analyzed
     * @return OperationType
     */
    public static OperationType getOperationType(TLOperation operation) {
    	FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory(null);
        boolean hasRequest = factory.getDelegate(operation.getRequest()).hasContent();
        boolean hasResponse = factory.getDelegate(operation.getResponse()).hasContent();
        boolean hasNotification = factory.getDelegate(operation.getNotification()).hasContent();
        OperationType opType;

        if (hasRequest && !hasResponse && !hasNotification) {
            opType = OperationType.ONE_WAY;

        } else if (!hasRequest && !hasResponse && hasNotification) {
            opType = OperationType.NOTIFICATION;

        } else if (hasRequest && hasResponse && !hasNotification) {
            opType = OperationType.REQUEST_RESPONSE;

        } else if (hasRequest && !hasResponse && hasNotification) {
            opType = OperationType.SOLICIT_NOTIFICATION;

        } else if (hasRequest && hasResponse && hasNotification) {
            opType = OperationType.REQUEST_RESPONSE_WITH_NOTIFICATION;

        } else {
            opType = OperationType.INVALID;
        }
        return opType;
    }

}
