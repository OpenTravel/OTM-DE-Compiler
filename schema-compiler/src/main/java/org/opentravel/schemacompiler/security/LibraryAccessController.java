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

package org.opentravel.schemacompiler.security;

import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Interface to be implemented by components that can determine whether a user should be allowed access to edit a
 * user-defined library instance.
 * 
 * @author S. Livezey
 */
public interface LibraryAccessController {

    /**
     * Returns true if the current user is allowed to modify the given library.
     * 
     * @param library the user-defined library
     * @return boolean
     */
    public boolean hasModifyPermission(TLLibrary library);

}
