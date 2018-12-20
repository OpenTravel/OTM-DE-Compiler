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
package org.opentravel.schemacompiler.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.opentravel.schemacompiler.loader.impl.LibrarySchema15ModuleLoader;
import org.opentravel.schemacompiler.loader.impl.LibrarySchema16ModuleLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Validates the functions of the <code>LibrarySchema1_3_ModuleLoader</code>.
 * 
 * @author S. Livezey
 */
public class TestLibraryModuleLoader {

    @Test
    public void testLoadLibrariesByInputSource() throws Exception {
        File libraryFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation()
                + "/test-package_v1/library_1_p1.xml");
        LibraryModuleLoader<InputStream> moduleLoader = OTM16Upgrade.otm16Enabled ?
        		new LibrarySchema16ModuleLoader() : new LibrarySchema15ModuleLoader();
        ValidationFindings findings = new ValidationFindings();

        LibraryModuleInfo<Object> libraryInfo = moduleLoader.loadLibrary(
                new LibraryStreamInputSource(URLUtils.toURL(libraryFile)), findings);

        String[] findingMessages = findings.getAllValidationMessages(FindingMessageFormat.DEFAULT);
        for (String message : findingMessages) {
            System.out.println("> " + message);
        }
        assertNotNull(libraryInfo);
        assertEquals("library_1_p1", libraryInfo.getLibraryName());
        assertFalse(findings.hasFinding());
    }

    @Test
    public void testLoadLibrariesByInputSourceWithInvalidUrl() throws Exception {
        File libraryFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation()
                + "/test-package_v1/library_xyz.xml");
        LibraryModuleLoader<InputStream> moduleLoader = new LibrarySchema15ModuleLoader();
        ValidationFindings findings = new ValidationFindings();

        LibraryModuleInfo<Object> libraryInfo = moduleLoader.loadLibrary(
                new LibraryStreamInputSource(URLUtils.toURL(libraryFile)), findings);

        assertNull(libraryInfo);
        assertEquals(1, findings.count());

        List<ValidationFinding> findingList = findings.getAllFindingsAsList();
        assertEquals(1, findingList.size());
        assertEquals("schemacompiler.loader.WARNING_LIBRARY_NOT_FOUND", findingList.get(0)
                .getMessageKey());
    }

    @Test
    public void testLoadLibrariesWithNullInputSource() throws Exception {
        LibraryModuleLoader<InputStream> moduleLoader = new LibrarySchema15ModuleLoader();
        ValidationFindings findings = new ValidationFindings();

        LibraryModuleInfo<Object> libraryInfo = moduleLoader.loadLibrary(null, findings);

        assertNull(libraryInfo);
        assertEquals(1, findings.count());

        List<ValidationFinding> findingList = findings.getAllFindingsAsList();
        assertEquals(1, findingList.size());
        assertEquals("schemacompiler.loader.WARNING_LIBRARY_NOT_FOUND", findingList.get(0)
                .getMessageKey());
    }

}
