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

/**
 * Handles the verification of user ID and password credentials submitted by a web service client
 * request.
 * 
 * @author S. Livezey
 */
public interface AuthenticationProvider {

    /**
     * Returns true if the given user is an authorized user of this repository and the password is
     * verified as being valid.
     * 
     * @param userId
     *            the user ID to verify
     * @param password
     *            the user's password to be authenticated
     * @return boolean
     * @throws RepositorySecurityException
     *             thrown if an error occurs during the authentication process
     */
    public boolean isValidUser(String userId, String password) throws RepositorySecurityException;

}
