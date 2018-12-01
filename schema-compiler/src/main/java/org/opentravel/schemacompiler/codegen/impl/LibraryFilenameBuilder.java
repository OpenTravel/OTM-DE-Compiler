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
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;

/**
 * Implementation of the <code>CodeGenerationFilenameBuilder</code> interface that can create
 * default filenames for the XML schema files associated with <code>AbstractLibrary</code>
 * instances.
 * 
 * @author S. Livezey
 */
public class LibraryFilenameBuilder<L extends AbstractLibrary> implements
        CodeGenerationFilenameBuilder<L> {
	
	private Map<L,String> baseFilenameMap;
	
    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(org.opentravel.schemacompiler.model.TLModelElement,java.lang.String)
     */
    @Override
    public String buildFilename(L item, String fileExtension) {
    	synchronized (this) {
    		if (baseFilenameMap == null) {
    			TLModel model = (item == null) ? null : item.getOwningModel();
    			
				baseFilenameMap = initBaseFilenames( model );
    		}
    	}
        String fileExt = (fileExtension.length() == 0) ? "" : ("." + fileExtension);
    	String filename = baseFilenameMap.get( item );
    	
    	if (filename == null) {
            filename = new FilenameDetails( item ).getFilename();
    	}
        if (!filename.toLowerCase().endsWith(fileExt)) {
            filename += fileExt;
        }
        return filename;
    }
    
    /**
     * Initializes the filenames that should be used for each library in the model.
     * 
     * @param model  the model that contains all libraries to which names should be assigned
     * @return Map<L,String>
     */
    @SuppressWarnings("unchecked")
	private Map<L,String> initBaseFilenames(TLModel model) {
    	Map<L,String> filenameMap = new HashMap<>();
    	
    	if (model != null) {
        	Set<FilenameDetails> filenameDetails = new HashSet<>();
        	boolean conflictsExist;
        	
        	// Build the initial list of filename details
        	for (AbstractLibrary library : model.getAllLibraries()) {
        		filenameDetails.add( new FilenameDetails( library ) );
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
            					fd.libraryFilename = fd.library.getName() + "_" + fd.nsComponents.remove( 0 );
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
        		filenameMap.put( (L) fd.library, fd.getFilename() );
        	}
    	}
    	return filenameMap;
    }
    
    /**
     * Encapsulates the various details of a library's filename (used during initialization).
     */
    private static class FilenameDetails {
    	
    	public AbstractLibrary library;
    	public List<String> nsComponents = new ArrayList<>();
    	public String libraryFilename;
    	public String versionSuffix;
    	
    	/**
    	 * Constructor that assigns the initial values for each component of the filename
    	 * details.
    	 * 
    	 * @param library  the library to which a filename will be assigned
    	 */
    	public FilenameDetails(AbstractLibrary library) {
    		String baseNS;
    		
    		this.library = library;
    		this.libraryFilename = library.getName();
    		
            if (library instanceof TLLibrary) {
            	TLLibrary tlLibrary = (TLLibrary) library;
            	
            	baseNS = tlLibrary.getBaseNamespace();
                this.versionSuffix = "_" + tlLibrary.getVersion().replaceAll("\\.", "_");
                
            } else {
            	baseNS = library.getNamespace();
            	this.versionSuffix = "";
            }
            
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
    		return libraryFilename + versionSuffix;
    	}
    	
    }
    
}
