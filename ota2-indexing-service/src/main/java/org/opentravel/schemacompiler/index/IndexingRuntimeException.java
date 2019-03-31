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

package org.opentravel.schemacompiler.index;

/**
 * Thrown by the indexing service if a runtime exception occurs during processing.
 */
public class IndexingRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 670922777950849075L;

    /**
     * Constructor that specifies the root cause and an exception message.
     * 
     * @param message the exception message
     * @param cause the root cause of the exception
     */
    public IndexingRuntimeException(String message, Throwable cause) {
        super( message, cause, false, true );
    }

}
