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

package org.opentravel.schemacompiler.codegen;

import org.opentravel.schemacompiler.validate.ValidationException;

import java.io.File;
import java.util.Collection;

/**
 * Orchestrates the code generation process by transforming the compiler's meta-model objects into an external
 * representation (e.g. XSD, WSDL, etc.).
 * 
 * <p>
 * NOTE: Code generators assume that the model content has been determined to be error-free prior to calling the
 * 'generateOutput()' method. The code generator itself does not perform any validation checking.
 * 
 * @param <S> the source type for which output content will be generated
 * @author S. Livezey
 */
public interface CodeGenerator<S> {

    /**
     * Performs all tasks necessary to generate the output specified by the 'source' model element provided. When
     * calling this method, the code generator itself is responsible for determining the target output location for
     * generated content.
     * 
     * @param source the source element that defines the content to be rendered
     * @param context the code generation context
     * @return Collection&lt;File&gt;
     * @throws ValidationException thrown if problems are detected during validation that prevent schema generation from
     *         proceeding
     * @throws CodeGenerationException thrown if a system or I/O exception occurs during output generation
     * @throws IllegalArgumentException thrown if the code generator instance does not support code generation for the
     *         type of source element provided
     */
    public Collection<File> generateOutput(S source, CodeGenerationContext context)
        throws ValidationException, CodeGenerationException;

    /**
     * Returns the code generation filter to use during processing.
     * 
     * @return CodeGenerationFilter
     */
    public CodeGenerationFilter getFilter();

    /**
     * Assigns the code generation filter to use during processing. If one is not specified, the code generator should
     * use a default filter implementation.
     * 
     * @param filter the code generation filter to assign
     */
    public void setFilter(CodeGenerationFilter filter);

    /**
     * Returns the filename builder to use during processing.
     * 
     * @return CodeGenerationFilenameBuilder&lt;S&gt;
     */
    public CodeGenerationFilenameBuilder<S> getFilenameBuilder();

    /**
     * Assigns the filename builder to use during processing. If one is not specified, the code generator should use a
     * default filename builder implementation.
     * 
     * @param filenameBuilder the filename builder to assign
     */
    public void setFilenameBuilder(CodeGenerationFilenameBuilder<S> filenameBuilder);

}
