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

package org.opentravel.schemacompiler.codegen.example;

import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions.DetailLevel;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ExtensionPointRegistry;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;

/**
 * Navigator that traverses model elements in order to produce visitor events that will allow the construction of an
 * example data set for the element(s) that are visited.
 * 
 * @author S. Livezey
 */
public class ExampleNavigator {

    private static FacetCodegenDelegateFactory facetDelegateFactory = new FacetCodegenDelegateFactory( null );

    private Deque<Object> entityStack = new LinkedList<>();
    private ExtensionPointRegistry extensionPointRegistry;
    private Map<TLChoiceObject,List<TLFacet>> choiceFacetRotation = new HashMap<>();
    private ExampleGeneratorOptions options;
    private ExampleVisitor visitor;

    /**
     * Constructor that initializes the visitor to be notified when model elements are encountered during navigation.
     * 
     * @param visitor the visitor to be notified when model elements are encountered
     * @param options the options to use during example navigation
     * @param model the model which contains all of the entities to be navigated
     */
    public ExampleNavigator(ExampleVisitor visitor, ExampleGeneratorOptions options, TLModel model) {
        this.options = (options != null) ? options : new ExampleGeneratorOptions();
        this.visitor = visitor;
        this.extensionPointRegistry = new ExtensionPointRegistry( model );
    }

    /**
     * Navigates all dependencies of the given element in a depth-first fashion using the given visitor for notification
     * callbacks.
     * 
     * @param target the model element whose dependencies should be navigated
     * @param visitor the visitor to be notified when model elements are encountered
     * @param options the options to use during example navigation
     */
    public static void navigate(NamedEntity target, ExampleVisitor visitor, ExampleGeneratorOptions options) {
        new ExampleNavigator( visitor, options, target.getOwningModel() ).navigateEntity( target );
    }

    /**
     * Navigates the given <code>NamedEntity</code> and its sub-elements in a depth-first fashion.
     * 
     * @param target the named entity for which an example is to be generated
     */
    public void navigateEntity(NamedEntity target) {
        if (target instanceof TLSimple) {
            navigateSimple( (TLSimple) target );

        } else if (target instanceof TLClosedEnumeration) {
            navigateClosedEnumeration( (TLClosedEnumeration) target );

        } else if (target instanceof TLOpenEnumeration) {
            navigateOpenEnumeration( (TLOpenEnumeration) target );

        } else if (target instanceof TLRoleEnumeration) {
            navigateRoleEnumeration( (TLRoleEnumeration) target );

        } else if (target instanceof TLValueWithAttributes) {
            navigateValueWithAttributes( (TLValueWithAttributes) target );

        } else if (target instanceof TLCoreObject) {
            navigateCoreObject( (TLCoreObject) target );

        } else if (target instanceof TLBusinessObject) {
            navigateBusinessObject( (TLBusinessObject) target );

        } else if (target instanceof TLChoiceObject) {
            navigateChoiceObject( (TLChoiceObject) target );

        } else if (target instanceof TLActionFacet) {
            navigateActionFacet( (TLActionFacet) target );

        } else if (target instanceof TLFacet) {
            navigateFacet( (TLFacet) target );

        } else if (target instanceof TLListFacet) {
            navigateListFacet( (TLListFacet) target );

        } else if (target instanceof TLAlias) {
            navigateAlias( (TLAlias) target );

        } else if (target instanceof TLExtensionPointFacet) {
            navigateExtensionPointFacet( (TLExtensionPointFacet) target );

        } else if (target instanceof XSDComplexType) {
            navigateXSDComplexType( (XSDComplexType) target );

        } else if (target instanceof XSDElement) {
            navigateXSDElement( (XSDElement) target );
        }
    }

    /**
     * Called when a <code>TLSimple</code> instance is encountered during model navigation.
     * 
     * @param simple the simple entity to visit and navigate
     */
    public void navigateSimple(TLSimple simple) {
        navigateSimpleType( simple );
    }

    /**
     * Called when a <code>TLValueWithAttributes</code> instance is encountered during model navigation.
     * 
     * @param valueWithAttributes the value-with-attributes entity to visit and navigate
     */
    public void navigateValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        try {
            incrementRecursionCount( valueWithAttributes );

            if (canVisit( valueWithAttributes )) {
                visitor.startValueWithAttributes( valueWithAttributes );

                for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes( valueWithAttributes )) {
                    navigateAttribute( attribute );
                }
                for (TLIndicator indicator : PropertyCodegenUtils.getInheritedIndicators( valueWithAttributes )) {
                    navigateIndicator( indicator );
                }
                visitor.endValueWithAttributes( valueWithAttributes );
            }
        } finally {
            decrementRecursionCount( valueWithAttributes );
        }
    }

    /**
     * Called when a <code>TLOpenEnumeration</code> instance is encountered during model navigation.
     * 
     * @param enumeration the enumeration entity to visit and navigate
     */
    public void navigateOpenEnumeration(TLOpenEnumeration enumeration) {
        try {
            incrementRecursionCount( enumeration );

            if (canVisit( enumeration )) {
                visitor.startOpenEnumeration( enumeration );
                visitor.endOpenEnumeration( enumeration );
            }
        } finally {
            decrementRecursionCount( enumeration );
        }
    }

    /**
     * Called when a <code>TLRoleEnumeration</code> instance is encountered during model navigation.
     * 
     * @param enumeration the enumeration entity to visit and navigate
     */
    public void navigateRoleEnumeration(TLRoleEnumeration enumeration) {
        try {
            incrementRecursionCount( enumeration );

            if (canVisit( enumeration )) {
                visitor.startRoleEnumeration( enumeration );
                visitor.endRoleEnumeration( enumeration );
            }
        } finally {
            decrementRecursionCount( enumeration );
        }
    }

    /**
     * Called when a <code>TLClosedEnumeration</code> instance is encountered during model navigation.
     * 
     * @param enumeration the enumeration entity to visit and navigate
     */
    public void navigateClosedEnumeration(TLClosedEnumeration enumeration) {
        navigateSimpleType( enumeration );
    }

    /**
     * Called when a <code>TLBusinessObject</code> instance is encountered during model navigation.
     * 
     * @param businessObject the business object entity to visit and navigate
     */
    public void navigateBusinessObject(TLBusinessObject businessObject) {
        try {
            incrementRecursionCount( businessObject );

            if (canVisit( businessObject )) {
                TLAbstractFacet exampleFacet = selectExampleFacet( businessObject.getSummaryFacet() );

                if (exampleFacet instanceof TLFacet) {
                    navigateFacet( (TLFacet) exampleFacet );

                } else {
                    // Cannot happen - no simple facets for business objects
                }
            }
        } finally {
            decrementRecursionCount( businessObject );
        }
    }

    /**
     * Called when a <code>TLCoreObject</code> instance is encountered during model navigation.
     * 
     * @param coreObject the core object entity to visit and navigate
     */
    public void navigateCoreObject(TLCoreObject coreObject) {
        try {
            incrementRecursionCount( coreObject );

            if (canVisit( coreObject )) {
                TLAbstractFacet exampleFacet = selectExampleFacet( coreObject.getSummaryFacet() );

                if (exampleFacet instanceof TLFacet) {
                    navigateFacet( (TLFacet) exampleFacet );

                } else { // must be a simple facet
                    navigateSimpleFacet( (TLSimpleFacet) exampleFacet );
                }
            }
        } finally {
            decrementRecursionCount( coreObject );
        }
    }

    /**
     * Called when a <code>TLChoiceObject</code> instance is encountered during model navigation.
     * 
     * @param choiceObject the choice object entity to visit and navigate
     */
    public void navigateChoiceObject(TLChoiceObject choiceObject) {
        try {
            incrementRecursionCount( choiceObject );

            if (canVisit( choiceObject )) {
                TLAbstractFacet exampleFacet = selectExampleFacet( choiceObject.getSharedFacet() );

                if (exampleFacet instanceof TLFacet) {
                    navigateFacet( (TLFacet) exampleFacet );

                } else {
                    // Cannot happen - no simple facets for choice objects
                }
            }
        } finally {
            decrementRecursionCount( choiceObject );
        }
    }

    /**
     * Called when a <code>TLExtensionPointFacet</code> instance is encountered during model navigation.
     * 
     * @param extensionPointFacet the extension point facet entity to visit and navigate
     */
    public void navigateExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
        try {
            incrementRecursionCount( extensionPointFacet );

            if (canVisit( extensionPointFacet )) {

                visitor.startExtensionPointFacet( extensionPointFacet );

                for (TLAttribute attribute : extensionPointFacet.getAttributes()) {
                    navigateAttribute( attribute );
                }
                for (TLProperty element : extensionPointFacet.getElements()) {
                    navigateElement( element );
                }
                for (TLIndicator indicator : extensionPointFacet.getIndicators()) {
                    navigateIndicator( indicator );
                }
                visitor.endExtensionPointFacet( extensionPointFacet );
            }
        } finally {
            decrementRecursionCount( extensionPointFacet );
        }
    }

    /**
     * Called when a <code>TLFacet</code> instance is encountered during model navigation.
     * 
     * @param facet the facet entity to visit and navigate
     */
    public void navigateFacet(TLFacet facet) {
        TLFacet navFacet = facet;

        try {
            if (facet instanceof TLContextualFacet) {
                TLFacet preferredFacet = options.getPreferredFacet( (TLContextualFacet) facet );

                if (preferredFacet != null) {
                    navFacet = preferredFacet;
                }
            }

            incrementRecursionCount( navFacet );

            if (canVisit( navFacet )) {
                visitor.startFacet( navFacet );
                navigateFacetMembers( navFacet );
                visitor.endFacet( navFacet );
            }
        } finally {
            decrementRecursionCount( navFacet );
        }
    }

    /**
     * Called when a <code>TLListFacet</code> instance is encountered during model navigation.
     * 
     * @param listFacet the list facet entity to visit and navigate
     */
    public void navigateListFacet(TLListFacet listFacet) {
        try {
            incrementRecursionCount( listFacet );

            if (canVisit( listFacet )) {
                TLCoreObject facetOwner = (TLCoreObject) listFacet.getOwningEntity();
                TLAbstractFacet itemFacet = listFacet.getItemFacet();

                if ((itemFacet instanceof TLFacet) && !facetOwner.getRoleEnumeration().getRoles().isEmpty()) {
                    for (TLRole role : facetOwner.getRoleEnumeration().getRoles()) {
                        visitor.startListFacet( listFacet, role );
                        navigateFacetMembers( (TLFacet) itemFacet );
                        visitor.endListFacet( listFacet, role );
                    }
                } else { // must be a simple facet
                    visitor.startListFacet( listFacet, null );
                    navigateSimpleFacet( (TLSimpleFacet) itemFacet );
                    visitor.endListFacet( listFacet, null );
                }
            }
        } finally {
            decrementRecursionCount( listFacet );
        }
    }

    /**
     * Called when a <code>TLFacetSimple</code> instance is encountered during model navigation.
     * 
     * @param facet the simple facet entity to visit and navigate
     */
    public void navigateSimpleFacet(TLSimpleFacet facet) {
        navigateSimpleType( facet );
    }

    /**
     * Called when a <code>TLActionFacet</code> instance is encountered during model navigation.
     * 
     * @param actionFacet the action facet entity to visit and navigate
     */
    public void navigateActionFacet(TLActionFacet actionFacet) {
        NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( actionFacet );

        if (payloadType instanceof TLCoreObject) {
            TLAbstractFacet exampleFacet = selectExampleFacet( ((TLCoreObject) payloadType).getSummaryFacet() );

            if (exampleFacet instanceof TLFacet) {
                navigateFacet( (TLFacet) exampleFacet );
            }

        } else if (payloadType instanceof TLChoiceObject) {
            TLAbstractFacet exampleFacet = selectExampleFacet( ((TLChoiceObject) payloadType).getSharedFacet() );

            if (exampleFacet instanceof TLFacet) {
                navigateFacet( (TLFacet) exampleFacet );
            }

        } else if (payloadType instanceof TLActionFacet) {
            try {
                incrementRecursionCount( actionFacet );

                if (canVisit( actionFacet )) {
                    NamedEntity basePayload = actionFacet.getBasePayload();
                    TLFacet payloadFacet = null;

                    if (basePayload instanceof TLCoreObject) {
                        payloadFacet = (TLFacet) selectExampleFacet( ((TLCoreObject) basePayload).getSummaryFacet() );

                    } else if (basePayload instanceof TLChoiceObject) {
                        payloadFacet = (TLFacet) selectExampleFacet( ((TLChoiceObject) basePayload).getSharedFacet() );
                    }
                    visitor.startActionFacet( actionFacet, payloadFacet );
                    navigateFacetMembers( actionFacet, payloadFacet );
                    visitor.endActionFacet( actionFacet, payloadFacet );
                }
            } finally {
                decrementRecursionCount( actionFacet );
            }
        }
    }

    /**
     * Called when a <code>TLAlias</code> instance is encountered during model navigation.
     * 
     * @param alias the alias entity to visit and navigate
     */
    public void navigateAlias(TLAlias alias) {
        try {
            // Depending upon what owns the alias, we may need to adjust the level of detail that
            // is displayed by the owning facet. To accomplish this, we will need to locate the
            // corresponding facet on the example facet (e.g. "CoreAlias" would correspond to
            // "CoreAliasDetail".
            TLAliasOwner aliasOwner = alias.getOwningEntity();
            TLAbstractFacet exampleFacet = null;

            if (aliasOwner instanceof TLBusinessObject) {
                exampleFacet = selectExampleFacet( ((TLBusinessObject) aliasOwner).getIdFacet() );
                alias = AliasCodegenUtils.getFacetAlias( alias, exampleFacet.getFacetType() );

            } else if (aliasOwner instanceof TLCoreObject) {
                exampleFacet = selectExampleFacet( ((TLCoreObject) aliasOwner).getSummaryFacet() );
                alias = AliasCodegenUtils.getFacetAlias( alias, exampleFacet.getFacetType() );
            }

            // Now perform the alias visitation normally
            incrementRecursionCount( alias );

            if (canVisit( alias )) {
                visitor.startAlias( alias );
                navigateEntity( alias.getOwningEntity() );
                visitor.endAlias( alias );
            }
        } finally {
            decrementRecursionCount( alias );
        }
    }

    /**
     * Called when a <code>XSDSimpleType</code> instance is encountered during model navigation.
     * 
     * @param xsdSimple the simple entity to visit and navigate
     */
    public void navigateXSDSimple(XSDSimpleType xsdSimple) {
        navigateSimpleType( xsdSimple );
    }

    /**
     * Called when a <code>XSDComplexType</code> instance is encountered during model navigation.
     * 
     * @param xsdComplex the XSD complex-type entity to visit and navigate
     */
    public void navigateXSDComplexType(XSDComplexType xsdComplex) {
        try {
            incrementRecursionCount( xsdComplex );

            if (canVisit( xsdComplex )) {
                visitor.startXsdComplexType( xsdComplex );
                visitor.endXsdComplexType( xsdComplex );
            }
        } finally {
            decrementRecursionCount( xsdComplex );
        }
    }

    /**
     * Called when a <code>XSDElement</code> instance is encountered during model navigation.
     * 
     * @param xsdElement the XSD element entity to visit and navigate
     */
    public void navigateXSDElement(XSDElement xsdElement) {
        try {
            incrementRecursionCount( xsdElement );

            if (canVisit( xsdElement )) {
                visitor.startXsdElement( xsdElement );
                visitor.endXsdElement( xsdElement );
            }
        } finally {
            decrementRecursionCount( xsdElement );
        }
    }

    /**
     * Increments the recursion count for the given object by 1.
     * 
     * @param obj the object being traversed
     */
    protected void incrementRecursionCount(Object obj) {
        if (obj != null) {
            entityStack.push( obj );
        }
    }

    /**
     * Decrements the recursion count for the given object by 1.
     * 
     * @param obj the object being traversed
     */
    protected void decrementRecursionCount(Object obj) {
        if (obj != null) {
            if (entityStack.isEmpty() || (entityStack.peek() == obj)) {
                entityStack.pop();
            } else {
                // This originally caused an IllegalStateException, but that ended
                // up masking the original exception. Now, the error is simply ignored.
            }
        }
    }

    /**
     * Returns true if the given object can be visited based on its recursion count within the current depth-first
     * navigation structure.
     * 
     * @param obj the object to be visited
     * @return boolean
     */
    protected boolean canVisit(Object obj) {
        boolean visitationAllowed = (obj != null);

        if (visitationAllowed) {
            if (entityStack.peek() != obj) {
                throw new IllegalStateException(
                    "Cannot perform a visitation check on an entity before calling 'incrementRecursionCount()'." );
            }
            int maxRecursion = Math.max( 1, options.getMaxRecursionDepth() );
            int recursionCount = 0;

            for (Object visitedEntity : entityStack) {
                if (visitedEntity == obj) {
                    recursionCount++;
                }
            }
            visitationAllowed = (recursionCount <= maxRecursion);
        }
        return visitationAllowed;
    }

    /**
     * Navigates the given <code>TLAttributeType</code> (i.e. simple type).
     * 
     * <p>
     * NOTE: Since simple types are capable of directly generating examples, no depth-first traversal is performed by
     * this method.
     * 
     * @param simpleType the simple type to be navigated
     */
    protected void navigateSimpleType(TLAttributeType simpleType) {
        visitor.visitSimpleType( simpleType );
    }

    /**
     * Recursively navigates the members (attributes, elements, and indicators) of the given facet. The navigated
     * members that are inherited from higher-level members of the same owner, as well as members that are inherited
     * from extended core/business objects.
     * 
     * @param facet the facet whose members are to be navigated
     */
    protected void navigateFacetMembers(TLFacet facet) {
        Map<TLFacetType,List<TLExtensionPointFacet>> facetExtensionsByType =
            extensionPointRegistry.getAllExtensionPoints( facet );
        Set<TLFacetType> processedExtensionPointTypes = new HashSet<>();
        String previousFacetIdentity = null;

        // Start by navigating attributes and indicators for this facet
        for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes( facet )) {
            if (attribute.isMandatory() || !options.isSuppressOptionalFields()) {
                navigateAttribute( attribute );
            }
        }
        for (TLIndicator indicator : PropertyCodegenUtils.getInheritedIndicators( facet )) {
            if (!indicator.isPublishAsElement() && !options.isSuppressOptionalFields()) {
                navigateIndicator( indicator );
            }
        }

        // Navigate the elements (properties) and extension points for this facet
        for (TLModelElement elementItem : PropertyCodegenUtils.getElementSequence( facet )) {
            previousFacetIdentity = navigateElementsAndExtensionPoints( elementItem, previousFacetIdentity,
                facetExtensionsByType, processedExtensionPointTypes );
        }

        // Wrap up by checking for any extension points for the current facet
        // (take into account
        // that the facet may not contain any properties and therefore may not
        // have checked for
        // extension points yet).
        List<TLFacet> facetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy( facet );

        for (TLFacet hFacet : facetHierarchy) {
            if (!processedExtensionPointTypes.contains( hFacet.getFacetType() )
                && extensionPointRegistry.hasExtensionPoint( hFacet )) {
                navigateExtensionPoint( hFacet, facetExtensionsByType.get( hFacet.getFacetType() ) );
            }
        }
    }

    /**
     * Recursively navigates the members (attributes, elements, and indicators) of the given action facet.
     * 
     * @param actionFacet the action facet whose members are to be navigated
     * @param payloadFacet the facet that will supply the members beyond the business object reference
     */
    protected void navigateFacetMembers(TLActionFacet actionFacet, TLFacet payloadFacet) {
        TLProperty boProperty = ResourceCodegenUtils.createBusinessObjectElement( actionFacet, payloadFacet );

        if (boProperty != null) {
            navigateElement( boProperty );
        }
        if (payloadFacet != null) {
            navigateFacetMembers( payloadFacet );
        }
    }

    /**
     * Navigate model elements and extension point facets.
     * 
     * @param elementItem the model element to navigate
     * @param previousFacetIdentity identity of the previously navigated facet
     * @param facetExtensionsByType map of extension point facets collated by facet type
     * @param processedExtensionPointTypes the set of previously processed extension point types (to prevent infinite
     *        loops)
     * @return String
     */
    private String navigateElementsAndExtensionPoints(TLModelElement elementItem, String previousFacetIdentity,
        Map<TLFacetType,List<TLExtensionPointFacet>> facetExtensionsByType,
        Set<TLFacetType> processedExtensionPointTypes) {
        if (elementItem instanceof TLProperty) {
            TLProperty element = (TLProperty) elementItem;
            TLFacet currentFacet = (TLFacet) element.getOwner();
            String currentFacetIdentity = extensionPointRegistry.getFacetIdentity( currentFacet );

            // Before navigating the element itself, check to see if we need
            // to insert any extension point facets
            if (!currentFacetIdentity.equals( previousFacetIdentity )) {
                previousFacetIdentity = navigateExtensionPointFacets( currentFacet, currentFacetIdentity,
                    facetExtensionsByType, processedExtensionPointTypes );
            }

            // Navigate the example content for the current element
            if (element.isMandatory() || !options.isSuppressOptionalFields()) {
                navigateElement( element );
            }

        } else if ((elementItem instanceof TLIndicator) && !options.isSuppressOptionalFields()) {
            navigateIndicator( (TLIndicator) elementItem );
        }
        return previousFacetIdentity;
    }

    /**
     * Navigates the extension point facets for the current facet element.
     * 
     * @param currentFacet the facet currently being navigated
     * @param currentFacetIdentity identity of the current facet
     * @param facetExtensionsByType map of extension point facets collated by facet type
     * @param processedExtensionPointTypes the set of previously processed extension point types (to prevent infinite
     *        loops)
     * @return String
     */
    private String navigateExtensionPointFacets(TLFacet currentFacet, String currentFacetIdentity,
        Map<TLFacetType,List<TLExtensionPointFacet>> facetExtensionsByType,
        Set<TLFacetType> processedExtensionPointTypes) {
        String previousFacetIdentity;
        List<TLFacet> facetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy( currentFacet );

        // Ignore the last element in the facet hierarchy list since
        // it is always the current facet we are processing
        for (int i = 0; i < (facetHierarchy.size() - 1); i++) {
            TLFacet hFacet = facetHierarchy.get( i );

            if (!processedExtensionPointTypes.contains( hFacet.getFacetType() )) {
                if (extensionPointRegistry.hasExtensionPoint( hFacet )) {
                    navigateExtensionPoint( hFacet, facetExtensionsByType.get( hFacet.getFacetType() ) );
                }
                processedExtensionPointTypes.add( hFacet.getFacetType() );
            }
        }
        previousFacetIdentity = currentFacetIdentity;
        return previousFacetIdentity;
    }

    /**
     * Navigates the specified extensions of the facet.
     * 
     * @param facet the facet whose extension points are to be navigated
     * @param facetExtensions the list of extension points for the facet
     */
    private void navigateExtensionPoint(TLFacet facet, List<TLExtensionPointFacet> facetExtensions) {
        if ((facetExtensions != null) && !facetExtensions.isEmpty()) {
            visitor.startExtensionPoint( facet );

            for (TLExtensionPointFacet xpFacet : facetExtensions) {
                navigateExtensionPointFacet( xpFacet );
            }
            visitor.endExtensionPoint( facet );
        }
    }

    /**
     * Called when a <code>TLAttribute</code> instance is encountered during model navigation.
     * 
     * @param attribute the attribute entity to visit and navigate
     */
    protected void navigateAttribute(TLAttribute attribute) {
        try {
            incrementRecursionCount( attribute );

            if (canVisit( attribute )) {
                TLPropertyType attributeType = PropertyCodegenUtils.getAttributeType( attribute );

                if (!PropertyCodegenUtils.isEmptyStringType( attributeType )) {
                    visitor.startAttribute( attribute );

                    if (attribute.isReference()) {
                        boolean multipleValues = (attribute.getReferenceRepeat() > 1);

                        navigateSimpleType( getSchemaForSchemasType( attribute.getOwningModel(),
                            multipleValues ? "IDREFS" : "IDREF" ) );

                    } else {
                        navigateSimpleType( (TLAttributeType) attributeType );
                    }
                    visitor.endAttribute( attribute );
                }
            }
        } finally {
            decrementRecursionCount( attribute );
        }
    }

    /**
     * Called when a <code>TLProperty</code> instance is encountered during model navigation.
     * 
     * @param element the element entity to visit and navigate
     */
    protected void navigateElement(TLProperty element) {
        try {
            incrementRecursionCount( element );

            if (canVisit( element )) {
                if (element.isReference()) {
                    navigateReferenceElement( element );

                } else { // normal (non-reference) element
                    navigateNonReferenceElement( element );
                }
            }
        } finally {
            decrementRecursionCount( element );
        }
    }

    /**
     * Navigates a non-reference element from the model.
     * 
     * @param element the element to be navigated
     */
    private void navigateNonReferenceElement(TLProperty element) {
        int maxRepeatCount = Math.max( 1, options.getMaxRepeat() );
        int repeatCount =
            (element.getRepeat() < 0) ? maxRepeatCount : Math.max( 1, Math.min( maxRepeatCount, element.getRepeat() ) );
        TLPropertyType propertyType = element.getType();

        // If the property type is a core object, select the appropriate level of detail
        // based on the navigation options
        if (propertyType instanceof TLCoreObject) {
            propertyType = selectExampleFacet( ((TLCoreObject) propertyType).getSummaryFacet() );
        }

        // If the property type is a list facet, use a repeat count of 1 since the
        // repeat will be handled during the list facet visitation
        if (propertyType instanceof TLListFacet) {
            repeatCount = 1;
        }

        // Repeat the navigation as many times as required by the property and/or the
        // navigation options
        for (int i = 0; i < repeatCount; i++) {
            boolean isAttributeType;

            if (propertyType instanceof TLListFacet) {
                isAttributeType = (((TLListFacet) propertyType).getItemFacet() instanceof TLAttributeType);
            } else {
                isAttributeType = !(propertyType instanceof TLRole) && !(propertyType instanceof TLValueWithAttributes)
                    && (propertyType instanceof TLAttributeType);
            }
            visitor.startElement( element );

            if (isAttributeType) {
                navigateSimpleType( (TLAttributeType) propertyType );

            } else { // complex entity type
                navigateEntity( propertyType );
            }
            visitor.endElement( element );
        }
    }

    /**
     * Navigates a reference element from the model.
     * 
     * @param element the element to be navigated
     */
    private void navigateReferenceElement(TLProperty element) {
        boolean multipleValues = (element.getRepeat() > 1);

        visitor.startElement( element );
        navigateSimpleType( getSchemaForSchemasType( element.getOwningModel(), multipleValues ? "IDREFS" : "IDREF" ) );
        visitor.endElement( element );
    }

    /**
     * Called when a <code>TLIndicator</code> instance is encountered during model navigation.
     * 
     * @param indicator the indicator entity to visit and navigate
     */
    protected void navigateIndicator(TLIndicator indicator) {
        try {
            incrementRecursionCount( indicator );

            if (canVisit( indicator )) {
                boolean isAttribute =
                    !indicator.isPublishAsElement() || (indicator.getOwner() instanceof TLValueWithAttributes);

                if (isAttribute) {
                    visitor.startIndicatorAttribute( indicator );
                    visitor.endIndicatorAttribute( indicator );

                } else {
                    visitor.startIndicatorElement( indicator );
                    visitor.endIndicatorElement( indicator );
                }
            }
        } finally {
            decrementRecursionCount( indicator );
        }
    }

    /**
     * Returns the facet that should be used for the purposes of producing example data in the generated output.
     * Depending upon the level of detail selected in the <code>ExampleGeneratorOptions</code>, the facet that is
     * returned may or may not be the one that is passed to this method.
     * 
     * @param defaultFacet the facet with the default amount of detail
     * @return TLAbstractFacet
     */
    protected TLAbstractFacet selectExampleFacet(TLFacet defaultFacet) {
        TLFacetOwner facetOwner = defaultFacet.getOwningEntity();
        TLAbstractFacet exampleFacet = options.getPreferredFacet( facetOwner );

        if (exampleFacet == null) { // no preferred facet assigned
            if (defaultFacet.getFacetType().isContextual()) {
                exampleFacet = defaultFacet;

            } else {
                if (options.getDetailLevel() == DetailLevel.MAXIMUM) {
                    if (facetOwner instanceof TLChoiceObject) {
                        exampleFacet = selectChoiceFacet( (TLChoiceObject) facetOwner );
                    } else {
                        exampleFacet = getMaximumDetail( facetOwner );
                    }
                } else {
                    exampleFacet = getMinimumDetail( defaultFacet );
                }
            }
        }
        return exampleFacet;
    }

    /**
     * Returns the list facet that should be used for the purposes of producing example data in the generated output.
     * Depending upon the level of detail selected in the <code>ExampleGeneratorOptions</code>, the list facet that is
     * returned may or may not be the one that is passed to this method.
     * 
     * @param preferredFacet the list facet with the preferred amount of detail (per the current model configuration)
     * @return TLListFacet
     */
    protected TLListFacet selectExampleListFacet(TLListFacet preferredFacet) {
        TLListFacet exampleFacet;

        if (options.getDetailLevel() == DetailLevel.MAXIMUM) {
            exampleFacet = getMaximumListDetail( (TLCoreObject) preferredFacet.getOwningEntity() );
        } else {
            exampleFacet = getMinimumListDetail( preferredFacet );
        }
        return exampleFacet;
    }

    /**
     * Returns a choice facet from the list of available facets that publish content. Successive calls to this method
     * will rotate through the available facets.
     * 
     * @param choiceObject the choice object from which to return a facet
     * @return TLFacet
     */
    protected TLFacet selectChoiceFacet(TLChoiceObject choiceObject) {
        FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory( null );
        List<TLFacet> choiceFacets = choiceFacetRotation.get( choiceObject );
        TLFacet selectedChoice;

        if (choiceFacets == null) {
            choiceFacets = new ArrayList<>();

            for (TLFacet choiceFacet : choiceObject.getChoiceFacets()) {
                FacetCodegenDelegate<TLFacet> delegate = factory.getDelegate( choiceFacet );

                if (((delegate != null) && delegate.hasContent()) || choiceFacet.declaresContent()) {
                    choiceFacets.add( choiceFacet );
                }
            }
        }
        if (choiceFacets.isEmpty()) {
            selectedChoice = choiceObject.getSharedFacet();

        } else {
            selectedChoice = choiceFacets.remove( 0 );
            choiceFacets.add( selectedChoice );
        }
        return selectedChoice;
    }

    /**
     * Returns the non-empty facet from the owner with the maximum amount of detail.
     * 
     * @param facetOwner the facet owner from which to retrieve the most detailed facet
     * @return TLAbstractFacet
     */
    private TLAbstractFacet getMaximumDetail(TLFacetOwner facetOwner) {
        List<TLAbstractFacet> candidateFacets = new ArrayList<>();
        TLAbstractFacet result = null;

        if (facetOwner instanceof TLBusinessObject) {
            TLBusinessObject businessObject = (TLBusinessObject) facetOwner;

            candidateFacets.add( businessObject.getDetailFacet() );
            candidateFacets.add( businessObject.getSummaryFacet() );
            candidateFacets.add( businessObject.getIdFacet() );

        } else if (facetOwner instanceof TLCoreObject) {
            TLCoreObject coreObject = (TLCoreObject) facetOwner;

            candidateFacets.add( coreObject.getDetailFacet() );
            candidateFacets.add( coreObject.getSummaryFacet() );
            candidateFacets.add( coreObject.getSimpleFacet() );
        }

        // Get the maximum level of detail that publishes content
        for (TLAbstractFacet candidate : candidateFacets) {
            FacetCodegenDelegate<TLAbstractFacet> delegate = facetDelegateFactory.getDelegate( candidate );
            boolean done = false;

            if (delegate != null) {
                if (delegate.hasContent()) {
                    result = candidate;
                    done = true;
                }
            } else if (candidate.declaresContent()) {
                result = candidate;
                done = true;
            }
            if (done) {
                break;
            }
        }

        // Last Resort - Use the first candidate facet in the list
        if ((result == null) && !candidateFacets.isEmpty()) {
            result = candidateFacets.get( 0 );
        }
        return result;
    }

    /**
     * Returns the non-empty list facet from the owner with the maximum amount of detail.
     * 
     * @param facetOwner the facet owner from which to retrieve the most detailed facet
     * @return TLAbstractFacet
     */
    private TLListFacet getMaximumListDetail(TLCoreObject facetOwner) {
        List<TLListFacet> candidateFacets = new ArrayList<>();
        TLListFacet result = null;

        candidateFacets.add( facetOwner.getDetailListFacet() );
        candidateFacets.add( facetOwner.getSummaryListFacet() );

        for (TLListFacet candidate : candidateFacets) {
            if (candidate.declaresContent()) {
                result = candidate;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the non-empty facet from the owner with the minimum amount of detail, but with at least as much detail as
     * the given preferred facet.
     * 
     * @param preferredFacet the facet with the preferred amount of detail
     * @return TLAbstractFacet
     */
    private TLAbstractFacet getMinimumDetail(TLFacet preferredFacet) {
        TLAbstractFacet result = preferredFacet;

        if ((preferredFacet != null) && !preferredFacet.declaresContent()) {
            TLFacetOwner facetOwner = preferredFacet.getOwningEntity();
            List<TLAbstractFacet> candidateFacets = new ArrayList<>();

            if (facetOwner instanceof TLBusinessObject) {
                getCandidateFacets( preferredFacet, candidateFacets, (TLBusinessObject) facetOwner );

            } else if (facetOwner instanceof TLCoreObject) {
                getCandidateFacets( preferredFacet, candidateFacets, (TLCoreObject) facetOwner );
            }

            for (TLAbstractFacet candidate : candidateFacets) {
                if (candidate.declaresContent()) {
                    result = candidate;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Assembles the list of candidate facets for the given core object.
     * 
     * @param preferredFacet the preferred facet type
     * @param candidateFacets the list of candidate facets being assembled
     * @param coreObject the core object from which to obtain the list of candidate facets
     */
    private void getCandidateFacets(TLFacet preferredFacet, List<TLAbstractFacet> candidateFacets,
        TLCoreObject coreObject) {
        switch (preferredFacet.getFacetType()) {
            case DETAIL:
                candidateFacets.add( 0, coreObject.getDetailFacet() );
                candidateFacets.add( 0, coreObject.getSummaryFacet() );
                candidateFacets.add( 0, coreObject.getSimpleFacet() );
                break;
            case SUMMARY:
                candidateFacets.add( 0, coreObject.getSummaryFacet() );
                candidateFacets.add( 0, coreObject.getSimpleFacet() );
                break;
            case SIMPLE:
                candidateFacets.add( 0, coreObject.getSimpleFacet() );
                break;
            default:
                break;
        }
    }

    /**
     * Assembles the list of candidate facets for the given business object.
     * 
     * @param preferredFacet the preferred facet type
     * @param candidateFacets the list of candidate facets being assembled
     * @param businessObject the business object from which to obtain the list of candidate facets
     */
    private void getCandidateFacets(TLFacet preferredFacet, List<TLAbstractFacet> candidateFacets,
        TLBusinessObject businessObject) {
        switch (preferredFacet.getFacetType()) {
            case DETAIL:
                candidateFacets.add( 0, businessObject.getDetailFacet() );
                candidateFacets.add( 0, preferredFacet );
                candidateFacets.add( 0, businessObject.getSummaryFacet() );
                candidateFacets.add( 0, businessObject.getIdFacet() );
                break;
            case CUSTOM:
                candidateFacets.add( 0, preferredFacet );
                candidateFacets.add( 0, businessObject.getSummaryFacet() );
                candidateFacets.add( 0, businessObject.getIdFacet() );
                break;
            case SUMMARY:
                candidateFacets.add( 0, businessObject.getSummaryFacet() );
                candidateFacets.add( 0, businessObject.getIdFacet() );
                break;
            case ID:
                candidateFacets.add( 0, businessObject.getIdFacet() );
                break;
            default:
                break;
        }
    }

    /**
     * Returns the non-empty list facet from the owner with the minimum amount of detail, but with at least as much
     * detail as the given preferred facet.
     * 
     * @param preferredFacet the list facet with the preferred amount of detail
     * @return TLListFacet
     */
    private TLListFacet getMinimumListDetail(TLListFacet preferredFacet) {
        TLListFacet result = preferredFacet;

        if ((preferredFacet != null) && !preferredFacet.declaresContent()) {
            TLCoreObject facetOwner = (TLCoreObject) preferredFacet.getOwningEntity();
            List<TLListFacet> candidateFacets = new ArrayList<>();

            switch (preferredFacet.getFacetType()) {
                case DETAIL:
                    candidateFacets.add( 0, facetOwner.getDetailListFacet() );
                    candidateFacets.add( 0, facetOwner.getSummaryListFacet() );
                    break;
                case SUMMARY:
                    candidateFacets.add( 0, facetOwner.getSummaryListFacet() );
                    break;
                default:
                    break;
            }

            for (TLListFacet candidate : candidateFacets) {
                if (candidate.declaresContent()) {
                    result = candidate;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the entity with the specified name type name from the schema-for-schemas built-in of the given model.
     * 
     * @param model the model to search
     * @param xsdType the local name of the type to return from the XML schema namespace
     * @return TLAttributeType
     */
    private TLAttributeType getSchemaForSchemasType(TLModel model, String xsdType) {
        TLAttributeType xsdEntity = null;

        for (BuiltInLibrary builtIn : model.getBuiltInLibraries()) {
            if (builtIn.getNamespace().equals( XMLConstants.W3C_XML_SCHEMA_NS_URI )) {
                xsdEntity = (TLAttributeType) builtIn.getNamedMember( xsdType );

                if (xsdEntity != null) {
                    break;
                }
            }
        }
        return xsdEntity;
    }

}
