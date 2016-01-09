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
package org.opentravel.schemacompiler.codegen.xsd;

import java.math.BigInteger;
import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.ComplexType;
import org.w3._2001.xmlschema.Element;
import org.w3._2001.xmlschema.ExplicitGroup;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelElement;

/**
 * Base transformer class for XSD code generation of action requests and responses.
 */
public abstract class TLBaseActionCodegenTransformer<S> extends AbstractXsdTransformer<S,CodegenArtifacts> {
	
	/**
	 * Creates the global element declaration for the action request or response.
	 * 
	 * @param elementName  the name of the element to create
	 * @param typeName  the type name to assign to the element
	 * @return Element
	 */
	protected Element createElement(QName elementName, String typeName) {
		TopLevelElement element = new TopLevelElement();
		
		element.setName( elementName.getLocalPart() );
		element.setType( new QName( elementName.getNamespaceURI(), typeName ) );
		return element;
	}
	
	/**
	 * Creates an XSD complex type with the specified name using the payload information
	 * provided in the action facet.
	 * 
	 * @param typeName  the name of the complex type to create
	 * @param payloadType  the action facet that specifies the structure and content of the payload
	 * @param source  the source object that will provide documentation for the generated type
	 * @return ComplexType
	 */
	protected ComplexType createType(String typeName, TLActionFacet payloadType, TLDocumentationOwner source) {
        List<TLAttribute> attributeList = PropertyCodegenUtils.getInheritedAttributes( payloadType );
        List<TLProperty> elementList = PropertyCodegenUtils.getInheritedProperties( payloadType );
        List<TLIndicator> indicatorList = PropertyCodegenUtils.getInheritedIndicators( payloadType );
        boolean hasFacetPayload = (typeName != null) && ((payloadType.getReferenceRepeat() != 0)
        		|| !attributeList.isEmpty() || !elementList.isEmpty() || !indicatorList.isEmpty());
        ComplexType type = null;
        
        // Only create a new type if we have multiple items that will belong to the type.  For example,
        // if the payload is only a non-repeating business object, then the BO itself can be the RQ/RS
        // payload without requiring that it be wrapped in another complex type.
        if (hasFacetPayload) {
            type = new TopLevelComplexType();
            type.setName( typeName );
            
            ObjectTransformer<TLAttribute, CodegenArtifacts, CodeGenerationTransformerContext> attributeTransformer = getTransformerFactory()
                    .getTransformer(TLAttribute.class, CodegenArtifacts.class);
            ObjectTransformer<TLProperty, TopLevelElement, CodeGenerationTransformerContext> elementTransformer = getTransformerFactory()
                    .getTransformer(TLProperty.class, TopLevelElement.class);
            ObjectTransformer<TLIndicator, Annotated, CodeGenerationTransformerContext> indicatorTransformer = getTransformerFactory()
                    .getTransformer(TLIndicator.class, Annotated.class);
            TLDocumentation sourceDoc = DocumentationFinder.getDocumentation( source );
            List<Annotated> jaxbAttributeList = type.getAttributeOrAttributeGroup();
            
            // Create the type sequence
            QName extensionPointElementName = SchemaDependency.getExtensionPointElement().toQName();
    		TLProperty boElement = ResourceCodegenUtils.getBusinessObjectElement( payloadType );
            TopLevelElement extensionPointElement = new TopLevelElement();
            ExplicitGroup sequence = new ExplicitGroup();
    		
    		if (boElement != null) {
                sequence.getParticle().add(
                        jaxbObjectFactory.createElement(elementTransformer.transform(boElement)));
    		}
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
            extensionPointElement.setRef( extensionPointElementName );
            extensionPointElement.setMinOccurs( BigInteger.valueOf( 0 ) );
            sequence.getParticle().add( jaxbObjectFactory.createElement( extensionPointElement ) );
            type.setSequence( sequence );
            
            // Add the type attributes
            for (TLAttribute attribute : attributeList) {
            	jaxbAttributeList.addAll(
            			attributeTransformer.transform( attribute ).getArtifactsOfType( Attribute.class ) );
            }
            for (TLIndicator indicator : indicatorList) {
                if (!indicator.isPublishAsElement()) {
                	jaxbAttributeList.add( indicatorTransformer.transform( indicator ) );
                }
            }
            
            // Generate the documentation block (if required)
            if (sourceDoc != null) {
                ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer =
                		getTransformerFactory().getTransformer(sourceDoc, Annotation.class);

                type.setAnnotation( docTransformer.transform( sourceDoc ) );
            }
            
            // Add any required application info
            XsdCodegenUtils.addAppInfo( (NamedEntity) source, type );
        }
		return type;
	}
	
}
