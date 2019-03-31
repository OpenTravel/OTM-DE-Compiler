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

package org.opentravel.schemacompiler.diff;

import org.opentravel.schemacompiler.repository.Release;

/**
 * Container for all change items identified during the comparison of two releases, as well as the library change sets
 * for the libraries that existed in both versions of the release.
 */
public class ReleaseChangeSet extends ChangeSet<Release,ReleaseChangeItem> {

    /**
     * Constructor that assigns the old and new version of a release that was modified.
     * 
     * @param oldRelease the old version of the release
     * @param newRelease the new version of the release
     */
    public ReleaseChangeSet(Release oldRelease, Release newRelease) {
        super( oldRelease, newRelease );
    }

    /**
     * @see org.opentravel.schemacompiler.diff.ChangeSet#getBookmarkId()
     */
    public String getBookmarkId() {
        Release release = (getNewVersion() != null) ? getNewVersion() : getOldVersion();
        return (release == null) ? "UNKNOWN_PROJECT" : ("rel$" + release.getName());
    }

}
