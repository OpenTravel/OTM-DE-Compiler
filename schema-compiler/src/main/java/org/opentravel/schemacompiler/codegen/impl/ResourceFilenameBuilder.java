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
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Filename builder for <code>TLResource</code> objects.
 */
public class ResourceFilenameBuilder implements CodeGenerationFilenameBuilder<TLResource> {

    private Map<TLResource,String> baseFilenameMap;

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    public String buildFilename(TLResource resource, String fileExtension) {
        synchronized (this) {
            if (baseFilenameMap == null) {
                TLModel model = (resource == null) ? null : resource.getOwningModel();

                baseFilenameMap = initBaseFilenames( model, fileExtension );
            }
        }
        String fileExt = (fileExtension.length() == 0) ? "" : ("." + fileExtension);
        String filename = baseFilenameMap.get( resource );

        if (filename == null) {
            filename = new FilenameDetails( resource, fileExtension ).getFilename();
        }
        if (!filename.toLowerCase().endsWith( fileExt )) {
            filename += fileExt;
        }
        return filename;
    }

    /**
     * Initializes the filenames that should be used for each resource in the model.
     * 
     * @param model the model that contains all resources to which names should be assigned
     * @param fileExtension the file extension that indicates the type of file being created
     * @return Map&lt;TLResource,String&gt;
     */
    private Map<TLResource,String> initBaseFilenames(TLModel model, String fileExtension) {
        Map<TLResource,String> filenameMap = new HashMap<>();

        if (model != null) {
            Set<FilenameDetails> filenameDetails = buildFilenameDetails( model, fileExtension );

            // Check for conflicts and continue attempting to resolve until no
            // more conflicts exist, or no further options are available.
            checkAndResolveConflicts( filenameDetails );

            for (FilenameDetails fd : filenameDetails) {
                filenameMap.put( fd.getResource(), fd.getFilename() );
            }
        }
        return filenameMap;
    }

    /**
     * Checks each of the filename details provided for naming conflicts and resolves those conflicts if any exist.
     * 
     * @param filenameDetails the filename details to check
     */
    private void checkAndResolveConflicts(Set<FilenameDetails> filenameDetails) {
        boolean conflictsExist;
        do {
            Map<String,List<FilenameDetails>> detailsByFilename;

            detailsByFilename = buildFilenameMap( filenameDetails );
            conflictsExist = false;

            for (List<FilenameDetails> detailsList : detailsByFilename.values()) {
                if (detailsList.size() > 1) {
                    boolean changesMade = false;

                    for (FilenameDetails fd : detailsList) {
                        if (!fd.getNsComponents().isEmpty()) {
                            fd.setResourceFilename(
                                fd.getResource().getName() + "_" + fd.getNsComponents().remove( 0 ) );
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
     * Constructs a map that associates each filename with the details of that file.
     * 
     * @param filenameDetails the collection of filename details to process
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
     * Builds a set that contains filename information for all resources in the given model.
     * 
     * @param model the model for which to create filename details
     * @param fileExtension the file extension that indicates the type of file being created
     * @return Set&lt;FilenameDetails&gt;
     */
    private Set<FilenameDetails> buildFilenameDetails(TLModel model, String fileExtension) {
        Set<FilenameDetails> filenameDetails;
        filenameDetails = new HashSet<>();

        for (TLLibrary library : model.getUserDefinedLibraries()) {
            for (TLResource resource : library.getResourceTypes()) {
                if (JsonSchemaCodegenUtils.isLatestMinorVersion( resource )) {
                    filenameDetails.add( new FilenameDetails( resource, fileExtension ) );
                }
            }
        }
        return filenameDetails;
    }

    /**
     * Encapsulates the various details of a resource's filename (used during initialization).
     */
    private static class FilenameDetails {

        private TLResource resource;
        private List<String> nsComponents = new ArrayList<>();
        private String resourceFilename;
        private String versionSuffix;

        /**
         * Constructor that assigns the initial values for each component of the filename details.
         * 
         * @param resource the resource to which a filename will be assigned
         * @param fileExtension the file extension that indicates the type of file being created
         */
        public FilenameDetails(TLResource resource, String fileExtension) {
            String baseNS = resource.getBaseNamespace();

            this.setResource( resource );
            this.setResourceFilename( resource.getName() );
            this.setVersionSuffix(
                "_" + LibraryFilenameBuilder.getLibraryFilenameVersion( resource.getOwningLibrary(), fileExtension ) );

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
            return getResourceFilename() + getVersionSuffix();
        }

        /**
         * Returns the value of the 'resource' field.
         *
         * @return TLResource
         */
        public TLResource getResource() {
            return resource;
        }

        /**
         * Assigns the value of the 'resource' field.
         *
         * @param resource the field value to assign
         */
        public void setResource(TLResource resource) {
            this.resource = resource;
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
         * Returns the value of the 'resourceFilename' field.
         *
         * @return String
         */
        public String getResourceFilename() {
            return resourceFilename;
        }

        /**
         * Assigns the value of the 'resourceFilename' field.
         *
         * @param resourceFilename the field value to assign
         */
        public void setResourceFilename(String resourceFilename) {
            this.resourceFilename = resourceFilename;
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
