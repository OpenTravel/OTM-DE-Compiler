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

import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Base class for all <code>ObjectTransformer</code> implementations that are part of the code
 * generation subsystem.
 * 
 * @param <S>
 *            the source type of the object transformation
 * @param <T>
 *            the target type of the object transformation
 * @author S. Livezey
 */
public abstract class AbstractCodegenTransformer<S, T> extends
        BaseTransformer<S, T, CodeGenerationTransformerContext> {

    /**
     * Returns the sub-folder location (relative to the target output folder) where built-in schemas
     * should be stored during the code generation process. If no sub-folder location is specified
     * by the code generation context, this method will return an empty string, indicating that
     * built-ins schemas should be saved in the same target output folder as the user-defined
     * library/service output.
     * 
     * @return String
     */
    protected String getBuiltInSchemaOutputLocation() {
        return XsdCodegenUtils.getBuiltInSchemaOutputLocation(context.getCodegenContext());
    }

    /**
     * Returns the sub-folder location (relative to the target output folder) where legacy schemas
     * should be stored during the code generation process. If no sub-folder location is specified
     * by the code generation context, this method will return an empty string, indicating that
     * legacy schemas should be saved in the same target output folder as the user-defined
     * library/service output.
     * 
     * @return String
     */
    protected String getLegacySchemaOutputLocation() {
        return XsdCodegenUtils.getLegacySchemaOutputLocation(context.getCodegenContext());
    }

}
