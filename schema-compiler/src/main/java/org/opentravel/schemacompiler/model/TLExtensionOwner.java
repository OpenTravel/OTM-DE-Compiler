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
package org.opentravel.schemacompiler.model;

/**
 * Interface to be implemented by model elements that can extend a single entity.
 * 
 * @author S. Livezey
 */
public interface TLExtensionOwner extends LibraryElement {

    /**
     * Returns the extension element for this entity.
     * 
     * @return TLExtension
     */
    public TLExtension getExtension();

    /**
     * Assigns the extension element for this entity.
     * 
     * @param extension
     *            the etension element to assign
     */
    public void setExtension(TLExtension extension);

}
