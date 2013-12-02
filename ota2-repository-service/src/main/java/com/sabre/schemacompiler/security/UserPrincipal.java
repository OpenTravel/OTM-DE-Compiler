/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Encapsulates the user ID and group assignments for an authenticated client of the OTA2.0
 * repository web service.
 * 
 * @author S. Livezey
 */
public final class UserPrincipal {
	
	public static final String ANONYMOUS_USER_ID = "anonymous";
	public static final UserPrincipal ANONYMOUS_USER = new UserPrincipal(ANONYMOUS_USER_ID, null);
	
	private String userId;
	private Collection<String> assignedGroups = new ArrayList<String>();
	private Collection<String> authorizationIds = new HashSet<String>();
	
	/**
	 * Full constructor that provides the user's ID and his/her group assignments.
	 * 
	 * @param userId  the ID of the authenticated user
	 * @param assignedGroups  the group assignments of the user
	 */
	public UserPrincipal(String userId, String[] assignedGroups) {
		this.userId = userId;
		
		if (assignedGroups != null) {
			for (String groupName : assignedGroups) {
				this.assignedGroups.add( groupName );
			}
		}
		this.authorizationIds.add( userId );
		this.authorizationIds.add( ANONYMOUS_USER_ID );
		this.authorizationIds.addAll( this.assignedGroups );
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
	 * Returns the group assignments of the user.
	 * 
	 * @return Collection<String>
	 */
	public Collection<String> getAssignedGroups() {
		return Collections.unmodifiableCollection(assignedGroups);
	}
	
	/**
	 * Returns a collection that includes the user's ID, their group assignments, and the
	 * default "anonymous" user.
	 * 
	 * @return Collection<String>
	 */
	public Collection<String> getAuthorizationIds() {
		return Collections.unmodifiableCollection(authorizationIds);
	}
	
}
