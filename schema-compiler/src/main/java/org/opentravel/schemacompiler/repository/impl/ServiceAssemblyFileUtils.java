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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
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

import org.opentravel.ns.ota2.assembly_v01_00.AssemblyIdentityType;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyItemType;
import org.opentravel.ns.ota2.assembly_v01_00.AssemblyType;
import org.opentravel.ns.ota2.assembly_v01_00.ObjectFactory;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LoaderValidationMessageKeys;
import org.opentravel.schemacompiler.loader.impl.FileValidationSource;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.ServiceAssembly;
import org.opentravel.schemacompiler.repository.ServiceAssemblyItem;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Static helper methods that handle the loading and saving of assemblies to and from the local file
 * system using JAXB.
 */
public class ServiceAssemblyFileUtils extends AbstractFileUtils {
	
	private static final String SCHEMA_CONTEXT = "org.opentravel.ns.ota2.assembly_v01_00";
    private static final String ASSEMBLY_FILE_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/Assembly_v01_00";
	
    private static javax.xml.validation.Schema releaseValidationSchema;
    private static ObjectFactory objectFactory = new ObjectFactory();
    private static JAXBContext jaxbContext;
	
    private RepositoryManager repositoryManager;
    
    /**
	 * Constructor that supplies the repository manager to be used during
	 * object transformations during the loading process.
	 * 
	 * @param repositoryManager  the repository manager instance
	 */
	public ServiceAssemblyFileUtils(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
    
    /**
     * Loads the OTM assembly from the specified file location.
     * 
     * @param assemblyFile  the assembly file to load
     * @param findings  the validation findings encountered during the load process
     * @return ServiceAssembly
     * @throws LibraryLoaderException  thrown if the assembly file cannot be loaded
     */
    public ServiceAssembly loadAssemblyFile(File assemblyFile, ValidationFindings findings)
            throws LibraryLoaderException {
    	ServiceAssembly assembly = null;
    	
        try (Reader reader = new FileReader( assemblyFile )){
        	assembly = loadAssembly( reader );
        	assembly.setAssemblyUrl( URLUtils.toURL( assemblyFile ) );
            return assembly;
            
        } catch (JAXBException e) {
            String filename = (assemblyFile == null) ? "[UNKNOWN FILE]" : assemblyFile.getName();
            
            findings.addFinding(FindingType.ERROR, new FileValidationSource(assemblyFile),
                    LoaderValidationMessageKeys.ERROR_UNREADABLE_RELEASE_CONTENT, filename,
                    ExceptionUtils.getExceptionClass(e).getSimpleName(),
                    ExceptionUtils.getExceptionMessage(e));

        } catch (IOException e) {
            if (findings != null) {
                findings.addFinding(FindingType.ERROR, new FileValidationSource(assemblyFile),
                        LoaderValidationMessageKeys.ERROR_RELEASE_NOT_FOUND,
                        (assemblyFile == null) ? "[UNKNOWN FILE]" : assemblyFile.getName());
            } else {
                throw new LibraryLoaderException(e.getMessage(), e);
            }

        } catch (Throwable t) {
            throw new LibraryLoaderException("Unknown error while loading assembly.", t);
        }
        return assembly;
    }

    /**
     * Loads the OTM assembly from the given content string.
     * 
     * @param contentString  the assembly content to unmarshal
     * @param findings  the validation findings encountered during the load process
     * @return ServiceAssembly
     * @throws LibraryLoaderException  thrown if the content cannot be loaded
     */
    public ServiceAssembly loadAssemblyContent(String contentString) throws LibraryLoaderException {
    	try {
    		return loadAssembly( new StringReader( contentString ) );
    		
    	} catch (JAXBException | IOException | RepositoryException e) {
    		throw new LibraryLoaderException("Error loading OTM release content.", e);
    	}
    }
    
    /**
     * Loads the OTM assembly from the given reader
     * 
     * @param reader  the reader from which to obtain the assembly content
     * @return ServiceAssembly
     * @throws JAXBException  thrown if the assembly content cannot be parsed
     * @throws IOException  thrown if the assembly content cannot be accessed
     * @throws RepositoryException  thrown if one or more of the assembly's constituent
     *								repository items cannot be resolved
     */
    @SuppressWarnings("unchecked")
    private ServiceAssembly loadAssembly(Reader reader)
    		throws JAXBException, IOException, RepositoryException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema( releaseValidationSchema );

        JAXBElement<AssemblyType> documentElement =
        		(JAXBElement<AssemblyType>) unmarshaller.unmarshal( reader );
        AssemblyType jaxbAssembly = documentElement.getValue();
        
        return transformToOtmAssembly( jaxbAssembly );
    }
    
    /**
     * Saves the OTM assembly to the local file system.
     * 
     * @param assembly  the OTM assembly to be saved
     * @param createBackup  flag indicating whether to create a backup before saving
     * @throws LibrarySaveException  thrown if the release file cannot be saved
     */
    public void saveAssemblyFile(ServiceAssembly assembly, boolean createBackup) throws LibrarySaveException {
		File assemblyFile = URLUtils.isFileURL( assembly.getAssemblyUrl() ) ?
				URLUtils.toFile( assembly.getAssemblyUrl() ) : null;
        boolean success = false;
        File backupFile = null;
        
        if (assemblyFile == null) {
        	throw new LibrarySaveException(
        			"Unable to save assembly because it is not stored on the local file system.");
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
        
        try (Writer writer = new FileWriter( assemblyFile ) ) {
        	marshalAssembly( assembly, writer );
            success = true;

        } catch (JAXBException | IOException e) {
            throw new LibrarySaveException("Unknown error while saving assembly.", e);

        } finally {
            if (!success && (backupFile != null)) {
                try {
                    restoreBackupFile( backupFile, assemblyFile.getName() );
                    
                } catch (Throwable t) {}
            }
        }
    }
    
    /**
     * Marshals the given assembly content to the writer provided.
     * 
     * @param assembly  the assembly to be marshalled
     * @param writer  the writer to which the marshalled content will be written
     * @throws JAXBException  thrown if the assembly content cannot be marshalled
     * @throws IOException  thrown if the writer is not able to process content
     */
    private void marshalAssembly(ServiceAssembly assembly, Writer writer) throws JAXBException, IOException {
    	AssemblyType jaxbAssembly = transformToJaxbAssembly( assembly );
        Marshaller marshaller = jaxbContext.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                new NamespacePrefixMapper() {
                    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                        return ASSEMBLY_FILE_NAMESPACE.equals(namespaceUri) ?
                        		SchemaDeclarations.OTA2_ASSEMBLY_SCHEMA.getDefaultPrefix() : suggestion;
                    }
                    public String[] getPreDeclaredNamespaceUris() {
                        return new String[] { ASSEMBLY_FILE_NAMESPACE };
                    }
                });
        marshaller.setSchema( releaseValidationSchema );
        marshaller.marshal( objectFactory.createAssembly( jaxbAssembly ), writer );
    }
    
    /**
     * Returns the filename to be used when loading or saving the given assembly.
     * 
     * @param assembly  the assembly for which to return a filename
     * @return String
     */
    public String getAssemblyFilename(ServiceAssembly assembly) {
    	if ((assembly.getName() != null) && (assembly.getVersion() != null)) {
    		StringBuilder filename = new StringBuilder();
    		
    		filename.append( assembly.getName().replaceAll( "\\s", "_ ") );
    		filename.append( "_" ).append( assembly.getVersion().replaceAll( "\\.", "_" ) );
    		filename.append( ".osm" );
    		return filename.toString();
    		
    	} else {
    		throw new IllegalArgumentException(
    				"Unable to determine assembly filename (missing information).");
    	}
    }
    
    /**
     * Transforms the given OTM assembly to its JAXB object representation.
     * 
     * @param assembly  the OTM assembly to transform
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
    	
    	for (ServiceAssemblyItem item : assembly.getProviderApis()) {
    		jaxbAssembly.getProvider().add( transformToJaxbAssemblyItem( item ) );
    	}
    	for (ServiceAssemblyItem item : assembly.getConsumerApis()) {
    		jaxbAssembly.getConsumer().add( transformToJaxbAssemblyItem( item ) );
    	}
    	return jaxbAssembly;
    }
    
    /**
     * Transforms the given assembly item to its JAXB object representation.
     * 
     * @param item  the assembly item to transform
     * @return AssemblyItemType
     */
    private AssemblyItemType transformToJaxbAssemblyItem(ServiceAssemblyItem item) {
    	AssemblyItemType jaxbItem = new AssemblyItemType();
    	RepositoryItem rItem = item.getReleaseItem();
    	QName resourceName = item.getResourceName();
    	
    	jaxbItem.setBaseNamespace( rItem.getBaseNamespace() );
    	jaxbItem.setFilename( rItem.getFilename() );
    	jaxbItem.setVersion( rItem.getVersion() );
    	
    	if (resourceName != null) {
        	jaxbItem.setResourceNamespace( resourceName.getNamespaceURI() );
        	jaxbItem.setResourceLocalName( resourceName.getLocalPart() );
    	}
    	return jaxbItem;
    }
    
    /**
     * Transforms the given JAXB assembly instance to an OTM service assembly.
     * 
     * @param jaxbAssembly  the JAXB assembly instance to transform
     * @return ServiceAssembly
     * @throws RepositoryException  thrown if one or more of the assembly's constituent
     *								repository items cannot be resolved
     */
    private ServiceAssembly transformToOtmAssembly(AssemblyType jaxbAssembly)
    		throws RepositoryException {
    	ServiceAssembly assembly = new ServiceAssembly();
    	AssemblyIdentityType jaxbIdentity = jaxbAssembly.getAssemblyIdentity();
    	
    	if (jaxbIdentity != null) {
        	assembly.setBaseNamespace( jaxbIdentity.getBaseNamespace() );
        	assembly.setName( jaxbIdentity.getName() );
        	assembly.setVersion( jaxbIdentity.getVersion() );
    	}
    	
    	for (AssemblyItemType jaxbItem : jaxbAssembly.getProvider()) {
    		assembly.addProviderApi( transformToAssemblyItem( jaxbItem ) );
    	}
    	for (AssemblyItemType jaxbItem : jaxbAssembly.getConsumer()) {
    		assembly.addConsumerApi( transformToAssemblyItem( jaxbItem ) );
    	}
    	return assembly;
    }
    
    /**
     * Transforms the given JAXB assembly item to an OTM assembly item.
     * 
     * @param jaxbItem  the JAXB assembly instance to transform
     * @return ServiceAssembly
     * @throws RepositoryException  thrown if the repository item of the release cannot be resolved
     */
    private ServiceAssemblyItem transformToAssemblyItem(AssemblyItemType jaxbItem)
    		throws RepositoryException {
    	ServiceAssemblyItem item = new ServiceAssemblyItem();
    	RepositoryItem rItem = repositoryManager.getRepositoryItem(
    			jaxbItem.getBaseNamespace(), jaxbItem.getFilename(), jaxbItem.getVersion() );
    	
    	item.setReleaseItem( rItem );
    	
    	if ((jaxbItem.getResourceNamespace() != null) && (jaxbItem.getResourceLocalName() != null)) {
        	item.setResourceName( new QName(
        			jaxbItem.getResourceNamespace(), jaxbItem.getResourceLocalName() ) );
    	}
    	return item;
    }
    
	/**
	 * Initializes the JAXB context.
	 */
	static {
		try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            InputStream schemaStream = SchemaDeclarations.OTA2_ASSEMBLY_SCHEMA.getContent(
            		CodeGeneratorFactory.XSD_TARGET_FORMAT );

            schemaFactory.setResourceResolver( new ClasspathResourceResolver() );
            releaseValidationSchema = schemaFactory.newSchema( new StreamSource( schemaStream ) );
			jaxbContext = JAXBContext.newInstance( SCHEMA_CONTEXT );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError( t );
		}
	}
	
}
