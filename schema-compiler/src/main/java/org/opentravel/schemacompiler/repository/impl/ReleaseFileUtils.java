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
package org.opentravel.schemacompiler.repository.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.opentravel.ns.ota2.release_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.release_v01_00.ReleaseType;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LoaderValidationMessageKeys;
import org.opentravel.schemacompiler.loader.impl.FileValidationSource;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Static helper methods that handle the loading and saving of projects to and from the local file
 * system using JAXB.
 */
public class ReleaseFileUtils extends AbstractFileUtils {
	
	private static final String SCHEMA_CONTEXT = "org.opentravel.ns.ota2.release_v01_00";
    private static final String RELEASE_FILE_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/Release_v01_00";
	
    private static javax.xml.validation.Schema releaseValidationSchema;
    private static ObjectFactory objectFactory = new ObjectFactory();
    private static JAXBContext jaxbContext;
	
    /**
     * Loads the JAXB representation of the release from the specified file location.
     * 
     * @param releaseFile  the release file to load
     * @param findings  the validation findings encountered during the load process
     * @return ReleaseType
     * @throws LibraryLoaderException  thrown if the project file cannot be loaded
     */
    @SuppressWarnings("unchecked")
    public static ReleaseType loadReleaseFile(File releaseFile, ValidationFindings findings)
            throws LibraryLoaderException {
    	ReleaseType jaxbRelease = null;
        InputStream is = null;
        try {
            is = new FileInputStream(releaseFile);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema( releaseValidationSchema );

            JAXBElement<ReleaseType> documentElement = (JAXBElement<ReleaseType>) unmarshaller.unmarshal(is);
            jaxbRelease = documentElement.getValue();

        } catch (JAXBException e) {
            String filename = (releaseFile == null) ? "[UNKNOWN FILE]" : releaseFile.getName();

            findings.addFinding(FindingType.ERROR, new FileValidationSource(releaseFile),
                    LoaderValidationMessageKeys.ERROR_UNREADABLE_RELEASE_CONTENT, filename,
                    ExceptionUtils.getExceptionClass(e).getSimpleName(),
                    ExceptionUtils.getExceptionMessage(e));

        } catch (IOException e) {
            if (findings != null) {
                findings.addFinding(FindingType.ERROR, new FileValidationSource(releaseFile),
                        LoaderValidationMessageKeys.ERROR_RELEASE_NOT_FOUND,
                        (releaseFile == null) ? "[UNKNOWN FILE]" : releaseFile.getName());
            } else {
                throw new LibraryLoaderException(e.getMessage(), e);
            }

        } catch (Throwable t) {
            throw new LibraryLoaderException("Unknown error while loading project.", t);

        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Throwable t) {
            }
        }
        return jaxbRelease;
    }

    /**
     * Saves the JAXB representation of the release to the specified file location.
     * 
     * @param release  the JAXB representation of the release's contents
     * @param releaseFile  the file to which the release contents should be saved
     * @throws LibrarySaveException  thrown if the release file cannot be saved
     */
    public static void saveReleaseFile(ReleaseType release, File releaseFile) throws LibrarySaveException {
        boolean success = false;
        File backupFile = null;
        try {
            backupFile = createBackupFile( releaseFile );
            
        } catch (IOException e) {
            // If we could not create the backup file, proceed without one
        }

        try {
            Marshaller marshaller = jaxbContext.createMarshaller();

            if (!releaseFile.exists()) {
                releaseFile.getParentFile().mkdirs();
            }
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                    new NamespacePrefixMapper() {
                        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                            return RELEASE_FILE_NAMESPACE.equals(namespaceUri) ?
                            		SchemaDeclarations.OTA2_RELEASE_SCHEMA.getDefaultPrefix() : suggestion;
                        }
                        public String[] getPreDeclaredNamespaceUris() {
                            return new String[] { RELEASE_FILE_NAMESPACE };
                        }
                    });
            marshaller.setSchema( releaseValidationSchema );
            marshaller.marshal( objectFactory.createRelease( release ), releaseFile );
            success = true;

        } catch (JAXBException e) {
            throw new LibrarySaveException("Unknown error while saving project.", e);

        } finally {
            if (!success && (backupFile != null)) {
                try {
                    restoreBackupFile( backupFile, releaseFile.getName() );
                    
                } catch (Throwable t) {}
            }
        }
    }

	/**
	 * Initializes the JAXB context.
	 */
	static {
		try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream schemaStream = SchemaDeclarations.OTA2_RELEASE_SCHEMA.getContent(
            		CodeGeneratorFactory.XSD_TARGET_FORMAT);

            schemaFactory.setResourceResolver(new ClasspathResourceResolver());
            releaseValidationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
			jaxbContext = JAXBContext.newInstance( SCHEMA_CONTEXT );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
