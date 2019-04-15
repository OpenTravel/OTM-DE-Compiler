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

package org.opentravel.schemacompiler.repository;

/**
 * Indicates the type of managed artifact from the OTM repository. Used for filtering searches and listings of
 * repository items.
 */
public enum RepositoryItemType {

    LIBRARY(".otm"),

    RELEASE(".otr"),

    ASSEMBLY(".osm");

    private String fileExtension;

    /**
     * Constructor that specifies the file extension associated with the repository item type.
     * 
     * @param fileExtension the file extension used for all artifacts of this type
     */
    private RepositoryItemType(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Returns true if the given repository item matches this item type.
     * 
     * @param itemFilename the filename of the item to check
     * @return boolean
     */
    public boolean isItemType(String itemFilename) {
        boolean result = false;

        if (itemFilename != null) {
            result = itemFilename.toLowerCase().endsWith( fileExtension );
        }
        return result;
    }

    /**
     * Returns the repository item type associated with the given filename. If the filename does not correspond to a
     * know item type, this method will return null.
     * 
     * @param itemFilename the filename for which to return a repository item type
     * @return RepositoryItemType
     */
    public static RepositoryItemType fromFilename(String itemFilename) {
        RepositoryItemType itemType = null;

        for (RepositoryItemType it : values()) {
            if (it.isItemType( itemFilename )) {
                itemType = it;
                break;
            }
        }
        return itemType;
    }

}
