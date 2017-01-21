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
package org.opentravel.schemacompiler.security.impl;

import java.io.File;
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

import org.opentravel.ns.ota2.security_v01_00.GroupAssignments;
import org.opentravel.ns.ota2.security_v01_00.NamespaceAuthorizations;
import org.opentravel.ns.ota2.security_v01_00.ObjectFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Utility methods used for loading and saving the contents of security-related files in the OTA2.0
 * repository.
 * 
 * @author S. Livezey
 */
public class SecurityFileUtils {

    private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.security_v01_00";
    private static final String SECURITY_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/Security_v01_00";
    private static final String SECURITY_SCHEMA_LOCATION = "/schemas/OTA2_Security_v1.0.0.xsd";
    public static final String AUTHORIZATION_FILENAME = "auth.xml";

    private static javax.xml.validation.Schema validationSchema;
    protected static ObjectFactory objectFactory = new ObjectFactory();
    protected static JAXBContext jaxbContext;

    private RepositoryManager repositoryManager;

    /**
     * Constructor that provides the root location of the OTA2.0 repository.
     * 
     * @param repositoryManager
     *            the repository manager for all file-system resources
     */
    public SecurityFileUtils(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    /**
     * Returns the underlying repository manager for this instance.
     * 
     * @return RepositoryManager
     */
    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    /**
     * Loads the group assignment information from the specified file.
     * 
     * @param file
     *            the file from which to load the group assignments
     * @return GroupAssignments
     * @throws IOException
     *             thrown if the contents of the file cannot be loaded
     */
    public GroupAssignments loadGroupAssignments(File file) throws IOException {
        return (GroupAssignments) loadFile(file);
    }

    /**
     * Saves the given group assignments to the specified file location.
     * 
     * @param file
     *            the file to which the group assignments will be saved
     * @param groupAssignments
     *            the group assignments to save
     * @throws IOException
     *             thrown if the file content cannot be saved
     */
    public void saveGroupAssignments(File file, GroupAssignments groupAssignments)
            throws IOException {
        saveFile(file, objectFactory.createGroupAssignments(groupAssignments));
    }

    /**
     * Returns the filename of the authorization file for the specified namespace. If the
     * 'namespace' parameter is null, the location of the global authorization file will be
     * returned.
     * 
     * <p>
     * NOTE: Even though the file location is returned by this method, the existence of an
     * authorization file is not guaranteed by this method.
     * 
     * @param namespace
     *            the namespace for which to return the authorization file location
     * @return File
     * @throws RepositorySecurityException
     *             thrown if the location of the authorization file cannot be identified
     */
    public File getAuthorizationFile(String namespace) throws RepositorySecurityException {
        try {
            File authorizationFolder;

            if (namespace != null) {
                authorizationFolder = repositoryManager.getFileManager().getNamespaceFolder(
                        namespace, null);

            } else {
                authorizationFolder = repositoryManager.getRepositoryLocation();
            }
            return new File(authorizationFolder, AUTHORIZATION_FILENAME);

        } catch (RepositoryException e) {
            throw new RepositorySecurityException(
                    "Unable to identify authorization file for namespace: " + namespace, e);
        }
    }

    /**
     * Loads the authorization permissions for the specified namespace.
     * 
     * @param authorizationFile
     *            the file from which to load the namespace authorizations
     * @return NamespaceAuthorizations
     * @throws IOException
     *             thrown if the contents of the file cannot be loaded
     */
    public NamespaceAuthorizations loadNamespaceAuthorizations(File authorizationFile)
            throws IOException {
        return (NamespaceAuthorizations) loadFile(authorizationFile);
    }

    /**
     * Saves the given authorization permissions to the specified file location.
     * 
     * @param authorizationFile
     *            the file to which the namespace authorizations will be saved
     * @param groupAssignments
     *            the group assignments to save
     * @throws IOException
     *             thrown if the file content cannot be saved
     */
    public void saveNamespaceAuthorizations(File authorizationFile,
            NamespaceAuthorizations authorizations) throws IOException {
        saveFile(authorizationFile, objectFactory.createNamespaceAuthorizations(authorizations));
    }

    /**
     * Loads the JAXB representation of the XML content from the specified file location.
     * 
     * @param file
     *            the repository file to load
     * @param findings
     *            the validation findings encountered during the load process
     * @return ProjectType
     * @throws IOException
     *             thrown if the file cannot be loaded
     */
    private Object loadFile(File file) throws IOException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(validationSchema);

            JAXBElement<?> documentElement = (JAXBElement<?>) unmarshaller.unmarshal(file);
            return documentElement.getValue();

        } catch (JAXBException e) {
            throw new IOException("Unrecognized file format.", e);
        }
    }

    /**
     * Saves the content of the given JAXB element the specified file location.
     * 
     * @param file
     *            the file to which the JAXB contents should be saved
     * @param jaxbElement
     *            the JAXB element whose content is to be saved
     * @throws IOException
     *             thrown if the file cannot be saved
     */
    private void saveFile(File file, JAXBElement<?> jaxbElement) throws IOException {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();

            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                    new NamespacePrefixMapper() {

                        @Override
                        public String getPreferredPrefix(String namespaceUri, String suggestion,
                                boolean requirePrefix) {
                            return SECURITY_NAMESPACE.equals(namespaceUri) ? "sec" : suggestion;
                        }

                        @Override
                        public String[] getPreDeclaredNamespaceUris() {
                            return new String[] { SECURITY_NAMESPACE };
                        }

                    });
            marshaller.setSchema(validationSchema);
            marshaller.marshal(jaxbElement, file);

        } catch (JAXBException e) {
            throw new IOException("Unknown error while repository file: " + file.getName(), e);
        }
    }

    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream schemaStream = SecurityFileUtils.class
                    .getResourceAsStream(SECURITY_SCHEMA_LOCATION);

            schemaFactory.setResourceResolver(new ClasspathResourceResolver());
            validationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
