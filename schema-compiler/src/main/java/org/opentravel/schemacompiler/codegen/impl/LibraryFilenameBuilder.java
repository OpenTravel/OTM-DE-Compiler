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
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Implementation of the <code>CodeGenerationFilenameBuilder</code> interface that can create
 * default filenames for the XML schema files associated with <code>AbstractLibrary</code>
 * instances.
 * 
 * @author S. Livezey
 */
public class LibraryFilenameBuilder<L extends AbstractLibrary> implements
        CodeGenerationFilenameBuilder<L> {

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(org.opentravel.schemacompiler.model.TLModelElement,
     *      java.lang.String)
     */
    @Override
    public String buildFilename(L item, String fileExtension) {
        String fileExt = (fileExtension.length() == 0) ? "" : ("." + fileExtension);
        String filename = item.getName();

        if (item instanceof TLLibrary) {
            filename += "_" + ((TLLibrary) item).getVersion().replaceAll("\\.", "_");
        }
        if (!filename.toLowerCase().endsWith(fileExt)) {
            filename += fileExt;
        }
        return filename;
    }

}
