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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.ComplexContent;
import org.w3._2001.xmlschema.ComplexType;
import org.w3._2001.xmlschema.Element;
import org.w3._2001.xmlschema.ExplicitGroup;
import org.w3._2001.xmlschema.ExtensionType;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelElement;

/**
 * Base class for facet code generation delegates used to generate code artifacts for
 * <code>TLFacet</code> model elements.
 * 
 * @author S. Livezey
 */
public abstract class TLFacetCodegenDelegate extends FacetCodegenDelegate<TLFacet> {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet
     *            the source facet
     */
    public TLFacetCodegenDelegate(TLFacet sourceFacet) {
        super(sourceFacet);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#generateElements()
     */
    @Override
    public FacetCodegenElements generateElements() {
    	boolean doDefaultElement = hasDefaultFacetElement();
        FacetCodegenElements codegenElements;
        
        if (doDefaultElement) {
            codegenElements = super.generateElements();
        } else {
            codegenElements = new FacetCodegenElements();
        }
        
        if (hasContent()) {
            TLFacet sourceFacet = getSourceFacet();
            TLFacetOwner facetOwner = sourceFacet.getOwningEntity();
            boolean doSubstitutionGroupElement = hasSubstitutionGroupElement();
            boolean doNonSubstitutableElement = hasNonSubstitutableElement();

            if (doSubstitutionGroupElement) {
                codegenElements.addSubstitutionGroupElement(facetOwner,
                        createSubstitutionGroupElement(null));
            }
            if (doNonSubstitutableElement) {
                codegenElements.addFacetElement(facetOwner, createNonSubstitutableElement(null));
            }

            for (TLAlias facetAlias : sourceFacet.getAliases()) {
                TLAlias ownerAlias = AliasCodegenUtils.getOwnerAlias(facetAlias);

                if (ownerAlias != null) {
                    if (doSubstitutionGroupElement) {
                        codegenElements.addSubstitutionGroupElement(ownerAlias,
                                createSubstitutionGroupElement(ownerAlias));
                    }
                    if (doDefaultElement) {
                        codegenElements.addFacetElement(ownerAlias, createElement(facetAlias));
                    }
                    if (doNonSubstitutableElement) {
                        codegenElements.addFacetElement(ownerAlias,
                                createNonSubstitutableElement(facetAlias));
                    }
                }
            }
        }
        return codegenElements;
    }
    
    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#hasContent()
     */
    @Override
    public boolean hasContent() {
        return super.hasContent() || !getAttributes().isEmpty() || !getIndicators().isEmpty()
                || !getElements().isEmpty();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#hasExtensionPoint()
     */
    @Override
    public boolean hasExtensionPoint() {
        boolean isExtensionPointPublished = false;

        if (getExtensionPointElement() != null) {
            isExtensionPointPublished = FacetCodegenUtils.isExtensible(getSourceFacet()
                    .getOwningEntity());

            if (!isExtensionPointPublished) {
                TLFacet sourceFacet = getSourceFacet();
                TLFacetOwner ownerExtension = FacetCodegenUtils.getFacetOwnerExtension(sourceFacet
                        .getOwningEntity());

                while (!isExtensionPointPublished && (ownerExtension != null)) {
                    isExtensionPointPublished = FacetCodegenUtils.isExtensible(ownerExtension);
                    ownerExtension = FacetCodegenUtils.getFacetOwnerExtension(ownerExtension);
                }
            }
        }
        return isExtensionPointPublished;
    }

    /**
     * Returns the facet instance that should serve as the base type for the source facet. In some
     * cases (business/core object extension), the facet returned by this method may have a
     * different owner than that of the source facet.
     * 
     * @return TLFacet
     */
    public TLFacet getBaseFacet() {
        TLFacet baseFacet = getLocalBaseFacet();

        // If the source facet does not have a base facet from its local owner, we need to look at
        // the business object/core/operation extension and select the corresponding facet from the
        // extended owner as the base
        if (baseFacet == null) {
            InheritedFacetSearch facetSearch = new InheritedFacetSearch(getSourceFacet()) {
                public boolean isMatchingCandidate(TLFacet candidateFacet) {
                    TLFacetCodegenDelegate candidateFacetDelegate = (TLFacetCodegenDelegate) new FacetCodegenDelegateFactory(
                            transformerContext).getDelegate(candidateFacet);

                    return candidateFacetDelegate.hasContent();
                }
            };

            baseFacet = facetSearch.getInheritedFacet();
        }
        return baseFacet;
    }
    
    /**
     * Returns true if the given facet (not necessarily the source facet) declares or
     * inherits fields (attributes, elements, or indicators) from another extended facet.
     * 
     * @param facet  the facet to analyze
     * @return boolean
     */
    protected boolean declaresOrInheritsFacetContent(TLFacet facet) {
    	FacetCodegenDelegate<TLFacet> delegate = (facet == getSourceFacet()) ? this : null;
    	
    	if (delegate == null) {
    		delegate = new FacetCodegenDelegateFactory(transformerContext).getDelegate( facet );
    	}
    	return delegate.hasContent();
    }

    /**
     * Returns the global type name of the local base facet for the source facet. By default, this
     * method simply returns the type name of the value returned by the 'getLocalBaseFacet()'
     * method. Sub-classes may override if the facet's schema type should extend from a type that is
     * not a member of the compiler's model.
     * 
     * @return QName
     */
    protected QName getLocalBaseFacetTypeName() {
        TLFacet baseFacet = getLocalBaseFacet();

        return (baseFacet == null) ? null : new QName(baseFacet.getNamespace(),
                XsdCodegenUtils.getGlobalTypeName(baseFacet));
    }

    /**
     * Returns true if this delegate should call the base class to generate a default
     * global element (returns true by default).
     * 
     * @return boolean
     */
    public boolean hasDefaultFacetElement() {
    	return true;
    }

    /**
     * Returns true if root substitution group element for the source facet owner should be generated
     * using the source facet.
     * 
     * <p>
     * NOTE: By default, this method returns false. Sub-classes should override to implement
     * facet-specific logic indicating when the reference element should be created.
     * 
     * @return boolean
     */
    protected boolean hasSubstitutionGroupElement() {
        return false;
    }

    /**
     * Returns the element that will serve as the head of the substitution group for the source
     * facet's owning entity.
     * 
     * @param ownerAlias
     *            the alias of the facet owner for which the element is being constructed (may be
     *            null)
     * @return Element
     */
    protected Element createSubstitutionGroupElement(TLAlias ownerAlias) {
        TLFacet sourceFacet = getSourceFacet();
        TLFacetOwner owner = sourceFacet.getOwningEntity();
        TopLevelElement element = new TopLevelElement();

        if (ownerAlias != null) {
            element.setName(XsdCodegenUtils.getSubstitutionGroupElementName(ownerAlias)
                    .getLocalPart());
        } else {
            element.setName(XsdCodegenUtils.getSubstitutionGroupElementName(owner).getLocalPart());
        }

        element.setAbstract( true );
        element.setType(new QName(sourceFacet.getNamespace(), XsdCodegenUtils
                .getGlobalTypeName(sourceFacet)));

        if (owner instanceof TLDocumentationOwner) {
            TLDocumentation doc = DocumentationFinder.getDocumentation((TLDocumentationOwner) owner);

            if (doc != null) {
                ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer =
                		getTransformerFactory().getTransformer(doc, Annotation.class);

                element.setAnnotation(docTransformer.transform(doc));
            }
            XsdCodegenUtils.addAppInfo(owner, element);
        }
        return element;
    }

    /**
     * Returns true if the source facet should have a non-substitutable facet in addition to the
     * substitutable one that is created by default.
     * 
     * <p>
     * NOTE: By default, this method returns false. Sub-classes should override to implement
     * facet-specific logic indicating when the reference element should be created.
     * 
     * @return boolean
     */
    public boolean hasNonSubstitutableElement() {
        return false;
    }

    /**
     * Returns a non-substitutable element for the source facet. Although it is termed
     * "non-substitutable", all global elements are members of the root "...SubGrp" element at a
     * minimum.
     * 
     * @param facetAlias
     *            the alias of the facet for which the element is being constructed (may be null)
     * @return Element
     */
    protected Element createNonSubstitutableElement(TLAlias facetAlias) {
        TLFacet sourceFacet = getSourceFacet();
        Element element = new TopLevelElement();

        element.setName(getNonSubstitableElementName(facetAlias));
        element.setType(new QName(sourceFacet.getNamespace(), XsdCodegenUtils
                .getGlobalTypeName(sourceFacet)));
        return element;
    }

    /**
     * Returns the name of the non-substitutable element used to represent the source facet or the
     * specified alias.
     * 
     * <p>
     * NOTE: By default, this method returns the results of the 'getElementName()' that is used to
     * construct names for the substitutable elements. Sub-classes that return true from the
     * 'hasNonSubstitutableElement()' method should override this method to supply a unique element
     * name for the non-substitutable element.
     * 
     * @param facetAlias
     *            the alias of the source facet element being created (may be null)
     * @return String
     */
    public String getNonSubstitableElementName(TLAlias facetAlias) {
        String elementName;

        if (facetAlias == null) {
            elementName = XsdCodegenUtils.getGlobalElementName(getSourceFacet()).getLocalPart();
        } else {
            elementName = XsdCodegenUtils.getGlobalElementName(facetAlias).getLocalPart();
        }
        return elementName;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#getElementName(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public String getElementName(TLAlias facetAlias) {
        String elementName;

        if (facetAlias == null) {
            elementName = XsdCodegenUtils.getSubstitutableElementName(getSourceFacet())
                    .getLocalPart();
        } else {
            elementName = XsdCodegenUtils.getSubstitutableElementName(facetAlias).getLocalPart();
        }
        return elementName;
    }

    /**
     * If the source facet should support an extension point element, this method will return the
     * qualified name of the global extension point element to use in the type's definition. If
     * extensions are not supported for the facet, this method should return null.
     * 
     * @return QName
     */
    public abstract QName getExtensionPointElement();

    /**
     * Assigns the substitution group as the name of the base type for the source facet's element.
     * 
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#getSubstitutionGroup(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    protected QName getSubstitutionGroup(TLAlias facetAlias) {
        QName subGrp = null;
        
        if (facetAlias != null) {
            subGrp = XsdCodegenUtils.getSubstitutionGroupElementName(AliasCodegenUtils
                    .getOwnerAlias(facetAlias));
        } else {
            subGrp = XsdCodegenUtils.getSubstitutionGroupElementName(getSourceFacet()
                    .getOwningEntity());
        }
        return subGrp;
    }

    /**
     * Returns the list of attributes to be generated for the source facet. By default, this method
     * returns the attributes declared by the source facet and inherited from like-typed facets of
     * the extended facet owner(s). Sub-classes may override to alter (or add to) the contents of
     * the resulting list.
     * 
     * @return List<TLAttribute>
     */
    public List<TLAttribute> getAttributes() {
        return PropertyCodegenUtils.getInheritedFacetAttributes(getSourceFacet());
    }

    /**
     * Returns the list of elements (properties) to be generated for the source facet. By default,
     * this method returns the elements declared by the source facet and inherited from like-typed
     * facets of the extended facet owner(s).. Sub-classes may override to alter (or add to) the
     * contents of the resulting list.
     * 
     * @return List<TLProperty>
     */
    public List<TLProperty> getElements() {
        return PropertyCodegenUtils.getInheritedFacetProperties(getSourceFacet());
    }

    /**
     * Returns the list of indicators to be generated for the source facet. By default, this method
     * returns the indicators declared by the source facet and inherited from like-typed facets of
     * the extended facet owner(s). Sub-classes may override to alter (or add to) the contents of
     * the resulting list.
     * 
     * @return List<TLIndicator>
     */
    public List<TLIndicator> getIndicators() {
        return PropertyCodegenUtils.getInheritedFacetIndicators(getSourceFacet());
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createType()
     */
    @Override
    protected Annotated createType() {
        TLFacet sourceFacet = getSourceFacet();
        String typeName = getTypeName();
        QName baseType = getLocalBaseFacetTypeName();
        QName globalExtensionPointElement = hasExtensionPoint() ? getExtensionPointElement() : null;

        List<TLProperty> elementList = getElements();
        List<TLIndicator> indicatorList = getIndicators();
        boolean hasSequence = (globalExtensionPointElement != null) ||
        		!elementList.isEmpty() || hasElementIndicators( indicatorList );
        ExplicitGroup sequence = hasSequence ? new ExplicitGroup() : null;
        ComplexType type = new TopLevelComplexType();
        List<Annotated> jaxbAttributeList;

        // Declare the type and assemble the structure of objects
        type.setName(typeName);

        if (baseType == null) {
            type.setSequence(sequence);
            jaxbAttributeList = type.getAttributeOrAttributeGroup();

        } else {
            ComplexContent content = new ComplexContent();
            ExtensionType extension = new ExtensionType();

            extension.setBase(baseType);
            extension.setSequence(sequence);
            content.setExtension(extension);
            type.setComplexContent(content);
            jaxbAttributeList = extension.getAttributeOrAttributeGroup();
        }

        // Generate elements for the sequence (if required)
        if (hasSequence) {
            ObjectTransformer<TLProperty, TopLevelElement, CodeGenerationTransformerContext> elementTransformer = getTransformerFactory()
                    .getTransformer(TLProperty.class, TopLevelElement.class);
            ObjectTransformer<TLIndicator, Annotated, CodeGenerationTransformerContext> indicatorTransformer = getTransformerFactory()
                    .getTransformer(TLIndicator.class, Annotated.class);

            for (TLProperty element : elementList) {
                sequence.getParticle().add(
                        jaxbObjectFactory.createElement(elementTransformer.transform(element)));
            }
            for (TLIndicator indicator : indicatorList) {
                if (indicator.isPublishAsElement()) {
                    sequence.getParticle().add(
                            jaxbObjectFactory.createElement((TopLevelElement) indicatorTransformer
                                    .transform(indicator)));
                }
            }

            if (globalExtensionPointElement != null) {
                TopLevelElement extensionPointElement = new TopLevelElement();

                extensionPointElement.setRef(globalExtensionPointElement);
                extensionPointElement.setMinOccurs(BigInteger.valueOf(0));
                sequence.getParticle().add(jaxbObjectFactory.createElement(extensionPointElement));
            }
        }

        // Generate attributes and indicators
        jaxbAttributeList.addAll(createJaxbAttributes());

        // Generate the documentation block (if required)
        type.setAnnotation(createJaxbDocumentation(sourceFacet));

        // Add any required application info
        XsdCodegenUtils.addAppInfo(sourceFacet.getOwningEntity(), type);

        return type;
    }
    
    /**
     * Common method used to populate a list of the facet's JAXB attributes using the attributes and
     * indicators of the source facet.
     * 
     * @return List<Annotated>
     */
    protected List<Annotated> createJaxbAttributes() {
        ObjectTransformer<TLAttribute, CodegenArtifacts, CodeGenerationTransformerContext> attributeTransformer = getTransformerFactory()
                .getTransformer(TLAttribute.class, CodegenArtifacts.class);
        ObjectTransformer<TLIndicator, Annotated, CodeGenerationTransformerContext> indicatorTransformer = getTransformerFactory()
                .getTransformer(TLIndicator.class, Annotated.class);
        List<Annotated> jaxbAttributes = new ArrayList<Annotated>();

        for (TLAttribute attribute : getAttributes()) {
            jaxbAttributes.addAll(attributeTransformer.transform(attribute).getArtifactsOfType(
                    Attribute.class));
        }
        for (TLIndicator indicator : getIndicators()) {
            if (!indicator.isPublishAsElement()) {
                jaxbAttributes.add(indicatorTransformer.transform(indicator));
            }
        }
        return jaxbAttributes;
    }

    /**
     * Constructs the XSD annotation using the OTM documentation for the given model element.
     * 
     * @param entity
     *            the model entity from which to obtain documentation
     * @return Annotation
     */
    protected Annotation createJaxbDocumentation(TLDocumentationOwner entity) {
        TLDocumentation doc = DocumentationFinder.getDocumentation( entity );
        Annotation annotation = null;

        if (doc != null) {
            ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer =
            		getTransformerFactory().getTransformer(doc, Annotation.class);

            annotation = docTransformer.transform(doc);
        }
        return annotation;
    }

    /**
     * Returns the specified facet type from the core object, unless that facet is empty. In those
     * cases, the facet hierarchy will be traversed upwards until a non-empty candidate is found.
     * 
     * @param coreObject
     *            the core object for which to return a facet instance
     * @param facetType
     *            the preferred type of facet to return
     * @return TLPropertyType
     */
    protected TLPropertyType getPreferredFacet(TLCoreObject coreObject, TLFacetType facetType) {
        Stack<TLAbstractFacet> facetHierarchy = new Stack<TLAbstractFacet>();

        facetHierarchy.push(coreObject.getSimpleFacet());
        facetHierarchy.push(coreObject.getSummaryFacet());
        facetHierarchy.push(coreObject.getDetailFacet());
        return getPreferredFacet(facetHierarchy, facetType);
    }

    /**
     * Returns the specified facet type from the business object, unless that facet is empty. In
     * those cases, the facet hierarchy will be traversed upwards until a non-empty candidate is
     * found.
     * 
     * @param coreObject
     *            the core object for which to return a facet instance
     * @param facetType
     *            the preferred type of facet to return
     * @return TLPropertyType
     */
    protected TLPropertyType getPreferredFacet(TLBusinessObject businessObject,
            TLFacetType facetType) {
        Stack<TLAbstractFacet> facetHierarchy = new Stack<TLAbstractFacet>();

        facetHierarchy.push(businessObject.getIdFacet());
        facetHierarchy.push(businessObject.getSummaryFacet());
        facetHierarchy.push(businessObject.getDetailFacet());
        return getPreferredFacet(facetHierarchy, facetType);
    }

    /**
     * Returns the specified facet type from the facet hierarchy provided, unless that facet is
     * empty. In those cases, the facet hierarchy will be traversed upwards until a non-empty
     * candidate is found.
     * 
     * @param facetHierarchy
     *            the facet hierarchy for the core/business object
     * @param facetType
     *            the preferred type of facet to return
     * @return TLPropertyType
     */
    private TLPropertyType getPreferredFacet(Stack<TLAbstractFacet> facetHierarchy,
            TLFacetType facetType) {
        while (!facetHierarchy.isEmpty() && (facetHierarchy.peek().getFacetType() != facetType)) {
            facetHierarchy.pop();
        }
        while (!facetHierarchy.isEmpty() && !facetHierarchy.peek().declaresContent()) {
            facetHierarchy.pop();
        }
        return (TLPropertyType) (facetHierarchy.isEmpty() ? null : facetHierarchy.peek());
    }
    
    /**
     * Returns true if the given list of indicators has at least one that should be declared
     * as an XSD element.
     * 
     * @param indicatorList  the list of indicators to analyze
     * @return boolean
     */
    private boolean hasElementIndicators(List<TLIndicator> indicatorList) {
    	boolean hasElement = false;
    	
    	for (TLIndicator indicator : indicatorList) {
    		if (indicator.isPublishAsElement()) {
    			hasElement = true;
    			break;
    		}
    	}
    	return hasElement;
    }

    /**
     * Helper class that searches the inheritance hierarchy of a facet, and returns a corresponding
     * facet from the next-higher owner if some (abstract) criteria are met.
     * 
     * @author S. Livezey
     */
    protected abstract class InheritedFacetSearch {

        private TLFacet sourceFacet;

        /**
         * Constructor that supplies the source facet that will be the subject of the search.
         * 
         * @param sourceFacet
         *            the source facet
         */
        public InheritedFacetSearch(TLFacet sourceFacet) {
            this.sourceFacet = sourceFacet;
        }

        /**
         * Searches the source facet's inheritance hierarchy, and returns the corresponding facet
         * from the next-higher owner if the test condition is met.
         * 
         * @return TLFacet
         */
        public TLFacet getInheritedFacet() {
            TLFacetOwner extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(sourceFacet
                    .getOwningEntity());
            TLFacet inheritedFacet = null, firstCandidate = null;

            while ((inheritedFacet == null) && (extendedOwner != null)) {
                TLFacet candidateFacet = FacetCodegenUtils.getFacetOfType(extendedOwner,
                        sourceFacet.getFacetType(), sourceFacet.getContext(),
                        sourceFacet.getLabel());

                if (firstCandidate == null) {
                    if (candidateFacet == null) {
                        // We should never need to do this for facet types such as ID's that are the
                        // root of their substitution group, and are guranteed (by validation rules)
                    	// to exist. For facet types such as Request, Response, Query, etc., the root
                    	// of the substitution group may need to be presumed to exist if it is inherited
                    	// from an extended facet owner. In these cases, we will create a "ghost facet"
                    	// to represent the missing base facet during code generation.
                        firstCandidate = new TLFacet();
                        firstCandidate.setFacetType(sourceFacet.getFacetType());
                        firstCandidate.setContext(sourceFacet.getContext());
                        firstCandidate.setLabel(sourceFacet.getLabel());
                        firstCandidate.setOwningEntity(extendedOwner);

                    } else {
                        firstCandidate = candidateFacet;
                    }
                }
                if (candidateFacet != null) {
                    if (isMatchingCandidate(candidateFacet)) {
                        inheritedFacet = firstCandidate;
                    }
                }
                if (inheritedFacet == null) {
                    extendedOwner = FacetCodegenUtils.getFacetOwnerExtension(extendedOwner);
                }
            }
            return inheritedFacet;
        }

        /**
         * Returns true if the given candidate facet meets specific conditions.
         * 
         * @param candidateFacet
         *            the inherited candidate facet to analyze
         * @return boolean
         */
        public abstract boolean isMatchingCandidate(TLFacet candidateFacet);

    }

}
