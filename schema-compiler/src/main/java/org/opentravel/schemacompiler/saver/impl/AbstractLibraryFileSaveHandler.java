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
package org.opentravel.schemacompiler.saver.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;

import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.impl.LibraryValidationSource;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.saver.LibrarySaveHandler;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.xml.LibraryLineBreakProcessor;
import org.opentravel.schemacompiler.xml.NamespacePrefixMapper;
import org.opentravel.schemacompiler.xml.XMLPrettyPrinter;
import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default implementation that saves JAXB library content as a file on the local file system.
 * 
 * @param <T>  the target type for the library format to be processed by this save handler
 * @author S. Livezey
 */
public abstract class AbstractLibraryFileSaveHandler<T> implements LibrarySaveHandler<T> {

    private static final String VALIDATION_MESSAGE_KEY = "org.opentravel.schemacompiler.TLLibrary.jaxbValidationWarning";

    private static final Map<String, String> preferredPrefixMappings;
    private static final String[] schemaDeclarations = new String[] {
        XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
    };

    private boolean createBackupFile = true;

    /**
     * @see org.opentravel.schemacompiler.saver.LibrarySaveHandler#canSave(java.net.URL)
     */
    @Override
    public boolean canSave(URL libraryUrl) {
        return URLUtils.isFileURL(libraryUrl);
    }

    /**
     * @see org.opentravel.schemacompiler.saver.LibrarySaveHandler#validateLibraryContent(java.lang.Object)
     */
    @Override
    public ValidationFindings validateLibraryContent(T library) {
        ValidationFindings findings = new ValidationFindings();
        try {
            JAXBElement<T> documentElement = createLibraryElement(library);
            Marshaller marshaller = getJaxbContext().createMarshaller();

            marshaller.setSchema(getValidationSchema());
            marshaller.marshal(documentElement, new DefaultHandler()); // effectively marshalls to
                                                                       // dev/null output

        } catch (JAXBException e) {
            findings.addFinding(FindingType.WARNING, new LibraryValidationSource(library),
                    VALIDATION_MESSAGE_KEY, getExceptionMessage(e));
        }
        return findings;
    }

    /**
     * @see org.opentravel.schemacompiler.saver.LibrarySaveHandler#saveLibraryContent(java.net.URL, java.lang.Object)
     */
    @Override
    public void saveLibraryContent(URL libraryUrl, T library) throws LibrarySaveException {
        File libraryFile = getFileForURL(libraryUrl);
        File backupFile = createBackupFile ? createBackupFile(libraryFile) : null;
        boolean success = false;

        try (OutputStream out = new FileOutputStream(libraryFile)){
            JAXBElement<T> documentElement = createLibraryElement(library);
            Marshaller marshaller = getJaxbContext().createMarshaller();
            Document domDocument = XMLPrettyPrinter.newDocument();

            // Marshall the JAXB content
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                    new LibrarySaveNamespacePrefixMapper());
            marshaller.setProperty("jaxb.schemaLocation", getLibrarySchemaLocation());
            marshaller.marshal(documentElement, domDocument); // no schema validation during file-save marshalling

            // Format the XML before saving it to a file
            new XMLPrettyPrinter(new LibraryLineBreakProcessor()).formatDocument(domDocument, out);
            success = true;

        } catch (IllegalArgumentException | JAXBException | IOException e) {
            throw new LibrarySaveException(e);

        } finally {
            if (!success && (backupFile != null)) {
                restoreBackupFile(backupFile, libraryFile);
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.saver.LibrarySaveHandler#isCreateBackupFile()
     */
    @Override
    public boolean isCreateBackupFile() {
        return createBackupFile;
    }

    /**
     * @see org.opentravel.schemacompiler.saver.LibrarySaveHandler#setCreateBackupFile(boolean)
     */
    @Override
    public void setCreateBackupFile(boolean createBackupFile) {
        this.createBackupFile = createBackupFile;
    }
    
    /**
     * Returns the target namespace of the library file.
     * 
     * @return String
     */
    protected abstract String getLibraryTargetNamespace();
    
    /**
     * Returns the <code>JAXBContext</code> to use when saving library files.
     * 
     * @return JAXBContext
     */
    protected abstract JAXBContext getJaxbContext();
    
    /**
     * Returns a <code>JAXBElement</code> wrapper for the given JAXB library instance.
     * 
     * @param library  the JAXB library for which to return an element wrapper
     * @return JAXBElement<T>
     */
    protected abstract JAXBElement<T> createLibraryElement(T library);
    
    /**
     * Returns the validation schema for the target library file format.
     * 
     * @return Schema
     */
    protected abstract Schema getValidationSchema();
    
    /**
     * Returns the XSI schema location declaration for the target library format.
     * 
     * @return String
     */
    protected abstract String getLibrarySchemaLocation();
    
    /**
     * If the indicated 'libraryFile' already exists, the existing file will be renamed with a
     * ".bak" extension. If a backup file already exists, it will be deleted. If a backup file is
     * created by this method, it will be returned to the caller. If no backup is required, null
     * will be returned.
     * 
     * @param libraryFile
     *            the handle for the library file to backup
     * @return File
     */
    protected File createBackupFile(File libraryFile) {
        File backupFile = null;

        if (libraryFile.exists()) {
            String filename = libraryFile.getName();
            int dotIdx = filename.lastIndexOf('.');

            if (dotIdx >= 0) {
                filename = filename.substring(0, dotIdx);
            }
            backupFile = new File(libraryFile.getParentFile(), filename + ".bak");
            FileUtils.delete( backupFile );
            FileUtils.renameTo( libraryFile, backupFile );

            if (libraryFile.exists()) {
                // Something went wrong - attempt to backup failed for some reason
                backupFile = null;
            }
        }
        return backupFile;
    }

    /**
     * Restores the indicated backup file by renaming the backup to match the name of the specified
     * library file. If the 'backupFile' is null, this method will return without action.
     * 
     * @param backupFile
     *            the backup file to restore
     * @param libraryFile
     *            the library file that was previously backed up
     */
    protected void restoreBackupFile(File backupFile, File libraryFile) {
        if ((backupFile != null) && backupFile.exists() && (libraryFile != null)) {
        		FileUtils.delete( libraryFile );
        		FileUtils.renameTo( backupFile, libraryFile );
        }
    }

    /**
     * Returns a file handle for the URL provided. If the URL does not represent a location on the
     * local host's file system, an <code>IllegalArgumentException</code> will be thrown by this
     * method.
     * 
     * @param libraryUrl
     *            the URL to convert
     * @return File
     * @throws IllegalArgumentException
     *             thrown if the URL is not a location on the local file system
     */
    protected File getFileForURL(URL libraryUrl) {
        return URLUtils.toFile(libraryUrl);
    }

    /**
     * Returns the first non-null message from the given exception or one of its nested caused-by
     * exceptions.
     * 
     * @param e
     *            the exception for which to return a user-displayable message
     * @return String
     */
    private String getExceptionMessage(JAXBException e) {
        String message = null;
        Throwable t = e;

        while ((t != null) && (message == null)) {
            message = t.getMessage();
            t = t.getCause();
        }
        return message;
    }

    /**
     * Prefix mapper used to publish namespace and schema location declarations for the XML output.
     * 
     * @author S. Livezey
     */
    private class LibrarySaveNamespacePrefixMapper extends NamespacePrefixMapper {
    	
    	private String[] schemaDecls;
    	
    	public LibrarySaveNamespacePrefixMapper() {
    		List<String> decls = new ArrayList<>();
    		
    		decls.addAll( Arrays.asList( schemaDeclarations ) );
    		decls.add( getLibraryTargetNamespace() );
    		schemaDecls = decls.toArray( new String[ decls.size() ] );
    	}
        /**
         * @see com.sun.xml.bind.marshaller.NamespacePrefixMapper#getPreferredPrefix(java.lang.String,
         *      java.lang.String, boolean)
         */
        @Override
        public String getPreferredPrefix(String namespaceUri, String suggestion,
                boolean requirePrefix) {
            String prefix = preferredPrefixMappings.get(namespaceUri);
            return (prefix == null) ? suggestion : prefix;
        }

        /**
         * @see com.sun.xml.bind.marshaller.NamespacePrefixMapper#getPreDeclaredNamespaceUris()
         */
        @Override
        public String[] getPreDeclaredNamespaceUris() {
            return schemaDecls;
        }

    }

    /**
     * Initializes the preferred prefix mappings for the JAXB <code>NamespacePrefixMapper</code>.
     */
    static {
        try {
            Map<String,String> prefixMappings = new HashMap<>();

            prefixMappings.put(SchemaDeclarations.OTA2_LIBRARY_SCHEMA_1_5.getNamespace(), "");
            prefixMappings.put(XMLConstants.W3C_XML_SCHEMA_NS_URI, "xsd");
            prefixMappings.put(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi");
            preferredPrefixMappings = Collections.unmodifiableMap(prefixMappings);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
