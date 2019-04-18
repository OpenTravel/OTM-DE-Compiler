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

package org.opentravel.schemacompiler.mvn;

/**
 * Specifies the key identity attributes of a managed OTM service assembly.
 */
public class Assembly {

    private String baseNamespace;
    private String filename;
    private String version;
    private String modelType;

    /**
     * Returns the base namespace of the managed OTM service assembly.
     *
     * @return String
     */
    public String getBaseNamespace() {
        return baseNamespace;
    }

    /**
     * Assigns the base namespace of the managed OTM service assembly.
     *
     * @param baseNamespace the field value to assign
     */
    public void setBaseNamespace(String baseNamespace) {
        this.baseNamespace = baseNamespace;
    }

    /**
     * Returns the filename of the managed OTM service assembly.
     *
     * @return String
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Assigns the filename of the managed OTM service assembly.
     *
     * @param filename the field value to assign
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Returns the version of the managed OTM service assembly.
     *
     * @return String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Assigns the version of the managed OTM service assembly.
     *
     * @param version the field value to assign
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the type of model that should be loaded for the managed OTM service assembly. Valid values are
     * 'provider', 'consumer', and 'implementation'.
     * 
     * @return String
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * Assigns the type of model that should be loaded for the managed OTM service assembly. Valid values are
     * 'provider', 'consumer', and 'implementation'.
     * 
     * @param modelType the model type identifier
     */
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

}
