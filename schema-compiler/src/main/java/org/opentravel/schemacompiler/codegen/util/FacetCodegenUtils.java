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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.OperationType;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Static utility methods used during the generation of code output for facets.
 * 
 * @author S. Livezey
 */
public class FacetCodegenUtils {

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
        List<TLFacet> facetList = new ArrayList<TLFacet>();

        if (facetType.isContextual()) {
            if (owner instanceof TLBusinessObject) {
                switch (facetType) {
                    case CUSTOM:
                        facetList.addAll(((TLBusinessObject) owner).getCustomFacets());
                        break;
                    case QUERY:
                        facetList.addAll(((TLBusinessObject) owner).getQueryFacets());
                        break;
					default:
						break;
                }
            } else if (owner instanceof TLChoiceObject) {
            	if (facetType == TLFacetType.CHOICE) {
            		facetList.addAll(((TLChoiceObject) owner).getChoiceFacets());
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
     * @param owner
     *            the facet owner from which to return a member facet
     * @param facetType
     *            the type of member facet to return
     * @return TLFacet
     */
    public static TLFacet getFacetOfType(TLFacetOwner owner, TLFacetType facetType) {
        return getFacetOfType(owner, facetType, null, null);
    }

    /**
     * Returns a facet of the specified type from the given owner. If no such facet is available
     * from the owner, this method will return null.
     * 
     * @param owner
     *            the facet owner from which to return a member facet
     * @param facetType
     *            the type of member facet to return
     * @param facetContext
     *            the context ID of the facet to return (only used for contextual facets)
     * @param facetLabel
     *            the label of the facet to return (only used for contextual or action facets)
     * @return TLFacet
     */
    public static TLFacet getFacetOfType(TLFacetOwner owner, TLFacetType facetType,
            String facetContext, String facetLabel) {
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
                    memberFacet = findContextualFacet(boOwner.getCustomFacets(), facetContext,
                            facetLabel);
                    break;
                case QUERY:
                    memberFacet = findContextualFacet(boOwner.getQueryFacets(), facetContext,
                            facetLabel);
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
        			memberFacet = findContextualFacet(choiceOwner.getChoiceFacets(), facetContext, facetLabel);
        			break;
				default:
					memberFacet = null;
					break;
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
     * Returns the contextual facet from the list with the specified context and label value. If no
     * such facet exists, this method will return null.
     * 
     * @param facetList
     *            the list of facets from which to select a member
     * @param facetContext
     *            the context ID of the facet to return
     * @param facetLabel
     *            the label of the facet to return
     * @return TLFacet
     */
    private static TLFacet findContextualFacet(List<TLFacet> facetList, String facetContext,
            String facetLabel) {
        TLFacet memberFacet = null;

        for (TLFacet facet : facetList) {
            String facetIdentity = facet.getFacetType().getIdentityName(facet.getContext(),
                    facet.getLabel());
            String testIdentity = facet.getFacetType().getIdentityName(facetContext, facetLabel);

            if ((facetIdentity != null) && facetIdentity.equals(testIdentity)) {
                memberFacet = facet;
                break;
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
            
        } else if (owner instanceof TLOperation) {
            result = !((TLOperation) owner).isNotExtendable();
        }
        return result;
    }

    /**
     * Returns the <code>TLFacetOwner</code> instance that is extended by the facet owner that is
     * passed to this method. If the owner does not extend another model entity, this method will
     * return null.
     * 
     * @param owner
     *            the facet owner for which to return the extended entity
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
        }
        return result;
    }

    /**
     * Returns the inheritance hierarchy for facets with the same owner. For example, passing the
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
        List<TLFacet> localHierarchy = new ArrayList<TLFacet>();
        TLFacetOwner facetOwner = facet.getOwningEntity();

        localHierarchy.add(facet); // start by including the facet that was passed to this method

        if (facetOwner instanceof TLBusinessObject) {
            switch (facet.getFacetType()) {
                case DETAIL:
                case CUSTOM:
                    localHierarchy.add(0, getFacetOfType(facetOwner, TLFacetType.SUMMARY));
                case SUMMARY:
                    localHierarchy.add(0, getFacetOfType(facetOwner, TLFacetType.ID));
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
        } else if (facetOwner instanceof TLOperation) {
            // No detail hierarchy within an operation's facets
        }
        return localHierarchy;
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
     * @return List<TLFacet>
     */
    public static List<TLFacet> findGhostFacets(TLFacetOwner facetOwner, TLFacetType facetType) {
        Set<String> inheritedFacetNames = new HashSet<String>();
        List<TLFacet> inheritedFacets = new ArrayList<TLFacet>();
        TLFacetOwner extendedOwner = getFacetOwnerExtension(facetOwner);
        Set<TLFacetOwner> visitedOwners = new HashSet<TLFacetOwner>();

        // Find all of the inherited facets of the specified facet type
        while (extendedOwner != null) {
            List<TLFacet> facetList = getAllFacetsOfType(extendedOwner, facetType);

            for (TLFacet facet : facetList) {
                String facetKey = facetType.getIdentityName(facet.getContext(), facet.getLabel());

                if (!inheritedFacetNames.contains(facetKey)) {
                    inheritedFacetNames.add(facetKey);
                    inheritedFacets.add(facet);
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
        List<TLFacet> ghostFacets = new ArrayList<TLFacet>();

        for (TLFacet inheritedFacet : inheritedFacets) {
            TLFacet declaredFacet = getFacetOfType(facetOwner, facetType,
                    inheritedFacet.getContext(), inheritedFacet.getLabel());

            if (declaredFacet == null) {
                TLFacet ghostFacet = new TLFacet();

                ghostFacet.setFacetType(facetType);
                ghostFacet.setContext(inheritedFacet.getContext());
                ghostFacet.setLabel(inheritedFacet.getLabel());
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
        Set<String> inheritedFacetNames = new HashSet<String>();
        List<TLActionFacet> inheritedFacets = new ArrayList<TLActionFacet>();
        TLResource extendedResource = ResourceCodegenUtils.getExtendedResource(resource);
        Set<TLResource> visitedOwners = new HashSet<TLResource>();

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
        List<TLActionFacet> ghostFacets = new ArrayList<TLActionFacet>();
    	
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
        OperationType opType = OperationType.INVALID;

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
