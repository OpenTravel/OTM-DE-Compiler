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

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions.DetailLevel;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;

import java.io.File;
import java.net.URL;

/**
 * Base class for ExampleCodeGenerators.
 * 
 * @author S. Livezey, E. Bronson
 *
 */
public abstract class AbstractExampleCodeGenerator extends AbstractCodeGenerator<TLModelElement> {

    /**
     * The extension for the generated files.
     */
    protected final String fileExtension;

    /**
     * Constructor which sets the file extension to be used.
     * 
     * @param fileExtension the generated file extension.
     */
    public AbstractExampleCodeGenerator(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Constructs the <code>ExampleGeneratorOptions</code> using the code generation context provided.
     * 
     * @param context the code generation context
     * @return ExampleGeneratorOptions
     */
    public ExampleGeneratorOptions getOptions(CodeGenerationContext context) {
        ExampleGeneratorOptions options = new ExampleGeneratorOptions();
        String detailLevel = context.getValue( CodeGenerationContext.CK_EXAMPLE_DETAIL_LEVEL );
        String exampleContext = context.getValue( CodeGenerationContext.CK_EXAMPLE_CONTEXT );
        Integer maxRepeat = context.getIntValue( CodeGenerationContext.CK_EXAMPLE_MAX_REPEAT );
        Integer maxDepth = context.getIntValue( CodeGenerationContext.CK_EXAMPLE_MAX_DEPTH );
        Boolean suppressOptionalFields = context.getBooleanValue( CodeGenerationContext.CK_SUPPRESS_OPTIONAL_FIELDS );

        if ((detailLevel != null) && detailLevel.equalsIgnoreCase( "MINIMUM" )) {
            options.setDetailLevel( DetailLevel.MINIMUM );
        }
        if (exampleContext != null) {
            options.setExampleContext( exampleContext );
        }
        if (maxRepeat != null) {
            options.setMaxRepeat( maxRepeat.intValue() );
        }
        if (maxDepth != null) {
            options.setMaxRecursionDepth( maxDepth.intValue() );
        }
        if (suppressOptionalFields != null) {
            options.setSuppressOptionalFields( suppressOptionalFields );
        }
        return options;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected File getOutputFile(TLModelElement source, CodeGenerationContext context) {
        if (source == null) {
            throw new NullPointerException( "Source model element cannot be null." );
        }
        AbstractLibrary library = getLibrary( source );
        URL libraryUrl = (library == null) ? null : library.getLibraryUrl();
        File outputFolder = getOutputFolder( context, libraryUrl );
        String filename = getFilenameBuilder().buildFilename( source, fileExtension );

        return new File( outputFolder, filename );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
     */
    @Override
    protected CodeGenerationFilenameBuilder<TLModelElement> getDefaultFilenameBuilder() {
        return (item, fExt) -> {
            String fileExt = ((fExt == null) || (fExt.length() == 0)) ? "" : ("." + fExt);
            String itemName;

            if ((item instanceof TLFacet) && (((TLFacet) item).getOwningEntity() instanceof TLOperation)) {
                TLFacet facetItem = (TLFacet) item;
                itemName =
                    ((TLOperation) facetItem.getOwningEntity()).getName() + facetItem.getFacetType().getIdentityName();

            } else if (item instanceof NamedEntity) {
                NamedEntity entity = (NamedEntity) item;

                if (PropertyCodegenUtils.hasGlobalElement( entity )) {
                    itemName = XsdCodegenUtils.getGlobalElementName( entity ).getLocalPart();
                } else {
                    itemName = entity.getLocalName();
                }

            } else {
                itemName = "";
            }
            return itemName.replaceAll( "_", "" ) + fileExt;
        };
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(org.opentravel.schemacompiler.model.ModelElement)
     */
    @Override
    protected AbstractLibrary getLibrary(TLModelElement source) {
        return (source instanceof LibraryElement) ? ((LibraryElement) source).getOwningLibrary() : null;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(org.opentravel.schemacompiler.model.ModelElement)
     */
    @Override
    protected boolean isSupportedSourceObject(TLModelElement source) {
        return (source instanceof NamedEntity) && !(source instanceof TLService);
    }

}
