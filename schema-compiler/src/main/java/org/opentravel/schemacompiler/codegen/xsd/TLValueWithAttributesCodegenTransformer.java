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

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;
import org.w3._2001.xmlschema.ComplexType;
import org.w3._2001.xmlschema.SimpleContent;
import org.w3._2001.xmlschema.SimpleExtensionType;
import org.w3._2001.xmlschema.TopLevelComplexType;

/**
 * Performs the translation from <code>TLValueWithAttributes</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLValueWithAttributesCodegenTransformer extends
        AbstractXsdTransformer<TLValueWithAttributes, CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLValueWithAttributes source) {
        ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer = getTransformerFactory()
                .getTransformer(source.getDocumentation(), Annotation.class);
        ObjectTransformer<TLAttribute, CodegenArtifacts, CodeGenerationTransformerContext> attributeTransformer = getTransformerFactory()
                .getTransformer(TLAttribute.class, CodegenArtifacts.class);
        ObjectTransformer<TLIndicator, Annotated, CodeGenerationTransformerContext> indicatorTransformer = getTransformerFactory()
                .getTransformer(TLIndicator.class, Annotated.class);
        NamedEntity vwaParentType = getBaseParentType(source);
        CodegenArtifacts artifacts = new CodegenArtifacts();
        ComplexType targetType = new TopLevelComplexType();
        SimpleContent simpleContent = new SimpleContent();
        SimpleExtensionType extType = new SimpleExtensionType();

        targetType.setName(source.getName());

        if (source.getDocumentation() != null) {
            targetType.setAnnotation(docTransformer.transform(source.getDocumentation()));
        }
        XsdCodegenUtils.addAppInfo(source, targetType);

        if (vwaParentType != null) {
            if ((vwaParentType instanceof TLOpenEnumeration)
                    || (vwaParentType instanceof TLRoleEnumeration)) {
                extType.setBase(new QName(vwaParentType.getNamespace(), XsdCodegenUtils
                        .getGlobalTypeName((TLPropertyType) vwaParentType) + "_Base"));
            } else {
                extType.setBase(new QName(vwaParentType.getNamespace(), XsdCodegenUtils
                        .getGlobalTypeName((TLPropertyType) vwaParentType)));
            }
        } else {
            SchemaDependency emptyElement = SchemaDependency.getEmptyElement();

            extType.setBase(emptyElement.toQName());
            addCompileTimeDependency(emptyElement);
        }
        if (source.getValueDocumentation() != null) {
            simpleContent.setAnnotation(docTransformer.transform(source.getValueDocumentation()));
        }
        simpleContent.setExtension(extType);
        targetType.setSimpleContent(simpleContent);

        // Transform the attributes and indicators of the target type
        if ((vwaParentType instanceof TLOpenEnumeration)
                || (vwaParentType instanceof TLRoleEnumeration)) {
            SchemaDependency enumExtension = SchemaDependency.getEnumExtension();
            Attribute extAttribute = new Attribute();

            extAttribute.setName("extension");
            extAttribute.setType(enumExtension.toQName());
            addCompileTimeDependency(enumExtension);
            extType.getAttributeOrAttributeGroup().add(extAttribute);
        }
        for (TLAttribute modelAttribute : PropertyCodegenUtils.getInheritedAttributes(source)) {
            extType.getAttributeOrAttributeGroup().addAll(
                    attributeTransformer.transform(modelAttribute).getArtifactsOfType(
                            Attribute.class));
        }
        for (TLIndicator modelIndicator : PropertyCodegenUtils.getInheritedIndicators(source)) {
            Annotated jaxbAttribute = indicatorTransformer.transform(modelIndicator);

            if (jaxbAttribute != null) {
                extType.getAttributeOrAttributeGroup().add(jaxbAttribute);
            }
        }

        artifacts.addArtifact(targetType);
        return artifacts;
    }

    /**
     * Returns the root parent type for the given VWA. If the parent type is another VWA, this
     * method will search the hierarchy for the base (non-VWA) parent type.
     * 
     * @param vwa
     *            the VWA for which schema artifacts are being generated
     * @return NamedEntity
     */
    private NamedEntity getBaseParentType(TLValueWithAttributes vwa) {
        NamedEntity parentType = vwa.getParentType();

        while (parentType instanceof TLValueWithAttributes) {
            parentType = ((TLValueWithAttributes) parentType).getParentType();
        }
        return parentType;
    }

}
