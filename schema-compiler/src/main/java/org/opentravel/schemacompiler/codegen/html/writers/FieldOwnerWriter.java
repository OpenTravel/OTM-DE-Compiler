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

package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.Content;

/**
 * @author Eric.Bronson
 *
 */
public interface FieldOwnerWriter {

    /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     */
    public void addPropertyInfo(Content memberTree);

    /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     */
    public void addAttributeInfo(Content memberTree);

    /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     */
    public void addIndicatorInfo(Content memberTree);

    /**
     * Get the member summary tree.
     *
     * @param memberTree the content tree used to build the summary tree
     */
    public void addExampleInfo(Content memberTree);

}
