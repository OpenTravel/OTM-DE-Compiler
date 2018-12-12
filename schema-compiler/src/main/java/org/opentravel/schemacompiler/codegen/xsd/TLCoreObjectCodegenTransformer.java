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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenElements;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.NoFixedFacet;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleContent;
import org.w3._2001.xmlschema.SimpleExtensionType;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Performs the translation from <code>TLCoreObject</code> objects to the JAXB nodes used to produce
 * the schema output.
 * 
 * @author S. Livezey
 */
public class TLCoreObjectCodegenTransformer extends
        AbstractXsdTransformer<TLCoreObject, CodegenArtifacts> {

	private static final String OPEN = "_Open";

	/**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLCoreObject source) {
        FacetCodegenDelegateFactory delegateFactory = new FacetCodegenDelegateFactory(context);
        FacetCodegenElements elementArtifacts = new FacetCodegenElements();
        CodegenArtifacts otherArtifacts = new CodegenArtifacts();

        otherArtifacts.addAllArtifacts(delegateFactory.getDelegate(source.getSimpleListFacet())
                .generateElements().getFacetElements(source.getSimpleListFacet()));
        otherArtifacts.addAllArtifacts(delegateFactory.getDelegate(source.getSimpleFacet())
                .generateArtifacts());
        otherArtifacts.addAllArtifacts(delegateFactory.getDelegate(source.getSimpleListFacet())
                .generateArtifacts());
        generateFacetArtifacts(delegateFactory.getDelegate(source.getSummaryFacet()),
                elementArtifacts, otherArtifacts, false);
        generateFacetArtifacts(delegateFactory.getDelegate(source.getDetailFacet()),
                elementArtifacts, otherArtifacts, false);

        if (!source.getRoleEnumeration().getRoles().isEmpty()) {
            otherArtifacts.addArtifact(createRoleEnumerationComplexType(source));
            otherArtifacts.addArtifact(createRoleEnumerationSimpleType(source, false));
            otherArtifacts.addArtifact(createRoleEnumerationSimpleType(source, true));
        }
        return buildCorrelatedArtifacts(source, elementArtifacts, otherArtifacts);
    }

    /**
     * Creates the XML schema simple enumeration for the roles of the core object.
     * 
     * @param source
     *            the core object being transformed
     * @return SimpleType
     */
    private TopLevelComplexType createRoleEnumerationComplexType(TLCoreObject source) {
        SchemaDependency enumExtension = SchemaDependency.getEnumExtension();
        TopLevelComplexType complexType = new TopLevelComplexType();
        SimpleContent simpleContent = new SimpleContent();
        SimpleExtensionType extension = new SimpleExtensionType();
        Attribute attribute = new Attribute();

        complexType.setName(source.getRoleEnumeration().getLocalName());
        complexType.setSimpleContent(simpleContent);
        XsdCodegenUtils.addAppInfo(source, complexType);
        simpleContent.setExtension(extension);
        extension.setBase(new QName(source.getNamespace(), source.getRoleEnumeration()
                .getLocalName() + OPEN));
        extension.getAttributeOrAttributeGroup().add(attribute);
        attribute.setName("extension");
        attribute.setType(enumExtension.toQName());
        addCompileTimeDependency(enumExtension);
        return complexType;
    }

    /**
     * Creates the XML schema simple enumeration for the roles of the core object.
     * 
     * @param source
     *            the core object being transformed
     * @param openEnumeration
     *            indicates whether to generate the open or closed enumeration variant of the simple
     *            type
     * @return TopLevelSimpleType
     */
    private TopLevelSimpleType createRoleEnumerationSimpleType(TLCoreObject source,
            boolean openEnumeration) {
        TopLevelSimpleType rolesEnum = new TopLevelSimpleType();
        Restriction restriction = new Restriction();

        if (openEnumeration) {
            rolesEnum.setName(source.getRoleEnumeration().getLocalName() + OPEN);
            rolesEnum.setRestriction(restriction);
            restriction.setBase(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string"));

        } else { // closed enumeration
            rolesEnum.setName(source.getRoleEnumeration().getLocalName() + "_Base");
            rolesEnum.setRestriction(restriction);
            restriction.setBase(new QName(source.getNamespace(), source.getRoleEnumeration()
                    .getLocalName() + OPEN));
        }

        for (TLRole role : PropertyCodegenUtils.getInheritedRoles(source)) {
        	TLDocumentation doc = DocumentationFinder.getDocumentation(role);
            NoFixedFacet roleEnumValue = new NoFixedFacet();

            roleEnumValue.setValue(role.getName());
            restriction.getFacets().add(jaxbObjectFactory.createEnumeration(roleEnumValue));

            if (doc != null) {
                ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer =
                		getTransformerFactory().getTransformer(doc, Annotation.class);

                roleEnumValue.setAnnotation(docTransformer.transform(doc));
            }
        }
        if (openEnumeration) {
            NoFixedFacet otherEnumValue = new NoFixedFacet();

            otherEnumValue.setValue(TLBaseEnumerationCodegenTransformer.OPEN_ENUM_VALUE);
            restriction.getFacets().add(jaxbObjectFactory.createEnumeration(otherEnumValue));
        }
        return rolesEnum;
    }

}
