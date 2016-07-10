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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.UserInfo;

/**
 * Encapsulates the user ID and group assignments for an authenticated client of the OTA2.0
 * repository web service.
 * 
 * @author S. Livezey
 */
public final class UserPrincipal {

    public static final String ANONYMOUS_USER_ID = "anonymous";
    public static final UserPrincipal ANONYMOUS_USER;

    private String userId;
    private String lastName;
    private String firstName;
    private String emailAddress;
    private Collection<String> assignedGroups = new ArrayList<String>();
    private Collection<String> authorizationIds = new HashSet<String>();
    
    /**
     * Default constructor.
     */
    public UserPrincipal() {}
    
    /**
     * Full constructor that provides the user's ID and his/her group assignments.
     * 
     * @param userInfo
     *            the basic information about the authenticated user
     * @param assignedGroups
     *            the group assignments of the user
     */
    public UserPrincipal(UserInfo userInfo, String[] assignedGroups) {
        this.userId = userInfo.getUserId();
        this.lastName = userInfo.getLastName();
        this.firstName = userInfo.getFirstName();
        this.emailAddress = userInfo.getEmailAddress();

        if (assignedGroups != null) {
            for (String groupName : assignedGroups) {
                this.assignedGroups.add(groupName);
            }
        }
        this.authorizationIds.add(userId);
        this.authorizationIds.add(ANONYMOUS_USER_ID);
        this.authorizationIds.addAll(this.assignedGroups);
    }

    /**
     * Return the ID of the authenticated user.
     * 
     * @return String
     */
    public String getUserId() {
        return userId;
    }

    /**
	 * Assigns the ID of the authenticated user.
	 *
	 * @param userId  the user ID to assign
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Returns the last name of the authenticated user.
	 *
	 * @return String
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Assigns the last name of the authenticated user.
	 *
	 * @param lastName  the last name value to assign
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Returns the first name of the authenticated user.
	 *
	 * @return String
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Assigns the first name of the authenticated user.
	 *
	 * @param firstName  the first name value to assign
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Returns the email address of the authenticated user.
	 *
	 * @return String
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * Assigns the email address of the authenticated user.
	 *
	 * @param emailAddress  the email address to assign
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
     * Returns the group assignments of the user.
     * 
     * @return Collection<String>
     */
    public Collection<String> getAssignedGroups() {
        return Collections.unmodifiableCollection(assignedGroups);
    }

    /**
     * Returns a collection that includes the user's ID, their group assignments, and the default
     * "anonymous" user.
     * 
     * @return Collection<String>
     */
    public Collection<String> getAuthorizationIds() {
        return Collections.unmodifiableCollection(authorizationIds);
    }
    
    /**
     * Initializes the anonymous user information.
     */
    static {
    	try {
    		UserInfo anonymousUser = new UserInfo();
    		
    		anonymousUser.setUserId( ANONYMOUS_USER_ID );
    		anonymousUser.setLastName( "Anonymous" );
    		ANONYMOUS_USER = new UserPrincipal( anonymousUser, null );
    		 
    	} catch (Throwable t) {
    		throw new ExceptionInInitializerError( t );
    	}
    }
    
}
