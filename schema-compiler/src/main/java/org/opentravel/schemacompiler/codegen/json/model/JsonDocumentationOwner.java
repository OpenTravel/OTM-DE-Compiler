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

package org.opentravel.schemacompiler.codegen.json.model;

import java.util.List;

/**
 * Interface for all code generation model objects that contain JSON documentation.
 */
public interface JsonDocumentationOwner {

    /**
     * Returns the documentation for the owner.
     *
     * @return JsonDocumentation
     */
    public JsonDocumentation getDocumentation();

    /**
     * Assigns the documentation for the owner.
     *
     * @param documentation the documentation item to assign
     */
    public void setDocumentation(JsonDocumentation documentation);

    /**
     * Returns the list of equivalent item definitions for the owner.
     *
     * @return List&lt;JsonContextualValue&gt;
     */
    public List<JsonContextualValue> getEquivalentItems();

    /**
     * Returns the list of EXAMPLE value definitions for the owner.
     *
     * @return List&lt;JsonContextualValue&gt;
     */
    public List<JsonContextualValue> getExampleItems();

}
