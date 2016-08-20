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

import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryStatus;

/**
 * Indicates the lifecycle status of a <code>TLLibrary</code>.
 * 
 * @author S. Livezey
 */
public enum TLLibraryStatus {

    /**
     * Indicates that the contents of a library may be modified without increasing its version
     * number.
     */
    DRAFT(LibraryStatus.DRAFT, 10),

    /**
     * Indicates that the contents of a library is under review and pending finalization, and
     * may be modified only by certain repository users.
     */
    UNDER_REVIEW(LibraryStatus.UNDER_REVIEW, 20),

    /** Indicates that a new version must be created before modifying the content of a library. */
    FINAL(LibraryStatus.FINAL, 30),

    /** Indicates that the library version is obsolete and should not be used. */
    OBSOLETE(LibraryStatus.OBSOLETE, 40);

    private LibraryStatus repositoryStatus;
    private int rank;

    /**
     * Constructor that associates the corresponding repository status with the new value.
     * 
     * @param repositoryStatus  the associated repository status value
     * @param rank  the relative rank of this status value
     */
    private TLLibraryStatus(LibraryStatus repositoryStatus, int rank) {
        this.repositoryStatus = repositoryStatus;
        this.rank = rank;
    }

    /**
     * Returns the corresponding repository status for the value.
     * 
     * @return LibraryStatus
     */
    public LibraryStatus toRepositoryStatus() {
        return repositoryStatus;
    }
    
    /**
     * Returns the <code>TLLibraryStatus</code> that corresponds to the given
     * repository status value.
     * 
     * @param status  the repository status value
     * @return TLLibraryStatus
     */
    public static TLLibraryStatus fromRepositoryStatus(LibraryStatus status) {
    	TLLibraryStatus libraryStatus = null;
    	
    	if (status != null) {
    		switch (status) {
				case DRAFT:
					libraryStatus = TLLibraryStatus.DRAFT;
					break;
				case UNDER_REVIEW:
					libraryStatus = TLLibraryStatus.UNDER_REVIEW;
					break;
				case FINAL:
					libraryStatus = TLLibraryStatus.FINAL;
					break;
				case OBSOLETE:
					libraryStatus = TLLibraryStatus.OBSOLETE;
					break;
    		}
    	}
    	return libraryStatus;
    }
    
    /**
     * Returns the relative rank of this status value.
     * 
     * @return int
     */
    public int getRank() {
    	return rank;
    }
    
    /**
     * Returns the next available status in the lifecycle.  The OBSOLETE status
     * will return null since it is the last stage in the status lifecycle.
     * 
     * @return TLLibraryStatus
     */
    public TLLibraryStatus nextStatus() {
    	TLLibraryStatus targetStatus;
    	
    	switch (this) {
			case DRAFT:
				targetStatus = TLLibraryStatus.UNDER_REVIEW;
				break;
			case UNDER_REVIEW:
				targetStatus = TLLibraryStatus.FINAL;
				break;
			case FINAL:
				targetStatus = TLLibraryStatus.OBSOLETE;
				break;
			case OBSOLETE:
			default:
				targetStatus = null;
				break;
    	}
    	return targetStatus;
    }
    
    /**
     * Returns the previous status in the lifecycle.  The DRAFT status
     * will return null since it is the first stage in the status lifecycle.
     * 
     * @return TLLibraryStatus
     */
    public TLLibraryStatus previousStatus() {
    	TLLibraryStatus targetStatus;
    	
    	switch (this) {
			case DRAFT:
				targetStatus = null;
				break;
			case UNDER_REVIEW:
				targetStatus = TLLibraryStatus.DRAFT;
				break;
			case FINAL:
				targetStatus = TLLibraryStatus.UNDER_REVIEW;
				break;
			case OBSOLETE:
			default:
				targetStatus = TLLibraryStatus.FINAL;
				break;
    	}
    	return targetStatus;
    }
    
}
