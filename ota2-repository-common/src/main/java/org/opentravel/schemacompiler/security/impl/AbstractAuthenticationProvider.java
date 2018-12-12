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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.RepositoryUsers;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.UserInfo;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.security.AuthenticationProvider;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Base class that provides common methods for all <code>AuthenticationProvider</code>
 * implementations.
 */
public abstract class AbstractAuthenticationProvider implements AuthenticationProvider {
	
    private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.repositoryinfoext_v01_00";
    private static final String REPOSITORY_EXT_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/RepositoryInfoExt_v01_00";
    private static final String REPOSITORY_EXT_SCHEMA_LOCATION = "/schemas/OTA2_RepositoryExt_v1.0.0.xsd";

    private static javax.xml.validation.Schema validationSchema;
    private static ObjectFactory objectFactory = new ObjectFactory();
    private static JAXBContext jaxbContext;
    
	protected static Map<String,UserInfo> userRegistry;
	private static long userFileLastModified = Integer.MIN_VALUE;
    private static Log log = LogFactory.getLog( AbstractAuthenticationProvider.class );
	
	private RepositoryManager repositoryManager;
	
	/**
	 * @see org.opentravel.schemacompiler.security.AuthenticationProvider#getUserInfo(java.lang.String)
	 */
	@Override
	public UserInfo getUserInfo(String userId) {
		refreshRegistry();
		UserInfo userInfo = userRegistry.get( userId );
		
		refreshUserInfo( userInfo );
		return userInfo;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.security.AuthenticationProvider#getAllUsers()
	 */
	@Override
	public List<UserInfo> getAllUsers() {
		refreshRegistry();
		refreshAllUsers();
		List<UserInfo> allUsers = new ArrayList<>( userRegistry.values() );
		
		Collections.sort( allUsers, new UserInfoComparator() );
		return Collections.unmodifiableList( allUsers );
	}
	
	/**
	 * @see org.opentravel.schemacompiler.security.AuthenticationProvider#addUser(org.opentravel.ns.ota2.repositoryinfoext_v01_00.UserInfo)
	 */
	@Override
	public void addUser(UserInfo userInfo) throws RepositoryException {
		validateUserInfo( userInfo );
		List<UserInfo> updatedUserList = new ArrayList<>( getAllUsers() );
		
		if (userRegistry.containsKey( userInfo.getUserId() )) {
			throw new RepositoryException( "A user with the ID '" + userInfo.getUserId() + "' already exists." );
		}
		updatedUserList.add( userInfo );
		Collections.sort( updatedUserList, new UserInfoComparator() );
		saveUserAccounts( updatedUserList );
	}

	/**
	 * @see org.opentravel.schemacompiler.security.AuthenticationProvider#updateUser(org.opentravel.ns.ota2.repositoryinfoext_v01_00.UserInfo)
	 */
	@Override
	public void updateUser(UserInfo userInfo) throws RepositoryException {
		validateUserInfo( userInfo );
		List<UserInfo> updatedUserList = new ArrayList<>();
		boolean found = false;
		
		for (UserInfo existingUser : getAllUsers()) {
			if (existingUser.getUserId().equals( userInfo.getUserId() )) {
				userInfo.setEncryptedPassword( existingUser.getEncryptedPassword() );
				updatedUserList.add( userInfo );
				found = true;
				
			} else {
				updatedUserList.add( existingUser );
			}
		}
		
		if (found) {
			saveUserAccounts( updatedUserList );
			
		} else {
			throw new RepositoryException( "User account does not exist: " + userInfo.getUserId() );
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.security.AuthenticationProvider#deleteUser(java.lang.String)
	 */
	@Override
	public void deleteUser(String userId) throws RepositoryException {
		List<UserInfo> updatedUserList = new ArrayList<>();
		boolean found = false;
		
		if (userId != null) {
			for (UserInfo existingUser : getAllUsers()) {
				if (userId.equals( existingUser.getUserId() )) {
					found = true;
					
				} else {
					updatedUserList.add( existingUser );
				}
			}
		}
		
		if (found) {
			saveUserAccounts( updatedUserList );
			
		} else {
			throw new RepositoryException( "User account does not exist: " + userId );
		}
	}
	
	/**
	 * Validates the given user account information.  By default, this method simply enforces
	 * that the user ID field is a valid non-null/blank value.  Sub-classes may extend to add
	 * additional validation criteria.
	 * 
	 * @param userInfo  the user account information to validate
	 * @throws RepositoryException  thrown if the user account information is invalid in any way
	 */
	protected void validateUserInfo(UserInfo userInfo) throws RepositoryException {
		if (userInfo == null) {
			throw new RepositoryException( "The user account to be created or updated cannot be null." );
		}
		String userId = userInfo.getUserId();
		
		if ((userId == null) || (userId.length() == 0)) {
			throw new RepositoryException( "The ID for a user account cannot be empty." );
		}
	}
	
	/**
	 * Refreshes the user information from its original source.  By default, this method does nothing;
	 * sub-classes may override to refresh user account information from a database or remote directory.
	 * 
	 * @param userInfo  the user account information to refresh
	 */
	protected void refreshUserInfo(UserInfo userInfo) {}
	
	/**
	 * Refreshes all user accounts from their original source(s).  By default, this method does nothing;
	 * sub-classes may override to refresh user account information from a database or remote directory.
	 */
	protected void refreshAllUsers() {}
	
	/**
	 * Overwrites the current user account file with the given list.
	 * 
	 * @param userList  the list of all user accounts to save
	 * @throws RepositoryException  thrown if the user accounts file cannot be updated for any reason
	 */
	protected void saveUserAccounts(List<UserInfo> userList) throws RepositoryException {
		RepositoryFileManager fileManager = repositoryManager.getFileManager();
		boolean success = false;
		fileManager.startChangeSet();
		
		try {
	        File usersFile = new File( fileManager.getRepositoryLocation(), REPOSITORY_USERS_FILE );
			RepositoryUsers repoUsers = new RepositoryUsers();
			Marshaller marshaller = jaxbContext.createMarshaller();
			
			repoUsers.getUser().addAll( userList );
			fileManager.addToChangeSet( usersFile );
			
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            marshaller.setProperty( "com.sun.xml.bind.namespacePrefixMapper",
                    new NamespacePrefixMapper() {
                        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                            return REPOSITORY_EXT_NAMESPACE.equals( namespaceUri ) ? "r" : suggestion;
                        }

                        @Override public String[] getPreDeclaredNamespaceUris() {
                            return new String[] { REPOSITORY_EXT_NAMESPACE };
                        }
                    } );
            marshaller.setSchema( validationSchema );
            marshaller.marshal( objectFactory.createRepositoryUsers( repoUsers ), usersFile );
			
            fileManager.commitChangeSet();
            success = true;
            
		} catch (JAXBException e) {
			throw new RepositoryException( "Error committing user updates to file system.", e );
			
		} finally {
            try {
                if (!success) fileManager.rollbackChangeSet();
            } catch (Exception e) {
                // Ignore error and continue
            }
		}
	}

	/**
	 * Refreshes the memory-resident user registry if necessary.  If the refresh was
	 * peformed, this method returns true; false if the memory-resident cache was already
	 * up-to-date.
	 * 
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	protected synchronized boolean refreshRegistry() {
		RepositoryFileManager fileManager = repositoryManager.getFileManager();
		File registryFile = new File( fileManager.getRepositoryLocation(), REPOSITORY_USERS_FILE );
		boolean refreshPerformed = false;
		
		if (userRegistry == null) {
			userRegistry = new HashMap<>();
		}
		
		if (registryFile.exists()) {
			long lastModified = registryFile.lastModified();
			
			if (lastModified > userFileLastModified) {
				try {
		            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		            unmarshaller.setSchema(validationSchema);

		            JAXBElement<RepositoryUsers> documentElement =
		            		(JAXBElement<RepositoryUsers>) unmarshaller.unmarshal( registryFile );
		            RepositoryUsers repoUsers = documentElement.getValue();
		            
		            userRegistry.clear();
		            
		            for (UserInfo userInfo : repoUsers.getUser()) {
		            	userRegistry.put( userInfo.getUserId(), userInfo );
		            }
					userFileLastModified = lastModified;
					refreshPerformed = true;
					
		        } catch (JAXBException e) {
		            log.error( "Repository user file is unreadable.", e );
				}
			}
		}
		return refreshPerformed;
	}
	
	/**
	 * Returns the repository manager for the repository.
	 *
	 * @return RepositoryManager
	 */
	public RepositoryManager getRepositoryManager() {
		return repositoryManager;
	}

	/**
	 * Assigns the repository manager for the repository.
	 *
	 * @param repositoryManager  the field value to assign
	 */
	public void setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}

	/**
	 * Comparator used to sort a list of <code>UserInfo</code> objects.
	 */
	private static class UserInfoComparator implements Comparator<UserInfo> {
		
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(UserInfo user1, UserInfo user2) {
			int result;
			
			if (user1 == null) {
				result = (user2 == null) ? 0 : -1;
				
			} else if (user2 == null) {
				result = 1;
				
			} else {
				result = compare( user1.getLastName(), user2.getLastName() );
				
				if (result == 0) {
					result = compare( user1.getFirstName(), user2.getFirstName() );
				}
				if (result == 0) {
					result = compare( user1.getEmailAddress(), user2.getEmailAddress() );
				}
				if (result == 0) {
					result = compare( user1.getUserId(), user2.getUserId() );
				}
			}
			return result;
		}
		
		/**
		 * Compares two string values.
		 * 
		 * @param str1  the first string value to compare
		 * @param str2  the second string value to compare
		 * @return int
		 */
		private int compare(String str1, String str2) {
			int result;
			
			if (str1 == null) {
				result = (str2 == null) ? 0 : -1;
				
			} else if (str2 == null) {
				result = 1;
				
			} else {
				result = str1.compareTo( str2 );
			}
			return result;
		}
		
	}
	
    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            InputStream schemaStream = SecurityFileUtils.class.getResourceAsStream( REPOSITORY_EXT_SCHEMA_LOCATION );

            schemaFactory.setResourceResolver( new ClasspathResourceResolver() );
            validationSchema = schemaFactory.newSchema( new StreamSource( schemaStream ) );
            jaxbContext = JAXBContext.newInstance( SCHEMA_CONTEXT );

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
