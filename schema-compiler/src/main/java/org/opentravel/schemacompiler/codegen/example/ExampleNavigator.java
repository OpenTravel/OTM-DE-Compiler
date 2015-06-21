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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions.DetailLevel;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
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

/**
 * Navigator that traverses model elements in order to produce visitor events that will allow the
 * construction of an example data set for the element(s) that are visited.
 * 
 * @author S. Livezey
 */
public class ExampleNavigator {

    private Stack<Object> entityStack = new Stack<Object>();
    private Map<TLFacet, List<TLExtensionPointFacet>> extensionPointRegistry;
    private ExampleGeneratorOptions options;
    private ExampleVisitor visitor;

    /**
     * Constructor that initializes the visitor to be notified when model elements are encountered
     * during navigation.
     * 
     * @param visitor
     *            the visitor to be notified when model elements are encountered
     * @param options
     *            the options to use during example navigation
     */
    public ExampleNavigator(ExampleVisitor visitor, ExampleGeneratorOptions options) {
        this.options = (options != null) ? options : new ExampleGeneratorOptions();
        this.visitor = visitor;
    }

    /**
     * Navigates all dependencies of the given element in a depth-first fashion using the given
     * visitor for notification callbacks.
     * 
     * @param target
     *            the model element whose dependencies should be navigated
     * @param visitor
     *            the visitor to be notified when model elements are encountered
     * @param options
     *            the options to use during example navigation
     */
    public static void navigate(NamedEntity target, ExampleVisitor visitor,
            ExampleGeneratorOptions options) {
        new ExampleNavigator(visitor, options).navigateEntity(target);
    }

    /**
     * Navigates the given <code>NamedEntity</code> and its sub-elements in a depth-first fashion.
     * 
     * @param target
     *            the named entity for which an example is to be generated
     */
    public void navigateEntity(NamedEntity target) {
        if (target instanceof TLSimple) {
            navigateSimple((TLSimple) target);

        } else if (target instanceof TLClosedEnumeration) {
            navigateClosedEnumeration((TLClosedEnumeration) target);

        } else if (target instanceof TLOpenEnumeration) {
            navigateOpenEnumeration((TLOpenEnumeration) target);

        } else if (target instanceof TLRoleEnumeration) {
            navigateRoleEnumeration((TLRoleEnumeration) target);

        } else if (target instanceof TLValueWithAttributes) {
            navigateValueWithAttributes((TLValueWithAttributes) target);

        } else if (target instanceof TLCoreObject) {
            navigateCoreObject((TLCoreObject) target);

        } else if (target instanceof TLBusinessObject) {
            navigateBusinessObject((TLBusinessObject) target);

        } else if (target instanceof TLFacet) {
            navigateFacet((TLFacet) target);

        } else if (target instanceof TLListFacet) {
            navigateListFacet((TLListFacet) target);

        } else if (target instanceof TLAlias) {
            navigateAlias((TLAlias) target);

        } else if (target instanceof TLExtensionPointFacet) {
            navigateExtensionPointFacet((TLExtensionPointFacet) target);

        } else if (target instanceof XSDComplexType) {
            navigateXSDComplexType((XSDComplexType) target);

        } else if (target instanceof XSDElement) {
            navigateXSDElement((XSDElement) target);
        }
    }

    /**
     * Called when a <code>TLSimple</code> instance is encountered during model navigation.
     * 
     * @param simple
     *            the simple entity to visit and navigate
     */
    public void navigateSimple(TLSimple simple) {
        navigateSimpleType(simple);
    }

    /**
     * Called when a <code>TLValueWithAttributes</code> instance is encountered during model
     * navigation.
     * 
     * @param valueWithAttributes
     *            the value-with-attributes entity to visit and navigate
     */
    public void navigateValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        try {
            incrementRecursionCount(valueWithAttributes);

            if (canVisit(valueWithAttributes)) {
                visitor.startValueWithAttributes(valueWithAttributes);

                for (TLAttribute attribute : PropertyCodegenUtils
                        .getInheritedAttributes(valueWithAttributes)) {
                    navigateAttribute(attribute);
                }
                for (TLIndicator indicator : PropertyCodegenUtils
                        .getInheritedIndicators(valueWithAttributes)) {
                    navigateIndicator(indicator);
                }
                visitor.endValueWithAttributes(valueWithAttributes);
            }
        } finally {
            decrementRecursionCount(valueWithAttributes);
        }
    }

    /**
     * Called when a <code>TLOpenEnumeration</code> instance is encountered during model navigation.
     * 
     * @param enumeration
     *            the enumeration entity to visit and navigate
     */
    public void navigateOpenEnumeration(TLOpenEnumeration enumeration) {
        try {
            incrementRecursionCount(enumeration);

            if (canVisit(enumeration)) {
                visitor.startOpenEnumeration(enumeration);
                visitor.endOpenEnumeration(enumeration);
            }
        } finally {
            decrementRecursionCount(enumeration);
        }
    }

    /**
     * Called when a <code>TLRoleEnumeration</code> instance is encountered during model navigation.
     * 
     * @param enumeration
     *            the enumeration entity to visit and navigate
     */
    public void navigateRoleEnumeration(TLRoleEnumeration enumeration) {
        try {
            incrementRecursionCount(enumeration);

            if (canVisit(enumeration)) {
                visitor.startRoleEnumeration(enumeration);
                visitor.endRoleEnumeration(enumeration);
            }
        } finally {
            decrementRecursionCount(enumeration);
        }
    }

    /**
     * Called when a <code>TLClosedEnumeration</code> instance is encountered during model
     * navigation.
     * 
     * @param enumeration
     *            the enumeration entity to visit and navigate
     */
    public void navigateClosedEnumeration(TLClosedEnumeration enumeration) {
        navigateSimpleType(enumeration);
    }

    /**
     * Called when a <code>TLBusinessObject</code> instance is encountered during model navigation.
     * 
     * @param businessObject
     *            the business object entity to visit and navigate
     */
    public void navigateBusinessObject(TLBusinessObject businessObject) {
        try {
            incrementRecursionCount(businessObject);

            if (canVisit(businessObject)) {
                TLAbstractFacet exampleFacet = selectExampleFacet(businessObject.getSummaryFacet());

                if (exampleFacet instanceof TLFacet) {
                    navigateFacet((TLFacet) exampleFacet);

                } else {
                    // Cannot happen - no simple facets for business objects
                }
            }
        } finally {
            decrementRecursionCount(businessObject);
        }
    }

    /**
     * Called when a <code>TLCoreObject</code> instance is encountered during model navigation.
     * 
     * @param coreObject
     *            the core object entity to visit and navigate
     */
    public void navigateCoreObject(TLCoreObject coreObject) {
        try {
            incrementRecursionCount(coreObject);

            if (canVisit(coreObject)) {
                TLAbstractFacet exampleFacet = selectExampleFacet(coreObject.getSummaryFacet());

                if (exampleFacet instanceof TLFacet) {
                    navigateFacet((TLFacet) exampleFacet);

                } else { // must be a simple facet
                    navigateSimpleFacet((TLSimpleFacet) exampleFacet);
                }
            }
        } finally {
            decrementRecursionCount(coreObject);
        }
    }

    /**
     * Called when a <code>TLExtensionPointFacet</code> instance is encountered during model
     * navigation.
     * 
     * @param extensionPointFacet
     *            the extension point facet entity to visit and navigate
     */
    public void navigateExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
        try {
            incrementRecursionCount(extensionPointFacet);

            if (canVisit(extensionPointFacet)) {

                visitor.startExtensionPointFacet(extensionPointFacet);

                for (TLAttribute attribute : extensionPointFacet.getAttributes()) {
                    navigateAttribute(attribute);
                }
                for (TLProperty element : extensionPointFacet.getElements()) {
                    navigateElement(element);
                }
                for (TLIndicator indicator : extensionPointFacet.getIndicators()) {
                    navigateIndicator(indicator);
                }
                visitor.endExtensionPointFacet(extensionPointFacet);
            }
        } finally {
            decrementRecursionCount(extensionPointFacet);
        }
    }

    /**
     * Called when a <code>TLFacet</code> instance is encountered during model navigation.
     * 
     * @param facet
     *            the facet entity to visit and navigate
     */
    public void navigateFacet(TLFacet facet) {
        try {
            incrementRecursionCount(facet);

            if (canVisit(facet)) {
                visitor.startFacet(facet);
                navigateFacetMembers(facet);
                visitor.endFacet(facet);
            }
        } finally {
            decrementRecursionCount(facet);
        }
    }

    /**
     * Called when a <code>TLListFacet</code> instance is encountered during model navigation.
     * 
     * @param listFacet
     *            the list facet entity to visit and navigate
     */
    public void navigateListFacet(TLListFacet listFacet) {
        try {
            incrementRecursionCount(listFacet);

            if (canVisit(listFacet)) {
                TLCoreObject facetOwner = (TLCoreObject) listFacet.getOwningEntity();
                TLAbstractFacet itemFacet = listFacet.getItemFacet();

                if ((itemFacet instanceof TLFacet)
                        && (facetOwner.getRoleEnumeration().getRoles().size() > 0)) {
                    for (TLRole role : facetOwner.getRoleEnumeration().getRoles()) {
                        visitor.startListFacet(listFacet, role);
                        navigateFacetMembers((TLFacet) itemFacet);
                        visitor.endListFacet(listFacet, role);
                    }
                } else { // must be a simple facet
                    visitor.startListFacet(listFacet, null);
                    navigateSimpleFacet((TLSimpleFacet) itemFacet);
                    visitor.endListFacet(listFacet, null);
                }
            }
        } finally {
            decrementRecursionCount(listFacet);
        }
    }

    /**
     * Called when a <code>TLFacetSimple</code> instance is encountered during model navigation.
     * 
     * @param facet
     *            the simple facet entity to visit and navigate
     */
    public void navigateSimpleFacet(TLSimpleFacet facet) {
        navigateSimpleType(facet);
    }

    /**
     * Called when a <code>TLAlias</code> instance is encountered during model navigation.
     * 
     * @param alias
     *            the alias entity to visit and navigate
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
                exampleFacet = selectExampleFacet(((TLBusinessObject) aliasOwner).getIdFacet());
                alias = AliasCodegenUtils.getFacetAlias(alias, exampleFacet.getFacetType());

            } else if (aliasOwner instanceof TLCoreObject) {
                exampleFacet = selectExampleFacet(((TLCoreObject) aliasOwner).getSummaryFacet());
                alias = AliasCodegenUtils.getFacetAlias(alias, exampleFacet.getFacetType());
            }

            // Now perform the alias visitation normally
            incrementRecursionCount(alias);

            if (canVisit(alias)) {
                visitor.startAlias(alias);
                navigateEntity(alias.getOwningEntity());
                visitor.endAlias(alias);
            }
        } finally {
            decrementRecursionCount(alias);
        }
    }

    /**
     * Called when a <code>XSDSimpleType</code> instance is encountered during model navigation.
     * 
     * @param xsdSimple
     *            the simple entity to visit and navigate
     */
    public void navigateXSDSimple(XSDSimpleType xsdSimple) {
        navigateSimpleType(xsdSimple);
    }

    /**
     * Called when a <code>XSDComplexType</code> instance is encountered during model navigation.
     * 
     * @param xsdComplex
     *            the XSD complex-type entity to visit and navigate
     */
    public void navigateXSDComplexType(XSDComplexType xsdComplex) {
        try {
            incrementRecursionCount(xsdComplex);

            if (canVisit(xsdComplex)) {
                visitor.startXsdComplexType(xsdComplex);
                visitor.endXsdComplexType(xsdComplex);
            }
        } finally {
            decrementRecursionCount(xsdComplex);
        }
    }

    /**
     * Called when a <code>XSDElement</code> instance is encountered during model navigation.
     * 
     * @param xsdElement
     *            the XSD element entity to visit and navigate
     */
    public void navigateXSDElement(XSDElement xsdElement) {
        try {
            incrementRecursionCount(xsdElement);

            if (canVisit(xsdElement)) {
                visitor.startXsdElement(xsdElement);
                visitor.endXsdElement(xsdElement);
            }
        } finally {
            decrementRecursionCount(xsdElement);
        }
    }

    /**
     * Increments the recursion count for the given object by 1.
     * 
     * @param obj
     *            the object being traversed
     */
    protected void incrementRecursionCount(Object obj) {
        if (obj != null) {
            entityStack.push(obj);
        }
    }

    /**
     * Decrements the recursion count for the given object by 1.
     * 
     * @param obj
     *            the object being traversed
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
     * Returns true if the given object can be visited based on its recursion count within the
     * current depth-first navigation structure.
     * 
     * @param obj
     *            the object to be visited
     * @return boolean
     */
    protected boolean canVisit(Object obj) {
        boolean visitationAllowed = (obj != null);

        if (visitationAllowed) {
            if (entityStack.peek() != obj) {
                throw new IllegalStateException(
                        "Cannot perform a visitation check on an entity before calling 'incrementRecursionCount()'.");
            }
            int maxRecursion = Math.max(1, options.getMaxRecursionDepth());
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
     * NOTE: Since simple types are capable of directly generating examples, no depth-first
     * traversal is performed by this method.
     * 
     * @param target
     *            the named entity for which an example is to be generated
     */
    protected void navigateSimpleType(TLAttributeType simpleType) {
        visitor.visitSimpleType(simpleType);
    }

    /**
     * Recursively navigates the members (attributes, elements, and indicators) of the given facet.
     * The navigated members that are inherited from higher-level members of the same owner, as well
     * as members that are inherited from extended core/business objects.
     * 
     * @param facet
     *            the facet whose members are to be navigated
     */
    protected void navigateFacetMembers(TLFacet facet) {
        Map<TLFacetType, List<TLExtensionPointFacet>> facetExtensionsByType = getExtensionPoints(facet);
        Set<TLFacetType> processedExtensionPointTypes = new HashSet<TLFacetType>();
        TLFacetType previousFacetType = null;

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
        for (TLProperty element : PropertyCodegenUtils.getInheritedProperties(facet)) {
            TLFacet currentFacet = (TLFacet) element.getPropertyOwner();

            // Before navigating the element itself, check to see if we need to insert any extension
            // point facets
            if (currentFacet.getFacetType() != previousFacetType) {
                List<TLFacet> facetHierarchy = FacetCodegenUtils
                        .getLocalFacetHierarchy(currentFacet);

                // Ignore the last element in the facet hierarchy list since it is always the
                // current
                // facet we are processing
                for (int i = 0; i < (facetHierarchy.size() - 1); i++) {
                    TLFacet hFacet = facetHierarchy.get(i);

                    if (!processedExtensionPointTypes.contains(hFacet.getFacetType())) {
                        List<TLExtensionPointFacet> facetExtensions = facetExtensionsByType
                                .get(hFacet.getFacetType());

                        navigateExtensionPoint(hFacet, facetExtensions);
                        processedExtensionPointTypes.add(hFacet.getFacetType());
                    }
                }
                previousFacetType = currentFacet.getFacetType();
            }

            // Navigate the example content for the current element
            navigateElement(element);
        }
        
        // Navigate indicators that are published as elements
        for (TLIndicator indicator : PropertyCodegenUtils.getInheritedIndicators(facet)) {
        	if (indicator.isPublishAsElement()) {
                navigateIndicator(indicator);
        	}
        }
        
        // Wrap up by checking for any extension points for the current facet (take into account
        // that
        // the facet may not contain any properties and therefore may not have checked for extension
        // points yet).
        List<TLFacet> facetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy(facet);

        for (TLFacet hFacet : facetHierarchy) {
            if (!processedExtensionPointTypes.contains(hFacet.getFacetType())) {
                navigateExtensionPoint(hFacet, facetExtensionsByType.get(hFacet.getFacetType()));
            }
        }
    }

    /**
     * Navigates the specified extensions of the facet.
     * 
     * @param facet
     *            the facets whose extension points are to be navigated
     * @param facetExtensions
     *            the list of extension points for the facet
     */
    private void navigateExtensionPoint(TLFacet facet, List<TLExtensionPointFacet> facetExtensions) {
        if ((facetExtensions != null) && !facetExtensions.isEmpty()) {
            visitor.startExtensionPoint(facet);

            for (TLExtensionPointFacet xpFacet : facetExtensions) {
                navigateExtensionPointFacet(xpFacet);
            }
            visitor.endExtensionPoint(facet);
        }
    }

    /**
     * Called when a <code>TLAttribute</code> instance is encountered during model navigation.
     * 
     * @param attribute
     *            the attribute entity to visit and navigate
     */
    protected void navigateAttribute(TLAttribute attribute) {
        try {
            incrementRecursionCount(attribute);

            if (canVisit(attribute)) {
                TLAttributeType attributeType = attribute.getType();

                while (attributeType instanceof TLValueWithAttributes) {
                    attributeType = ((TLValueWithAttributes) attributeType).getParentType();
                }
                visitor.startAttribute(attribute);
                navigateSimpleType(attributeType);
                visitor.endAttribute(attribute);
            }
        } finally {
            decrementRecursionCount(attribute);
        }
    }

    /**
     * Called when a <code>TLProperty</code> instance is encountered during model navigation.
     * 
     * @param element
     *            the element entity to visit and navigate
     */
    protected void navigateElement(TLProperty element) {
        try {
            incrementRecursionCount(element);

            if (canVisit(element)) {
                if (element.isReference()) {
                    boolean multipleValues = (element.getRepeat() > 1);

                    visitor.startElement(element);
                    navigateSimpleType(getSchemaForSchemasType(element.getOwningModel(),
                            multipleValues ? "IDREFS" : "IDREF"));
                    visitor.endElement(element);

                } else { // normal (non-reference) element
                    int maxRepeatCount = Math.max(1, options.getMaxRepeat());
                    int repeatCount = (element.getRepeat() < 0) ? maxRepeatCount : Math.max(1,
                            Math.min(maxRepeatCount, element.getRepeat()));
                    TLPropertyType propertyType = element.getType();

                    // If the property type is a core object, select the appropriate level of detail
                    // based
                    // on the navigation options
                    if (propertyType instanceof TLCoreObject) {
                        propertyType = selectExampleFacet(((TLCoreObject) propertyType)
                                .getSummaryFacet());
                    }

                    // Repeat the navigation as many times as required by the property and/or the
                    // navigation options
                    for (int i = 0; i < repeatCount; i++) {
                        boolean isAttributeType;

                        if (propertyType instanceof TLListFacet) {
                            isAttributeType = (((TLListFacet) propertyType).getItemFacet() instanceof TLAttributeType);
                        } else {
                            isAttributeType = !(propertyType instanceof TLRole)
                                    && !(propertyType instanceof TLValueWithAttributes)
                                    && (propertyType instanceof TLAttributeType);
                        }
                        visitor.startElement(element);

                        if (isAttributeType) {
                            navigateSimpleType((TLAttributeType) propertyType);

                        } else { // complex entity type
                            navigateEntity(propertyType);
                        }
                        visitor.endElement(element);
                    }
                }
            }
        } finally {
            decrementRecursionCount(element);
        }
    }

    /**
     * Called when a <code>TLIndicator</code> instance is encountered during model navigation.
     * 
     * @param indicator
     *            the indicator entity to visit and navigate
     */
    protected void navigateIndicator(TLIndicator indicator) {
        try {
            incrementRecursionCount(indicator);

            if (canVisit(indicator)) {
                boolean isAttribute = !indicator.isPublishAsElement()
                        || (indicator.getOwner() instanceof TLValueWithAttributes);

                if (isAttribute) {
                    visitor.startIndicatorAttribute(indicator);
                    visitor.endIndicatorAttribute(indicator);

                } else {
                    visitor.startIndicatorElement(indicator);
                    visitor.endIndicatorElement(indicator);
                }
            }
        } finally {
            decrementRecursionCount(indicator);
        }
    }

    /**
     * Returns the facet that should be used for the purposes of producing example data in the
     * generated output. Depending upon the level of detail selected in the
     * <code>ExampleGeneratorOptions</code>, the facet that is returned may or may not be the one
     * that is passed to this method.
     * 
     * @param preferredFacet  the facet with the default amount of detail
     * @return TLAbstractFacet
     */
    protected TLAbstractFacet selectExampleFacet(TLFacet defaultFacet) {
    	TLFacetOwner facetOwner = (defaultFacet == null) ? null : defaultFacet.getOwningEntity();
        TLAbstractFacet exampleFacet = options.getPreferredFacet( facetOwner );
        
        if (exampleFacet == null) { // no preferred facet assigned
            if (defaultFacet.getFacetType().isContextual()) {
                exampleFacet = defaultFacet;

            } else {
                if (options.getDetailLevel() == DetailLevel.MAXIMUM) {
                    exampleFacet = getMaximumDetail(facetOwner);
                } else {
                    exampleFacet = getMinimumDetail(defaultFacet);
                }
            }
        }
        return exampleFacet;
    }

    /**
     * Returns the list facet that should be used for the purposes of producing example data in the
     * generated output. Depending upon the level of detail selected in the
     * <code>ExampleGeneratorOptions</code>, the list facet that is returned may or may not be the
     * one that is passed to this method.
     * 
     * @param preferredFacet
     *            the list facet with the preferred amount of detail (per the current model
     *            configuration)
     * @return TLListFacet
     */
    protected TLListFacet selectExampleListFacet(TLListFacet preferredFacet) {
        TLListFacet exampleFacet;

        if (options.getDetailLevel() == DetailLevel.MAXIMUM) {
            exampleFacet = getMaximumListDetail((TLCoreObject) preferredFacet.getOwningEntity());
        } else {
            exampleFacet = getMinimumListDetail(preferredFacet);
        }
        return exampleFacet;
    }

    /**
     * Returns the non-empty facet from the owner with the maximum amount of detail.
     * 
     * @param facetOwner
     *            the facet owner from which to retrieve the most detailed facet
     * @return TLAbstractFacet
     */
    private TLAbstractFacet getMaximumDetail(TLFacetOwner facetOwner) {
        FacetCodegenDelegateFactory factory = new FacetCodegenDelegateFactory(null);
        List<TLAbstractFacet> candidateFacets = new ArrayList<TLAbstractFacet>();
        TLAbstractFacet result = null;

        if (facetOwner instanceof TLBusinessObject) {
            TLBusinessObject businessObject = (TLBusinessObject) facetOwner;

            candidateFacets.add(businessObject.getDetailFacet());
            candidateFacets.add(businessObject.getSummaryFacet());
            candidateFacets.add(businessObject.getIdFacet());

        } else if (facetOwner instanceof TLCoreObject) {
            TLCoreObject coreObject = (TLCoreObject) facetOwner;

            candidateFacets.add(coreObject.getDetailFacet());
            candidateFacets.add(coreObject.getSummaryFacet());
            candidateFacets.add(coreObject.getSimpleFacet());
        }

        // Get the maximum level of detail that publishes content
        for (TLAbstractFacet candidate : candidateFacets) {
            FacetCodegenDelegate<TLAbstractFacet> delegate = factory.getDelegate(candidate);

            if (delegate != null) {
                if (delegate.hasContent()) {
                    result = candidate;
                    break;
                }
            } else if (candidate.declaresContent()) {
                result = candidate;
                break;
            }
        }

        // Last Resort - Use the first candidate facet in the list
        if ((result == null) && (candidateFacets.size() > 0)) {
            result = candidateFacets.get(0);
        }
        return result;
    }

    /**
     * Returns the non-empty list facet from the owner with the maximum amount of detail.
     * 
     * @param facetOwner
     *            the facet owner from which to retrieve the most detailed facet
     * @return TLAbstractFacet
     */
    private TLListFacet getMaximumListDetail(TLCoreObject facetOwner) {
        List<TLListFacet> candidateFacets = new ArrayList<TLListFacet>();
        TLListFacet result = null;

        candidateFacets.add(facetOwner.getDetailListFacet());
        candidateFacets.add(facetOwner.getSummaryListFacet());

        for (TLListFacet candidate : candidateFacets) {
            if (candidate.declaresContent()) {
                result = candidate;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the non-empty facet from the owner with the minimum amount of detail, but with at
     * least as much detail as the given preferred facet.
     * 
     * @param preferredFacet
     *            the facet with the preferred amount of detail
     * @return TLAbstractFacet
     */
    private TLAbstractFacet getMinimumDetail(TLFacet preferredFacet) {
        TLAbstractFacet result = preferredFacet;

        if ((preferredFacet != null) && !preferredFacet.declaresContent()) {
            TLFacetOwner facetOwner = preferredFacet.getOwningEntity();
            List<TLAbstractFacet> candidateFacets = new ArrayList<TLAbstractFacet>();

            if (facetOwner instanceof TLBusinessObject) {
                TLBusinessObject businessObject = (TLBusinessObject) facetOwner;

                switch (preferredFacet.getFacetType()) {
                    case DETAIL:
                        candidateFacets.add(0, businessObject.getDetailFacet());
                    case CUSTOM:
                        candidateFacets.add(0, preferredFacet);
                    case SUMMARY:
                        candidateFacets.add(0, businessObject.getSummaryFacet());
                    case ID:
                        candidateFacets.add(0, businessObject.getIdFacet());
                    default:
                    	break;
                }
            } else if (facetOwner instanceof TLCoreObject) {
                TLCoreObject coreObject = (TLCoreObject) facetOwner;

                switch (preferredFacet.getFacetType()) {
                    case DETAIL:
                        candidateFacets.add(0, coreObject.getDetailFacet());
                    case SUMMARY:
                        candidateFacets.add(0, coreObject.getSummaryFacet());
                    case SIMPLE:
                        candidateFacets.add(0, coreObject.getSimpleFacet());
                    default:
                    	break;
                }
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
     * Returns the non-empty list facet from the owner with the minimum amount of detail, but with
     * at least as much detail as the given preferred facet.
     * 
     * @param preferredFacet
     *            the list facet with the preferred amount of detail
     * @return TLListFacet
     */
    private TLListFacet getMinimumListDetail(TLListFacet preferredFacet) {
        TLListFacet result = preferredFacet;

        if ((preferredFacet != null) && !preferredFacet.declaresContent()) {
            TLCoreObject facetOwner = (TLCoreObject) preferredFacet.getOwningEntity();
            List<TLListFacet> candidateFacets = new ArrayList<TLListFacet>();

            switch (preferredFacet.getFacetType()) {
                case DETAIL:
                    candidateFacets.add(0, facetOwner.getDetailListFacet());
                case SUMMARY:
                    candidateFacets.add(0, facetOwner.getSummaryListFacet());
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
     * Returns the entity with the specified name type name from the schema-for-schemas built-in of
     * the given model.
     * 
     * @param model
     *            the model to search
     * @param xsdType
     *            the local name of the type to return from the XML schema namespace
     * @return TLAttributeType
     */
    private TLAttributeType getSchemaForSchemasType(TLModel model, String xsdType) {
        TLAttributeType xsdEntity = null;

        for (BuiltInLibrary builtIn : model.getBuiltInLibraries()) {
            if (builtIn.getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                xsdEntity = (TLAttributeType) builtIn.getNamedMember(xsdType);
                if (xsdEntity != null)
                    break;
            }
        }
        return xsdEntity;
    }

    /**
     * Returns the extension points from the model that reference the given entity facet. The
     * resulting map is indexed by the facet-type to which each <code>TLExtensionPointFacet</code>
     * is associated. The lists of extension point facets include those items that reference
     * extended entities of the facet's owner.
     * 
     * @param facet
     *            the facet for which to return extension points
     * @return Map<TLFacetType,List<TLExtensionPointFacet>>
     */
    private Map<TLFacetType, List<TLExtensionPointFacet>> getExtensionPoints(TLFacet facet) {
        Map<TLFacetType, List<TLExtensionPointFacet>> result = new HashMap<TLFacetType, List<TLExtensionPointFacet>>();
        TLModel model = (facet == null) ? null : facet.getOwningModel();

        // Initialize the registry of extension point facets if not already done
        if ((extensionPointRegistry == null) && (model != null)) {
            extensionPointRegistry = new HashMap<TLFacet, List<TLExtensionPointFacet>>();

            for (TLLibrary library : model.getUserDefinedLibraries()) {
                for (TLExtensionPointFacet xpFacet : library.getExtensionPointFacetTypes()) {
                    TLExtension extension = xpFacet.getExtension();
                    NamedEntity extendedFacet = (extension == null) ? null : extension
                            .getExtendsEntity();

                    if (extendedFacet instanceof TLFacet) {
                        TLFacet extendedTLFacet = (TLFacet) extendedFacet;
                        List<TLExtensionPointFacet> extensionPoints = extensionPointRegistry
                                .get(extendedTLFacet);

                        if (extensionPoints == null) {
                            extensionPoints = new ArrayList<TLExtensionPointFacet>();
                            extensionPointRegistry.put(extendedTLFacet, extensionPoints);
                        }
                        extensionPoints.add(xpFacet);
                    }
                }
            }
        }

        // Lookup the extension point facets that reference the given entity facet
        if (extensionPointRegistry != null) {
            List<TLFacet> facetHierarchy = FacetCodegenUtils.getLocalFacetHierarchy(facet);

            for (TLFacet hFacet : facetHierarchy) {
                TLFacetOwner facetOwner = hFacet.getOwningEntity();

                while ((hFacet != null) && (facetOwner != null)) {
                    List<TLExtensionPointFacet> hExtensionPoints = extensionPointRegistry
                            .get(hFacet);

                    if (hExtensionPoints != null) {
                        List<TLExtensionPointFacet> extensionPoints = result.get(hFacet
                                .getFacetType());

                        if (extensionPoints == null) {
                            extensionPoints = new ArrayList<TLExtensionPointFacet>();
                            result.put(hFacet.getFacetType(), extensionPoints);
                        }
                        for (TLExtensionPointFacet xpFacet : hExtensionPoints) {
                            extensionPoints.add(0, xpFacet); // add to beginning of list
                        }
                    }
                    facetOwner = FacetCodegenUtils.getFacetOwnerExtension(facetOwner);
                    hFacet = (facetOwner == null) ? null : FacetCodegenUtils.getFacetOfType(
                            facetOwner, hFacet.getFacetType(), hFacet.getContext(),
                            hFacet.getLabel());
                }
            }
        }
        return result;
    }

}
