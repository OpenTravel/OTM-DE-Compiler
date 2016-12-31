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

package org.opentravel.schemacompiler.model;

/**
 * Used in patch libraries to override the existing contents of a documentation owner.
 */
public class TLDocumentationPatch extends TLLibraryMember {
	
    private String patchedVersion;
    private String docPath;
    private TLDocumentation documentation;
    
	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        StringBuilder identity = new StringBuilder();
        String localName = getLocalName();

        if (owningLibrary != null) {
            identity.append(owningLibrary.getValidationIdentity()).append(" : ");
        }
        if (localName == null) {
            identity.append("[Unnamed Documentation Patch]");
        } else {
            identity.append(localName);
        }
        return identity.toString();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
	 */
	@Override
	public String getLocalName() {
        StringBuilder localName = new StringBuilder();
        
    	localName.append( (patchedVersion != null) ? patchedVersion : "Unknown_Patch_Version" ).append('/');
    	localName.append( (docPath != null) ? docPath : "Unknown_Path" );
        return localName.toString();
	}
	
    /**
	 * Returns the version of the library that contains the patched documentation owner.
	 *
	 * @return String
	 */
	public String getPatchedVersion() {
		return patchedVersion;
	}

	/**
	 * Assigns the version of the library that contains the patched documentation owner.
	 *
	 * @param patchedVersion  the library version to assign
	 */
	public void setPatchedVersion(String patchedVersion) {
		this.patchedVersion = patchedVersion;
	}

	/**
	 * Returns the path of the documentation owner within the patched library.
	 *
	 * @return String
	 */
	public String getDocPath() {
		return docPath;
	}

	/**
	 * Assigns the path of the documentation owner within the patched library.
	 *
	 * @param docPath  the documentation path to assign
	 */
	public void setDocPath(String docPath) {
		this.docPath = docPath;
	}

	/**
     * Returns the documentation instance for this patch.
     * 
     * @return TLDocumentation
     */
    public TLDocumentation getDocumentation() {
    	return documentation;
    }
    
    /**
     * Assigns the documentation instance for this patch.
     * 
     * @param documentation  the documentation instance to assign
     */
    public void setDocumentation(TLDocumentation documentation) {
    	this.documentation = documentation;
    }
    
}
