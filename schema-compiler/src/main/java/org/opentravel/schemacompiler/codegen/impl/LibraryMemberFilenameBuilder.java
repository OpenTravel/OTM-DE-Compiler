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
import org.opentravel.schemacompiler.model.LibraryMember;

/**
 * Implementation of the <code>CodeGenerationFilenameBuilder</code> interface that can create default filenames for
 * <code>LibraryMember</code> entities.
 * 
 * @param <S> the soure type for which code is being generated
 * @author S. Livezey
 */
public class LibraryMemberFilenameBuilder<S extends LibraryMember> implements CodeGenerationFilenameBuilder<S> {

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    public String buildFilename(LibraryMember item, String fileExtension) {
        String fileExt = ((fileExtension == null) || (fileExtension.length() == 0)) ? "" : ("." + fileExtension);
        String filename = item.getLocalName();

        if (!filename.toLowerCase().endsWith( fileExt )) {
            filename += fileExt;
        }
        return filename;
    }

}
