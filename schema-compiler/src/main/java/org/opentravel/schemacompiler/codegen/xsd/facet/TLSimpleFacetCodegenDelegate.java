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

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Code generation delegate for <code>TLSimpleFacet</code> instances with a facet type of
 * <code>SIMPLE</code> and a facet owner of type <code>TLCoreObject</code>.
 * 
 * @author S. Livezey
 */
public class TLSimpleFacetCodegenDelegate extends FacetCodegenDelegate<TLSimpleFacet> {

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet
     *            the source facet
     */
    public TLSimpleFacetCodegenDelegate(TLSimpleFacet sourceFacet) {
        super(sourceFacet);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#generateElements()
     */
    @Override
    public FacetCodegenElements generateElements() {
        return new FacetCodegenElements(); // No global elements generated for simple facets
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createType()
     */
    @Override
    protected Annotated createType() {
        TLSimpleFacet sourceFacet = getSourceFacet();
        Restriction restriction = new Restriction();
        SimpleType type = null;
        QName baseType;

        type = new TopLevelSimpleType();
        type.setName(XsdCodegenUtils.getGlobalTypeName(sourceFacet));
        type.setRestriction(restriction);

        if (sourceFacet.getSimpleType() instanceof TLCoreObject) {
            // Special Case: For core objects, use the simple facet as the base type
            TLCoreObject coreObject = (TLCoreObject) sourceFacet.getSimpleType();
            TLSimpleFacet coreSimple = coreObject.getSimpleFacet();

            baseType = new QName(coreSimple.getNamespace(),
                    XsdCodegenUtils.getGlobalTypeName(coreSimple));

        } else { // normal case
            baseType = new QName(sourceFacet.getSimpleType().getNamespace(),
                    XsdCodegenUtils.getGlobalTypeName(sourceFacet.getSimpleType()));
        }
        restriction.setBase(baseType);

        if (sourceFacet.getDocumentation() != null) {
            ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(sourceFacet.getDocumentation(), Annotation.class);

            type.setAnnotation(docTransformer.transform(sourceFacet.getDocumentation()));
        }
        XsdCodegenUtils.addAppInfo(sourceFacet, type);
        return type;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#getLocalBaseFacet()
     */
    @Override
    public TLSimpleFacet getLocalBaseFacet() {
        return null;
    }

}
