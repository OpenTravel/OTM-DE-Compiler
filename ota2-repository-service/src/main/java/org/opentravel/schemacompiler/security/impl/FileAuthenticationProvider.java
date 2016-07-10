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
package org.opentravel.schemacompiler.security.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.UserInfo;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.security.PasswordHelper;
import org.opentravel.schemacompiler.security.UserPrincipal;

/**
 * Authentication provider that performs its checks against a credentials file on the repository's
 * file system.
 * 
 * @author S. Livezey
 */
public class FileAuthenticationProvider extends AbstractAuthenticationProvider {

	private static Pattern userIdPattern = Pattern.compile( "[A-Za-z_][A-Za-z0-9\\.\\-_]*" );

    /**
     * @see org.opentravel.schemacompiler.security.AuthenticationProvider#isValidUser(java.lang.String,java.lang.String)
     */
    @Override
    public boolean isValidUser(String userId, String password) {
    	UserInfo user = getUserInfo( userId );
    	boolean isValid = false;
    	
    	if ((user != null) && (password != null)) {
    		String encryptedPassword = user.getEncryptedPassword();
            isValid = password.equals(PasswordHelper.decrypt(encryptedPassword));
    	}
        return isValid;
    }

    /**
	 * @see org.opentravel.schemacompiler.security.impl.AbstractAuthenticationProvider#validateUserInfo(org.opentravel.ns.ota2.repositoryinfoext_v01_00.UserInfo)
	 */
	@Override
	protected void validateUserInfo(UserInfo userInfo) throws RepositoryException {
		super.validateUserInfo(userInfo);
		String lastName = userInfo.getLastName();
		
		if (!userIdPattern.matcher( userInfo.getUserId() ).matches()) {
			throw new RepositoryException( "Invalid repository user ID: " + userInfo.getUserId() );
		}
		if ((lastName == null) || (lastName.trim().length() == 0)) {
			throw new RepositoryException( "A last name is required for all user accounts." );
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.security.AuthenticationProvider#setUserPassword(java.lang.String, java.lang.String)
	 */
	@Override
	public void setUserPassword(String userId, String password) throws RepositoryException {
		List<UserInfo> updatedUserList = new ArrayList<>();
		boolean found = false;
		
		for (UserInfo user : getAllUsers()) {
			if (user.getUserId().equals( userId )) {
				user.setEncryptedPassword( PasswordHelper.encrypt( password ) );
				found = true;
			}
			updatedUserList.add( user );
		}
		
		if (found) {
			saveUserAccounts( updatedUserList );
			
		} else {
			throw new RepositoryException( "User account does not exist: " + userId );
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.security.AuthenticationProvider#searchCandidateUsers(java.lang.String, int)
	 */
	@Override
	public List<UserPrincipal> searchCandidateUsers(String searchCriteria, int maxResults) throws RepositoryException {
		throw new UnsupportedOperationException("Repositories configured for local user management do not support candidate user searches.");
	}

}
