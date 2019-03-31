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

import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Element;
import org.w3._2001.xmlschema.TopLevelElement;

import javax.xml.namespace.QName;

/**
 * Code generation delegate for facet used to separate logic and break up the complexity of the facet artifact
 * generation process.
 * 
 * @param <F> the type of facet for which the delegate will generate artifacts
 * @author S. Livezey
 */
public abstract class FacetCodegenDelegate<F extends TLAbstractFacet> {

    protected static org.w3._2001.xmlschema.ObjectFactory jaxbObjectFactory =
        new org.w3._2001.xmlschema.ObjectFactory();

    protected CodeGenerationTransformerContext transformerContext;
    private F sourceFacet;

    /**
     * Constructor that specifies the source facet for which code artifacts are being generated.
     * 
     * @param sourceFacet the source facet
     */
    public FacetCodegenDelegate(F sourceFacet) {
        this.sourceFacet = sourceFacet;
    }

    /**
     * Returns the source facet for this delegate instance.
     * 
     * @return F
     */
    public F getSourceFacet() {
        return sourceFacet;
    }

    /**
     * Assigns the transformer context to use when processing facet sub-elements.
     * 
     * @param transformerContext the transformer context to assign
     */
    public void setTransformerContext(CodeGenerationTransformerContext transformerContext) {
        this.transformerContext = transformerContext;
    }

    /**
     * Adds the schemas associated with the given compile-time dependency to the current list of dependencies maintained
     * by the orchestrating code generator.
     * 
     * @param dependency the compile-time dependency to add
     */
    protected void addCompileTimeDependency(SchemaDependency dependency) {
        if (transformerContext != null) {
            CodeGenerator<?> codeGenerator = transformerContext.getCodeGenerator();

            if (codeGenerator instanceof AbstractJaxbCodeGenerator) {
                ((AbstractJaxbCodeGenerator<?>) codeGenerator)
                    .addCompileTimeDependency( dependency.getSchemaDeclaration() );
            }
        }
    }

    /**
     * Returns the transformer factory to use when obtaining object transformers for facet sub-elements.
     * 
     * @return TransformerFactory&lt;CodeGenerationTransformerContext&gt;
     */
    protected TransformerFactory<CodeGenerationTransformerContext> getTransformerFactory() {
        return transformerContext.getTransformerFactory();
    }

    /**
     * Generates the code artifacts of the facet. Typically, the artifacts produced for each facet include a JAXB type
     * and a global element definition. Sub-classes may extend this method to add additional artifacts as required.
     * 
     * @return CodegenArtifacts
     */
    public CodegenArtifacts generateArtifacts() {
        CodegenArtifacts artifacts = new CodegenArtifacts();

        if (hasContent()) {
            artifacts.addArtifact( createType() );
        }
        return artifacts;
    }

    /**
     * Creates a list of global XML schema elements that will represent the source facet.
     * 
     * @return FacetCodegenElements
     */
    public FacetCodegenElements generateElements() {
        FacetCodegenElements codegenElements = new FacetCodegenElements();

        if (hasContent()) {
            codegenElements.addFacetElement( getSourceFacet().getOwningEntity(), createElement( null ) );
        }
        return codegenElements;
    }

    /**
     * Returns true if the given source facet declares any content. May be overridden to introduce new logic (i.e.
     * inheritance) that will introduce generated content from indirect sources.
     * 
     * @return boolean
     */
    public boolean hasContent() {
        return sourceFacet.declaresContent();
    }

    /**
     * Returns true if the facet supports an extension point element.
     * 
     * @return boolean
     */
    public boolean hasExtensionPoint() {
        return false;
    }

    /**
     * Returns the facet instance that should serve as the base type for the source facet. The facet that is returned by
     * this method will always belong to the same owner as the given source facet.
     * 
     * @return F
     */
    public abstract F getLocalBaseFacet();

    /**
     * Creates the simple or complex XML schema type for the source facet.
     * 
     * @return Annotated
     */
    protected abstract Annotated createType();

    /**
     * Returns the name of the XML schema type for this facet.
     * 
     * @return String
     */
    protected String getTypeName() {
        return XsdCodegenUtils.getGlobalTypeName( getSourceFacet() );
    }

    /**
     * Returns a single top-level XML schema element using the information provided.
     * 
     * @param facetAlias the alias of the source facet element being created (may be null)
     * @return Element
     */
    protected Element createElement(TLAlias facetAlias) {
        Element element = new TopLevelElement();

        element.setName( getElementName( facetAlias ) );
        element.setType( new QName( sourceFacet.getNamespace(), getTypeName() ) );
        element.setSubstitutionGroup( getSubstitutionGroup( facetAlias ) );
        return element;
    }

    /**
     * Returns the name of the element used to represent the source facet or the specified alias.
     * 
     * @param facetAlias the alias of the source facet element being created (may be null)
     * @return String
     */
    public String getElementName(TLAlias facetAlias) {
        String elementName;

        if (facetAlias == null) {
            elementName = XsdCodegenUtils.getGlobalElementName( sourceFacet ).getLocalPart();
        } else {
            elementName = XsdCodegenUtils.getGlobalElementName( facetAlias ).getLocalPart();
        }
        return elementName;
    }

    /**
     * Returns the substitution group for the source facet. By default, the substitution group for an element is null
     * (not defined); sub-classes may override to assign a substitution group to the element(s) that are generated for
     * the facet.
     * 
     * @param facetAlias the alias of the source facet element being created (may be null)
     * @return QName
     */
    protected QName getSubstitutionGroup(TLAlias facetAlias) {
        return null;
    }

}
