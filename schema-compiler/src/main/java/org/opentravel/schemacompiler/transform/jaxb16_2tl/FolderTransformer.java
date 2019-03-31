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

package org.opentravel.schemacompiler.transform.jaxb16_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_06.Folder;
import org.opentravel.ns.ota2.librarymodel_v01_06.FolderItem;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLFolder;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Handles the transformation of objects from the <code>Folder</code> type to the <code>TLFolder</code> type.
 */
public class FolderTransformer extends ComplexTypeTransformer<Folder,TLFolder> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLFolder transform(Folder source) {
        TLLibrary targetLibrary =
            (TLLibrary) context.getContextCacheEntry( LibraryTransformer.TARGET_LIBRARY_CONTEXT_ID );
        TLFolder target = newFolder( source, targetLibrary );

        // Assign the member entities to this folder
        for (FolderItem sourceItem : source.getFolderItem()) {
            String entityName = sourceItem.getValue();
            LibraryMember entity = targetLibrary.getNamedMember( entityName );

            if (entity != null) {
                try {
                    target.addEntity( entity );

                } catch (IllegalArgumentException e) {
                    // Ignore and skip this entity
                }
            }
        }

        // Recursively build the folder structure
        for (Folder subFolder : source.getFolder()) {
            target.addFolder( transform( subFolder ) );
        }

        return target;
    }

    /**
     * Constructs a new library folder using the JAXB source folder provided. If the new folder's name is invalid, a
     * dummy name will be assigned to the resulting folder.
     * 
     * @param folder the source folder from which to create the new library folder
     * @param targetLibrary the owning library for the new folder
     * @return TLFolder
     */
    private TLFolder newFolder(Folder folder, TLLibrary targetLibrary) {
        TLFolder targetFolder;

        try {
            targetFolder = new TLFolder( folder.getName(), targetLibrary );

        } catch (IllegalArgumentException e) {
            targetFolder = new TLFolder( "invalid-folder-name", targetLibrary );
        }
        return targetFolder;
    }
}
