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

package org.opentravel.schemacompiler.codegen.openapi;

import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiHeader;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiParameter;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiResponse;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiSecurityScheme;

import java.util.List;

/**
 * Interface for components that can add additional message components and binding information to an OpenAPI document
 * during code generation.
 */
public interface CodeGenerationOpenApiBindings {

    /**
     * Returns the list of all parameters that should be declared globally in the generated OpenAPI document. In
     * addition to being declared globally, the parameters returned by this method will be referenced by every operation
     * defined in the OpenAPI document.
     * 
     * @return List&lt;OpenApiParameter&gt;
     */
    public List<OpenApiParameter> getGlobalParameters();

    /**
     * Returns a list of response headers that will be included in every response declared in the generated OpenAPI
     * document.
     * 
     * @return List&lt;OpenApiHeader&gt;
     */
    public List<OpenApiHeader> getGlobalResponseHeaders();

    /**
     * Returns a list of responses that will be included for every operation declared in the generated OpenAPI document.
     * If a duplicate response status code is explicitly provided in the model, the global response with the same status
     * code will be omitted.
     * 
     * @return List&lt;OpenApiResponse&gt;
     */
    public List<OpenApiResponse> getGlobalResponses();

    /**
     * Returns the list of supported security schemes that should be included in the generated OpenAPI document.
     * 
     * @return List&lt;OpenApiSecurityScheme&gt;
     */
    public List<OpenApiSecurityScheme> getSecuritySchemes();

}
