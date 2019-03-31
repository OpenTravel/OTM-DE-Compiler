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

package org.opentravel.schemacompiler.transform.tl2jaxb16;

import org.opentravel.ns.ota2.librarymodel_v01_06.Folder;
import org.opentravel.ns.ota2.librarymodel_v01_06.FolderItem;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLFolder;

/**
 * Handles the transformation of objects from the <code>TLFolder</code> type to the <code>Folder</code> type.
 */
public class TLFolderTransformer extends TLComplexTypeTransformer<TLFolder,Folder> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public Folder transform(TLFolder source) {
        Folder target = new Folder();

        target.setName( source.getName() );

        // Create the member entities for this folder
        for (LibraryMember entity : source.getEntities()) {
            FolderItem item = new FolderItem();

            item.setValue( entity.getLocalName() );
            target.getFolderItem().add( item );
        }

        // Recursively create the sub-folder structure
        for (TLFolder subFolder : source.getFolders()) {
            target.getFolder().add( transform( subFolder ) );
        }

        return target;
    }

}
