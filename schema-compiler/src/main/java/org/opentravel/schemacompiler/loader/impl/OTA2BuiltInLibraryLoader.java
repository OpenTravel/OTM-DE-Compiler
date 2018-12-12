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

import java.io.InputStream;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Built-in library loader that obtains its content from an OTA2 <code>TLLibrary</code> file.
 * 
 * @author S. Livezey
 */
public class OTA2BuiltInLibraryLoader extends AbstractBuiltInLibraryLoader {

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
            LibraryModuleInfo<Object> libraryInfo = moduleLoader.loadLibrary(inputSource, findings);

            // Next, transform the schema into an XSDLibrary
            if (!findings.hasFinding()) {
                DefaultTransformerContext transformContext = new DefaultTransformerContext();
                TransformerFactory<DefaultTransformerContext> transformFactory = TransformerFactory
                        .getInstance(SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY,
                                transformContext);
                ObjectTransformer<Object, TLLibrary, DefaultTransformerContext> transformer = transformFactory
                        .getTransformer(libraryInfo.getJaxbArtifact(), TLLibrary.class);
                TLLibrary ota2Library = transformer.transform(libraryInfo.getJaxbArtifact());

                if (ota2Library.getPrefix() == null) {
                    ota2Library.setPrefix(getLibraryDeclaration().getDefaultPrefix());
                }

                library = new BuiltInLibrary(ota2Library.getNamespace(), ota2Library.getName(),
                        ota2Library.getPrefix(), inputSource.getLibraryURL(),
                        ota2Library.getNamedMembers(), ota2Library.getNamespaceImports(),
                        ota2Library.getIncludes(), getLibraryDeclaration(),
                        ota2Library.getVersionScheme());
            }
        } catch (Exception e) {
            throw new LibraryLoaderException("Error constructing built-in library instance ("
                    + inputSource.getLibraryURL() + ")", e);
        }
        return library;
    }

}
