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

import org.opentravel.ns.ota2.assembly_v01_00.AssemblyIdentityType;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyItemType;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyType;
import org.opentravel.ns.ota2.assembly_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.assembly_v01_00.QualifiedNameType;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LoaderConstants;
import org.opentravel.schemacompiler.loader.impl.FileValidationSource;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.ServiceAssembly;
import org.opentravel.schemacompiler.repository.ServiceAssemblyMember;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.xml.NamespacePrefixMapper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

/**
 * Static helper methods that handle the loading and saving of assemblies to and from the local file system using JAXB.
 */
public class ServiceAssemblyFileUtils extends AbstractFileUtils {

    private static final String SCHEMA_CONTEXT = "org.opentravel.ns.ota2.assembly_v01_00";
    private static final String ASSEMBLY_FILE_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/Assembly_v01_00";

    private static NamespacePrefixMapper prefixMapper = new NamespacePrefixMapper() {
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            return ASSEMBLY_FILE_NAMESPACE.equals( namespaceUri )
                ? SchemaDeclarations.OTA2_ASSEMBLY_SCHEMA.getDefaultPrefix()
                : suggestion;
        }

        @Override
        public String[] getPreDeclaredNamespaceUris() {
            return new String[] {ASSEMBLY_FILE_NAMESPACE};
        }
    };
    private static javax.xml.validation.Schema releaseValidationSchema;
    private static ObjectFactory objectFactory = new ObjectFactory();
    private static JAXBContext jaxbContext;

    /**
     * Constructor that supplies the repository manager to be used during object transformations during the loading
     * process.
     * 
     * @param repositoryManager the repository manager instance
     */
    public ServiceAssemblyFileUtils(RepositoryManager repositoryManager) {
        super( repositoryManager );
    }

    /**
     * Loads the OTM assembly from the specified file location.
     * 
     * @param assemblyFile the assembly file to load
     * @param findings the validation findings encountered during the load process
     * @return ServiceAssembly
     * @throws LibraryLoaderException thrown if the assembly file cannot be loaded
     */
    public ServiceAssembly loadAssemblyFile(File assemblyFile, ValidationFindings findings)
        throws LibraryLoaderException {
        ServiceAssembly assembly = null;

        try (Reader reader = new FileReader( assemblyFile )) {
            assembly = loadAssembly( reader );
            assembly.setAssemblyUrl( URLUtils.toURL( assemblyFile ) );
            return assembly;

        } catch (JAXBException e) {
            if (findings != null) {
                String filename = (assemblyFile == null) ? "[UNKNOWN FILE]" : assemblyFile.getName();

                findings.addFinding( FindingType.ERROR, new FileValidationSource( assemblyFile ),
                    LoaderConstants.ERROR_UNREADABLE_RELEASE_CONTENT, filename,
                    ExceptionUtils.getExceptionClass( e ).getSimpleName(), ExceptionUtils.getExceptionMessage( e ) );
            } else {
                throw new LibraryLoaderException( e.getMessage(), e );
            }

        } catch (IOException e) {
            if (findings != null) {
                findings.addFinding( FindingType.ERROR, new FileValidationSource( assemblyFile ),
                    LoaderConstants.ERROR_RELEASE_NOT_FOUND,
                    (assemblyFile == null) ? "[UNKNOWN FILE]" : assemblyFile.getName() );
            } else {
                throw new LibraryLoaderException( e.getMessage(), e );
            }

        } catch (Exception e) {
            throw new LibraryLoaderException( "Unknown error while loading assembly.", e );
        }
        return assembly;
    }

    /**
     * Loads the OTM assembly from the given content string.
     * 
     * @param contentString the assembly content to unmarshal
     * @return ServiceAssembly
     * @throws LibraryLoaderException thrown if the content cannot be loaded
     */
    public ServiceAssembly loadAssemblyContent(String contentString) throws LibraryLoaderException {
        try {
            return loadAssembly( new StringReader( contentString ) );

        } catch (JAXBException | RepositoryException e) {
            throw new LibraryLoaderException( "Error loading OTM release content.", e );
        }
    }

    /**
     * Loads the OTM assembly from the given reader
     * 
     * @param reader the reader from which to obtain the assembly content
     * @return ServiceAssembly
     * @throws JAXBException thrown if the assembly content cannot be parsed
     * @throws RepositoryException thrown if one or more of the assembly's constituent repository items cannot be
     *         resolved
     */
    @SuppressWarnings("unchecked")
    private ServiceAssembly loadAssembly(Reader reader) throws JAXBException, RepositoryException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema( releaseValidationSchema );

        JAXBElement<AssemblyType> documentElement = (JAXBElement<AssemblyType>) unmarshaller.unmarshal( reader );
        AssemblyType jaxbAssembly = documentElement.getValue();

        return transformToOtmAssembly( jaxbAssembly );
    }

    /**
     * Saves the OTM assembly to the local file system.
     * 
     * @param assembly the OTM assembly to be saved
     * @param createBackup flag indicating whether to create a backup before saving
     * @throws LibrarySaveException thrown if the release file cannot be saved
     */
    public void saveAssemblyFile(ServiceAssembly assembly, boolean createBackup) throws LibrarySaveException {
        File assemblyFile =
            URLUtils.isFileURL( assembly.getAssemblyUrl() ) ? URLUtils.toFile( assembly.getAssemblyUrl() ) : null;
        boolean success = false;
        File backupFile = null;

        if (assemblyFile == null) {
            throw new LibrarySaveException(
                "Unable to save assembly because it is not stored on the local file system." );
        }

        try {
            if (createBackup) {
                backupFile = createBackupFile( assemblyFile );
            }

        } catch (IOException e) {
            // If we could not create the backup file, proceed without one
        }

        if (!assemblyFile.exists()) {
            assemblyFile.getParentFile().mkdirs();
        }

        try (Writer writer = new FileWriter( assemblyFile )) {
            marshalAssembly( assembly, writer );
            success = true;

        } catch (JAXBException | IOException e) {
            throw new LibrarySaveException( "Unknown error while saving assembly.", e );

        } finally {
            if (!success && (backupFile != null)) {
                try {
                    restoreBackupFile( backupFile, assemblyFile.getName() );

                } catch (Exception e) {
                    // Ignore error and continue
                }
            }
        }
    }

    /**
     * Marshals the given assembly content to the writer provided.
     * 
     * @param assembly the assembly to be marshalled
     * @param writer the writer to which the marshalled content will be written
     * @throws JAXBException thrown if the assembly content cannot be marshalled
     */
    public void marshalAssembly(ServiceAssembly assembly, Writer writer) throws JAXBException {
        AssemblyType jaxbAssembly = transformToJaxbAssembly( assembly );
        Marshaller marshaller = jaxbContext.createMarshaller();

        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        marshaller.setProperty( "com.sun.xml.bind.namespacePrefixMapper", prefixMapper );
        marshaller.setSchema( releaseValidationSchema );
        marshaller.marshal( objectFactory.createAssembly( jaxbAssembly ), writer );
    }

    /**
     * Marshals the given assembly content and returns the resulting string.
     * 
     * @param assembly the assembly to be marshalled
     * @return String
     * @throws JAXBException thrown if the assembly content cannot be marshalled
     */
    public String marshalAssemblyContent(ServiceAssembly assembly) throws JAXBException {
        StringWriter writer = new StringWriter();

        marshalAssembly( assembly, writer );
        return writer.toString();
    }

    /**
     * Marshals the given assembly member content to the writer provided.
     * 
     * @param member the assembly member to be marshalled
     * @param isProvider flag indicating whether the given item represents a consumer or a provider API
     * @return String
     * @throws JAXBException thrown if the assembly content cannot be marshalled
     */
    public String marshalAssemblyMember(ServiceAssemblyMember member, boolean isProvider) throws JAXBException {
        AssemblyItemType jaxbMember = transformToJaxbAssemblyItem( member );
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter writer = new StringWriter();
        Object memberElement;

        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        marshaller.setProperty( "com.sun.xml.bind.namespacePrefixMapper", prefixMapper );
        marshaller.setSchema( releaseValidationSchema );

        if (isProvider) {
            memberElement = objectFactory.createProvider( jaxbMember );
        } else {
            memberElement = objectFactory.createConsumer( jaxbMember );
        }
        marshaller.marshal( memberElement, writer );
        return writer.toString();
    }

    /**
     * Unmarshals a JAXB assembly member from the string provided.
     * 
     * @param memberContent the string content for the assembly member to be unmarshalled
     * @return ReleaseMemberType
     * @throws RepositoryException thrown if the repository item for the associated release cannot be identified
     * @throws IOException thrown if the assembly member cannot be unmarshalled
     */
    @SuppressWarnings("unchecked")
    public ServiceAssemblyMember unmarshalAssemblyMemberContent(String memberContent)
        throws RepositoryException, JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<AssemblyItemType> memberElement =
            (JAXBElement<AssemblyItemType>) unmarshaller.unmarshal( new StringReader( memberContent ) );

        return transformToAssemblyMember( memberElement.getValue() );
    }

    /**
     * Returns the filename to be used when loading or saving the given assembly.
     * 
     * @param assembly the assembly for which to return a filename
     * @return String
     */
    public String getAssemblyFilename(ServiceAssembly assembly) {
        if ((assembly.getName() != null) && (assembly.getVersion() != null)) {
            StringBuilder filename = new StringBuilder();

            filename.append( assembly.getName().replaceAll( "\\s", "_ " ) );
            filename.append( "_" ).append( assembly.getVersion().replaceAll( "\\.", "_" ) );
            filename.append( ".osm" );
            return filename.toString();

        } else {
            throw new IllegalArgumentException( "Unable to determine assembly filename (missing information)." );
        }
    }

    /**
     * Transforms the given OTM assembly to its JAXB object representation.
     * 
     * @param assembly the OTM assembly to transform
     * @return AssemblyType
     */
    private AssemblyType transformToJaxbAssembly(ServiceAssembly assembly) {
        AssemblyType jaxbAssembly = new AssemblyType();
        AssemblyIdentityType identity = new AssemblyIdentityType();

        identity.setBaseNamespace( assembly.getBaseNamespace() );
        identity.setFilename( URLUtils.getUrlFilename( assembly.getAssemblyUrl() ) );
        identity.setVersion( assembly.getVersion() );
        identity.setName( assembly.getName() );
        jaxbAssembly.setAssemblyIdentity( identity );
        jaxbAssembly.setDescription( assembly.getDescription() );

        for (ServiceAssemblyMember member : assembly.getProviderApis()) {
            jaxbAssembly.getProvider().add( transformToJaxbAssemblyItem( member ) );
        }
        for (ServiceAssemblyMember member : assembly.getConsumerApis()) {
            jaxbAssembly.getConsumer().add( transformToJaxbAssemblyItem( member ) );
        }
        return jaxbAssembly;
    }

    /**
     * Transforms the given assembly item to its JAXB object representation.
     * 
     * @param member the assembly item to transform
     * @return AssemblyItemType
     */
    private AssemblyItemType transformToJaxbAssemblyItem(ServiceAssemblyMember member) {
        AssemblyItemType jaxbItem = new AssemblyItemType();
        RepositoryItem rItem = member.getReleaseItem();
        QName resourceName = member.getResourceName();

        jaxbItem.setBaseNamespace( rItem.getBaseNamespace() );
        jaxbItem.setFilename( rItem.getFilename() );
        jaxbItem.setVersion( rItem.getVersion() );

        if (resourceName != null) {
            QualifiedNameType jaxbResourceName = new QualifiedNameType();

            jaxbResourceName.setNamespace( resourceName.getNamespaceURI() );
            jaxbResourceName.setLocalName( resourceName.getLocalPart() );
            jaxbItem.setResourceName( jaxbResourceName );
        }
        return jaxbItem;
    }

    /**
     * Transforms the given JAXB assembly instance to an OTM service assembly.
     * 
     * @param jaxbAssembly the JAXB assembly instance to transform
     * @return ServiceAssembly
     * @throws RepositoryException thrown if one or more of the assembly's constituent repository items cannot be
     *         resolved
     */
    private ServiceAssembly transformToOtmAssembly(AssemblyType jaxbAssembly) throws RepositoryException {
        ServiceAssembly assembly = new ServiceAssembly();
        AssemblyIdentityType jaxbIdentity = jaxbAssembly.getAssemblyIdentity();

        if (jaxbIdentity != null) {
            assembly.setBaseNamespace( jaxbIdentity.getBaseNamespace() );
            assembly.setName( jaxbIdentity.getName() );
            assembly.setVersion( jaxbIdentity.getVersion() );
        }
        assembly.setDescription( jaxbAssembly.getDescription() );

        for (AssemblyItemType jaxbItem : jaxbAssembly.getProvider()) {
            assembly.addProviderApi( transformToAssemblyMember( jaxbItem ) );
        }
        for (AssemblyItemType jaxbItem : jaxbAssembly.getConsumer()) {
            assembly.addConsumerApi( transformToAssemblyMember( jaxbItem ) );
        }
        return assembly;
    }

    /**
     * Transforms the given JAXB assembly item to an OTM assembly member.
     * 
     * @param jaxbItem the JAXB assembly instance to transform
     * @return ServiceAssembly
     * @throws RepositoryException thrown if the repository item of the release cannot be resolved
     */
    private ServiceAssemblyMember transformToAssemblyMember(AssemblyItemType jaxbItem) throws RepositoryException {
        ServiceAssemblyMember member = new ServiceAssemblyMember();
        RepositoryItem rItem = repositoryManager.getRepositoryItem( jaxbItem.getBaseNamespace(), jaxbItem.getFilename(),
            jaxbItem.getVersion() );

        member.setReleaseItem( rItem );

        if (jaxbItem.getResourceName() != null) {
            QualifiedNameType jaxbResourceName = jaxbItem.getResourceName();

            member.setResourceName( new QName( jaxbResourceName.getNamespace(), jaxbResourceName.getLocalName() ) );
        }
        return member;
    }

    /**
     * Initializes the JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            InputStream schemaStream =
                SchemaDeclarations.OTA2_ASSEMBLY_SCHEMA.getContent( CodeGeneratorFactory.XSD_TARGET_FORMAT );

            schemaFactory.setResourceResolver( new ClasspathResourceResolver() );
            releaseValidationSchema = schemaFactory.newSchema( new StreamSource( schemaStream ) );
            jaxbContext = JAXBContext.newInstance( SCHEMA_CONTEXT );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
