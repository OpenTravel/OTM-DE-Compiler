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

package org.opentravel.repocommon.subscription;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionList;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.repocommon.util.RepositoryJaxbContext;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.xml.NamespacePrefixMapper;

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

/**
 * Utility methods used for loading and saving the contents of subscription-related files in the OTM repository.
 *
 * @author S. Livezey
 */
public class SubscriptionFileUtils {

    private static final String REPOSITORY_EXT_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/RepositoryInfoExt_v01_00";
    private static final String REPOSITORY_EXT_SCHEMA_LOCATION = "/schemas/OTA2_RepositoryExt_v1.0.0.xsd";
    private static final String NS_SUBSCRIPTION_FILENAME = "ns-subscriptions.xml";
    private static final String LIBRARY_SUBSCRIPTION_SUFFIX = "-subscriptions.xml";

    private static javax.xml.validation.Schema validationSchema;
    protected static ObjectFactory objectFactory;
    protected static JAXBContext jaxbContext;

    private RepositoryManager manager;

    /**
     * Constructor that specifies the manager of the OTM repository.
     * 
     * @param repositoryManager the repository manager for all file-system resources
     */
    public SubscriptionFileUtils(RepositoryManager repositoryManager) {
        this.manager = repositoryManager;
    }

    /**
     * Loads the subscription list for the specified base namespace. If no subscriptions are defined for the namespace,
     * this method will return null.
     * 
     * @param baseNS the base namespace for which to load the subscription list
     * @return SubscriptionList
     * @throws IOException thrown if an error occurs while loading the subscription list
     */
    public SubscriptionList loadNamespaceSubscriptionList(String baseNS) throws IOException {
        File sFile = getSubscriptionListFile( baseNS, null, null );
        SubscriptionList subscriptionList = null;

        if (sFile.exists()) {
            subscriptionList = loadFile( sFile );
        }
        return subscriptionList;
    }

    /**
     * Loads the subscription list for all versions of the specified repository item. If no such subscriptions are
     * defined for the item, this method will return null.
     * 
     * @param item the repository item for which to return the all-versions subscriptions
     * @return SubscriptionList
     * @throws IOException thrown if an error occurs while loading the subscription list
     */
    public SubscriptionList loadAllVersionsSubscriptionList(RepositoryItem item) throws IOException {
        File sFile = getSubscriptionListFile( item.getBaseNamespace(), item.getLibraryName(), null );
        SubscriptionList subscriptionList = null;

        if (sFile.exists()) {
            subscriptionList = loadFile( sFile );
        }
        return subscriptionList;
    }

    /**
     * Loads the subscription list for this specific version of the repository item. If no such subscriptions are
     * defined for the item, this method will return null.
     * 
     * @param item the repository item for which to return the single-versions subscriptions
     * @return SubscriptionList
     * @throws IOException thrown if an error occurs while loading the subscription list
     */
    public SubscriptionList loadSingleVersionSubscriptionList(RepositoryItem item) throws IOException {
        File sFile = getSubscriptionListFile( item.getBaseNamespace(), item.getLibraryName(), item.getVersion() );
        SubscriptionList subscriptionList = null;

        if (sFile.exists()) {
            subscriptionList = loadFile( sFile );
        }
        return subscriptionList;
    }

    /**
     * Saves the given subscription list to its appropriate file location and adds it to the current change set of the
     * file manager.
     * 
     * @param subscriptionList the subscription list to save
     * @throws IOException thrown if an error occurs while saving the subscription list
     */
    public void saveSubscriptionList(SubscriptionList subscriptionList) throws IOException {
        RepositoryFileManager fileManager = manager.getFileManager();
        boolean success = false;
        try {
            fileManager.startChangeSet();
            SubscriptionTarget target = subscriptionList.getSubscriptionTarget();
            File sFile =
                getSubscriptionListFile( target.getBaseNamespace(), target.getLibraryName(), target.getVersion() );

            fileManager.addToChangeSet( sFile );
            saveFile( sFile, subscriptionList );
            success = true;
            fileManager.commitChangeSet();

        } catch (RepositoryException e) {
            throw new IOException( "Unable to add subscription file to current change set.", e );

        } finally {
            if (!success) {
                try {
                    fileManager.rollbackChangeSet();
                } catch (Exception e) {
                    // Ignore error and continue
                }
            }
        }
    }

    /**
     * Returns the file location of a subscription list with the given characteristics.
     * 
     * @param baseNS the base namespace of the subscription target
     * @param libraryName the library name of the subscription target
     * @param version the library version of the subscription target
     * @return File
     * @throws IOException thrown if the subscription list file cannot be identified
     */
    protected File getSubscriptionListFile(String baseNS, String libraryName, String version) throws IOException {
        try {
            File folderLocation = manager.getFileManager().getNamespaceFolder( baseNS, version );
            String filename =
                (libraryName == null) ? NS_SUBSCRIPTION_FILENAME : (libraryName + LIBRARY_SUBSCRIPTION_SUFFIX);

            return new File( folderLocation, File.separatorChar + filename );

        } catch (RepositoryException e) {
            throw new IOException( "Unable to identify namespace folder location.", e );
        }
    }

    /**
     * Loads the JAXB representation of the XML content from the specified file location.
     * 
     * @param file the repository file to load
     * @return SubscriptionList
     * @throws IOException thrown if the file cannot be loaded
     */
    @SuppressWarnings("unchecked")
    protected SubscriptionList loadFile(File file) throws IOException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema( validationSchema );

            JAXBElement<SubscriptionList> documentElement =
                (JAXBElement<SubscriptionList>) FileUtils.unmarshalFileContent( file, unmarshaller );
            return documentElement.getValue();

        } catch (JAXBException e) {
            throw new IOException( "Unrecognized file format.", e );
        }
    }

    /**
     * Saves the content of the given subscription list to the specified file location.
     * 
     * @param file the file to which the subscription list should be saved
     * @param subscriptionList the subscription list whose content is to be saved
     * @throws IOException thrown if the file cannot be saved
     */
    protected void saveFile(File file, SubscriptionList subscriptionList) throws IOException {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();

            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            marshaller.setProperty( "com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                    return REPOSITORY_EXT_NAMESPACE.equals( namespaceUri ) ? "r" : suggestion;
                }

                @Override
                public String[] getPreDeclaredNamespaceUris() {
                    return new String[] {REPOSITORY_EXT_NAMESPACE};
                }
            } );
            marshaller.setSchema( validationSchema );
            marshaller.marshal( objectFactory.createSubscriptionList( subscriptionList ), file );

        } catch (JAXBException e) {
            throw new IOException( "Unknown error while repository file: " + file.getName(), e );
        }
    }

    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            InputStream schemaStream =
                SubscriptionFileUtils.class.getResourceAsStream( REPOSITORY_EXT_SCHEMA_LOCATION );

            schemaFactory.setResourceResolver( new ClasspathResourceResolver() );
            validationSchema = schemaFactory.newSchema( new StreamSource( schemaStream ) );
            jaxbContext = RepositoryJaxbContext.getExtContext();
            objectFactory = new ObjectFactory();

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
