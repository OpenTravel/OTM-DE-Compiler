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

/**
 * Filename builder used to generate filenames for legacy schema extension files produced during the
 * code generation process.
 * 
 * @param <T>
 *            the type of model element for which filenames can be generated
 * @author S. Livezey
 */
public class LegacySchemaExtensionFilenameBuilder<T> implements CodeGenerationFilenameBuilder<T> {

    private CodeGenerationFilenameBuilder<T> delegateBuilder;

    /**
     * Constructor that specifies the builder to use when generating the base filename from which
     * the extension schema's filename will be derived.
     * 
     * @param filenameBuilder
     *            the base filename builder to assign
     */
    public LegacySchemaExtensionFilenameBuilder(CodeGenerationFilenameBuilder<T> filenameBuilder) {
        this.delegateBuilder = filenameBuilder;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    public String buildFilename(T item, String fileExtension) {
        String fileExt = (fileExtension.length() == 0) ? "" : ("." + fileExtension);
        String baseFilename = delegateBuilder.buildFilename(item, "");

        return baseFilename + "_Ext" + fileExt;
    }

}
