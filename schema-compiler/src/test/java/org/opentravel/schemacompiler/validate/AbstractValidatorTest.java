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

package org.opentravel.schemacompiler.validate;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.util.DocumentationPathResolver;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Base class for all validator tests that pre-loads a model containing known errors to be used for verification.
 */
public abstract class AbstractValidatorTest {

    private static boolean DEBUG = false;

    protected static final QName TLIBRARY_ERROR1_V1 =
        new QName( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-error1/v1", "library_error1" );
    protected static final QName TLIBRARY_ERROR1_V1_1 =
        new QName( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-error1/v1_1", "library_error1" );

    protected static TLModel model;
    protected static ValidationFindings findings = new ValidationFindings();

    @BeforeClass
    public static void setupModel() throws Exception {
        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource( new File(
            SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-errors/library_error1_v1_1.xml" ) );
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();

        findings = modelLoader.loadLibraryModel( libraryInput );
        model = modelLoader.getLibraryModel();

        if (DEBUG) {
            for (String message : findings.getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT )) {
                System.out.println( message );
            }
        }
    }

    protected ValidationFindings validateModelElement(TLModelElement modelElement) {
        return TLModelCompileValidator.validateModelElement( modelElement, false );
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
        return (T) member;
    }

    protected Set<String> getFindingMessageKeys(Validatable source) {
        return getFindingMessageKeys( findings, source );
    }

    protected Set<String> getFindingMessageKeys(ValidationFindings f, Validatable source) {
        Set<String> messageKeys = new HashSet<>();

        if (source == null) {
            fail( "Validation source object not found." );
        }

        for (ValidationFinding finding : f.getFindingsAsList( source )) {
            String messageKey = finding.getMessageKey();
            int lastIdx = messageKey.lastIndexOf( '.' );

            messageKeys.add( messageKey.substring( lastIdx + 1 ) );
        }
        return messageKeys;
    }

}
