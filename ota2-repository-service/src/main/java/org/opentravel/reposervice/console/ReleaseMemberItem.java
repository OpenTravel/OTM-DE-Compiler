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

package org.opentravel.reposervice.console;

import org.opentravel.repocommon.index.LibrarySearchResult;

import java.util.Date;

/**
 * Encapsulates all relevant information for a release member to be displayed on the UI.
 */
public class ReleaseMemberItem {

    private LibrarySearchResult library;
    private Date effectiveDate;

    /**
     * Constructor that specifies the member library and the effective date.
     * 
     * @param library the library member
     * @param effectiveDate the effective date of the member (may be null)
     */
    public ReleaseMemberItem(LibrarySearchResult library, Date effectiveDate) {
        this.library = library;
        this.effectiveDate = effectiveDate;
    }

    /**
     * Returns the the library member.
     *
     * @return LibrarySearchResult
     */
    public LibrarySearchResult getLibrary() {
        return library;
    }

    /**
     * Returns the effective date of the member (may be null).
     *
     * @return Date
     */
    public Date getEffectiveDate() {
        return effectiveDate;
    }

}
