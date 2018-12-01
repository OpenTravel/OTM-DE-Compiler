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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Filename builder for <code>TLResource</code> objects.
 */
public class ResourceFilenameBuilder implements CodeGenerationFilenameBuilder<TLResource> {
	
	private Map<TLResource,String> baseFilenameMap;
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(java.lang.Object, java.lang.String)
	 */
	@Override
	public String buildFilename(TLResource resource, String fileExtension) {
    	synchronized (this) {
    		if (baseFilenameMap == null) {
    			TLModel model = (resource == null) ? null : resource.getOwningModel();
    			
        		baseFilenameMap = initBaseFilenames( model );
    		}
    	}
        String fileExt = (fileExtension.length() == 0) ? "" : ("." + fileExtension);
    	String filename = baseFilenameMap.get( resource );
    	
    	if (filename == null) {
            filename = new FilenameDetails( resource ).getFilename();
    	}
        if (!filename.toLowerCase().endsWith(fileExt)) {
            filename += fileExt;
        }
        return filename;
	}
	
    /**
     * Initializes the filenames that should be used for each resource in the model.
     * 
     * @param model  the model that contains all resources to which names should be assigned
     * @return Map<TLResource,String>
     */
	private Map<TLResource,String> initBaseFilenames(TLModel model) {
		Map<TLResource,String> filenameMap = new HashMap<>();
		
		if (model != null) {
	    	Set<FilenameDetails> filenameDetails = new HashSet<>();
	    	boolean conflictsExist;
	    	
	    	// Build the initial list of filename details
	    	for (TLLibrary library : model.getUserDefinedLibraries()) {
	    		for (TLResource resource : library.getResourceTypes()) {
	        		filenameDetails.add( new FilenameDetails( resource ) );
	    		}
	    	}
	    	
	    	// Check for conflicts and continue attempting to resolve until no more
	    	// conflicts exist, or no further options are available.
	    	do {
	    		Map<String,List<FilenameDetails>> detailsByFilename = new HashMap<>();
	    		
	    		for (FilenameDetails fd : filenameDetails) {
	    			List<FilenameDetails> detailsList = detailsByFilename.get( fd.getFilename() );
	    			
	    			if (detailsList == null) {
	    				detailsList = new ArrayList<>();
	    				detailsByFilename.put( fd.getFilename(), detailsList );
	    			}
	    			detailsList.add( fd );
	    		}
	    		conflictsExist = false;
	    		
	    		for (List<FilenameDetails> detailsList : detailsByFilename.values()) {
	    			if (detailsList.size() > 1) {
	    				boolean changesMade = false;
	    				
	    				for (FilenameDetails fd : detailsList) {
	    					if (!fd.nsComponents.isEmpty()) {
	        					fd.resourceFilename = fd.resource.getName() + "_" + fd.nsComponents.remove( 0 );
	    						changesMade = true;
	    					}
	    				}
	    				
	    				// If no more namespace options are available, allow the conflict to exist.  In this
	    				// situation, there are other errors in the model that should not have allowed us to
	    				// get this far.  Exiting at this point will prevent us from getting stuck in an
	    				// infinite loop.
	    				conflictsExist |= changesMade;
	    			}
	    		}
	    		
	    	} while (conflictsExist);
	    	
	    	for (FilenameDetails fd : filenameDetails) {
	    		filenameMap.put( fd.resource, fd.getFilename() );
	    	}
		}
		return filenameMap;
    }
    
    /**
     * Encapsulates the various details of a resource's filename (used during initialization).
     */
    private static class FilenameDetails {
    	
    	public TLResource resource;
    	public List<String> nsComponents = new ArrayList<>();
    	public String resourceFilename;
    	public String versionSuffix;
    	
    	/**
    	 * Constructor that assigns the initial values for each component of the filename
    	 * details.
    	 * 
    	 * @param resource  the resource to which a filename will be assigned
    	 */
    	public FilenameDetails(TLResource resource) {
    		String baseNS = resource.getBaseNamespace();
    		
    		this.resource = resource;
    		this.resourceFilename = resource.getName();
            this.versionSuffix = "_" + resource.getVersion().replaceAll("\\.", "_");
    		
            if (baseNS.endsWith("/")) {
            	baseNS = baseNS.substring( 0, baseNS.length() - 1 );
            }
            this.nsComponents = new ArrayList<>( Arrays.asList( baseNS.split( "/" ) ) );
            Collections.reverse( this.nsComponents );
    	}
    	
    	/**
    	 * Returns the filename as currently specified by these details.
    	 * 
    	 * @return String
    	 */
    	public String getFilename() {
    		return resourceFilename + versionSuffix;
    	}
    	
    }
    
}
