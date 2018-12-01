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
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.opentravel.ns.ota2.release_v01_00.CompileOptionType;
import org.opentravel.ns.ota2.release_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.release_v01_00.PreferredFacetType;
import org.opentravel.ns.ota2.release_v01_00.PrincipalMembersType;
import org.opentravel.ns.ota2.release_v01_00.ReferencedMembersType;
import org.opentravel.ns.ota2.release_v01_00.ReleaseIdentityType;
import org.opentravel.ns.ota2.release_v01_00.ReleaseMemberType;
import org.opentravel.ns.ota2.release_v01_00.ReleaseType;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LoaderValidationMessageKeys;
import org.opentravel.schemacompiler.loader.impl.FileValidationSource;
import org.opentravel.schemacompiler.repository.Release;
import org.opentravel.schemacompiler.repository.ReleaseCompileOptions;
import org.opentravel.schemacompiler.repository.ReleaseMember;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.xml.XMLGregorianCalendarConverter;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Static helper methods that handle the loading and saving of releases to and from the local file
 * system using JAXB.
 */
public class ReleaseFileUtils extends AbstractFileUtils {
	
	private static final String SCHEMA_CONTEXT = "org.opentravel.ns.ota2.release_v01_00";
    private static final String RELEASE_FILE_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/Release_v01_00";
	
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
	public ReleaseFileUtils(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
    
    /**
     * Loads the OTM release from the specified file location.
     * 
     * @param releaseFile  the release file to load
     * @param findings  the validation findings encountered during the load process
     * @return Release
     * @throws LibraryLoaderException  thrown if the release file cannot be loaded
     */
    public Release loadReleaseFile(File releaseFile, ValidationFindings findings)
            throws LibraryLoaderException {
    	Release release = null;
    	
        try (Reader reader = new FileReader( releaseFile )){
        	release = loadRelease( reader );
            release.setReleaseUrl( URLUtils.toURL( releaseFile ) );
            return release;
            
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
            throw new LibraryLoaderException("Unknown error while loading release.", t);
        }
        return release;
    }

    /**
     * Loads the OTM release from the given content string.
     * 
     * @param contentString  the release content to unmarshal
     * @param findings  the validation findings encountered during the load process
     * @return Release
     * @throws LibraryLoaderException  thrown if the content cannot be loaded
     */
    public Release loadReleaseContent(String contentString) throws LibraryLoaderException {
    	try {
    		return loadRelease( new StringReader( contentString ) );
    		
    	} catch (JAXBException | IOException e) {
    		throw new LibraryLoaderException("Error loading OTM release content.", e);
    	}
    }
    
    /**
     * Loads the OTM release from the given reader
     * 
     * @param reader  the reader from which to obtain the release content
     * @return Release
     * @throws JAXBException  thrown if the release content cannot be parsed
     * @throws IOException  thrown if the release content cannot be accessed
     */
    @SuppressWarnings("unchecked")
    private Release loadRelease(Reader reader) throws JAXBException, IOException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema( releaseValidationSchema );

        JAXBElement<ReleaseType> documentElement =
        		(JAXBElement<ReleaseType>) unmarshaller.unmarshal( reader );
        ReleaseType jaxbRelease = documentElement.getValue();
        
        return transformToOtmRelease( jaxbRelease );
    }
    
    /**
     * Saves the OTM release to the local file system.
     * 
     * @param release  the OTM release to be saved
     * @param createBackup  flag indicating whether to create a backup before saving
     * @throws LibrarySaveException  thrown if the release file cannot be saved
     */
    public void saveReleaseFile(Release release, boolean createBackup) throws LibrarySaveException {
		File releaseFile = URLUtils.isFileURL( release.getReleaseUrl() ) ?
				URLUtils.toFile( release.getReleaseUrl() ) : null;
        boolean success = false;
        File backupFile = null;
        
        if (releaseFile == null) {
        	throw new LibrarySaveException(
        			"Unable to save release because it is not stored on the local file system.");
        }
        
        try {
            if (createBackup) {
            	backupFile = createBackupFile( releaseFile );
            }
            
        } catch (IOException e) {
            // If we could not create the backup file, proceed without one
        }

        if (!releaseFile.exists()) {
            releaseFile.getParentFile().mkdirs();
        }
        
        try (Writer writer = new FileWriter( releaseFile ) ) {
        	marshalRelease( release, writer );
            success = true;

        } catch (JAXBException | IOException e) {
            throw new LibrarySaveException("Unknown error while saving release.", e);

        } finally {
            if (!success && (backupFile != null)) {
                try {
                    restoreBackupFile( backupFile, releaseFile.getName() );
                    
                } catch (Throwable t) {}
            }
        }
    }
    
    /**
     * Marshals the given OTM release and returns the string content.
     * 
     * @param release  the OTM release to be marshalled
     * @throws LibrarySaveException  thrown if the release file cannot be marshalled
     */
    public String marshalReleaseContent(Release release) throws LibrarySaveException {
    	try {
        	StringWriter writer = new StringWriter();
        	
			marshalRelease( release, writer );
	    	return writer.toString();
	    	
		} catch (JAXBException | IOException e) {
    		throw new LibrarySaveException("Error marshalling OTM release content.", e);
		}
    }
    
    /**
     * Marshals the given release content to the writer provided.
     * 
     * @param release  the release to be marshalled
     * @param writer  the writer to which the marshalled content will be written
     * @throws JAXBException  thrown if the release content cannot be marshalled
     * @throws IOException  thrown if the writer is not able to process content
     */
    private void marshalRelease(Release release, Writer writer) throws JAXBException, IOException {
    	ReleaseType jaxbRelease = transformToJaxbRelease( release );
        Marshaller marshaller = jaxbContext.createMarshaller();

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
        marshaller.marshal( objectFactory.createRelease( jaxbRelease ), writer );
    }
    
    /**
     * Returns the filename to be used when loading or saving the given release.
     * 
     * @param release  the release for which to return a filename
     * @return String
     */
    public String getReleaseFilename(Release release) {
    	if ((release.getName() != null) && (release.getVersion() != null)) {
    		StringBuilder filename = new StringBuilder();
    		
    		filename.append( release.getName().replaceAll( "\\s", "_ ") );
    		filename.append( "_" ).append( release.getVersion().replaceAll( "\\.", "_" ) );
    		filename.append( ".otr" );
    		return filename.toString();
    		
    	} else {
    		throw new IllegalArgumentException(
    				"Unable to determine release filename (missing information).");
    	}
    }
    
    /**
     * Transforms the given OTM release to its JAXB object representation.
     * 
     * @param release  the OTM release to transform
     * @return ReleaseType
     */
    private ReleaseType transformToJaxbRelease(Release release) {
    	ReleaseIdentityType releaseId = new ReleaseIdentityType();
    	PrincipalMembersType principalMembers = new PrincipalMembersType();
    	ReferencedMembersType referencedMembers = new ReferencedMembersType();
    	ReleaseCompileOptions compilerOptions = release.getCompileOptions();
    	Map<String,String> optionProps = (compilerOptions == null) ?
    			new ReleaseCompileOptions().toProperties() : compilerOptions.toProperties();
    	ReleaseType jaxbRelease = new ReleaseType();
    	
    	releaseId.setBaseNamespace( release.getBaseNamespace() );
    	releaseId.setName( release.getName() );
    	releaseId.setVersion( release.getVersion() );
		releaseId.setFilename( getReleaseFilename( release ) );
    	
    	jaxbRelease.setReleaseIdentity( releaseId );
    	jaxbRelease.setStatus( release.getStatus() );
    	jaxbRelease.setDescription( trimString( release.getDescription() ) );
    	jaxbRelease.setPrincipalMembers( principalMembers );
    	jaxbRelease.setReferencedMembers( referencedMembers );
    	jaxbRelease.setDefaultEffectiveDate( XMLGregorianCalendarConverter.
    			toXMLGregorianCalendar( release.getDefaultEffectiveDate() ) );
    	
    	for (ReleaseMember member : release.getPrincipalMembers()) {
    		principalMembers.getReleaseMember().add( transformToJaxbReleaseMember( member ) );
    	}
    	
    	for (ReleaseMember member : release.getReferencedMembers()) {
    		referencedMembers.getReleaseMember().add( transformToJaxbReleaseMember( member ) );
    	}
    	
    	for (String optionKey : optionProps.keySet()) {
    		String optionValue = optionProps.get( optionKey );
    		
    		if (optionValue != null) {
    			CompileOptionType jaxbOption = new CompileOptionType();
    			
    			jaxbOption.setKey( optionKey );
    			jaxbOption.setValue( optionValue );
    			jaxbRelease.getCompileOption().add( jaxbOption );
    		}
    	}
    	
    	for (QName ownerName : release.getPreferredFacets().keySet()) {
    		QName facetName = release.getPreferredFacets().get( ownerName );
    		
    		if (facetName != null) {
    			PreferredFacetType jaxbPF = new PreferredFacetType();
    			
    			jaxbPF.setOwnerNamespace( ownerName.getNamespaceURI() );
    			jaxbPF.setOwnerName( ownerName.getLocalPart() );
    			jaxbPF.setFacetNamespace( facetName.getNamespaceURI() );
    			jaxbPF.setFacetName( facetName.getLocalPart() );
    			jaxbRelease.getPreferredFacet().add( jaxbPF );
    		}
    	}
    	return jaxbRelease;
    }
    
    /**
     * Transforms the given OTM release member to its JAXB object representation.
     * 
     * @param member  the OTM release member to transform
     * @return ReleaseMemberType
     */
    private ReleaseMemberType transformToJaxbReleaseMember(ReleaseMember member) {
    	RepositoryItem repoItem = member.getRepositoryItem();
    	ReleaseMemberType jaxbMember = new ReleaseMemberType();
    	
    	if (repoItem != null) {
    		jaxbMember.setRepositoryID( repoItem.getRepository().getId() );
    		jaxbMember.setBaseNamespace( repoItem.getBaseNamespace() );
    		jaxbMember.setNamespace( repoItem.getNamespace() );
    		jaxbMember.setFilename( repoItem.getFilename() );
    		jaxbMember.setLibraryName( repoItem.getLibraryName() );
    		jaxbMember.setVersion( repoItem.getVersion() );
    	}
    	jaxbMember.setEffectiveDate( XMLGregorianCalendarConverter.
    			toXMLGregorianCalendar( member.getEffectiveDate() ) );
    	return jaxbMember;
    }
    
    /**
     * Transforms the given JAXB release instance to an OTM release.
     * 
     * @param jaxbRelease  the JAXB release instance to transform
     * @return Release
     */
    private Release transformToOtmRelease(ReleaseType jaxbRelease) {
    	ReleaseIdentityType releaseId = jaxbRelease.getReleaseIdentity();
    	Map<String,String> compileOptions = new HashMap<>();
    	Map<QName,QName> preferredFacets = new HashMap<>();
    	Release release = new Release();
    	
    	release.setBaseNamespace( releaseId.getBaseNamespace() );
    	release.setName( releaseId.getName() );
    	release.setVersion( releaseId.getVersion() );
    	release.setStatus( jaxbRelease.getStatus() );
    	release.setDescription( trimString( jaxbRelease.getDescription() ) );
    	release.setDefaultEffectiveDate( XMLGregorianCalendarConverter.
    			toJavaDate( jaxbRelease.getDefaultEffectiveDate() ) );
    	
    	if (jaxbRelease.getPrincipalMembers() != null) {
    		for (ReleaseMemberType jaxbMember : jaxbRelease.getPrincipalMembers().getReleaseMember()) {
    			release.getPrincipalMembers().add( transformToOtmReleaseMember( jaxbMember ) );
    		}
    	}
    	
    	if (jaxbRelease.getReferencedMembers() != null) {
    		for (ReleaseMemberType jaxbMember : jaxbRelease.getReferencedMembers().getReleaseMember()) {
    			release.getReferencedMembers().add( transformToOtmReleaseMember( jaxbMember ) );
    		}
    	}
    	
    	for (CompileOptionType jaxbOption : jaxbRelease.getCompileOption()) {
    		if ((jaxbOption.getKey() != null) && (jaxbOption.getValue() != null)) {
    			compileOptions.put( jaxbOption.getKey(), jaxbOption.getValue() );
    		}
    	}
    	release.setCompileOptions( new ReleaseCompileOptions( compileOptions ) );
    	
    	for (PreferredFacetType jaxbPF : jaxbRelease.getPreferredFacet()) {
    		QName ownerName = new QName( jaxbPF.getOwnerNamespace(), jaxbPF.getOwnerName() );
    		QName facetName = new QName( jaxbPF.getFacetNamespace(), jaxbPF.getFacetName() );
    		
    		preferredFacets.put( ownerName, facetName );
    	}
		release.setPreferredFacets( preferredFacets );
    	
    	return release;
    }
    
    /**
     * Transforms the given JAXB release member instance to an OTM release member.
     * 
     * @param jaxbItem  the JAXB release member instance to transform
     * @return ReleaseMember
     */
    private ReleaseMember transformToOtmReleaseMember(ReleaseMemberType jaxbMember) {
    	RepositoryItemImpl repoItem = new RepositoryItemImpl();
    	ReleaseMember item = new ReleaseMember();
    	
    	repoItem.setRepository( repositoryManager.getRepository( jaxbMember.getRepositoryID() ) );
    	repoItem.setBaseNamespace( jaxbMember.getBaseNamespace() );
    	repoItem.setNamespace( jaxbMember.getNamespace() );
    	repoItem.setFilename( jaxbMember.getFilename() );
    	repoItem.setLibraryName( jaxbMember.getLibraryName() );
    	repoItem.setVersion( jaxbMember.getVersion() );
    	item.setRepositoryItem( repoItem );
    	item.setEffectiveDate( XMLGregorianCalendarConverter.
    			toJavaDate( jaxbMember.getEffectiveDate() ) );
    	return item;
    }
    
    /**
     * Trims the given string.  If the resulting string is empty, null will be
     * returned.
     * 
     * @param str  the string to be trimmed
     * @return String
     */
    private String trimString(String str) {
    	String resultStr = null;
    	
    	if (str != null) {
    		resultStr = str.trim();
    		if (resultStr.length() == 0) resultStr = null;
    	}
    	return resultStr;
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
