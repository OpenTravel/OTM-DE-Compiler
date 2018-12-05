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
import org.opentravel.schemacompiler.codegen.util.EnumCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Performs the translation from <code>TLClosedEnumeration</code> objects to the JAXB nodes used to
 * produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumerationCodegenTransformer extends
        TLBaseEnumerationCodegenTransformer<TLClosedEnumeration, CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLClosedEnumeration source) {
        CodegenArtifacts artifacts = new CodegenArtifacts();
        SimpleType xsdEnum = new TopLevelSimpleType();
        Restriction restriction = new Restriction();

        xsdEnum.setName(source.getName());
        restriction.setBase(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string"));

        // Generate the documentation BLOCK (if required)
        TLDocumentation sourceDoc = DocumentationFinder.getDocumentation( source );
        
        if (sourceDoc != null) {
            ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer =
            		getTransformerFactory().getTransformer(sourceDoc, Annotation.class);

            xsdEnum.setAnnotation(docTransformer.transform(sourceDoc));
        }
        XsdCodegenUtils.addAppInfo(source, xsdEnum);

        for (TLEnumValue modelEnum : EnumCodegenUtils.getInheritedValues( source )) {
            restriction.getFacets().add(createEnumValue(modelEnum));
        }
        xsdEnum.setRestriction(restriction);
        artifacts.addArtifact(xsdEnum);

        return artifacts;
    }

}
