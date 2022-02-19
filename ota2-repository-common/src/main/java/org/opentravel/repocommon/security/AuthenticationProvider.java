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

package org.opentravel.repocommon.security;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.UserInfo;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;

import java.util.List;

/**
 * Handles the verification of user ID and password credentials submitted by a web service client request.
 * 
 * @author S. Livezey
 */
public interface AuthenticationProvider {

    /**
     * Returns true if the given user is an authorized user of this repository and the password is verified as being
     * valid.
     * 
     * @param userId the user ID to verify
     * @param password the user's password to be authenticated
     * @return boolean
     * @throws RepositorySecurityException thrown if an error occurs during the authentication process
     */
    public boolean isValidUser(String userId, String password) throws RepositorySecurityException;

    /**
     * Returns account information for the user with the specified ID, or null if the user account does not exist.
     * 
     * @param userId the ID of the user for which to return account information
     * @return UserInfo
     */
    public UserInfo getUserInfo(String userId);

    /**
     * Loads the list of all user accounts for the repository.
     * 
     * @return List&lt;UserInfo&gt;
     */
    public List<UserInfo> getAllUsers();

    /**
     * Adds a user to the list of accounts who are authorized to access the repository.
     * 
     * @param userInfo the account information of the user to add
     * @throws RepositoryException thrown if the user account cannot be added for any reason
     */
    public void addUser(UserInfo userInfo) throws RepositoryException;

    /**
     * Updates the account information for an existing repository user.
     * 
     * @param userInfo the user account information to update
     * @throws RepositoryException thrown if the user account cannot be updated for any reason
     */
    public void updateUser(UserInfo userInfo) throws RepositoryException;

    /**
     * Deletes a user from the list of accounts who are authorized to access the repository.
     * 
     * @param userId the ID of the user account to delete
     * @throws RepositoryException thrown if the user account cannot be deleted for any reason
     */
    public void deleteUser(String userId) throws RepositoryException;

    /**
     * Assigns or modifies the user's password.
     * 
     * @param userId the ID of the user whose password is to be assigned
     * @param password the clear-text password to assign
     * @throws RepositoryException thrown if the user's password cannot be assigned
     */
    public void setUserPassword(String userId, String password) throws RepositoryException;

    /**
     * Searches the directory and returns the candidate users based on the search criteria provided.
     * 
     * @param searchCriteria the search string that will be compared against multiple directory attributes
     * @param maxResults the maximum number of results to return
     * @return List&lt;UserPrincipal&gt;
     * @throws RepositoryException thrown if an error occurs while searching the remote directory
     */
    public List<UserPrincipal> searchCandidateUsers(String searchCriteria, int maxResults) throws RepositoryException;

}
