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
 * Interface that defines the task options required for the generation of EXAMPLE output.
 * 
 * @author S. Livezey
 */
public interface ExampleCompilerTaskOptions {

    /**
     * Returns the option flag indicating that EXAMPLE data files should be generated.
     * 
     * @return boolean
     */
    public boolean isGenerateExamples();

    /**
     * Returns true if the maximum amount of detail is to be included in generated EXAMPLE data. If false, minimum
     * detail will be generated.
     * 
     * @return boolean
     */
    public boolean isGenerateMaxDetailsForExamples();

    /**
     * Returns the preferred context to use when producing EXAMPLE values for simple data types.
     * 
     * @return String
     */
    public String getExampleContext();

    /**
     * Returns the maximum number of times that repeating elements should be displayed in generated EXAMPLE output.
     * 
     * @return Integer
     */
    public Integer getExampleMaxRepeat();

    /**
     * Returns the maximum depth that should be included for nested elements in generated EXAMPLE output.
     * 
     * @return Integer
     */
    public Integer getExampleMaxDepth();

    /**
     * Returns the flag indicating whether optional fields should be suppressed during EXAMPLE generation.
     * 
     * @return boolean
     */
    public boolean isSuppressOptionalFields();

}
