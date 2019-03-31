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

/**
 * Service to be implemented by components that can generate file names for the target model element(s) of a code
 * generation service.
 * 
 * @param <T> the type of model element for which filenames can be generated
 * @author S. Livezey
 */
public interface CodeGenerationFilenameBuilder<T> {

    /**
     * Returns the filename that is associated with the given model element.
     * 
     * @param item the model element for which to return a filename
     * @param fileExtension the extension of the filename to be created
     * @return String
     */
    public String buildFilename(T item, String fileExtension);

}
