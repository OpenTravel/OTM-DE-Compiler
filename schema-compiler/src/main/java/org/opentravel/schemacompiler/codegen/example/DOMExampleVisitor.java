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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <code>ExampleVisitor</code> component used to construct a DOM tree containing the example data.
 * 
 * @author S. Livezey
 */
public class DOMExampleVisitor extends AbstractExampleVisitor {

    private static Map<TLFacetType, SchemaDependency> extensionPointTypeMap;

    private Map<String, String> namespaceMappings = new HashMap<String, String>();
    private Set<String> extensionPointNamespaces = new HashSet<String>();
    private Set<String> externalDependencyNamespaces = new HashSet<String>();
    private Stack<ExampleContext> contextStack = new Stack<ExampleContext>();
    private Stack<TLPropertyOwner> facetStack = new Stack<TLPropertyOwner>();
    protected CodeGenerationWsdlBindings wsdlBindings = null;
    private Document domDocument;
    private ExampleContext context = new ExampleContext(null);
    private String lastExampleValue;

    private Map<QName, List<String>> idRegistry = new HashMap<QName, List<String>>();
    private List<IdReferenceAssignment> referenceAssignments = new ArrayList<IdReferenceAssignment>();

    /**
     * Default constructor.
     */
    public DOMExampleVisitor() {
        this(null);
    }

    /**
     * Contstructor that provides the navigation options to use during example generation.
     * 
     * @param options
     *            the example generation options
     */
    public DOMExampleVisitor(String preferredContext) {
        super(preferredContext);

        try {
            ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();

            if (appContext
                    .containsBean(SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS)) {
                this.wsdlBindings = (CodeGenerationWsdlBindings) appContext
                        .getBean(SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS);
            }
            this.domDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .newDocument();

        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to create DOM document instance.", e);
        }
    }

    /**
     * Returns the DOM document instance that was constructed during the navigation process.
     * 
     * @return Document
     */
    public Document getDocument() {
        // Before returning the document, assign any pending IDREF(S) values discovered during
        // generation of the DOM document
        synchronized (referenceAssignments) {
            for (IdReferenceAssignment refAssignment : referenceAssignments) {
                refAssignment.assignReferenceValue();
            }
            referenceAssignments.clear(); // clear the list so we don't do this more than once
            idRegistry.clear();
        }
        return domDocument;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#getBoundNamespaces()
     */
    @Override
    public Collection<String> getBoundNamespaces() {
        List<String> nsList = new ArrayList<String>();

        /*
         * Commented out this line due to false validation errors on substitution group elements
         * that result when multiple namespace mappings are included in the 'xsi:schemaLocation'
         * attribute of an example XML document.
         * 
         * nsList.addAll( extensionPointNamespaces );
         */
        nsList.addAll( externalDependencyNamespaces );
        Collections.sort( nsList );

        if ((domDocument != null) && (domDocument.getDocumentElement() != null)) {
            nsList.add(0, domDocument.getDocumentElement().getNamespaceURI());
        }
        return nsList;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#visitSimpleType(org.opentravel.schemacompiler.model.TLAttributeType)
     */
    @Override
    public void visitSimpleType(TLAttributeType simpleType) {
        super.visitSimpleType(simpleType);
        createSimpleElement(simpleType);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startFacet(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void startFacet(TLFacet facet) {
        super.startFacet(facet);
        facetStack.push(facet);
        createComplexElement(facet);

        if (facet.getOwningEntity() instanceof TLCoreObject) {
            addRoleAttributes((TLCoreObject) facet.getOwningEntity());

        } else if (facet.getOwningEntity() instanceof TLOperation) {
            addOperationPayloadContent(facet);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#endFacet(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void endFacet(TLFacet facet) {
        super.endFacet(facet);

        if (facetStack.peek() == facet) {
            facetStack.pop();
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startListFacet(org.opentravel.schemacompiler.model.TLListFacet,
     *      org.opentravel.schemacompiler.model.TLRole)
     */
    @Override
    public void startListFacet(TLListFacet listFacet, TLRole role) {
        super.startListFacet(listFacet, role);

        // If this is a repeat visit of the list facet for a different role, we must complete the
        // old element and start a new one.
        if (context.getDomElement() != null) {
        	TLAlias contextAlias = context.getModelAlias();

            context = new ExampleContext(context.getModelElement());
            context.setModelAlias(contextAlias);
        }

        // If this is a list facet for a normal TLFacet, push the underlying item facet onto the
        // stack
        if (listFacet.getItemFacet() instanceof TLFacet) {
            facetStack.push((TLFacet) listFacet.getItemFacet());
        }
        createComplexElement(listFacet);

        if ((listFacet.getOwningEntity() instanceof TLCoreObject)
                && !(listFacet.getItemFacet() instanceof TLSimpleFacet)) {
            addRoleAttributes((TLCoreObject) listFacet.getOwningEntity());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#endListFacet(org.opentravel.schemacompiler.model.TLListFacet,
     *      org.opentravel.schemacompiler.model.TLRole)
     */
    @Override
    public void endListFacet(TLListFacet listFacet, TLRole role) {
        super.endListFacet(listFacet, role);

        if (facetStack.peek() == listFacet.getItemFacet()) {
            facetStack.pop();
        }
    }

    /**
	 * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#getContextFacet()
	 */
	@Override
	protected NamedEntity getContextFacet() {
		NamedEntity elementType = (context.modelElement == null) ? null : context.modelElement.getType();
		NamedEntity contextFacet;
		
		if (elementType instanceof TLExtensionPointFacet) {
			contextFacet = null; // No inheritance or aliases for extension point facets
			
		} else if (elementType instanceof TLValueWithAttributes) {
			contextFacet = elementType;
			
		} else {
			ExampleContext facetContext = context;
			
			// If we are currently processing an attribute value, the facet context will be the
			// current one.  If we are processing an element value, the facet context will be
			// on top of the context stack.
			if ((facetContext.modelAttribute == null) && !contextStack.isEmpty()) {
				facetContext = contextStack.peek();
			}
			
			if (facetContext.modelAlias != null) {
				TLAlias facetAlias = facetContext.modelAlias;
				
				if (facetAlias.getOwningEntity() instanceof TLListFacet) {
					TLAlias coreAlias = AliasCodegenUtils.getOwnerAlias( facetAlias );
					
					facetAlias = AliasCodegenUtils.getFacetAlias( coreAlias,
							((TLListFacet) facetAlias.getOwningEntity()).getItemFacet().getFacetType());
				}
				contextFacet = facetAlias;
				
			} else {
				contextFacet = facetStack.isEmpty() ? null : facetStack.peek();
			}
		}
		return contextFacet;
	}

	/**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void startAlias(TLAlias alias) {
        super.startAlias(alias);

        if (context == null) {
            throw new IllegalStateException(
                    "Alias encountered without an available element context.");
        }
        context.setModelAlias(alias);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void endAlias(TLAlias alias) {
        super.endAlias(alias);

        if (context == null) {
            throw new IllegalStateException(
                    "Alias encountered without an available element context.");
        }
        context.setModelAlias(null);
    }

    /**
	 * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#startRequest(org.opentravel.schemacompiler.model.TLActionRequest)
	 */
	@Override
	public void startRequest(TLActionRequest request) {
		super.startRequest(request);
		
		if (request.getPayloadType() instanceof TLActionFacet) {
			TLActionFacet payloadType = (TLActionFacet) request.getPayloadType();
			
	        facetStack.push(payloadType);
	        createComplexElement(request);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#endRequest(org.opentravel.schemacompiler.model.TLActionRequest)
	 */
	@Override
	public void endRequest(TLActionRequest request) {
		super.endRequest(request);
		
		if (request.getPayloadType() instanceof TLActionFacet) {
			TLActionFacet payloadType = (TLActionFacet) request.getPayloadType();
			
	        if (facetStack.peek() == payloadType) {
	            facetStack.pop();
	        }
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#startResponse(org.opentravel.schemacompiler.model.TLActionResponse)
	 */
	@Override
	public void startResponse(TLActionResponse response) {
		super.startResponse(response);
		
		if (response.getPayloadType() instanceof TLActionFacet) {
			TLActionFacet payloadType = (TLActionFacet) response.getPayloadType();
			
	        facetStack.push(payloadType);
	        createComplexElement(response);
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#endResponse(org.opentravel.schemacompiler.model.TLActionResponse)
	 */
	@Override
	public void endResponse(TLActionResponse response) {
		super.endResponse(response);
		
		if (response.getPayloadType() instanceof TLActionFacet) {
			TLActionFacet payloadType = (TLActionFacet) response.getPayloadType();
			
	        if (facetStack.peek() == payloadType) {
	            facetStack.pop();
	        }
		}
	}

	/**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void startAttribute(TLAttribute attribute) {
        super.startAttribute(attribute);

        if (context == null) {
            throw new IllegalStateException(
                    "Attribute encountered without an available element context.");
        }
        context.setModelAttribute(attribute);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void endAttribute(TLAttribute attribute) {
        super.endAttribute(attribute);

        if (context == null) {
            throw new IllegalStateException(
                    "Attribute encountered without an available element context.");
        }

        // Capture ID values in the registry for use during post-processing
        if (XsdCodegenUtils.isIdType(attribute.getType())) {
        	TLAttributeOwner owner = attribute.getOwner();
        	NamedEntity contextFacet = getContextFacet();
        	
        	if (contextFacet != null) {
                registerIdValue(contextFacet, lastExampleValue);
        	} else {
                registerIdValue(owner, lastExampleValue);
        	}
        }

        // Queue up IDREF(S) attributes for assignment during post-processing
        if (XsdCodegenUtils.isIdRefType(attribute.getType())) {
            referenceAssignments.add(new IdReferenceAssignment(null, 1, attribute.getName()));
        }
        if (XsdCodegenUtils.isIdRefsType(attribute.getType())) {
            referenceAssignments.add(new IdReferenceAssignment(null, 3, attribute.getName()));
        }

        // If the attribute was an open enumeration type, we have to add an additional attribute
        // for the 'Extension' value.
        TLAttributeType attributeType = attribute.getType();

        while (attributeType instanceof TLValueWithAttributes) {
            attributeType = ((TLValueWithAttributes) attributeType).getParentType();
        }
        if (attributeType instanceof TLOpenEnumeration) {
            context.getDomElement().setAttribute(
                    context.getModelAttribute().getName() + "Extension",
                    context.getModelAttribute().getName() + "_Other_Value");
        }
        context.setModelAttribute(null);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void startElement(TLProperty element) {
        super.startElement(element);
        contextStack.push(context);
        context = new ExampleContext(element);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void endElement(TLProperty element) {
        super.endElement(element);

        // Capture ID values in the registry for use during post-processing
        if (XsdCodegenUtils.isIdType(element.getType())) {
        	TLPropertyOwner owner = element.getOwner();
        	NamedEntity contextFacet = getContextFacet();
        	
        	if (contextFacet != null) {
                registerIdValue(contextFacet, lastExampleValue);
        	} else {
                registerIdValue(owner, lastExampleValue);
        	}
        }

        // Queue up IDREF(S) attributes for assignment during post-processing
        if (XsdCodegenUtils.isIdRefType(element.getType())) {
            referenceAssignments.add(new IdReferenceAssignment(null, 1));
        }
        if (XsdCodegenUtils.isIdRefsType(element.getType())) {
            referenceAssignments.add(new IdReferenceAssignment(null, 3));
        }
        if (element.isReference()) {
            int referenceCount = (element.getRepeat() <= 1) ? 1 : element.getRepeat();

            referenceAssignments.add(new IdReferenceAssignment(element.getType(), referenceCount));
        }
        context = contextStack.pop();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#startIndicatorAttribute(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void startIndicatorAttribute(TLIndicator indicator) {
        super.startIndicatorAttribute(indicator);

        if (context.getDomElement() == null) {
            throw new IllegalStateException(
                    "Indicator encountered without an available element context.");
        }
        String attributeName = indicator.getName();

        if (!attributeName.endsWith("Ind")) {
            attributeName += "Ind";
        }
        context.getDomElement().setAttribute(attributeName, "true");
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#startIndicatorElement(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void startIndicatorElement(TLIndicator indicator) {
        super.startIndicatorElement(indicator);
        String elementName = indicator.getName();

        if (!elementName.endsWith("Ind")) {
            elementName += "Ind";
        }
        Element element = createXmlElement(indicator.getOwner().getNamespace(), elementName,
                indicator.getOwner());

        element.setTextContent("true");
        context.getDomElement().appendChild(element);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
     */
    @Override
    public void startOpenEnumeration(TLOpenEnumeration openEnum) {
        super.startOpenEnumeration(openEnum);
        createSimpleElement(openEnum);
        context.getDomElement().setAttribute("extension", "Other_Value");
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#startRoleEnumeration(org.opentravel.schemacompiler.model.TLRoleEnumeration)
     */
    @Override
    public void startRoleEnumeration(TLRoleEnumeration roleEnum) {
        super.startRoleEnumeration(roleEnum);
        createSimpleElement(roleEnum);
        context.getDomElement().setAttribute("extension", "Other_Value");
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
     */
    @Override
    public void startValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        NamedEntity parentType = valueWithAttributes.getParentType();

        // Find the root parent type for the VWA
        while (parentType instanceof TLValueWithAttributes) {
            parentType = ((TLValueWithAttributes) parentType).getParentType();
        }

        // Construct the example content for the VWA
        super.startValueWithAttributes(valueWithAttributes);
        createComplexElement(valueWithAttributes);

        if ((parentType instanceof TLOpenEnumeration) || (parentType instanceof TLRoleEnumeration)) {
            context.getDomElement().setAttribute("extension", "Other_Value");
        }
        context.getDomElement().setTextContent(generateExampleValue(valueWithAttributes));
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#startExtensionPoint(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void startExtensionPoint(TLPatchableFacet facet) {
        super.startExtensionPoint(facet);
        SchemaDependency extensionElement = extensionPointTypeMap.get(facet.getFacetType());
        Element owningDomElement = context.getDomElement();

        if ((extensionElement != null) && (owningDomElement != null)) {
            contextStack.push(context);
            context = new ExampleContext(null);
            context.setDomElement(createXmlElement(extensionElement.getSchemaDeclaration()
                    .getNamespace(), extensionElement.getLocalName(), extensionElement
                    .getSchemaDeclaration().getDefaultPrefix()));
            owningDomElement.appendChild(context.getDomElement());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#endExtensionPoint(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void endExtensionPoint(TLPatchableFacet facet) {
        super.endExtensionPoint(facet);
        SchemaDependency extensionElement = extensionPointTypeMap.get(facet.getFacetType());
        Element domElement = context.getDomElement();

        if ((extensionElement != null)
                && (domElement != null)
                && domElement.getLocalName().equals(extensionElement.getLocalName())
                && domElement.getNamespaceURI().equals(
                        extensionElement.getSchemaDeclaration().getNamespace())) {
            context = contextStack.pop();
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
     */
    @Override
    public void startExtensionPointFacet(TLExtensionPointFacet facet) {
        super.startExtensionPointFacet(facet);

        extensionPointNamespaces.add(facet.getNamespace());
        contextStack.push(context);
        context = new ExampleContext(null);
        facetStack.push(facet);
        createComplexElement(facet);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#endExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
     */
    @Override
    public void endExtensionPointFacet(TLExtensionPointFacet facet) {
        super.endExtensionPointFacet(facet);

        if (facetStack.peek() == facet) {
            facetStack.pop();
        }
        context = contextStack.pop();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startXsdComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
     */
    @Override
    public void startXsdComplexType(XSDComplexType xsdComplexType) {
        super.startXsdComplexType(xsdComplexType);
        createComplexElement(xsdComplexType);
        addLegacyElementContent(xsdComplexType.getNamespace(), xsdComplexType.getLocalName());
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startXsdElement(org.opentravel.schemacompiler.model.XSDElement)
     */
    @Override
    public void startXsdElement(XSDElement xsdElement) {
        super.startXsdElement(xsdElement);
        createComplexElement(xsdElement);
        addLegacyElementContent(xsdElement.getNamespace(), xsdElement.getLocalName());
    }

    /**
     * Constructs a complex XML element using the current context information. The new element is
     * assigned as a child of the 'currentElement' for this visitor, and replaces that
     * 'currentElement' as the DOM element that will receive attributes and child elements within
     * the current context.
     * 
     * @param elementType
     *            specifies the type of element to use when the current context is null (i.e. when
     *            the new element will be the root of the XML document)
     */
    private void createComplexElement(NamedEntity elementType) {
        Element newElement;

        // Check the context state to make sure nothing illegal is happening
        if (context.getModelAttribute() != null) {
            throw new IllegalStateException(
                    "Complex elements cannot be created as DOM attribute values.");
        }
        if (context.getDomElement() != null) {
            throw new IllegalStateException(
                    "A complex element has already been defined for the current context.");
        }

        // Create the new DOM element
        if ((context.getModelElement() != null)
                && ((elementType instanceof TLAttributeType) || (elementType.equals(context
                        .getModelElement().getType())))) {
            newElement = createPropertyElement(context.getModelElement(),
                    (TLPropertyType) elementType);
        } else {
            newElement = createXmlElement(elementType.getNamespace(),
                    getContextElementName(elementType, false), elementType);
        }
        context.setDomElement(newElement);

        // Assign the new DOM element as a child of the previous context
        if (contextStack.isEmpty() || (contextStack.peek().getDomElement() == null)) {
            domDocument.appendChild(newElement);
        } else {
            contextStack.peek().getDomElement().appendChild(newElement);
        }
    }

    /**
     * Constructs a simple XML element or an XML attribute depending upon the content of the current
     * context information. New elements are assigned as a child of the 'currentElement' for this
     * visitor, and then replace that 'currentElement' as the DOM element for the context.
     * 
     * @param elementType
     *            specifies the type of element to use when the current context is null (i.e. when
     *            the new element will be the root of the XML document)
     */
    private void createSimpleElement(NamedEntity elementType) {
        if (context.getModelAttribute() != null) {
            if (context.getDomElement() == null) {
                throw new IllegalStateException("No element available for new attribute creation.");
            }
            context.getDomElement().setAttribute(context.getModelAttribute().getName(),
                    generateExampleValue(context.getModelAttribute()));

        } else {
            if (contextStack.isEmpty() && (context.getDomElement() == null)) {
                Element rootElement = createXmlElement(elementType.getNamespace(),
                        elementType.getLocalName(), elementType);

                rootElement.setTextContent(generateExampleValue(elementType));
                context.setDomElement(rootElement);
                domDocument.appendChild(rootElement);
            } else {
                // If the element has not already been created, do it now
                if (context.getDomElement() == null) {
                    createComplexElement(elementType); // constructs a new DOM element with no
                                                       // content
                }
                if (context.getModelElement().isReference()) {
                    context.getDomElement().setTextContent(generateExampleValue(elementType));

                } else {
                    context.getDomElement().setTextContent(
                            generateExampleValue(context.getModelElement()));
                }
            }
        }
    }

    /**
     * Creates a DOM element using the property information provided.
     * 
     * @param property
     *            the model property for which to construct a DOM element
     * @param propertyType
     *            the type of the property being navigated (may be different than the property's
     *            assigned type)
     * @return Element
     */
    private Element createPropertyElement(TLProperty property, TLPropertyType propertyType) {
        NamedEntity prefixEntity = null;
        String elementNamespace = null;
        String elementName = null;

        if (!PropertyCodegenUtils.hasGlobalElement(propertyType)) {
            if (context.getModelAlias() != null) {
                elementName = XsdCodegenUtils.getGlobalElementName(context.getModelAlias())
                        .getLocalPart();
                elementNamespace = context.getModelAlias().getNamespace();
                prefixEntity = context.getModelAlias();

            } else if (propertyType instanceof TLListFacet) {
                TLListFacet listFacetType = (TLListFacet) propertyType;

                if (listFacetType.getFacetType() != TLFacetType.SIMPLE) {
                    elementName = XsdCodegenUtils
                            .getGlobalElementName(listFacetType.getItemFacet()).getLocalPart();
                    elementNamespace = listFacetType.getNamespace();
                    prefixEntity = listFacetType;
                }
            }

            if (elementName == null) {
                if ((property.getName() == null) || (property.getName().length() == 0)) {
                    elementName = propertyType.getLocalName();
                } else {
                    elementName = property.getName();
                }

                // The element may be inherited, so to obtain the proper namespace we need to use
                // the
                // most recently encountered facet (i.e. the top of the facet stack)

                if (facetStack.isEmpty()) {
                    elementNamespace = property.getOwner().getNamespace();
                    prefixEntity = property.getOwner();
                } else {
                    TLPropertyOwner propertyOwner;

                    if (facetStack.peek() == propertyType) {
                        // If the top of the facet stack is our property type, we need to go up one
                        // more level
                        // to find the facet that declared (or inherited) this property.
                        if (facetStack.size() > 1) {
                            propertyOwner = facetStack.get(facetStack.size() - 2);
                        } else {
                            propertyOwner = property.getOwner();
                        }
                    } else {
                        propertyOwner = facetStack.peek();
                    }
                    elementNamespace = propertyOwner.getNamespace();
                    prefixEntity = propertyOwner;
                }
            }

            if (property.isReference() && !elementName.endsWith("Ref")) {
                // probably a VWA reference, so we need to make sure the "Ref" suffix is appended
                elementName += "Ref";
            }

        } else {
            prefixEntity = propertyType;
            elementNamespace = propertyType.getNamespace();
            elementName = getContextElementName(propertyType, property.isReference());
        }
        return createXmlElement(elementNamespace, elementName, prefixEntity);
    }

    /**
     * Returns the element name for the current context model element.
     * 
     * @param elementType
     *            specifies the type of element to use when the current context is null or the given
     *            entity does not have a pre-defined global element
     * @param isReferenceProperty
     *            indicates whether the element type is assigned by value or reference
     * @return String
     */
    private String getContextElementName(NamedEntity elementType, boolean isReferenceProperty) {
        boolean useSubstitutableElementName = false;
        QName elementQName;

        // Determine whether we should be using the substitutable or non-substitutable name for the
        // element
        if (!XsdCodegenUtils.isSimpleCoreObject(elementType)) {
            if (context.getModelElement() != null) {
                TLPropertyType modelPropertyType = context.getModelElement().getType();

                if (modelPropertyType instanceof TLAlias) {
                    modelPropertyType = (TLPropertyType) ((TLAlias) modelPropertyType)
                            .getOwningEntity();
                }
                if ((modelPropertyType instanceof TLBusinessObject)
                        || (modelPropertyType instanceof TLCoreObject)) {
                    useSubstitutableElementName = true;
                }
            } else { // no property - this is the root element of the document
                if (elementType instanceof TLAlias) {
                    elementType = ((TLAlias) elementType).getOwningEntity();
                }
                if (elementType instanceof TLFacet) {
                    TLFacet elementTypeFacet = (TLFacet) elementType;

                    if (elementTypeFacet.getFacetType() == TLFacetType.SUMMARY) {
                        elementType = elementTypeFacet.getOwningEntity();
                    }
                }
                if ((elementType instanceof TLBusinessObject)
                        || (elementType instanceof TLCoreObject)) {
                    useSubstitutableElementName = true;
                }
            }
        }

        // Lookup the correct name for the element depending upon its specific characteristics
        if (context.getModelAlias() != null) {
            if (!isReferenceProperty && useSubstitutableElementName) {
                elementQName = XsdCodegenUtils.getSubstitutableElementName((TLAlias) context
                        .getModelAlias());

            } else {
                elementQName = PropertyCodegenUtils.getDefaultXmlElementName(
                        context.getModelAlias(), isReferenceProperty);
            }
        } else {
            if (!isReferenceProperty && useSubstitutableElementName
                    && (elementType instanceof TLFacet)) {
                elementQName = XsdCodegenUtils.getSubstitutableElementName((TLFacet) elementType);

            } else {
                elementQName = PropertyCodegenUtils.getDefaultXmlElementName(elementType,
                        isReferenceProperty);
            }
        }
        
        // If a default XML element name could not be discovered, try finding a global
        // schema element name for the entity
        if (elementQName == null) {
        	elementQName = XsdCodegenUtils.getGlobalElementName( elementType );
        }
        
        // If all other attempts to discover a global element name have failed, use the local
        // name of the entity.  This will not be schema valid, but that is okay for simple types
        // and VWA example documents.
        return (elementQName != null) ? elementQName.getLocalPart() : elementType.getLocalName();
    }

    /**
     * Constructs a DOM element using the information provided. In addition to the element itself,
     * any namespace mappings that are required for the element's prefix are also added to the
     * document's root.
     * 
     * @param namespace
     *            the namespace of the element to create
     * @param localName
     *            the local (tag) name of the element to create
     * @param prefixEntity
     *            the entity whose library should be used to obtain the preferred prefix of the new
     *            element
     * @return Element
     */
    private Element createXmlElement(String namespace, String localName, NamedEntity prefixEntity) {
        return createXmlElement(namespace, localName, prefixEntity.getOwningLibrary().getPrefix());
    }

    /**
     * Constructs a DOM element using the information provided. In addition to the element itself,
     * any namespace mappings that are required for the element's prefix are also added to the
     * document's root.
     * 
     * @param namespace
     *            the namespace of the element to create
     * @param localName
     *            the local (tag) name of the element to create
     * @param preferredPrefix
     *            the preferred prefix of the new element
     * @return Element
     */
    private Element createXmlElement(String namespace, String localName, String preferredPrefix) {
        Element element = domDocument.createElementNS(namespace, localName);
        String prefix = namespaceMappings.get(namespace);

        if (prefix == null) {
            Element rootElement = domDocument.getDocumentElement();
            if (rootElement == null)
                rootElement = element;

            if ((namespace != null) && !namespace.equals(rootElement.getNamespaceURI())) {

                // Identify a unique prefix for this namespace
                prefix = preferredPrefix;
                if (prefix == null)
                    prefix = "ns1";

                if (namespaceMappings.containsValue(prefix)) {
                    String prefixStr = prefix.equals("ns1") ? "ns" : prefix;
                    int nsCounter = 0;

                    while (namespaceMappings.containsValue(prefix)) {
                        prefix = prefixStr + (nsCounter++);
                    }
                }
                namespaceMappings.put(namespace, prefix);
                rootElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + prefix,
                        namespace);
            }
        }
        element.setPrefix(prefix);

        return element;
    }

    /**
     * Adds an example role value for the given core object and each of the extended objects that it
     * inherits role attributes from.
     * 
     * @param coreObject
     *            the core object for which to generate role attributes
     */
    private void addRoleAttributes(TLCoreObject coreObject) {
        while (coreObject != null) {
            if (coreObject.getRoleEnumeration().getRoles().size() > 0) {
                TLCoreObject extendedCore = (TLCoreObject) FacetCodegenUtils
                        .getFacetOwnerExtension(coreObject);
                String attrName = (extendedCore == null) ? "role" : coreObject.getLocalName()
                        + "Role";

                context.getDomElement().setAttribute(attrName,
                        exampleValueGenerator.getExampleRoleValue(coreObject));
            }
            coreObject = (TLCoreObject) FacetCodegenUtils.getFacetOwnerExtension(coreObject);
        }
    }

    /**
     * Adds any XML attributes and/or child elements that are required by the base payload type of
     * the operation facet to the current DOM element.
     * 
     * @param operationFacet
     *            the operation facet for which to add example web service payload content
     */
    private void addOperationPayloadContent(TLFacet operationFacet) {
        Element domElement = context.getDomElement();

        if ((domElement != null) && (wsdlBindings != null)) {
        	Map<String,String> nsMappings = new HashMap<String,String>();
        	Element rootElement = domElement.getOwnerDocument().getDocumentElement();
        	
            wsdlBindings.addPayloadExampleContent(domElement, nsMappings, operationFacet);
            
            for (String ns : nsMappings.keySet()) {
            	String _prefix = nsMappings.get( ns );
            	String prefix = _prefix;
            	int counter = 1;
            	
            	while (namespaceMappings.containsValue( prefix )) {
            		prefix = _prefix + counter;
            		counter++;
            	}
            	namespaceMappings.put( ns, prefix );
                rootElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + prefix, ns );
            }
            applyElementPrefixes( domElement );
        }
    }
    
    /**
     * Recursively applies a namespace prefix to the given element and all of its descendants
     * using the current set of namespace prefix mappings.
     * 
     * @param element  the DOM element to process
     */
    private void applyElementPrefixes(Element element) {
    	if (element.getPrefix() == null) {
        	element.setPrefix( namespaceMappings.get( element.getNamespaceURI() ) );
    	}
    	NodeList children = element.getChildNodes();
    	
    	for (int i = 0; i < children.getLength(); i++) {
    		Node child = children.item( 0 );
    		
    		if (child instanceof Element) {
    			applyElementPrefixes( (Element) child ); 
    		}
    	}
    }

    /**
     * Adds a comment as a child of the current DOM element indicating that the content is based on
     * a legacy type that cannot be generated by this visitor component.
     * 
     * @param xsdNamespace
     *            the namespace of the legacy schema entity
     * @param xsdLocalName
     *            the local name of the legacy schema entity
     */
    private void addLegacyElementContent(String xsdNamespace, String xsdLocalName) {
        context.getDomElement().appendChild(
                domDocument.createComment("  Legacy Content: {" + xsdNamespace + "}:"
                        + xsdLocalName + "  "));
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.AbstractExampleVisitor#generateExampleValue(java.lang.Object)
     */
    protected String generateExampleValue(Object entity) {
        lastExampleValue = super.generateExampleValue(entity);
        return lastExampleValue;
    }

    /**
     * Adds the given ID to the registry under the qualified name of the given entity.
     * 
     * @param identifiedEntity
     *            the named entity type that is referenced by the ID
     * @param id
     *            the ID value to add to the registry
     */
    private void registerIdValue(NamedEntity identifiedEntity, String id) {
        QName entityName = new QName(identifiedEntity.getNamespace(),
                identifiedEntity.getLocalName());
        List<String> idList = idRegistry.get(entityName);

        if (idList == null) {
            idList = new ArrayList<String>();
            idRegistry.put(entityName, idList);
        }
        idList.add(id);
    }

    /**
     * Encapsulates the data-generation context within the current element/property being visited.
     * 
     * @author S. Livezey
     */
    private class ExampleContext {

        private Element domElement;
        private TLProperty modelElement;
        private TLAlias modelAlias;
        private TLAttribute modelAttribute;

        /**
         * Constructor that specifies the <code>TLProperty</code> instance with which this context
         * is associated. If the model element passed to this method is null, the context will be
         * assumed to represent the root element of the DOM document that is created.
         * 
         * @param modelElement
         *            the model property to be associated with the new context
         */
        public ExampleContext(TLProperty modelElement) {
            this.modelElement = modelElement;
        }

        /**
         * Returns the DOM element that is being generated for this context.
         * 
         * @return Element
         */
        public Element getDomElement() {
            return domElement;
        }

        /**
         * Assigns the DOM element that is being generated for this context.
         * 
         * @param domElement
         *            the DOM element to assign
         */
        public void setDomElement(Element domElement) {
            this.domElement = domElement;
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
         * @param modelAlias
         *            the alias or role to be associated with this context
         */
        public void setModelAlias(TLAlias modelAlias) {
            this.modelAlias = modelAlias;
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
         * @param modelAttribute
         *            the model attribute to assign
         */
        public void setModelAttribute(TLAttribute modelAttribute) {
            this.modelAttribute = modelAttribute;
        }

    }

    /**
     * Handles the deferred assignment of 'IDREF' and 'IDREFS' values as a post-processing step of
     * the example generation process.
     */
    private class IdReferenceAssignment {

        private Element domElement;
        private String attributeName;
        private NamedEntity referencedEntity;
        private int referenceCount;

        /**
         * Constructor used for assigning an IDREF(S) value to an XML element.
         * 
         * @param referencedEntity
         *            the named entity that was referenced (may be null for legacy IDREF(S) values)
         * @param referenceCount
         *            indicates the number of reference values that should be applied
         */
        public IdReferenceAssignment(NamedEntity referencedEntity, int referenceCount) {
            this(referencedEntity, referenceCount, null);
        }

        /**
         * Constructor used for assigning an IDREF(S) value to an XML attribute.
         * 
         * @param referencedEntity
         *            the named entity that was referenced (may be null for legacy IDREF(S) values)
         * @param referenceCount
         *            indicates the number of reference values that should be applied
         * @param attributeName
         *            the name of the IDREF(S) attribute to which the value should be assigned
         */
        public IdReferenceAssignment(NamedEntity referencedEntity, int referenceCount,
                String attributeName) {
            this.referencedEntity = referencedEntity;
            this.referenceCount = referenceCount;
            this.attributeName = attributeName;
            this.domElement = context.domElement;
        }

        /**
         * Assigns the IDREF value(s) to the appropriate attribute or element based on information
         * collected in the message ID registry during document generation.
         */
        public void assignReferenceValue() {
            String referenceValue = getIdValues();

            if (referenceValue != null) {
                if (attributeName == null) {
                    domElement.setTextContent(referenceValue);
                } else {
                    domElement.setAttribute(attributeName, referenceValue);
                }
            }
        }

        /**
         * Retrieves a space-separated list of appropriate ID values from the registry.
         * 
         * @return String
         */
        private String getIdValues() {
            QName elementName = getReferenceElementName();
            List<String> idValues = idRegistry.get(elementName);
            int count = (idValues == null) ? 0 : Math.min(referenceCount, idValues.size());
            StringBuilder valueStr = new StringBuilder();

            for (int i = 0; i < count; i++) {
                String id = idValues.remove(0);

                if (valueStr.length() > 0)
                    valueStr.append(" ");
                valueStr.append(id);
                idValues.add(id); // rotate to the end of the list
            }
            return (valueStr.length() == 0) ? null : valueStr.toString();
        }

        /**
         * Searches the ID registry for an ID that is compatible with the entity type that was
         * referenced by the model attribute or element.
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
                // Build a list of eligible entity names that qualify for the reference
                List<QName> entityNames = new ArrayList<QName>();
                NamedEntity entity = referencedEntity;

                // Add all applicable names for the entity and its alias equivalents
//                if (entity instanceof TLAlias) {
//                    addEntityNames(entity, entityNames);
//                    entity = ((TLAlias) entity).getOwningEntity();
//                }
                addEntityNames(entity, entityNames);

//                if (entity instanceof TLAliasOwner) {
//                    for (TLAlias alias : ((TLAliasOwner) entity).getAliases()) {
//                        addEntityNames(alias, entityNames);
//                    }
//                }

                // Iterate through the list and select the first name that appears in the ID
                // registry
                for (QName entityName : entityNames) {
                    if (idRegistry.containsKey(entityName)) {
                        elementName = entityName;
                        break;
                    }
                }
            }
            return elementName;
        }

        /**
         * Adds the names of of the given entity and all of its applicable facets to the list of
         * names provided.
         * 
         * @param entityRef
         *            the referenced entity from which to collect names
         * @param entityNames
         *            the list of entity names to be appended
         */
        private void addEntityNames(NamedEntity entityRef, List<QName> entityNames) {
            // Always include the entity whose name was referenced directly
            entityNames.add(new QName(entityRef.getNamespace(), entityRef.getLocalName()));

            // If the entity is a Core or Business Object (or a Core/BO alias), we can include the
            // names of
            // its non-query facets in the list of eligible entities
            if (entityRef instanceof TLAlias) {
                TLAlias entityAlias = (TLAlias) entityRef;
                NamedEntity owner = entityAlias.getOwningEntity();

                if (owner instanceof TLBusinessObject) {
                    TLBusinessObject entity = (TLBusinessObject) owner;
                    TLAlias idAlias = AliasCodegenUtils.getFacetAlias(entityAlias, TLFacetType.ID);
                    TLAlias summaryAlias = AliasCodegenUtils.getFacetAlias(entityAlias,
                            TLFacetType.SUMMARY);
                    TLAlias detailAlias = AliasCodegenUtils.getFacetAlias(entityAlias,
                            TLFacetType.DETAIL);

                    entityNames.add(new QName(idAlias.getNamespace(), idAlias.getLocalName()));
                    entityNames.add(new QName(summaryAlias.getNamespace(), summaryAlias
                            .getLocalName()));

                    for (TLFacet customFacet : entity.getCustomFacets()) {
                        TLAlias customAlias = AliasCodegenUtils.getFacetAlias(entityAlias,
                                TLFacetType.CUSTOM, customFacet.getContext(),
                                customFacet.getLabel());

                        entityNames.add(new QName(customAlias.getNamespace(), customAlias
                                .getLocalName()));
                    }
                    entityNames.add(new QName(detailAlias.getNamespace(), detailAlias
                            .getLocalName()));

                } else if (owner instanceof TLCoreObject) {
                    TLAlias summaryAlias = AliasCodegenUtils.getFacetAlias(entityAlias,
                            TLFacetType.SUMMARY);
                    TLAlias detailAlias = AliasCodegenUtils.getFacetAlias(entityAlias,
                            TLFacetType.DETAIL);

                    entityNames.add(new QName(summaryAlias.getNamespace(), summaryAlias
                            .getLocalName()));
                    entityNames.add(new QName(detailAlias.getNamespace(), detailAlias
                            .getLocalName()));
                }
            } else {
                if (entityRef instanceof TLBusinessObject) {
                    TLBusinessObject entity = (TLBusinessObject) entityRef;

                    entityNames.add(new QName(entity.getIdFacet().getNamespace(), entity
                            .getIdFacet().getLocalName()));
                    entityNames.add(new QName(entity.getSummaryFacet().getNamespace(), entity
                            .getSummaryFacet().getLocalName()));

                    for (TLFacet customFacet : entity.getCustomFacets()) {
                        entityNames.add(new QName(customFacet.getNamespace(), customFacet
                                .getLocalName()));
                    }
                    entityNames.add(new QName(entity.getDetailFacet().getNamespace(), entity
                            .getDetailFacet().getLocalName()));

                } else if (entityRef instanceof TLCoreObject) {
                    TLCoreObject entity = (TLCoreObject) entityRef;

                    entityNames.add(new QName(entity.getSummaryFacet().getNamespace(), entity
                            .getSummaryFacet().getLocalName()));
                    entityNames.add(new QName(entity.getDetailFacet().getNamespace(), entity
                            .getDetailFacet().getLocalName()));
                }
            }
        }

    }

    /**
     * Initializes the mapping of facet types to the extension point elements used to encapsulate
     * extension point facet elements in the generated XML content.
     */
    static {
        try {
            extensionPointTypeMap = new HashMap<TLFacetType, SchemaDependency>();
            extensionPointTypeMap.put(TLFacetType.SUMMARY,
                    SchemaDependency.getExtensionPointSummaryElement());
            extensionPointTypeMap.put(TLFacetType.DETAIL,
                    SchemaDependency.getExtensionPointDetailElement());
            extensionPointTypeMap.put(TLFacetType.CUSTOM,
                    SchemaDependency.getExtensionPointCustomElement());
            extensionPointTypeMap.put(TLFacetType.QUERY,
                    SchemaDependency.getExtensionPointQueryElement());

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
