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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.util.ClassSpecificFunction;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Adapter base class for the <code>ExampleVisitor</code> interface that can optionally print debugging information as
 * logging output.
 * 
 * @author S. Livezey
 */
public abstract class AbstractExampleVisitor<T> implements ExampleVisitor {

    private static final Logger log = LogManager.getLogger( AbstractExampleVisitor.class );

    private static FacetCodegenDelegateFactory facetDelegateFactory = new FacetCodegenDelegateFactory( null );

    private StringBuilder debugIndent = new StringBuilder();
    protected ExampleValueGenerator exampleValueGenerator;
    protected CodeGenerationWsdlBindings wsdlBindings = null;
    protected String lastExampleValue;
    protected Map<QName,List<String>> idRegistry = new HashMap<>();
    protected Deque<TLPropertyOwner> facetStack = new LinkedList<>();
    protected ExampleContext context = new ExampleContext( null );
    protected Deque<ExampleContext> contextStack = new LinkedList<>();

    /**
     * Contstructor that provides the navigation options to use during example generation.
     * 
     * @param preferredContext the context ID of the preferred context from which to generate examples
     */
    public AbstractExampleVisitor(String preferredContext) {
        this.exampleValueGenerator = ExampleValueGenerator.getInstance( preferredContext );
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();

        if (appContext.containsBean( SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS )) {
            this.wsdlBindings = (CodeGenerationWsdlBindings) appContext
                .getBean( SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS );
        }
    }

    private ClassSpecificFunction<String> exampleValueFunction = new ClassSpecificFunction<String>()
        .addFunction( TLSimple.class, e -> exampleValueGenerator.getExampleValue( e ) )
        .addFunction( TLSimpleFacet.class, e -> exampleValueGenerator.getExampleValue( e ) )
        .addFunction( XSDSimpleType.class, e -> exampleValueGenerator.getExampleValue( e ) )
        .addFunction( TLOpenEnumeration.class, e -> exampleValueGenerator.getExampleValue( e ) )
        .addFunction( TLRoleEnumeration.class, e -> exampleValueGenerator.getExampleValue( e ) )
        .addFunction( TLClosedEnumeration.class, e -> exampleValueGenerator.getExampleValue( e ) )
        .addFunction( TLValueWithAttributes.class, e -> exampleValueGenerator.getExampleValue( e ) )
        .addFunction( TLCoreObject.class, e -> exampleValueGenerator.getExampleValue( e.getSimpleFacet() ) );

    private ClassSpecificFunction<Integer> fractionDigitsFunction = new ClassSpecificFunction<Integer>()
        .addFunction( TLSimple.class, this::getFractionDigits )
        .addFunction( TLSimpleFacet.class, this::getFractionDigits ).addFunction( XSDSimpleType.class, e -> -1 )
        .addFunction( TLOpenEnumeration.class, e -> -1 ).addFunction( TLRoleEnumeration.class, e -> -1 )
        .addFunction( TLClosedEnumeration.class, e -> -1 )
        .addFunction( TLValueWithAttributes.class, this::getFractionDigits ).addFunction( TLCoreObject.class, e -> -1 );

    /**
     * Generates an example value for the given model entity (if possible).
     * 
     * @param entity the entity for which to generate an example
     * @return String
     */
    protected String generateExampleValue(Object entity) {
        String exampleValue = null;
        int fractionDigits = -1;

        if (exampleValueFunction.canApply( entity )) {
            exampleValue = exampleValueFunction.apply( entity );
            fractionDigits = fractionDigitsFunction.apply( entity );

        } else if (entity instanceof TLAttribute) {
            TLAttributeOwner owner = ((TLAttribute) entity).getOwner();
            NamedEntity contextFacet = getContextFacet();

            if (contextFacet != null) {
                exampleValue = exampleValueGenerator.getExampleValue( (TLAttribute) entity, contextFacet );
            } else {
                exampleValue = exampleValueGenerator.getExampleValue( (TLAttribute) entity, owner );
            }
            fractionDigits = getFractionDigits( entity );

        } else if (entity instanceof TLProperty) {
            TLPropertyOwner owner = ((TLProperty) entity).getOwner();
            NamedEntity contextFacet = getContextFacet();

            if (contextFacet != null) {
                exampleValue = exampleValueGenerator.getExampleValue( (TLProperty) entity, contextFacet );
            } else {
                exampleValue = exampleValueGenerator.getExampleValue( (TLProperty) entity, owner );
            }
            fractionDigits = getFractionDigits( entity );
        }

        // For decimal values that specify a fraction-digits constraint, adjust
        // the string to be compliant with that constraint
        if (fractionDigits >= 0) {
            try {
                exampleValue =
                    new BigDecimal( exampleValue ).setScale( fractionDigits, RoundingMode.HALF_UP ).toString();

            } catch (NumberFormatException e) {
                // Ignore error - example string will remain unchanged
            }
        }

        lastExampleValue = (exampleValue == null) ? null : exampleValue.intern();
        return lastExampleValue;
    }

    /**
     * Returns the context facet that is the current owner (or possibly an alias of the owner) for all attributes and
     * elements that are encountered. By default, this method returns null; sub-classes may override.
     * 
     * @return TLFacet
     */
    protected NamedEntity getContextFacet() {
        NamedEntity elementType = (context.modelElement == null) ? null : context.modelElement.getType();
        NamedEntity contextFacet;

        if (elementType instanceof TLExtensionPointFacet) {
            contextFacet = null; // No inheritance or aliases for extension point facets

        } else if (elementType instanceof TLValueWithAttributes) {
            contextFacet = elementType;

        } else {
            contextFacet = getDefaultContextFacet();
        }
        return contextFacet;
    }

    /**
     * Returns the default context facet, presuming that the edge conditions have been eliminated prior to this method
     * call.
     * 
     * @return NamedEntity
     */
    private NamedEntity getDefaultContextFacet() {
        NamedEntity contextFacet;
        ExampleContext facetContext = context;

        // If we are currently processing an attribute value, the facet context will be the
        // current one. If we are processing an element value, the facet context will be
        // on top of the context stack.
        if ((facetContext.modelAttribute == null) && !contextStack.isEmpty()) {
            facetContext = contextStack.peek();
        }

        if (facetContext.modelAlias != null) {
            TLAlias facetAlias = facetContext.modelAlias;

            if (facetAlias.getOwningEntity() instanceof TLListFacet) {
                TLAlias coreAlias = AliasCodegenUtils.getOwnerAlias( facetAlias );

                facetAlias = AliasCodegenUtils.getFacetAlias( coreAlias,
                    ((TLListFacet) facetAlias.getOwningEntity()).getItemFacet().getFacetType() );
            }
            contextFacet = facetAlias;

        } else if (facetContext.getModelActionFacet() != null) {
            contextFacet = facetContext.getModelActionFacet();

        } else {
            contextFacet = facetStack.isEmpty() ? null : facetStack.peek();
        }
        return contextFacet;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#visitSimpleType(org.opentravel.schemacompiler.model.TLAttributeType)
     */
    @Override
    public void visitSimpleType(TLAttributeType simpleType) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%svisitSimpleType() : %s --> %s", debugIndent, simpleType.getLocalName(),
                generateExampleValue( simpleType ) ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startFacet(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void startFacet(TLFacet facet) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartFacet() : %s", debugIndent, facet.getLocalName() ) );
            debugIndent.append( "  " );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endFacet(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void endFacet(TLFacet facet) {
        if (log.isDebugEnabled()) {
            if (debugIndent.length() > 0) {
                debugIndent.setLength( debugIndent.length() - 2 );
            }
            log.debug( String.format( "%sendFacet() : %s", debugIndent, facet.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startListFacet(org.opentravel.schemacompiler.model.TLListFacet,
     *      org.opentravel.schemacompiler.model.TLRole)
     */
    @Override
    public void startListFacet(TLListFacet listFacet, TLRole role) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartListFacet() : %s / %s", debugIndent, listFacet.getLocalName(),
                role.getName() ) );
            debugIndent.append( "  " );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endListFacet(org.opentravel.schemacompiler.model.TLListFacet,
     *      org.opentravel.schemacompiler.model.TLRole)
     */
    @Override
    public void endListFacet(TLListFacet listFacet, TLRole role) {
        if (log.isDebugEnabled()) {
            if (debugIndent.length() > 0) {
                debugIndent.setLength( debugIndent.length() - 2 );
            }
            log.debug(
                String.format( "%sendListFacet() : %s / %s", debugIndent, listFacet.getLocalName(), role.getName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void startAlias(TLAlias alias) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartAlias() : %s", debugIndent, alias.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void endAlias(TLAlias alias) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sendAlias() : %s", debugIndent, alias.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startActionFacet(org.opentravel.schemacompiler.model.TLActionFacet,
     *      org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void startActionFacet(TLActionFacet actionFacet, TLFacet payloadFacet) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartActionFacet() : %s", debugIndent, actionFacet.getLocalName() ) );
            debugIndent.append( "  " );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endActionFacet(org.opentravel.schemacompiler.model.TLActionFacet,
     *      org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void endActionFacet(TLActionFacet actionFacet, TLFacet payloadFacet) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sendActionFacet() : %s", debugIndent, actionFacet.getLocalName() ) );
            debugIndent.append( "  " );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void startAttribute(TLAttribute attribute) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartAttribute() : %s", debugIndent, attribute.getName() ) );
            debugIndent.append( "  " );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void endAttribute(TLAttribute attribute) {
        if (log.isDebugEnabled()) {
            if (debugIndent.length() > 0) {
                debugIndent.setLength( debugIndent.length() - 2 );
            }
            log.debug( String.format( "%sendAttribute() : %s", debugIndent, attribute.getName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void startElement(TLProperty element) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartElement() : %s", debugIndent, element.getName() ) );
            debugIndent.append( "  " );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void endElement(TLProperty element) {
        if (log.isDebugEnabled()) {
            if (debugIndent.length() > 0) {
                debugIndent.setLength( debugIndent.length() - 2 );
            }
            log.debug( String.format( "%sendElement() : %s", debugIndent, element.getName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startIndicatorAttribute(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void startIndicatorAttribute(TLIndicator indicator) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartIndicatorAttribute() : %s", debugIndent, indicator.getName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endIndicatorAttribute(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void endIndicatorAttribute(TLIndicator indicator) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sendIndicatorAttribute() : %s", debugIndent, indicator.getName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startIndicatorElement(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void startIndicatorElement(TLIndicator indicator) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartIndicatorElement() : %s", debugIndent, indicator.getName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endIndicatorElement(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void endIndicatorElement(TLIndicator indicator) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sendIndicatorElement() : %s", debugIndent, indicator.getName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
     */
    @Override
    public void startOpenEnumeration(TLOpenEnumeration openEnum) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartOpenEnumeration() : %s --> %s", debugIndent, openEnum.getLocalName(),
                generateExampleValue( openEnum ) ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
     */
    @Override
    public void endOpenEnumeration(TLOpenEnumeration openEnum) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sendOpenEnumeration() : %s", debugIndent, openEnum.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startRoleEnumeration(org.opentravel.schemacompiler.model.TLRoleEnumeration)
     */
    @Override
    public void startRoleEnumeration(TLRoleEnumeration roleEnum) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartRoleEnumeration() : %s --> %s", debugIndent, roleEnum.getLocalName(),
                generateExampleValue( roleEnum ) ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endRoleEnumeration(org.opentravel.schemacompiler.model.TLRoleEnumeration)
     */
    @Override
    public void endRoleEnumeration(TLRoleEnumeration roleEnum) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sendRoleEnumeration() : %s", debugIndent, roleEnum.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
     */
    @Override
    public void startValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartValueWithAttributes() : %s --> %s", debugIndent,
                valueWithAttributes.getLocalName(), generateExampleValue( valueWithAttributes ) ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
     */
    @Override
    public void endValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if (log.isDebugEnabled()) {
            log.debug(
                String.format( "%sendValueWithAttributes() : %s", debugIndent, valueWithAttributes.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startExtensionPoint(org.opentravel.schemacompiler.model.TLPatchableFacet)
     */
    @Override
    public void startExtensionPoint(TLPatchableFacet facet) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartExtensionPoint() : %s", debugIndent, facet.getLocalName() ) );
            debugIndent.append( "  " );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endExtensionPoint(org.opentravel.schemacompiler.model.TLPatchableFacet)
     */
    @Override
    public void endExtensionPoint(TLPatchableFacet facet) {
        if (log.isDebugEnabled()) {
            if (debugIndent.length() > 0) {
                debugIndent.setLength( debugIndent.length() - 2 );
            }
            log.debug( String.format( "%sendExtensionPoint() : %s", debugIndent, facet.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
     */
    @Override
    public void startExtensionPointFacet(TLExtensionPointFacet facet) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartExtensionPointFacet() : %s", debugIndent, facet.getLocalName() ) );
            debugIndent.append( "  " );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
     */
    @Override
    public void endExtensionPointFacet(TLExtensionPointFacet facet) {
        if (log.isDebugEnabled()) {
            if (debugIndent.length() > 0) {
                debugIndent.setLength( debugIndent.length() - 2 );
            }
            log.debug( String.format( "%sendExtensionPointFacet() : %s", debugIndent, facet.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startXsdComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
     */
    @Override
    public void startXsdComplexType(XSDComplexType xsdComplexType) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartXsdComplexType() : %s", debugIndent, xsdComplexType.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endXsdComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
     */
    @Override
    public void endXsdComplexType(XSDComplexType xsdComplexType) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sendXsdComplexType() : %s", debugIndent, xsdComplexType.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startXsdElement(org.opentravel.schemacompiler.model.XSDElement)
     */
    @Override
    public void startXsdElement(XSDElement xsdElement) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sstartXsdElement() : %s", debugIndent, xsdElement.getLocalName() ) );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endXsdElement(org.opentravel.schemacompiler.model.XSDElement)
     */
    @Override
    public void endXsdElement(XSDElement xsdElement) {
        if (log.isDebugEnabled()) {
            log.debug( String.format( "%sendXsdElement() : %s", debugIndent, xsdElement.getLocalName() ) );
        }
    }

    /**
     * Adds the given ID to the registry under the qualified name of the given entity.
     * 
     * @param identifiedEntity the named entity type that is referenced by the ID
     * @param id the ID value to add to the registry
     */
    protected void registerIdValue(NamedEntity identifiedEntity, String id) {
        QName entityName = new QName( identifiedEntity.getNamespace(), identifiedEntity.getLocalName() );

        idRegistry.computeIfAbsent( entityName, en -> new ArrayList<>() ).add( id );
    }

    /**
     * Adds an example role value for the given core object and each of the extended objects that it inherits role
     * attributes from.
     * 
     * @param coreObject the core object for which to generate role attributes
     */
    protected abstract void addRoleAttributes(TLCoreObject coreObject);

    /**
     * Adds any attributes and/or child elements that are required by the base payload type of the operation facet to
     * the current object tree.
     *
     * @param operationFacet the operation facet for which to add example web service payload content
     */
    protected abstract void addOperationPayloadContent(TLFacet operationFacet);

    /**
     * Returns the qualified name of the extension point that should be used for the given facet.
     * 
     * @param facet the facet for which to return the extension point name
     * @return QName
     */
    protected QName getExtensionPoint(TLPatchableFacet facet) {
        QName epfName;

        if (facet instanceof TLFacet) {
            epfName = ((TLFacetCodegenDelegate) facetDelegateFactory.getDelegate( (TLFacet) facet ))
                .getExtensionPointElement();
        } else {
            epfName = null;
        }
        return epfName;
    }

    /**
     * Returns the attribute name as it should be generated in the example document.
     * 
     * @param attribute the attribute whose name is to be returned
     * @return String
     */
    protected String getAttributeName(TLAttribute attribute) {
        String attrName = attribute.getName();

        if ((attrName != null) && attribute.isReference() && !attrName.endsWith( "Ref" )) {
            attrName += "Ref";
        }
        return attrName;
    }

    /**
     * Returns the repeat count for the given attribute.
     * 
     * @param attribute the attribute to check
     * @return int
     */
    protected int getRepeatCount(TLAttribute attribute) {
        int refRepeat = attribute.getReferenceRepeat();

        if (refRepeat == 0) {
            refRepeat = 1;
        } else if (refRepeat < 0) {
            refRepeat = 3;
        }
        return refRepeat;
    }

    /**
     * Returns the number of fraction digits for the given entity or -1 if no fraction digits constraint is specified.
     * 
     * @param entity the entity type to analyze
     * @return int
     */
    private int getFractionDigits(Object entity) {
        int fractionDigits = -1;

        while (entity != null) {
            if (entity instanceof TLSimple) {
                TLSimple simpleEntity = (TLSimple) entity;

                if (simpleEntity.getFractionDigits() >= 0) {
                    fractionDigits = simpleEntity.getFractionDigits();
                    break;

                } else {
                    entity = simpleEntity.getParentType();
                }

            } else if (entity instanceof TLAttribute) {
                entity = ((TLAttribute) entity).getType();

            } else if (entity instanceof TLProperty) {
                entity = ((TLProperty) entity).getType();

            } else {
                entity = null;
            }
        }
        return fractionDigits;
    }

    /**
     * Resolves the base element type for the given named entity.
     * 
     * @param elementType the element type to be resolved
     * @return NamedEntity
     */
    protected NamedEntity resolveBaseElementType(NamedEntity elementType) {
        if (!XsdCodegenUtils.isSimpleCoreObject( elementType ) && (context.getModelElement() == null)) {
            if (elementType instanceof TLAlias) {
                elementType = ((TLAlias) elementType).getOwningEntity();
            }
            if (elementType instanceof TLFacet) {
                TLFacet elementTypeFacet = (TLFacet) elementType;

                if (elementTypeFacet.getFacetType() == TLFacetType.SUMMARY) {
                    elementType = elementTypeFacet.getOwningEntity();
                }
            }
        }
        return elementType;
    }

    /**
     * Determine whether we should be using the substitutable or non-substitutable name for the element.
     * 
     * @param elementType the type of element for which the determination should be made
     * @return boolean
     */
    protected boolean useSubstitutableElementName(NamedEntity elementType) {
        boolean useSubstitutableElementName = false;

        if (!XsdCodegenUtils.isSimpleCoreObject( elementType )) {
            if (context.getModelElement() != null) {
                TLPropertyType modelPropertyType = context.getModelElement().getType();

                if (modelPropertyType instanceof TLAlias) {
                    modelPropertyType = (TLPropertyType) ((TLAlias) modelPropertyType).getOwningEntity();
                }
                if ((modelPropertyType instanceof TLBusinessObject) || (modelPropertyType instanceof TLCoreObject)) {
                    useSubstitutableElementName = true;
                }

            } else { // no property - this is the root element of the document
                NamedEntity tempElementType = resolveBaseElementType( elementType );

                if ((tempElementType instanceof TLBusinessObject) || (tempElementType instanceof TLCoreObject)) {
                    useSubstitutableElementName = true;
                }
            }
        }
        return useSubstitutableElementName;
    }

    /**
     * Handles the deferred assignment of 'IDREF' and 'IDREFS' values as a post-processing step of the example
     * generation process.
     */
    protected abstract class IdReferenceAssignment {

        protected String nodeName;
        protected NamedEntity referencedEntity;
        protected int referenceCount;

        /**
         * Constructor used for assigning an IDREF(S) value to an XML element.
         * 
         * @param referencedEntity the named entity that was referenced (may be null for legacy IDREF(S) values)
         * @param referenceCount indicates the number of reference values that should be applied
         */
        protected IdReferenceAssignment(NamedEntity referencedEntity, int referenceCount) {
            this( referencedEntity, referenceCount, null );
        }

        /**
         * Constructor used for assigning an IDREF(S) value to an XML attribute.
         * 
         * @param referencedEntity the named entity that was referenced (may be null for legacy IDREF(S) values)
         * @param referenceCount indicates the number of reference values that should be applied
         * @param nodeName the name of the IDREF(S) attribute/element to which the value should be assigned
         */
        protected IdReferenceAssignment(NamedEntity referencedEntity, int referenceCount, String nodeName) {
            this.referencedEntity = referencedEntity;
            this.referenceCount = referenceCount;
            this.nodeName = nodeName;
        }

        /**
         * Assigns the IDREF value(s) to the appropriate attribute or element based on information collected in the
         * message ID registry during document generation.
         */
        public abstract void assignReferenceValue();

        /**
         * Retrieves a space-separated list of appropriate ID values from the registry.
         * 
         * @return String
         */
        protected String getIdValues() {
            QName elementName = getReferenceElementName();
            List<String> idValues = idRegistry.get( elementName );
            int count = (idValues == null) ? 0 : Math.min( referenceCount, idValues.size() );
            StringBuilder valueStr = new StringBuilder();

            for (int i = 0; i < count; i++) {
                String id = idValues.remove( 0 );

                if (valueStr.length() > 0) {
                    valueStr.append( " " );
                }
                valueStr.append( id );
                idValues.add( id ); // rotate to the end of the list
            }
            return (valueStr.length() == 0) ? null : valueStr.toString();
        }

        /**
         * Searches the ID registry for an ID that is compatible with the entity type that was referenced by the model
         * attribute or element.
         * 
         * @return QName
         */
        private QName getReferenceElementName() {
            QName elementName = null;

            if (referencedEntity == null) {
                if (!idRegistry.keySet().isEmpty()) {
                    elementName = idRegistry.keySet().iterator().next();
                }
            } else {
                // Build a list of eligible entity names that qualify for the
                // reference
                List<QName> entityNames = new ArrayList<>();
                NamedEntity entity = referencedEntity;

                // Add all applicable names for the entity and its alias
                // equivalents
                addEntityNames( entity, entityNames );

                // Iterate through the list and select the first name that
                // appears in the ID registry
                for (QName entityName : entityNames) {
                    if (idRegistry.containsKey( entityName )) {
                        elementName = entityName;
                        break;
                    }
                }
            }
            return elementName;
        }

        /**
         * Adds the names of of the given entity and all of its applicable facets to the list of names provided.
         * 
         * @param entityRef the referenced entity from which to collect names
         * @param entityNames the list of entity names to be appended
         */
        private void addEntityNames(NamedEntity entityRef, List<QName> entityNames) {
            // Always include the entity whose name was referenced directly
            entityNames.add( new QName( entityRef.getNamespace(), entityRef.getLocalName() ) );

            if (entityRef instanceof TLAlias) {
                addAliasEntityNames( (TLAlias) entityRef, entityNames );

            } else {
                addNonAliasEntityNames( entityRef, entityNames );
            }
        }

        /**
         * Adds entity names for the given entity based on its specific type.
         * 
         * @param entityRef the entity for which to add names
         * @param entityNames the list of entity names to be appended
         */
        private void addNonAliasEntityNames(NamedEntity entityRef, List<QName> entityNames) {
            if (entityRef instanceof TLBusinessObject) {
                TLBusinessObject entity = (TLBusinessObject) entityRef;

                entityNames.add( new QName( entity.getIdFacet().getNamespace(), entity.getIdFacet().getLocalName() ) );
                entityNames.add(
                    new QName( entity.getSummaryFacet().getNamespace(), entity.getSummaryFacet().getLocalName() ) );

                for (TLFacet customFacet : entity.getCustomFacets()) {
                    entityNames.add( new QName( customFacet.getNamespace(), customFacet.getLocalName() ) );
                }
                entityNames
                    .add( new QName( entity.getDetailFacet().getNamespace(), entity.getDetailFacet().getLocalName() ) );

            } else if (entityRef instanceof TLCoreObject) {
                TLCoreObject entity = (TLCoreObject) entityRef;

                entityNames.add(
                    new QName( entity.getSummaryFacet().getNamespace(), entity.getSummaryFacet().getLocalName() ) );
                entityNames
                    .add( new QName( entity.getDetailFacet().getNamespace(), entity.getDetailFacet().getLocalName() ) );

            } else if (entityRef instanceof TLChoiceObject) {
                TLChoiceObject entity = (TLChoiceObject) entityRef;

                entityNames
                    .add( new QName( entity.getSharedFacet().getNamespace(), entity.getSharedFacet().getLocalName() ) );

                for (TLFacet choiceFacet : entity.getChoiceFacets()) {
                    entityNames.add( new QName( choiceFacet.getNamespace(), choiceFacet.getLocalName() ) );
                }
            }
        }

        /**
         * Adds entity names for the given alias based on its underlying entity type.
         * 
         * @param entityAlias the alias for which to add entity names
         * @param entityNames the list of entity names to be appended
         */
        private void addAliasEntityNames(TLAlias entityAlias, List<QName> entityNames) {
            NamedEntity owner = entityAlias.getOwningEntity();

            if (owner instanceof TLBusinessObject) {
                TLBusinessObject entity = (TLBusinessObject) owner;
                TLAlias idAlias = AliasCodegenUtils.getFacetAlias( entityAlias, TLFacetType.ID );
                TLAlias summaryAlias = AliasCodegenUtils.getFacetAlias( entityAlias, TLFacetType.SUMMARY );
                TLAlias detailAlias = AliasCodegenUtils.getFacetAlias( entityAlias, TLFacetType.DETAIL );

                entityNames.add( new QName( idAlias.getNamespace(), idAlias.getLocalName() ) );
                entityNames.add( new QName( summaryAlias.getNamespace(), summaryAlias.getLocalName() ) );

                for (TLFacet customFacet : entity.getCustomFacets()) {
                    TLAlias customAlias = AliasCodegenUtils.getFacetAlias( entityAlias, TLFacetType.CUSTOM,
                        FacetCodegenUtils.getFacetName( customFacet ) );

                    entityNames.add( new QName( customAlias.getNamespace(), customAlias.getLocalName() ) );
                }
                entityNames.add( new QName( detailAlias.getNamespace(), detailAlias.getLocalName() ) );

            } else if (owner instanceof TLCoreObject) {
                TLAlias summaryAlias = AliasCodegenUtils.getFacetAlias( entityAlias, TLFacetType.SUMMARY );
                TLAlias detailAlias = AliasCodegenUtils.getFacetAlias( entityAlias, TLFacetType.DETAIL );

                entityNames.add( new QName( summaryAlias.getNamespace(), summaryAlias.getLocalName() ) );
                entityNames.add( new QName( detailAlias.getNamespace(), detailAlias.getLocalName() ) );

            } else if (owner instanceof TLChoiceObject) {
                TLChoiceObject entity = (TLChoiceObject) owner;
                TLAlias sharedAlias = AliasCodegenUtils.getFacetAlias( entityAlias, TLFacetType.SHARED );

                entityNames.add( new QName( sharedAlias.getNamespace(), sharedAlias.getLocalName() ) );

                for (TLFacet choiceFacet : entity.getChoiceFacets()) {
                    TLAlias choiceAlias = AliasCodegenUtils.getFacetAlias( entityAlias, TLFacetType.CHOICE,
                        FacetCodegenUtils.getFacetName( choiceFacet ) );

                    entityNames.add( new QName( choiceAlias.getNamespace(), choiceAlias.getLocalName() ) );
                }
            }
        }

    }

    /**
     * Encapsulates the data-generation context within the current element/property being visited.
     * 
     * @author S. Livezey, E. Bronson
     */
    protected class ExampleContext {

        private T node;
        private TLProperty modelElement;
        private TLAlias modelAlias;
        private TLActionFacet modelActionFacet;
        private TLAttribute modelAttribute;

        /**
         * Constructor that specifies the <code>TLProperty</code> instance with which this context is associated. If the
         * model element passed to this method is null, the context will be assumed to represent the root element of the
         * DOM document that is created.
         * 
         * @param modelElement the model property to be associated with the new context
         */
        public ExampleContext(TLProperty modelElement) {
            this.modelElement = modelElement;
        }

        /**
         * Returns the element that is being generated for this context.
         * 
         * @return T
         */
        public T getNode() {
            return node;
        }

        /**
         * Assigns the element that is being generated for this context.
         * 
         * @param node the element to assign
         */
        public void setNode(T node) {
            this.node = node;
        }

        /**
         * Returns the <code>TLProperty</code> instance with which this context is associated.
         * 
         * @return TLProperty
         */
        public TLProperty getModelElement() {
            return modelElement;
        }

        /**
         * Returns the alias or role (if any) that is associated with this context.
         * 
         * @return TLAlias
         */
        public TLAlias getModelAlias() {
            return modelAlias;
        }

        /**
         * Assigns the alias or role (if any) that is associated with this context.
         * 
         * @param modelAlias the alias or role to be associated with this context
         */
        public void setModelAlias(TLAlias modelAlias) {
            this.modelAlias = modelAlias;
        }

        /**
         * Returns the action facet (if any) that is associated with this context.
         *
         * @return TLActionFacet
         */
        public TLActionFacet getModelActionFacet() {
            return modelActionFacet;
        }

        /**
         * Assigns the action facet (if any) that is associated with this context.
         *
         * @param modelActionFacet the action facet to assign
         */
        public void setModelActionFacet(TLActionFacet modelActionFacet) {
            this.modelActionFacet = modelActionFacet;
        }

        /**
         * Returns the attribute (if any) that is currently associated with this context.
         * 
         * @return TLAttribute
         */
        public TLAttribute getModelAttribute() {
            return modelAttribute;
        }

        /**
         * Assigns the attribute (if any) that is currently associated with this context.
         * 
         * @param modelAttribute the model attribute to assign
         */
        public void setModelAttribute(TLAttribute modelAttribute) {
            this.modelAttribute = modelAttribute;
        }

    }

}
