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

import org.opentravel.ns.ota2.appinfo_v01_00.OTA2Entity;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Appinfo;
import org.w3._2001.xmlschema.TopLevelElement;

import java.math.BigInteger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * Performs the translation from <code>TLProperty</code> objects to the JAXB nodes used to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLPropertyCodegenTransformer extends AbstractXsdTransformer<TLProperty,TopLevelElement> {

    private static org.opentravel.ns.ota2.appinfo_v01_00.ObjectFactory appInfoObjectFactory =
        new org.opentravel.ns.ota2.appinfo_v01_00.ObjectFactory();

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TopLevelElement transform(TLProperty source) {
        TopLevelElement element =
            source.isReference() ? transformReferenceProperty( source ) : transformValueProperty( source );

        // Add documentation, equivalents, and examples to the element's annotation as required
        TLDocumentation sourceDoc = DocumentationFinder.getDocumentation( source );

        if (sourceDoc != null) {
            ObjectTransformer<TLDocumentation,Annotation,CodeGenerationTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( sourceDoc, Annotation.class );

            element.setAnnotation( docTransformer.transform( sourceDoc ) );
        }
        XsdCodegenUtils.addEquivalentInfo( source, element );
        return element;
    }

    /**
     * Performs the transformation of the property as a standard value element.
     * 
     * @param source the source object being transformed
     * @return TopLevelElement
     */
    private TopLevelElement transformValueProperty(TLProperty source) {
        TLPropertyOwner propertyOwner = source.getOwner();
        TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType( source.getType() );
        TopLevelElement element = new TopLevelElement();

        if (!PropertyCodegenUtils.hasGlobalElement( propertyType )) {
            // If the property references a type that does not define a global element, assign the
            // name/type fields of the JAXB element
            element.setType(
                new QName( propertyType.getNamespace(), XsdCodegenUtils.getGlobalTypeName( propertyType, source ) ) );

            // If the element's name has not been specified, use the name of its assigned type
            if ((source.getName() == null) || (source.getName().length() == 0)) {
                element.setName( source.getType().getLocalName() );
            } else {
                element.setName( source.getName() );
            }

        } else {
            // If the property references a type that defines a global element, assign the 'ref'
            // field of the JAXB element.
            QName propertyRef = PropertyCodegenUtils.getDefaultSchemaElementName( propertyType, false );
            String propertyTypeNS = propertyRef.getNamespaceURI();

            // If this type is from a chameleon schema, replace its namespace with that of the local
            // library
            if ((propertyTypeNS == null) || propertyTypeNS.equals( AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE )) {
                propertyRef = new QName( propertyOwner.getNamespace(), propertyRef.getLocalPart() );
            }
            element.setRef( propertyRef );
        }

        assignElementMinMaxValues( source, element );
        XsdCodegenUtils.addExampleInfo( source, element );

        return element;
    }

    /**
     * Assign the mix/max occurs for the generated element.
     * 
     * @param source the source property being transformed
     * @param element the XML schema element being generated
     */
    private void assignElementMinMaxValues(TLProperty source, TopLevelElement element) {
        if (source.isMandatory()) {
            element.setMinOccurs( BigInteger.valueOf( 1 ) );
        } else {
            element.setMinOccurs( BigInteger.valueOf( 0 ) );
        }
        if (source.getType() instanceof TLListFacet) {
            TLCoreObject facetOwner = (TLCoreObject) ((TLListFacet) source.getType()).getOwningEntity();

            if (!facetOwner.getRoleEnumeration().getRoles().isEmpty()) {
                element.setMaxOccurs( facetOwner.getRoleEnumeration().getRoles().size() + "" );
            } else {
                element.setMaxOccurs( PropertyCodegenUtils.getMaxOccurs( source ) );
            }
        } else {
            element.setMaxOccurs( PropertyCodegenUtils.getMaxOccurs( source ) );
        }
    }

    /**
     * Performs the transformation of the property as an IDREF(S) element.
     * 
     * @param source the source object being transformed
     * @return TopLevelElement
     */
    private TopLevelElement transformReferenceProperty(TLProperty source) {
        TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType( source.getType() );
        TopLevelElement element = new TopLevelElement();
        Annotation annotation = new Annotation();
        Appinfo appInfo = new Appinfo();
        OTA2Entity ota2Entity = new OTA2Entity();
        String maxOccurs = PropertyCodegenUtils.getMaxOccurs( source );
        boolean isMultipleReference;
        String elementName;

        if (PropertyCodegenUtils.hasGlobalElement( propertyType )) {
            elementName = PropertyCodegenUtils.getDefaultSchemaElementName( propertyType, true ).getLocalPart();

        } else {
            elementName = source.getName();

            if (!elementName.endsWith( "Ref" )) {
                // probably a VWA reference, so we need to make sure the "Ref" suffix is appended
                elementName += "Ref";
            }
        }

        if (maxOccurs == null) {
            isMultipleReference = false;

        } else if (maxOccurs.equals( "unbounded" )) {
            isMultipleReference = true;

        } else {
            try {
                isMultipleReference = Integer.parseInt( maxOccurs ) > 1;

            } catch (NumberFormatException e) {
                // should never happen, but just in case...
                isMultipleReference = false;
            }
        }

        element.setName( elementName );
        element.setType( new QName( XMLConstants.W3C_XML_SCHEMA_NS_URI, isMultipleReference ? "IDREFS" : "IDREF" ) );
        element.setAnnotation( annotation );
        annotation.getAppinfoOrDocumentation().add( appInfo );
        ota2Entity.setType( element.getType().getLocalPart() );
        ota2Entity.setValue( source.getType().getLocalName() );
        appInfo.getContent().add( appInfoObjectFactory.createOTA2EntityReference( ota2Entity ) );

        if (!source.isMandatory()) {
            element.setMinOccurs( BigInteger.ZERO );
        }
        return element;
    }

}
