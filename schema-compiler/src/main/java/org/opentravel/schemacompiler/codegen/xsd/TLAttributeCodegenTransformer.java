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
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Attribute;

/**
 * Performs the translation from <code>TLAttribute</code> objects to the JAXB nodes used to produce
 * the schema output.
 * 
 * @author S. Livezey
 */
public class TLAttributeCodegenTransformer extends
        AbstractXsdTransformer<TLAttribute, CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLAttribute source) {
        TLAttributeType attributeType = PropertyCodegenUtils.getAttributeType(source);
        CodegenArtifacts artifacts = new CodegenArtifacts();
        Attribute attr = new Attribute();

        // If the attribute's name has not been specified, use the name of its assigned type
        if ((source.getName() == null) || (source.getName().length() == 0)) {
            attr.setName(attributeType.getLocalName());
        } else {
            attr.setName(source.getName());
        }
        artifacts.addArtifact(attr);

        if (attributeType instanceof TLCoreObject) {
            // Special Case: For core objects, use the simple facet as the attribute type
            TLCoreObject coreObject = (TLCoreObject) attributeType;
            TLSimpleFacet coreSimple = coreObject.getSimpleFacet();

            attr.setType(new QName(coreSimple.getNamespace(), XsdCodegenUtils
                    .getGlobalTypeName(coreSimple)));

        } else if (attributeType instanceof TLRole) {
            // Special Case: For role assignments, use the core object's simple facet as the
            // attribute type
            TLCoreObject coreObject = ((TLRole) attributeType).getRoleEnumeration()
                    .getOwningEntity();
            TLSimpleFacet coreSimple = coreObject.getSimpleFacet();

            attr.setType(new QName(coreSimple.getNamespace(), XsdCodegenUtils
                    .getGlobalTypeName(coreSimple)));

        } else if (attributeType instanceof TLOpenEnumeration) {
            Attribute extensionAttr = new Attribute();

            extensionAttr.setName(attr.getName() + "Extension");
            extensionAttr.setType(SchemaDependency.getEnumExtension().toQName());
            attr.setType(new QName(attributeType.getNamespace(),
                    ((TLOpenEnumeration) attributeType).getLocalName() + "_Base"));
            artifacts.addArtifact(extensionAttr);

        } else if (attributeType instanceof TLRoleEnumeration) {
            attr.setType(new QName(attributeType.getNamespace(),
                    ((TLRoleEnumeration) attributeType).getLocalName() + "_Base"));
        	
        } else { // normal case
            String attrTypeNS = attributeType.getNamespace();

            if ((attrTypeNS == null)
                    || attrTypeNS.equals(AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE)) {
                // If this type is from a chameleon schema, replace its namespace with that of the
                // local library
                attrTypeNS = source.getOwner().getNamespace();
            }
            attr.setType(new QName(attrTypeNS, XsdCodegenUtils.getGlobalTypeName(attributeType)));
        }

        if (source.isMandatory()) {
            attr.setUse("required");
        } else {
            attr.setUse("optional");
        }

        // Add documentation, equivalents, and examples to the attribute's annotation as required
        TLDocumentation sourceDoc = DocumentationFinder.getDocumentation( source );
        
        if (sourceDoc != null) {
            ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer =
            		getTransformerFactory().getTransformer(sourceDoc, Annotation.class);

            attr.setAnnotation(docTransformer.transform(sourceDoc));
        }
        XsdCodegenUtils.addEquivalentInfo(source, attr);
        XsdCodegenUtils.addExampleInfo(source, attr);

        return artifacts;
    }

}
