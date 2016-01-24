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
package org.opentravel.schemacompiler.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.InputStream;

import org.junit.Test;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Unit tests for elements of the compiler meta-model.
 * 
 * @author S. Livezey
 */
public class TestModelUnits {

    public static final String PACKAGE_2_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2";

    @Test
    public void testClearModel() throws Exception {
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation()
                        + "/test-package_v2/library_2_p2.xml"));
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);

        SchemaCompilerTestUtils.printFindings(findings);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        TLModel model = modelLoader.getLibraryModel();
        assertEquals(2, model.getBuiltInLibraries().size());
        assertEquals(5, model.getUserDefinedLibraries().size());

        model.clearModel();
        assertEquals(2, model.getBuiltInLibraries().size());
        assertEquals(0, model.getUserDefinedLibraries().size());
    }

    @Test
    public void testGetReferenceCount() throws Exception {
        LibraryInputSource<InputStream> library1Input = new LibraryStreamInputSource(
        		new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v2/library_1_p2.xml" ) );
        LibraryInputSource<InputStream> library2Input = new LibraryStreamInputSource(
        		new File( SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package_v2/library_2_p2.xml" ) );
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        ValidationFindings findings = modelLoader.loadLibraryModel( library1Input );
        
        findings.addAll( modelLoader.loadLibraryModel( library2Input ) );

        SchemaCompilerTestUtils.printFindings(findings);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        TLModel model = modelLoader.getLibraryModel();
        AbstractLibrary library = null;

        for (AbstractLibrary lib : model.getAllLibraries()) {
            if (lib.getName().equals("library_2_p2")) {
                library = lib;
            }
        }
        assertNotNull(library);
        assertEquals(5, library.getReferenceCount());
    }

    @Test
    public void testLibraryServiceMethods() throws Exception {
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation()
                        + "/test-package_v2/library_1_p2.xml"));
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);

        SchemaCompilerTestUtils.printFindings(findings);
        assertFalse(findings.hasFinding(FindingType.ERROR));

        TLModel model = modelLoader.getLibraryModel();
        TLLibrary library = (TLLibrary) model.getLibrary(PACKAGE_2_NAMESPACE, "library_1_p2");
        TLService service = library.getService();

        assertNotNull(service);
        library.removeNamedMember(service);
        assertNull(library.getService());
        assertNull(service.getOwningLibrary());
        library.addNamedMember(service);
        assertNotNull(library.getService());
        assertNotNull(service.getOwningLibrary());
    }

    @Test
    public void testDocumentationOwner() throws Exception {
        TLBusinessObject sourceBO = new TLBusinessObject();
        TLBusinessObject destinationBO = new TLBusinessObject();
        TLDocumentation doc = new TLDocumentation();

        sourceBO.setDocumentation(doc);
        destinationBO.setDocumentation(doc);

        assertEquals(doc, destinationBO.getDocumentation());
        assertEquals(doc, sourceBO.getDocumentation());
        assertEquals(destinationBO, doc.getOwner());

        // Original Bug: Documentation owner was nulled out with this call prior to fix
        destinationBO.setDocumentation(sourceBO.getDocumentation());

        assertEquals(doc, destinationBO.getDocumentation());
        assertEquals(doc, sourceBO.getDocumentation());
        assertEquals(destinationBO, doc.getOwner());
    }

}
