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
package org.opentravel.schemacompiler.saver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Verifies the operation of the <code>LibraryModelSaver</code> components.
 * 
 * @author S. Livezey
 */
public class TestLibraryModelSaver {

    private static final String SAVE_FOLDER = "/target/test-save-location/";

    @Test
    public void testSaveLibraryModel() throws Exception {
        TLModel model = loadTestModel();

        try {
            for (TLLibrary library : model.getUserDefinedLibraries()) {
                moveLibraryUrlToTempLocation(library);
            }
            ValidationFindings findings = new LibraryModelSaver().saveAllLibraries(model);

            SchemaCompilerTestUtils.printFindings(findings);
            assertFalse(findings.hasFinding());

            for (TLLibrary library : model.getUserDefinedLibraries()) {
                assertLibraryFileExists(library);
            }
        } finally {
            for (TLLibrary library : model.getUserDefinedLibraries()) {
                deleteLibraryFile(library);
            }
        }
    }

    @Test
    public void testSaveSingleLibrary() throws Exception {
        TLModel model = loadTestModel();
        TLLibrary library = null;

        try {
            for (TLLibrary lib : model.getUserDefinedLibraries()) {
                library = lib;
                break;
            }
            assertNotNull(library);
            moveLibraryUrlToTempLocation(library);

            ValidationFindings findings = new LibraryModelSaver().saveLibrary(library);

            SchemaCompilerTestUtils.printFindings(findings);
            assertFalse(findings.hasFinding());
            assertLibraryFileExists(library);

        } finally {
            if (library != null) {
                deleteLibraryFile(library);
            }
        }
    }

    @Test
    public void testBackupFile() throws Exception {
        TLModel model = loadTestModel();
        TLLibrary library = null;
        File backupFile = null;

        try {
            for (TLLibrary lib : model.getUserDefinedLibraries()) {
                library = lib;
                break;
            }
            assertNotNull(library);
            moveLibraryUrlToTempLocation(library);

            // Save the file to the temp-location
            ValidationFindings findings = new LibraryModelSaver().saveLibrary(library);

            SchemaCompilerTestUtils.printFindings(findings);
            assertFalse(findings.hasFinding());
            assertLibraryFileExists(library);

            // Save the file again to force the creation of a backup
            backupFile = getBackupFile(library);

            if (backupFile.exists()) {
                backupFile.delete();
            }
            findings = new LibraryModelSaver().saveLibrary(library);
            assertTrue(backupFile.exists());

        } finally {
            if (library != null) {
                deleteLibraryFile(library);
            }
            if (backupFile != null) {
                backupFile.delete();
            }
        }
    }

    // @Test
    public void testLoadAndSave_ManualTest() throws Exception {
        String filepath = "src/test/resources/libraries_1_5/test-package_v2/";
        String filename = "library_3_ext.xml";
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(new File(
                System.getProperty("user.dir"), filepath + filename));
        File saveFile = new File(System.getProperty("user.dir"), SAVE_FOLDER + filename);
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);

        if (findings.hasFinding()) {
            System.out.println("Loader Errors/Warnings:");

            for (String message : findings
                    .getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
                System.out.println("  " + message);
            }
        }

        TLModel model = modelLoader.getLibraryModel();
        assertNotNull(model);
        TLLibrary library = null;

        for (TLLibrary lib : model.getUserDefinedLibraries()) {
            if (lib.getLibraryUrl().toExternalForm().endsWith(filename)) {
                library = lib;
                break;
            }
        }
        assertNotNull(library);

        if (saveFile.exists()) {
            saveFile.delete();
        }
        library.setLibraryUrl(URLUtils.toURL(saveFile));
        findings = new LibraryModelSaver().saveLibrary(library);

        if (findings.hasFinding()) {
            System.out.println("Saver Errors/Warnings:");

            for (String message : findings
                    .getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
                System.out.println("  " + message);
            }
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
    }

    @BeforeClass
    public static void createTestFolder() {
        File testFolder = new File(System.getProperty("user.dir"), SAVE_FOLDER);

        if (!testFolder.exists()) {
            testFolder.mkdirs();
        }
    }

    private TLModel loadTestModel() throws Exception {
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(new File(
                SchemaCompilerTestUtils.getBaseLibraryLocation()
                        + "/test-package_v2/library_1_p2.xml"));
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
        modelLoader.loadLibraryModel(libraryInput);

        return modelLoader.getLibraryModel();
    }

    private void moveLibraryUrlToTempLocation(TLLibrary library) throws Exception {
        String urlPath = library.getLibraryUrl().toExternalForm();
        String filename = urlPath.substring(urlPath.lastIndexOf('/') + 1);
        File tempFile = new File(System.getProperty("user.dir"), SAVE_FOLDER + filename);

        if (tempFile.exists()) {
            tempFile.delete();
        }
        library.setLibraryUrl(URLUtils.toURL(tempFile));
    }

    private void assertLibraryFileExists(TLLibrary library) throws Exception {
        String urlPath = library.getLibraryUrl().toExternalForm();
        String filename = urlPath.substring(urlPath.lastIndexOf('/') + 1);
        File libraryFile = new File(System.getProperty("user.dir"), SAVE_FOLDER + filename);

        if (!libraryFile.exists()) {
            fail("Saved library file does not exist at the expected location: "
                    + libraryFile.getAbsolutePath());
        }
    }

    private void deleteLibraryFile(TLLibrary library) {
        String urlPath = library.getLibraryUrl().toExternalForm();

        if (urlPath.indexOf(SAVE_FOLDER) >= 0) {
            String filename = urlPath.substring(urlPath.lastIndexOf('/') + 1);
            File libraryFile = new File(System.getProperty("user.dir"), SAVE_FOLDER + filename);

            if (libraryFile.exists()) {
                libraryFile.delete();
            }
        }
    }

    private File getBackupFile(TLLibrary library) {
        File libraryFile = URLUtils.toFile(library.getLibraryUrl());
        String filename = libraryFile.getName();
        int dotIdx = filename.lastIndexOf('.');

        if (dotIdx >= 0) {
            filename = filename.substring(0, dotIdx);
        }
        return new File(libraryFile.getParentFile(), filename + ".bak");
    }

}
