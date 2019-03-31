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

package org.opentravel.schemacompiler.diff;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.util.DocumentationPathResolver;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;

import java.io.File;
import java.io.InputStream;

import javax.xml.namespace.QName;

/**
 * Base class for all model-diff tests that pre-loads a model containing known differences to be used for verification.
 */
public abstract class AbstractDiffTest {

    protected static final QName TLIBRARY_DIFF1_V1 =
        new QName( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-diff1/v1", "library_diff1" );
    protected static final QName TLIBRARY_DIFF1_V1_1 =
        new QName( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-diff1/v1_1", "library_diff1" );
    protected static final QName TLIBRARY_DIFF1_V2 =
        new QName( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-diff2/v2", "library_diff2" );

    protected static TLModel model;

    @BeforeClass
    public static void setupModel() throws Exception {
        model = loadModel( "/test-package-diff/library_diff2_v2.xml" );
        Assert.assertEquals( 3, model.getUserDefinedLibraries().size() );
    }

    protected static TLModel loadModel(String libraryPath) throws Exception {
        LibraryInputSource<InputStream> libraryInput =
            new LibraryStreamInputSource( new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + libraryPath ) );
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        modelLoader.loadLibraryModel( libraryInput );

        return modelLoader.getLibraryModel();
    }

    protected TLLibrary getLibrary(QName libraryId) {
        AbstractLibrary library = null;

        for (AbstractLibrary l : model.getLibrariesForNamespace( libraryId.getNamespaceURI() )) {
            if (l.getName().equals( libraryId.getLocalPart() )) {
                library = l;
                break;
            }
        }
        return (library instanceof TLLibrary) ? (TLLibrary) library : null;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getMember(QName libraryId, String docPath, Class<T> expectedType) {
        TLLibrary library = getLibrary( libraryId );
        ModelElement member = (library == null) ? null : DocumentationPathResolver.resolve( docPath, library );

        if ((member != null) && !expectedType.isAssignableFrom( member.getClass() )) {
            throw new ClassCastException(
                "Unexpected member type '" + member.getClass().getSimpleName() + "' for path: " + docPath );
        }
        assertNotNull( member );
        return (T) member;
    }

}
