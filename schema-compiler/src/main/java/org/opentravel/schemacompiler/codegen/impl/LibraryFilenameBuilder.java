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

package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.swagger.SwaggerCodeGenerator;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the <code>CodeGenerationFilenameBuilder</code> interface that can create default filenames for the
 * XML schema files associated with <code>AbstractLibrary</code> instances.
 * 
 * @author S. Livezey
 */
public class LibraryFilenameBuilder<L extends AbstractLibrary> implements CodeGenerationFilenameBuilder<L> {

    private Map<L,String> baseFilenameMap;

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    public String buildFilename(L item, String fileExtension) {
        synchronized (this) {
            if (baseFilenameMap == null) {
                TLModel model = (item == null) ? null : item.getOwningModel();

                baseFilenameMap = initBaseFilenames( model, fileExtension );
            }
        }
        String fileExt = (fileExtension.length() == 0) ? "" : ("." + fileExtension);
        String filename = baseFilenameMap.get( item );

        if (filename == null) {
            filename = new FilenameDetails( item, fileExtension ).getFilename();
        }
        if (!filename.toLowerCase().endsWith( fileExt )) {
            filename += fileExt;
        }
        return filename;
    }

    /**
     * Returns the version string to use when constructing a library filename. For JSON schemas and Swagger/OpenAPI
     * files, only the major version is used. For all other file types, the full three-part (major/minor/patch) version
     * is used.
     * 
     * @param library the library for which to return a filename version identifier
     * @param fileExtension the file extension that indicates the type of file being created
     * @return String
     */
    protected static String getLibraryFilenameVersion(AbstractLibrary library, String fileExtension) {
        String filenameVersion = "";

        if (library instanceof TLLibrary) {
            String libraryVersion = ((TLLibrary) library).getVersion();

            if (isJsonSchema( fileExtension )) {
                try {
                    VersionScheme versionScheme =
                        VersionSchemeFactory.getInstance().getVersionScheme( library.getVersionScheme() );

                    filenameVersion = versionScheme.getMajorVersion( libraryVersion );

                } catch (VersionSchemeException e) {
                    filenameVersion = libraryVersion.replaceAll( "\\.", "_" );
                }

            } else {
                filenameVersion = libraryVersion.replaceAll( "\\.", "_" );
            }
        }
        return filenameVersion;
    }

    /**
     * Returns true if the given file extension is associated with a JSON schema file or Swagger/OpenAPI specification.
     * 
     * @param fileExtension the file extension that indicates the type of file being created
     * @return boolean
     */
    protected static boolean isJsonSchema(String fileExtension) {
        return (fileExtension != null) && (fileExtension.endsWith( JsonSchemaCodegenUtils.JSON_SCHEMA_FILENAME_EXT )
            || fileExtension.endsWith( SwaggerCodeGenerator.SWAGGER_FILENAME_EXT ));
    }

    /**
     * Initializes the filenames that should be used for each library in the model.
     * 
     * @param model the model that contains all libraries to which names should be assigned
     * @param fileExtension the file extension that indicates the type of file being created
     * @return Map&lt;L,String&gt;
     */
    @SuppressWarnings("unchecked")
    private Map<L,String> initBaseFilenames(TLModel model, String fileExtension) {
        Map<L,String> filenameMap = new HashMap<>();

        if (model != null) {
            Set<FilenameDetails> filenameDetails = new HashSet<>();
            List<AbstractLibrary> allLibraries = new ArrayList<>();

            // Construct a list of all libraries
            if (isJsonSchema( fileExtension )) {
                for (AbstractLibrary library : model.getAllLibraries()) {
                    boolean includeLib = !(library instanceof TLLibrary)
                        || JsonSchemaCodegenUtils.isLatestMinorVersion( (TLLibrary) library );

                    if (includeLib) {
                        allLibraries.add( library );
                    }
                }

            } else {
                allLibraries.addAll( model.getAllLibraries() );
            }

            // Build the initial list of filename details
            for (AbstractLibrary library : allLibraries) {
                filenameDetails.add( new FilenameDetails( library, fileExtension ) );
            }

            // Check for conflicts and continue attempting to resolve until no
            // more conflicts exist, or no further options are available.
            checkAndResolveConflicts( filenameDetails );

            for (FilenameDetails fd : filenameDetails) {
                filenameMap.put( (L) fd.getLibrary(), fd.getFilename() );
            }
        }
        return filenameMap;
    }

    /**
     * Checks the given list for conflicting filenames and resolves the conflicts if any exist.
     * 
     * @param filenameDetails the list of filename details for which to resolve conflicts
     */
    private void checkAndResolveConflicts(Set<FilenameDetails> filenameDetails) {
        boolean conflictsExist;

        do {
            Map<String,List<FilenameDetails>> detailsByFilename = buildFilenameMap( filenameDetails );

            conflictsExist = false;

            for (List<FilenameDetails> detailsList : detailsByFilename.values()) {
                if (detailsList.size() > 1) {
                    boolean changesMade = false;

                    for (FilenameDetails fd : detailsList) {
                        if (!fd.getNsComponents().isEmpty()) {
                            fd.setLibraryFilename( fd.getLibrary().getName() + "_" + fd.getNsComponents().remove( 0 ) );
                            changesMade = true;
                        }
                    }

                    // If no more namespace options are available, allow the conflict to exist. In this
                    // situation, there are other errors in the model that should not have allowed us to
                    // get this far. Exiting at this point will prevent us from getting stuck in an
                    // infinite loop.
                    conflictsExist |= changesMade;
                }
            }

        } while (conflictsExist);
    }

    /**
     * Builds a map that associates each filename with the filename details from the set provided.
     * 
     * @param filenameDetails the set of filename details
     * @return Map&lt;String, List&lt;FilenameDetails&gt;&gt;
     */
    private Map<String,List<FilenameDetails>> buildFilenameMap(Set<FilenameDetails> filenameDetails) {
        Map<String,List<FilenameDetails>> detailsByFilename;
        detailsByFilename = new HashMap<>();

        for (FilenameDetails fd : filenameDetails) {
            detailsByFilename.computeIfAbsent( fd.getFilename(), fn -> new ArrayList<>() ).add( fd );
        }
        return detailsByFilename;
    }

    /**
     * Encapsulates the various details of a library's filename (used during initialization).
     */
    private static class FilenameDetails {

        private AbstractLibrary library;
        private List<String> nsComponents = new ArrayList<>();
        private String libraryFilename;
        private String versionSuffix;

        /**
         * Constructor that assigns the initial values for each component of the filename details.
         * 
         * @param library the library to which a filename will be assigned
         * @param fileExtension the file extension that indicates the type of file being created
         */
        public FilenameDetails(AbstractLibrary library, String fileExtension) {
            String baseNS;

            this.setLibrary( library );
            this.setLibraryFilename( library.getName() );

            if (library instanceof TLLibrary) {
                TLLibrary tlLibrary = (TLLibrary) library;

                baseNS = tlLibrary.getBaseNamespace();
                this.setVersionSuffix( "_" + getLibraryFilenameVersion( tlLibrary, fileExtension ) );

            } else {
                baseNS = library.getNamespace();
                this.setVersionSuffix( "" );
            }

            if (baseNS.endsWith( "/" )) {
                baseNS = baseNS.substring( 0, baseNS.length() - 1 );
            }
            this.setNsComponents( new ArrayList<>( Arrays.asList( baseNS.split( "/" ) ) ) );
            Collections.reverse( this.getNsComponents() );
        }

        /**
         * Returns the filename as currently specified by these details.
         * 
         * @return String
         */
        public String getFilename() {
            return getLibraryFilename() + getVersionSuffix();
        }

        /**
         * Returns the value of the 'library' field.
         *
         * @return AbstractLibrary
         */
        public AbstractLibrary getLibrary() {
            return library;
        }

        /**
         * Assigns the value of the 'library' field.
         *
         * @param library the field value to assign
         */
        public void setLibrary(AbstractLibrary library) {
            this.library = library;
        }

        /**
         * Returns the value of the 'nsComponents' field.
         *
         * @return List&lt;String&gt;
         */
        public List<String> getNsComponents() {
            return nsComponents;
        }

        /**
         * Assigns the value of the 'nsComponents' field.
         *
         * @param nsComponents the field value to assign
         */
        public void setNsComponents(List<String> nsComponents) {
            this.nsComponents = nsComponents;
        }

        /**
         * Returns the value of the 'libraryFilename' field.
         *
         * @return String
         */
        public String getLibraryFilename() {
            return libraryFilename;
        }

        /**
         * Assigns the value of the 'libraryFilename' field.
         *
         * @param libraryFilename the field value to assign
         */
        public void setLibraryFilename(String libraryFilename) {
            this.libraryFilename = libraryFilename;
        }

        /**
         * Returns the value of the 'versionSuffix' field.
         *
         * @return String
         */
        public String getVersionSuffix() {
            return versionSuffix;
        }

        /**
         * Assigns the value of the 'versionSuffix' field.
         *
         * @param versionSuffix the field value to assign
         */
        public void setVersionSuffix(String versionSuffix) {
            this.versionSuffix = versionSuffix;
        }

    }

}
