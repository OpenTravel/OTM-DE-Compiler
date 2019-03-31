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

package org.opentravel.schemacompiler.loader.impl;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.w3._2001.xmlschema.Schema;

import java.io.InputStream;

import javax.xml.namespace.QName;

/**
 * Built-in library loader that obtains its content from a legacy schema (.xsd) file.
 * 
 * @author S. Livezey
 */
public class LegacySchemaBuiltInLibraryLoader extends AbstractBuiltInLibraryLoader {

    /**
     * @see org.opentravel.schemacompiler.loader.BuiltInLibraryLoader#loadBuiltInLibrary()
     */
    @Override
    public BuiltInLibrary loadBuiltInLibrary() throws LibraryLoaderException {
        LibraryInputSource<InputStream> inputSource = getInputSource();
        BuiltInLibrary library = null;

        try {
            // First, load the schema from the specified classpath location
            LibraryModuleLoader<InputStream> moduleLoader = new MultiVersionLibraryModuleLoader();
            ValidationFindings findings = new ValidationFindings();
            LibraryModuleInfo<Schema> schemaInfo = moduleLoader.loadSchema( inputSource, findings );

            // Next, transform the schema into an XSDLibrary
            if (!findings.hasFinding()) {
                DefaultTransformerContext transformContext = new DefaultTransformerContext();
                TransformerFactory<DefaultTransformerContext> transformerFactory = TransformerFactory
                    .getInstance( SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY, transformContext );
                ObjectTransformer<Schema,XSDLibrary,DefaultTransformerContext> transformer =
                    transformerFactory.getTransformer( schemaInfo.getJaxbArtifact(), XSDLibrary.class );
                XSDLibrary xsdLibrary = transformer.transform( schemaInfo.getJaxbArtifact() );

                if (xsdLibrary.getPrefix() == null) {
                    xsdLibrary.setPrefix( getLibraryDeclaration().getDefaultPrefix() );
                }

                SchemaDeclaration libraryDeclaration = getLibraryDeclaration();
                QName qualifiedName = new QName( schemaInfo.getJaxbArtifact().getTargetNamespace(),
                    libraryDeclaration.getName(), libraryDeclaration.getDefaultPrefix() );

                library = new BuiltInLibrary( qualifiedName, inputSource.getLibraryURL(), xsdLibrary.getNamedMembers(),
                    xsdLibrary.getNamespaceImports(), getLibraryDeclaration(), xsdLibrary.getVersionScheme() );
            }

        } catch (Exception e) {
            throw new LibraryLoaderException(
                "Error constructing built-in library instance (" + inputSource.getLibraryURL() + ")" );
        }
        return library;
    }

}
