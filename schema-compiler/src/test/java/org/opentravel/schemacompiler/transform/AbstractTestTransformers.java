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

package org.opentravel.schemacompiler.transform;

import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;
import org.opentravel.schemacompiler.transform.tl2jaxb.TL2JaxbLibrarySymbolResolver;
import org.opentravel.schemacompiler.transform.util.ChameleonFilter;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for test classes that validate the transformation subsystem.
 * 
 * @author S. Livezey
 */
public abstract class AbstractTestTransformers {

    public static final String PACKAGE_1_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1";
    public static final String PACKAGE_2_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2";
    public static final String PACKAGE_EXT_NAMESPACE =
        "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-ext_v2";

    private static Map<String,TLModel> testModelsByBaseLocation = new HashMap<String,TLModel>();

    /**
     * Returns the base location (root folder) for the test files that are to be used to load and construct the model.
     */
    protected abstract String getBaseLocation();

    protected TLModel getTestModel() throws Exception {
        String baseLocation = getBaseLocation().intern();

        synchronized (baseLocation) {
            TLModel testModel = testModelsByBaseLocation.get( baseLocation );

            if (testModel == null) {
                LibraryInputSource<InputStream> library1Input =
                    new LibraryStreamInputSource( new File( getBaseLocation() + "/test-package_v2/library_1_p2.xml" ) );
                LibraryInputSource<InputStream> library2Input =
                    new LibraryStreamInputSource( new File( getBaseLocation() + "/test-package_v2/library_2_p2.xml" ) );
                LibraryInputSource<InputStream> library3Input =
                    new LibraryStreamInputSource( new File( getBaseLocation() + "/test-package_v2/library_3_p2.xml" ) );
                LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
                ValidationFindings findings = modelLoader.loadLibraryModel( library1Input );
                findings.addAll( modelLoader.loadLibraryModel( library2Input ) );
                findings.addAll( modelLoader.loadLibraryModel( library3Input ) );
                SchemaCompilerTestUtils.printFindings( findings );

                testModel = modelLoader.getLibraryModel();
                testModelsByBaseLocation.put( baseLocation, testModel );
            }
            return testModel;
        }
    }

    protected TLLibrary getLibrary(String namespace, String libraryName) throws Exception {
        TLLibrary library = null;

        for (AbstractLibrary lib : getTestModel().getLibrariesForNamespace( namespace )) {
            if (lib.getName().equals( libraryName )) {
                library = (TLLibrary) lib;
                break;
            }
        }
        return library;
    }

    protected SymbolResolverTransformerContext getContextJAXBTransformation(AbstractLibrary contextLibrary)
        throws Exception {
        SymbolResolverTransformerContext context = new SymbolResolverTransformerContext();
        SymbolResolver symbolResolver =
            new TL2JaxbLibrarySymbolResolver( SymbolTableFactory.newSymbolTableFromModel( getTestModel() ) );

        symbolResolver.setPrefixResolver( new LibraryPrefixResolver( contextLibrary ) );
        symbolResolver.setAnonymousEntityFilter( new ChameleonFilter( contextLibrary ) );
        context.setSymbolResolver( symbolResolver );
        return context;
    }

}
