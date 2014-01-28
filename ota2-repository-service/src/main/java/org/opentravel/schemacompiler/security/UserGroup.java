/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the name of a group and the user ID of each member assigned to it.
 * 
 * @author S. Livezey
 */
public class UserGroup {
	
	private String groupName;
	private List<String> memberIds = new ArrayList<String>();
	
	/**
	 * Constructor that defines the name of the group and the user ID of each member assigned to it.
	 * 
	 * @param groupName  the name of the group
	 * @param memberIds  the user ID of each member assigned to the group
	 */
	public UserGroup(String groupName, List<String> memberIds) {
		if (memberIds != null) {
			this.memberIds = memberIds;
		}
		this.groupName = groupName;
	}
	
	/**
	 * Returns the value of the 'groupName' field.
	 *
	 * @return String
	 */
	public String getGroupName() {
		return groupName;
	}
	
	/**
	 * Returns the value of the 'memberIds' field.
	 *
	 * @return List<String>
	 */
	public List<String> getMemberIds() {
		return memberIds;
	}
	
}
