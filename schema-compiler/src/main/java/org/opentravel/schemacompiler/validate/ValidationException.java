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
package org.opentravel.schemacompiler.validate;

import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Exception that encapsulates a <code>ValidationFindings</code> instance, allowing it to throw
 * multiple findings instead of a single error condition.
 * 
 * @author S. Livezey
 */
public class ValidationException extends SchemaCompilerException {

	private static final long serialVersionUID = 3933016239871704438L;
	
	private ValidationFindings findings;

    /**
     * Constructor that assigns the validation findings to be thrown.
     * 
     * @param findings
     *            the validation findings to throw
     */
    public ValidationException(ValidationFindings findings) {
        super();
        this.findings = findings;
    }

    /**
     * Constructor that assigns the exception message and the validation findings to be thrown.
     * 
     * @param message
     *            the message string for the exception
     * @param findings
     *            the validation findings to throw
     */
    public ValidationException(String message, ValidationFindings findings) {
        super(message);
        this.findings = findings;
    }

    /**
     * Returns the findings encapsulated by the exception.
     * 
     * @return ValidationFindings
     */
    public ValidationFindings getFindings() {
        return findings;
    }

}
