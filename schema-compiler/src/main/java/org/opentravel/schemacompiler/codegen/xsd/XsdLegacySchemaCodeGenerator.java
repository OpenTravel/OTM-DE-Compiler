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

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.util.URLUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Code generator for legacy XML schemas referenced in a library meta-model. The behavior of this code generator is to
 * simply copy the content of the file from its source URL to the proper output location.
 * 
 * @author S. Livezey
 */
public class XsdLegacySchemaCodeGenerator extends AbstractCodeGenerator<XSDLibrary> {

    private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(org.opentravel.schemacompiler.model.ModelElement)
     */
    @Override
    protected AbstractLibrary getLibrary(XSDLibrary source) {
        return source;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    public void doGenerateOutput(XSDLibrary source, CodeGenerationContext context) throws CodeGenerationException {
        try (BufferedReader reader = getContentReader( source.getLibraryUrl() )) {
            File outputFile = getOutputFile( source, context );
            String line = null;

            try (BufferedWriter writer = new BufferedWriter( new FileWriter( outputFile ) )) {
                while ((line = reader.readLine()) != null) {
                    writer.write( line );
                    writer.write( LINE_SEPARATOR );
                }
                addGeneratedFile( outputFile );
            }

        } catch (IOException e) {
            throw new CodeGenerationException( e );
        }
    }

    /**
     * Returns a buffered reader to read content from the given URL.
     * 
     * @param contentUrl the URL for which to return a reader
     * @return BufferedReader
     * @throws IOException thrown if an error occurs while creating the reader
     */
    private BufferedReader getContentReader(URL contentUrl) throws IOException {
        BufferedReader reader;

        if (URLUtils.isFileURL( contentUrl )) {
            reader = new BufferedReader( new FileReader( URLUtils.toFile( contentUrl ) ) );
        } else {
            reader = new BufferedReader( new InputStreamReader( contentUrl.openStream() ) );
        }
        return reader;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected File getOutputFile(XSDLibrary source, CodeGenerationContext context) {
        File outputFolder = getOutputFolder( context, source.getLibraryUrl() );
        String filename = context.getValue( CodeGenerationContext.CK_SCHEMA_FILENAME );

        if ((filename == null) || filename.trim().equals( "" )) {
            filename = getFilenameBuilder().buildFilename( source, "xsd" );
        }
        return new File( outputFolder, filename );
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFolder(org.opentravel.schemacompiler.codegen.CodeGenerationContext,
     *      java.net.URL)
     */
    @Override
    protected File getOutputFolder(CodeGenerationContext context, URL libraryUrl) {
        File outputFolder = super.getOutputFolder( context, libraryUrl );
        String legacySchemaFolder = getLegacySchemaOutputLocation( context );

        if (legacySchemaFolder != null) {
            outputFolder = new File( outputFolder, legacySchemaFolder );

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }
        }
        return outputFolder;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(org.opentravel.schemacompiler.model.ModelElement)
     */
    @Override
    protected boolean isSupportedSourceObject(XSDLibrary source) {
        return (source != null);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#canGenerateOutput(org.opentravel.schemacompiler.model.ModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected boolean canGenerateOutput(XSDLibrary source, CodeGenerationContext context) {
        CodeGenerationFilter filter = getFilter();

        return super.canGenerateOutput( source, context )
            && ((filter == null) || filter.processLibrary( source ) || filter.processExtendedLibrary( source ));
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
     */
    @Override
    protected CodeGenerationFilenameBuilder<XSDLibrary> getDefaultFilenameBuilder() {
        return new LibraryFilenameBuilder<>();
    }

}
