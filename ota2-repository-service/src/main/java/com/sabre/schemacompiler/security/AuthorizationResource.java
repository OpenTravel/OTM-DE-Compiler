/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.opentravel.ns.ota2.security_v01_00.AuthorizationSpec;
import org.opentravel.ns.ota2.security_v01_00.NamespaceAuthorizations;
import org.opentravel.ns.ota2.security_v01_00.RepositoryPermission;

import com.sabre.schemacompiler.config.FileResource;
import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryFileManager;
import com.sabre.schemacompiler.security.impl.SecurityFileUtils;

/**
 * File-based resource that provides access to authorization grants (and denies) for a single
 * namespace URI.
 * 
 * @author S. Livezey
 */
public class AuthorizationResource extends FileResource<NamespaceAuthorizations> {
	
	private RepositoryFileManager fileManager;
	private SecurityFileUtils fileUtils;
	private String namespace;
	
	/**
	 * Constructor that specifies the namespace for which authorizations should be retrieved.
	 * 
	 * @param repositoryLocation  the root folder location of the OTA2.0 repository
	 * @param namespace  the namespace for which authorizations should be retrieved
	 * @throws RepositorySecurityException  thrown if the location of the authorization file resource cannot be identified
	 */
	public AuthorizationResource(SecurityFileUtils fileUtils, String namespace) throws RepositorySecurityException {
		super( fileUtils.getAuthorizationFile(namespace) );
		this.fileManager = fileUtils.getRepositoryManager().getFileManager();
		this.fileUtils = fileUtils;
		this.namespace = namespace;
		invalidateResource();
	}
	
	/**
	 * Returns the namespace URI to which the authorization permissions apply.
	 * 
	 * @return String
	 */
	public String getNamespace() {
		return namespace;
	}
	
	/**
	 * Returns the permissions that have been explicitly granted to the specified user in
	 * this resource's namespace.
	 * 
	 * @param user  the user for which granted permissions should be returned
	 * @return Set<RepositoryPermission>
	 */
	public Set<RepositoryPermission> getGrantedPermissions(UserPrincipal user) {
		Set<RepositoryPermission> grantedPermissions = new HashSet<RepositoryPermission>();
		NamespaceAuthorizations authorizations = getResource();
		
		// Search the grants/denies to determine which permissions apply to this user
		for (AuthorizationSpec grant : authorizations.getGrant()) {
			if (appliesToUser(grant, user)) {
				grantedPermissions.add( grant.getPermission() );
			}
		}
		return grantedPermissions;
	}
	
	/**
	 * Returns the permissions that have been explicitly denied to the specified user in
	 * this resource's namespace.
	 * 
	 * @param user  the user for which denied permissions should be returned
	 * @return Set<RepositoryPermission>
	 */
	public Set<RepositoryPermission> getDeniedPermissions(UserPrincipal user) {
		Set<RepositoryPermission> deniedPermissions = new HashSet<RepositoryPermission>();
		NamespaceAuthorizations authorizations = getResource();
		
		// Search the grants/denies to determine which permissions apply to this user
		for (AuthorizationSpec deny : authorizations.getDeny()) {
			if (appliesToUser(deny, user)) {
				deniedPermissions.add( deny.getPermission() );
			}
		}
		return deniedPermissions;
	}
	
	/**
	 * Returns true if the given grant/deny specification applies to the indicated user.
	 * 
	 * @param spec  the grant/deny specification to analyze
	 * @param user  the user whose permissions are to be checked
	 * @return boolean
	 */
	private boolean appliesToUser(AuthorizationSpec spec, UserPrincipal user) {
		boolean result = false;
		
		for (String authPrincipal : spec.getPrincipal()) {
			if (user.getAuthorizationIds().contains(authPrincipal)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * @see com.sabre.schemacompiler.config.FileResource#getDefaultResourceValue()
	 */
	@Override
	protected NamespaceAuthorizations getDefaultResourceValue() {
		return new NamespaceAuthorizations();
	}

	/**
	 * @see com.sabre.schemacompiler.config.FileResource#loadResource(java.io.File)
	 */
	@Override
	protected NamespaceAuthorizations loadResource(File dataFile) throws IOException {
		if (fileUtils == null) {
			return getDefaultResourceValue(); // Special case for constructor initialization
		}
		return fileUtils.loadNamespaceAuthorizations( dataFile );
	}
	
	/**
	 * Saves the collection of namespace authorizations to the locally maintained authorizations file.
	 * 
	 * @param authorizations  the authorizations to be saved
	 * @throws RepositoryException  thrown if the authorizations file cannot be saved
	 */
	public synchronized void saveNamespaceAuthorizations(NamespaceAuthorizations authorizations) throws RepositoryException {
		fileManager.startChangeSet();
		boolean success = false;
		try {
			File dataFile = getDataFile();
			
			fileManager.addToChangeSet( dataFile );
			fileUtils.saveNamespaceAuthorizations( dataFile, authorizations );
			fileManager.commitChangeSet();
			
		} catch (IOException e) {
			throw new RepositoryException("Error saving the group assignments file.", e);
			
		} finally {
			try {
				if (!success) fileManager.rollbackChangeSet();
			} catch (Throwable t) {}
		}
	}
	
}
