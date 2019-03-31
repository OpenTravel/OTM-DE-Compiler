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

package org.opentravel.schemacompiler.task;

/**
 * Base interface that defines the options that are common to all code generation tasks.
 * 
 * @author S. Livezey
 */
public interface CommonCompilerTaskOptions {

    /**
     * Copies all of the known task options from the given set of options into this instance.
     * 
     * @param taskOptions the task options from which to copy the configuration settings
     */
    public void applyTaskOptions(CommonCompilerTaskOptions taskOptions);

    /**
     * Returns the location of the library catalog file as either an absolute or relative URL string.
     * 
     * @return String
     */
    public String getCatalogLocation();

    /**
     * Returns the output folder location as either an absolute or relative URL string.
     * 
     * @return String
     */
    public String getOutputFolder();

}
