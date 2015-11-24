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

import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Exception thrown when errors are encountered during schema output generation.
 * 
 * @author S. Livezey
 */
public class CodeGenerationException extends SchemaCompilerException {

	private static final long serialVersionUID = -8992007234245167340L;

	/**
     * Default constructor.
     */
    public CodeGenerationException() {
    }

    /**
     * Constructs an exception with the specified message.
     * 
     * @param message
     *            the exception message
     */
    public CodeGenerationException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified underlying cause.
     * 
     * @param cause
     *            the underlying exception that caused this one
     */
    public CodeGenerationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an exception with the specified message and underlying cause.
     * 
     * @param message
     *            the exception message
     * @param cause
     *            the underlying exception that caused this one
     */
    public CodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

}
