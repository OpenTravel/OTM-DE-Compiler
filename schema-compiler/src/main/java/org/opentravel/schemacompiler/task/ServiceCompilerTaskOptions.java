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

import java.net.URL;

/**
 * Interface that defines the options that are specific to the service (WSDL) code generation task.
 * 
 * @author S. Livezey
 */
public interface ServiceCompilerTaskOptions extends CommonCompilerTaskOptions, ExampleCompilerTaskOptions {

    /**
     * Returns the URL of the OTM library that contains the single service to be generated. If present, only that
     * service's WSDL will be generated. If not present, WSDL's will be generated for all services that exist in the OTM
     * model being processed.
     * 
     * @return URL
     */
    public URL getServiceLibraryUrl();

    /**
     * Returns the base URL for all service endpoints generated in WSDL documents.
     * 
     * @return String
     */
    public String getServiceEndpointUrl();

}
