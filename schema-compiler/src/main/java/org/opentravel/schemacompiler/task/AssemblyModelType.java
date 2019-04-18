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
 * Enumeration that represents the types of models that can be loaded for a service assembly.
 */
public enum AssemblyModelType {

    PROVIDER("provider"), CONSUMER("consumer"), IMPLEMENTATION("implementation");

    private String identifier;

    /**
     * Private constructor that specifies the string identifier associated with the enumeration value.
     * 
     * @param identifier the string identifier for the value
     */
    private AssemblyModelType(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the enumeration value that corresponds to the model-type identifier string provided.
     * 
     * @param identifierStr the model-type identifier string
     * @return AssemblyModelType
     */
    public static AssemblyModelType fromIdentifier(String identifierStr) {
        AssemblyModelType type = null;

        for (AssemblyModelType mt : values()) {
            if (mt.identifier.equals( identifierStr )) {
                type = mt;
                break;
            }
        }
        return type;
    }

}
