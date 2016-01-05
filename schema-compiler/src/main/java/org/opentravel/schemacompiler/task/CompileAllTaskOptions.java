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
 * Interface that consolidates the options that are required for the simultaneous execution of all
 * code generation tasks.
 * 
 * @author S. Livezey
 */
public interface CompileAllTaskOptions extends SchemaCompilerTaskOptions,
        ServiceCompilerTaskOptions {

    /**
     * Returns the option flag indicating that XML schema (XSD) files should be generated for all
     * libraries.
     * 
     * @return boolean
     */
    public boolean isCompileSchemas();

    /**
     * Returns the option flag indicating that JSON schema files should be generated for all
     * libraries.
     * 
     * @return boolean
     */
    public boolean isCompileJsonSchemas();

    /**
     * Returns the option flag indicating that service (WSDL) files should be generated for service
     * definitions.
     * 
     * @return boolean
     */
    public boolean isCompileServices();

}
