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
 * Interface to be implemented by any element that can specify a reference to a context declaration.
 * 
 * @author S. Livezey
 */
public interface TLContextReferrer extends LibraryElement {

    /**
     * Returns the ID of the context declaration to which this entity refers.
     * 
     * @return String
     */
    public String getContext();

    /**
     * Assigns the ID of the context declaration to which this entity refers.
     * 
     * @param context the context ID to assign
     */
    public void setContext(String context);

}
