/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security.impl;

import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sabre.schemacompiler.security.AuthenticationProvider;
import com.sabre.schemacompiler.security.PasswordHelper;
import com.sabre.schemacompiler.security.PasswordValidator;
import com.sabre.schemacompiler.security.RepositorySecurityException;

/**
 * Authentication provider that performs its checks against an LDAP directory.  The configuration
 * settings required for JNDI authentication should be included in the <code>ota2-repository-config.xml</code>
 * configuration file.
 * 
 * <p>JNDI authentication for OTA2.0 repositories is very similar to that of standard Tomcat authentication
 * using a JNDI security realm.  The configuration options for this provider support three primary modes of
 * operation:
 * 
 * <ul>
 * 	<li><b>User Authentication Mode</b> - In this mode, each user's credentials are used to attempt a login
 * 		to the remote directory server.  This approach is sometimes considered more secure because it does not
 *		require an LDAP administrator's password to be stored with the configuration settings of the repository.
 *		In some cases, however, this mode is not possible because user accounts in a corporate directory are not
 *		granted permission to login to the LDAP server itself.
 * 	<li><b>User Lookup Mode</b> - In user lookup mode, an authenticated user (typically an LDAP administrator) is
 *		used to establish all connections to the remote directory.  User accounts are identified by a distinguished
 *		name format that is the same for all users defined in the directory.  Once identified, encrypted password
 *		credentials are retrieved from the directory and compared with the credentials provided by the remote user
 *		of the repository.
 * 	<li><b>User Search Mode</b> - Like user-lookup, this mode of operation establishes remote connections using a
 * 		single authenticated user account.  User accounts are located by searches within the directory using one
 *		or more configurable query strings.  Once user accounts are located by a search, the user's encrypted password
 *		credentials are retrieved from the directory and compared with the credentials provided by the remote user
 *		of the repository.
 * </ul>
 * 
 * <p>The table below provides the detail of the various configuration options for each mode of operation:
 * 
 * <table border="1" cellspacing="0" cellpadding="0">
 * 	<tr>
 * 		<th rowspan="2">Property Name</th>
 * 		<th rowspan="2">Description</th>
 * 		<th colspan="3">Status (Required / Optional)</th>
 * 	</tr>
 * 	<tr>
 * 		<th>User Authentication Mode</th>
 * 		<th>User Lookup Mode</th>
 * 		<th>User Search Mode</th>
 * 	</tr>
 * 	<tr>
 * 		<td>contextFactory</td>
 * 		<td>Fully qualified Java class name of the factory class used to acquire our JNDI InitialContext. By default, assumes that the standard JNDI LDAP provider will be utilized.</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>connectionUrl</td>
 * 		<td>The connection URL to be passed to the JNDI driver when establishing a connection to the directory.</td>
 * 		<td>Required</td>
 * 		<td>Required</td>
 * 		<td>Required</td>
 * 	</tr>
 * 	<tr>
 * 		<td>alternateUrl</td>
 * 		<td>If a socket connection can not be made to the provider at the <code>connectionURL</code> an attempt will be made to use this address.</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>connectionProtocol</td>
 * 		<td>A string specifying the security protocol to use. If not given the providers default is used.</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>securityAuthentication</td>
 * 		<td>A string specifying the type of authentication to use. "none", "simple", "strong" or a provider specific definition can be used. If no value is given the providers default is used.</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>connectionTimeout</td>
 * 		<td>The timeout in milliseconds to use when establishing the connection to the LDAP directory. If not specified, a value of 5000 (5 seconds) is used.</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>authenticationCacheTimeout</td>
 * 		<td>The amount of time (in milliseconds) that the results of a user's login attempt should be cached.  Default value is 5 minutes.</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>connectionPrincipal</td>
 * 		<td>The directory username to use when establishing a connection to the directory for LDAP search and lookup operations. If not specified an anonymous connection is made, which is often sufficient unless you specify the <code>connectionPassword</code> property.</td>
 * 		<td>N/A</td>
 * 		<td>Required</td>
 * 		<td>Required</td>
 * 	</tr>
 * 	<tr>
 * 		<td>connectionPassword</td>
 * 		<td>The directory password to use when establishing a connection to the directory for LDAP search and lookup operations. If not specified an anonymous connection is made.</td>
 * 		<td>N/A</td>
 * 		<td>Required</td>
 * 		<td>Required</td>
 * 	</tr>
 * 	<tr>
 * 		<td>userPattern</td>
 * 		<td>Pattern for the distinguished name (DN) of the user's directory entry, with <code>{0}</code> marking where the actual username should be inserted.</td>
 * 		<td>Required</td>
 * 		<td>Required</td>
 * 		<td>N/A</td>
 * 	</tr>
 * 	<tr>
 * 		<td>userSearchBase</td>
 * 		<td>The base element for user searches performed using the 'userSearchPatterns' expressions.</td>
 * 		<td>N/A</td>
 * 		<td>N/A</td>
 * 		<td>Required</td>
 * 	</tr>
 * 	<tr>
 * 		<td>searchUserSubtree</td>
 * 		<td>Set to true if you want to search the entire subtree of the element specified by the 'userSearchBase' property for the user's entry. The default value of false causes only the top level to be searched.</td>
 * 		<td>N/A</td>
 * 		<td>N/A</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>userSearchPatterns</td>
 * 		<td>A colon-separated list of LDAP filter expressions to use when searching for a user's directory entry, with <code>{0}</code> marking where the actual username should be inserted.</td>
 * 		<td>N/A</td>
 * 		<td>N/A</td>
 * 		<td>Required</td>
 * 	</tr>
 * 	<tr>
 * 		<td>userSearchTimeout</td>
 * 		<td>Specifies the time (in milliseconds) to wait for records to be returned when employing the user-search mode of operation. If not specified, the default of 0 is used which indicates no limit.</td>
 * 		<td>N/A</td>
 * 		<td>N/A</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>userPasswordAttribute</td>
 * 		<td>Specifies the name of the attribute where passwords are stored on user entries.  If not specified, a default value of "userPassword" is assumed.</td>
 * 		<td>N/A</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>referralStrategy</td>
 * 		<td>Specifies the strategy for JNDI referrals; allowed values are "ignore", "follow", or "throw" (see javax.naming.Context.REFERRAL for more information). Microsoft Active Directory often returns referrals. If you need to follow them set referrals to "follow". Caution: if your DNS is not part of AD, the LDAP client lib might try to resolve your domain name in DNS to find another LDAP server.</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 	</tr>
 * 	<tr>
 * 		<td>digestAlgorithm</td>
 * 		<td>The digest algorithm to apply to the plaintext password offered by the user before comparing it with the value retrieved from the directory. Valid values are those accepted for the algorithm name by the java.security.MessageDigest class. If not specified the plaintext password is assumed to be retrieved.</td>
 * 		<td>N/A</td>
 * 		<td>Required</td>
 * 		<td>Required</td>
 * 	</tr>
 * 	<tr>
 * 		<td>digestEncoding</td>
 * 		<td>The encoding character set to use when applying the digest algorithm.</td>
 * 		<td>N/A</td>
 * 		<td>Optional</td>
 * 		<td>Optional</td>
 * 	</tr>
 * </table>
 * 
 * @author S. Livezey
 */
public class JNDIAuthenticationProvider implements AuthenticationProvider {
	
	private static Log log = LogFactory.getLog(JNDIAuthenticationProvider.class);
	
	private static enum AuthenticationMode { USER_AUTHENTICATION, USER_LOOKUP, USER_SEARCH };
	
	private AuthenticationMode mode;
	
	private String contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
	private String connectionUrl;
	private String alternateUrl;
	private String connectionProtocol;
	private String securityAuthentication;
	private int connectionTimeout = 5000;
	private String connectionPrincipal;
	private String connectionPassword;
	private String digestAlgorithm;
	private String digestEncoding;
	
	private MessageFormat userPattern;
	private String userSearchBase;
	private boolean searchUserSubtree = false;
	private MessageFormat[] userSearchPatterns;
	private int userSearchTimeout = 5000;
	private String userPasswordAttribute = "userPassword";
	private String referralStrategy = "ignore";
	private boolean isInitialized = false;
	private long authenticationCacheTimeout = 300000; // 5-minutes
	
	private Map<String,AuthenticationCacheEntry> authenticationCache = new HashMap<String,AuthenticationCacheEntry>();
	private PasswordValidator passwordValidator;
	
	/**
	 * @see com.sabre.schemacompiler.security.AuthenticationProvider#isValidUser(java.lang.String,java.lang.String)
	 */
	@Override
	public boolean isValidUser(String userId, String password) throws RepositorySecurityException {
		boolean isValid = false;
		try {
			// Occasionally, the directory context will timeout, so always make a second attempt
			// before giving up.
			try {
				initializeConfigurationSettings();
				isValid = checkCredentials( userId, password );
				
			} catch (CommunicationException e) {
				isValid = checkCredentials( userId, password );
				
			} catch (ServiceUnavailableException e) {
				isValid = checkCredentials( userId, password );
			}
			
		} catch (NamingException e) {
			log.error("Error from remote directory: " + e.getMessage(), e);
		}
		return isValid;
	}
	
	/**
	 * Contacts the remote directory to determine if the password provided is valid for the specified
	 * userId.
	 * 
	 * @param userId  the user ID to verify
	 * @param authCredentials  the user's password to be authenticated
	 * @return boolean
	 * @throws NamingException  thrown if an error occurs while communicating with the remote directory
	 */
	protected boolean checkCredentials(String userId, String authCredentials) throws NamingException {
		DirContext context = null;
		boolean isValid = false;
		
		try {
			AuthenticationCacheEntry cacheEntry = getCachedAuthentication( userId );
			boolean isAuthenticationCached = false;
			
			if (cacheEntry != null) {
				if ((authCredentials != null) &&
						PasswordHelper.encrypt( authCredentials ).equals( cacheEntry.getEncryptedPassword() )) {
					isAuthenticationCached = true;
					isValid = cacheEntry.isAuthenticationSuccessful();
				}
			}
			
			if (!isAuthenticationCached) {
				// The cached authentication was expired or unavailable, so we need to perform
				// a live authentication against the JNDI server.
				if (mode == AuthenticationMode.USER_AUTHENTICATION) {
					try {
						String userDn = userPattern.format( new String[] { userId } );
						
						context = openConnection( userDn, authCredentials );
						isValid = true;
						
					} catch (NamingException e) {
						// Ignore and return false
					}
					
				} else {
					context = openConnection( connectionPrincipal, connectionPassword );
					String userPassword;
					
					if (mode == AuthenticationMode.USER_LOOKUP) {
						userPassword = lookupUserPassword( userId, context );
						
					} else { // AuthenticationMode.USER_SEARCH
						userPassword = findUserPassword( userId, context );
					}
					if (userPassword != null) {
						isValid = passwordValidator.isValidPassword( authCredentials, userPassword );
					}
				}
				
				// Add these results to the cache so the results will be cached for the next inquiry
				setCachedAuthentication(
						new AuthenticationCacheEntry( userId, PasswordHelper.encrypt(authCredentials), isValid ) );
			}
			return isValid;
			
		} finally {
			try {
				if (context != null) context.close();
			} catch (Throwable t) {}
		}
	}
	
	/**
	 * Opens a connection to the remote directory.  If the first attempt is unsuccessful using the primary
	 * connection URL, a second attempt is made using the alternate URL (if one has been provided).
	 * 
	 * @param loginId  the user principal ID to use when establishing the connection
	 * @param loginPassword  the password credentials to use when establishing the connection
	 * @return DirContext
	 * @throws NamingException  thrown if a connection to the remote directory cannot be established
	 */
	protected DirContext openConnection(String loginId, String loginPassword) throws NamingException {
		DirContext context = null;
		
		if (alternateUrl == null) {
			context = new InitialDirContext( getDirectoryContextEnvironment(loginId, loginPassword, false) );
			
		} else {
			try {
				context = new InitialDirContext( getDirectoryContextEnvironment(loginId, loginPassword, false) );
				
			} catch (NamingException e) {
				log.warn("Unable to connect using primary directory URL - attempting using alternate address.");
				context = new InitialDirContext( getDirectoryContextEnvironment(loginId, loginPassword, true) );
			}
		}
		return context;
	}
	
	/**
	 * Performs a lookup of the user's password in the remote directory.
	 * 
	 * @param userId  the ID of the user whose password is to be retrieved
	 * @param context  the directory context from which to retrieve the user's password
	 * @return String
	 * @throws NamingException
	 */
	protected String lookupUserPassword(String userId, DirContext context) throws NamingException {
		String userPassword = null;
		try {
			String userDn = userPattern.format( new String[] { userId } );
			Attributes userAttributes = context.getAttributes( userDn, new String[] { userPasswordAttribute } );
			
			userPassword = getAttributeValue( userAttributes, userPasswordAttribute );
			
		} catch (NameNotFoundException e) {
			e.printStackTrace(System.out);
			// Ignore and return null
		}
		return userPassword;
	}
	
	/**
	 * Searches the remote directory for the user's entry and returns the value of its password attribute.
	 * 
	 * @param userId  the ID of the user whose password is to be retrieved
	 * @param context  the directory context from which to retrieve the user's password
	 * @return String
	 * @throws NamingException
	 */
	protected String findUserPassword(String userId, DirContext context) throws NamingException {
		String userPassword = null;
		
		for (MessageFormat userSearchPattern : userSearchPatterns) {
			try {
				String searchFilter = userSearchPattern.format( new String[] { userId } );
				SearchControls constraints = new SearchControls();
				
				constraints.setSearchScope( searchUserSubtree ? SearchControls.SUBTREE_SCOPE : SearchControls.ONELEVEL_SCOPE );
				constraints.setTimeLimit( userSearchTimeout );
				
				NamingEnumeration<SearchResult> results = context.search(userSearchBase, searchFilter, constraints);
				SearchResult result = null;
				
				try {
					if ((results != null) && results.hasMore()) {
						result = results.next();
						
						// Make sure only one entry exists for the requested user
						if (results.hasMore()) {
							log.warn("Multiple entries found for user: " + userId);
							result = null;
						}
					}
				} catch (PartialResultException e) {
					// Ignore partial result errors - most likely due to ActiveDirectory referrals
				}
				
				if (result != null) {
					userPassword = getAttributeValue(result.getAttributes(), userPasswordAttribute);
					break;
				}
				
			} catch (NameNotFoundException e) {
				// Ignore and keep searching
			}
		}
		return userPassword;
	}
	
	/**
	 * Returns the specified attribute value from the list of attributes provided.  If multiple values are assigned
	 * to the requested attribute, only the first available value will be returned.
	 * 
	 * @param attributes  the list of attributes from which to return a value
	 * @param attributeName  the name of the attribute whose value is to be returned
	 * @return String
	 * @throws NamingException  thrown if an error occurs while scanning the attributes
	 */
	private String getAttributeValue(Attributes attributes, String attributeName) throws NamingException {
		Attribute attribute = (attributes == null) ? null : attributes.get( attributeName );
		String attributeValue = null;
		
		if (attribute != null) {
			Object attrValue = attribute.get();
			
			if (attrValue != null) {
				if (attrValue instanceof byte[]) {
					attributeValue = new String( (byte[]) attrValue );
					
				} else {
					attributeValue = attrValue.toString();
				}
			}
		}
		return attributeValue;
	}
	
	/**
	 * Creates the directory context configuration.
	 * 
	 * @param loginId  the user principal ID to use when establishing the connection
	 * @param loginPassword  the password credentials to use when establishing the connection
	 * @param isConnectionRetry  if true, the alternate URL will be employed
	 * @return Hashtable<String,String>
	 */
	protected Hashtable<String,String> getDirectoryContextEnvironment(String loginId, String loginPassword, boolean isConnectionRetry) {
		Hashtable<String,String> env = new Hashtable<String,String>();
		
		env.put( Context.INITIAL_CONTEXT_FACTORY, contextFactory );
		
		if (!isConnectionRetry) {
			env.put( Context.PROVIDER_URL, connectionUrl );
			
		} else if (alternateUrl != null) {
			env.put( Context.PROVIDER_URL, alternateUrl );
		}
		if (loginId != null) {
			env.put( Context.SECURITY_PRINCIPAL, loginId );
		}
		if (loginPassword != null) {
			env.put( Context.SECURITY_CREDENTIALS, loginPassword );
		}
		if (securityAuthentication != null) {
			env.put( Context.SECURITY_AUTHENTICATION, securityAuthentication );
		}
		if (connectionProtocol != null) {
			env.put( Context.SECURITY_PROTOCOL, connectionProtocol );
		}
		if (referralStrategy != null) {
			env.put( Context.REFERRAL, referralStrategy );
		}
		if (connectionTimeout > 0) {
			 env.put( "com.sun.jndi.ldap.connect.timeout", connectionTimeout + "" );
		}
		return env;
	}
	
	/**
	 * Ensure that the configuration settings are valid for one of the allowed modes of operatoin.
	 * 
	 * @throws RepositorySecurityException  thrown if the JNDI configuration settings are not valid for
	 *										one of the allowed modes of operation
	 */
	protected synchronized void initializeConfigurationSettings() throws RepositorySecurityException {
		if (isInitialized) return; // Already done - return without action
		
		// First, verify that all required mode-independent settings are present
		if ((connectionUrl == null) || (connectionUrl.length() == 0)) {
			throw new RepositorySecurityException(
					"The 'authentication.jndi.connectionUrl' property is a required value for all JNDI authentication modes.");
		}
		
		// Next, identify the mode of operation based on the configuration settings provided
		if (connectionPrincipal == null) {
			mode = AuthenticationMode.USER_AUTHENTICATION;
			
		} else if (userPattern != null) {
			mode = AuthenticationMode.USER_LOOKUP;
			
		} else {
			mode = AuthenticationMode.USER_SEARCH;
		}
		
		// Finally, ensure that all required mode-specific settings have been provided
		switch (mode) {
			case USER_AUTHENTICATION:
				if (userPattern == null) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.userPattern' property is a required value for the JNDI user-authentication mode.");
				}
				break;
			case USER_LOOKUP:
				if ((connectionPrincipal == null) || (connectionPrincipal.length() == 0)) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.connectionPrincipal' property is a required value for the JNDI user-lookup mode.");
				}
				if ((connectionPassword == null) || (connectionPassword.length() == 0)) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.connectionPassword' property is a required value for the JNDI user-lookup mode.");
				}
				if (userPattern == null) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.userPattern' property is a required value for the JNDI user-lookup mode.");
				}
				if (passwordValidator == null) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.digestAlgorithm' property is a required value for the JNDI user-lookup mode.");
				}
				break;
			case USER_SEARCH:
				// userSearchBase, userSearchPatterns
				if ((connectionPrincipal == null) || (connectionPrincipal.length() == 0)) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.connectionPrincipal' property is a required value for the JNDI user-lookup mode.");
				}
				if ((connectionPassword == null) || (connectionPassword.length() == 0)) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.connectionPassword' property is a required value for the JNDI user-lookup mode.");
				}
				if ((userSearchBase == null) || (userSearchBase.length() == 0)) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.userSearchBase' property is a required value for the JNDI user-lookup mode.");
				}
				if ((userSearchPatterns == null) || (userSearchPatterns.length == 0)) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.userSearchPatterns' property is a required value for the JNDI user-lookup mode.");
				}
				if (passwordValidator == null) {
					throw new RepositorySecurityException(
							"The 'authentication.jndi.digestAlgorithm' property is a required value for the JNDI user-lookup mode.");
				}
				break;
		}
	}
	
	/**
	 * Returns the fully qualified Java class name of the factory class used to acquire our JNDI
	 * InitialContext. By default, assumes that the standard JNDI LDAP provider will be utilized.
	 *
	 * @return String
	 */
	public String getContextFactory() {
		return contextFactory;
	}

	/**
	 * Assigns the fully qualified Java class name of the factory class used to acquire our JNDI
	 * InitialContext. By default, assumes that the standard JNDI LDAP provider will be utilized.
	 *
	 * @param contextFactory  the field value to assign
	 */
	public void setContextFactory(String contextFactory) {
		this.isInitialized = false;
		this.contextFactory = contextFactory;
	}

	/**
	 * Returns the connection URL to be passed to the JNDI driver when establishing a connection to the
	 * directory.
	 *
	 * @return String
	 */
	public String getConnectionUrl() {
		return connectionUrl;
	}

	/**
	 * Assigns the connection URL to be passed to the JNDI driver when establishing a connection to the
	 * directory.
	 *
	 * @param connectionUrl  the field value to assign
	 */
	public void setConnectionUrl(String connectionUrl) {
		this.isInitialized = false;
		this.connectionUrl = connectionUrl;
	}

	/**
	 * Returns the connection URL to be used if a socket connection can not be made to the provider at
	 * the <code>connectionURL</code> an attempt will be made to use this address.
	 *
	 * @return String
	 */
	public String getAlternateUrl() {
		return alternateUrl;
	}

	/**
	 * Assigns the connection URL to be used if a socket connection can not be made to the provider at
	 * the <code>connectionURL</code> an attempt will be made to use this address.
	 *
	 * @param alternateUrl  the field value to assign
	 */
	public void setAlternateUrl(String alternateUrl) {
		this.isInitialized = false;
		this.alternateUrl = alternateUrl;
	}

	/**
	 * Returns the string specifying the security protocol to use. If not given the providers default
	 * is used.
	 *
	 * @return String
	 */
	public String getConnectionProtocol() {
		return connectionProtocol;
	}

	/**
	 * Assigns the string specifying the security protocol to use. If not given the providers default
	 * is used.
	 *
	 * @param connectionProtocol  the field value to assign
	 */
	public void setConnectionProtocol(String connectionProtocol) {
		this.isInitialized = false;
		this.connectionProtocol = connectionProtocol;
	}

	/**
	 * Returns the string specifying the type of authentication to use. "none", "simple", "strong" or a
	 * provider specific definition can be used. If no value is given the providers default is used.
	 *
	 * @return String
	 */
	public String getSecurityAuthentication() {
		return securityAuthentication;
	}

	/**
	 * Assigns the string specifying the type of authentication to use. "none", "simple", "strong" or a
	 * provider specific definition can be used. If no value is given the providers default is used.
	 *
	 * @param securityAuthentication  the field value to assign
	 */
	public void setSecurityAuthentication(String securityAuthentication) {
		this.isInitialized = false;
		this.securityAuthentication = securityAuthentication;
	}

	/**
	 * Returns the timeout in milliseconds to use when establishing the connection to the LDAP directory.
	 * If not specified, a value of 5000 (5 seconds) is used.
	 *
	 * @return int
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Assigns the timeout in milliseconds to use when establishing the connection to the LDAP directory.
	 * If not specified, a value of 5000 (5 seconds) is used.
	 *
	 * @param connectionTimeout  the field value to assign
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.isInitialized = false;
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * Returns the directory username to use when establishing a connection to the directory for LDAP
	 * search and lookup operations. If not specified an anonymous connection is made, which is often
	 * sufficient unless you specify the <code>connectionPassword</code> property.
	 *
	 * @return String
	 */
	public String getConnectionPrincipal() {
		return connectionPrincipal;
	}

	/**
	 * Assigns the directory username to use when establishing a connection to the directory for LDAP
	 * search and lookup operations. If not specified an anonymous connection is made, which is often
	 * sufficient unless you specify the <code>connectionPassword</code> property.
	 *
	 * @param connectionPrincipal  the field value to assign
	 */
	public void setConnectionPrincipal(String connectionPrincipal) {
		this.isInitialized = false;
		this.connectionPrincipal = connectionPrincipal;
	}

	/**
	 * Returns the directory password to use when establishing a connection to the directory for LDAP
	 * search and lookup operations. If not specified an anonymous connection is made.
	 *
	 * @return String
	 */
	public String getConnectionPassword() {
		return connectionPassword;
	}

	/**
	 * Assigns the directory password to use when establishing a connection to the directory for LDAP
	 * search and lookup operations. If not specified an anonymous connection is made.
	 *
	 * @param connectionPassword  the field value to assign
	 */
	public void setConnectionPassword(String connectionPassword) {
		this.isInitialized = false;
		this.connectionPassword = connectionPassword;
	}

	/**
	 * Returns the pattern for the distinguished name (DN) of the user's directory entry, with
	 * <code>{0}</code> marking where the actual username should be inserted.
	 *
	 * @return String
	 */
	public String getUserPattern() {
		return (userPattern == null) ? null : userPattern.toPattern();
	}

	/**
	 * Assigns the pattern for the distinguished name (DN) of the user's directory entry, with
	 * <code>{0}</code> marking where the actual username should be inserted.
	 *
	 * @param userPattern  the field value to assign
	 */
	public void setUserPattern(String userPattern) {
		this.isInitialized = false;
		this.userPattern = (userPattern == null) ? null : new MessageFormat( userPattern );
	}

	/**
	 * Returns the base element for user searches performed using the 'userSearchPatterns' expressions.
	 *
	 * @return String
	 */
	public String getUserSearchBase() {
		return userSearchBase;
	}

	/**
	 * Assigns the base element for user searches performed using the 'userSearchPatterns' expressions.
	 *
	 * @param userSearchBase  the field value to assign
	 */
	public void setUserSearchBase(String userSearchBase) {
		this.isInitialized = false;
		this.userSearchBase = userSearchBase;
	}

	/**
	 * Returns the flag value that determines whether to search the entire subtree of the element specified by the
	 * 'userSearchBase' property for the user's entry. The default value of false causes only the top level to be
	 * searched.
	 *
	 * @return boolean
	 */
	public boolean isSearchUserSubtree() {
		return searchUserSubtree;
	}

	/**
	 * Assigns the flag value that determines whether to search the entire subtree of the element specified by the
	 * 'userSearchBase' property for the user's entry. The default value of false causes only the top level to be
	 * searched.
	 *
	 * @param searchUserSubtree  the field value to assign
	 */
	public void setSearchUserSubtree(boolean searchUserSubtree) {
		this.isInitialized = false;
		this.searchUserSubtree = searchUserSubtree;
	}

	/**
	 * Returns the colon-separated list of LDAP filter expressions to use when searching for a user's directory
	 * entry, with <code>{0}</code> marking where the actual username should be inserted.
	 *
	 * @return String
	 */
	public String getUserSearchPatterns() {
		String searchPatterns;
		
		if (userSearchPatterns != null) {
			StringBuilder sp = new StringBuilder();
			
			for (MessageFormat pattern : userSearchPatterns) {
				if (sp.length() > 0) sp.append(":");
				sp.append( pattern.toPattern() );
			}
			searchPatterns = sp.toString();
			
		} else {
			searchPatterns = null;
		}
		return searchPatterns;
	}

	/**
	 * Assigns the colon-separated list of LDAP filter expressions to use when searching for a user's directory
	 * entry, with <code>{0}</code> marking where the actual username should be inserted.
	 *
	 * @param userSearchPatterns  the field value to assign
	 */
	public void setUserSearchPatterns(String userSearchPatterns) {
		this.isInitialized = false;
		
		if (userSearchPatterns != null) {
			String[] patternList = userSearchPatterns.split( "\\:" );
			this.userSearchPatterns = new MessageFormat[ patternList.length ];
			
			for (int i = 0; i < patternList.length; i++) {
				this.userSearchPatterns[i] = new MessageFormat( patternList[i] );
			}
		} else {
			this.userSearchPatterns = null;
		}
	}

	/**
	 * Returns the time (in milliseconds) to wait for records to be returned when employing the user-search
	 * mode of operation. If not specified, the default of 0 is used which indicates no limit.
	 *
	 * @return int
	 */
	public int getUserSearchTimeout() {
		return userSearchTimeout;
	}

	/**
	 * Assignsthe time (in milliseconds) to wait for records to be returned when employing the user-search
	 * mode of operation. If not specified, the default of 0 is used which indicates no limit.
	 *
	 * @param userSearchTimeout  the field value to assign
	 */
	public void setUserSearchTimeout(int userSearchTimeout) {
		this.isInitialized = false;
		this.userSearchTimeout = userSearchTimeout;
	}

	/**
	 * Returns the name of the attribute where passwords are stored on user entries.  If not specified,
	 * a default value of "userPassword" is assumed.
	 *
	 * @return String
	 */
	public String getUserPasswordAttribute() {
		return userPasswordAttribute;
	}

	/**
	 * Assigns the name of the attribute where passwords are stored on user entries.  If not specified,
	 * a default value of "userPassword" is assumed.
	 *
	 * @param userPasswordAttribute  the field value to assign
	 */
	public void setUserPasswordAttribute(String userPasswordAttribute) {
		this.isInitialized = false;
		this.userPasswordAttribute = userPasswordAttribute;
	}

	/**
	 * Returns the strategy for JNDI referrals; allowed values are "ignore", "follow", or "throw" (see
	 * <code>javax.naming.Context.REFERRAL</code> for more information). Microsoft Active Directory often
	 * returns referrals. If you need to follow them set referrals to "follow". Caution: if your DNS is
	 * not part of AD, the LDAP client lib might try to resolve your domain name in DNS to find another
	 * LDAP server.
	 *
	 * @return String
	 */
	public String getReferralStrategy() {
		return referralStrategy;
	}

	/**
	 * Assigns the strategy for JNDI referrals; allowed values are "ignore", "follow", or "throw" (see
	 * <code>javax.naming.Context.REFERRAL</code> for more information). Microsoft Active Directory often
	 * returns referrals. If you need to follow them set referrals to "follow". Caution: if your DNS is
	 * not part of AD, the LDAP client lib might try to resolve your domain name in DNS to find another
	 * LDAP server.
	 *
	 * @param referralStrategy  the field value to assign
	 */
	public void setReferralStrategy(String referralStrategy) {
		this.isInitialized = false;
		this.referralStrategy = referralStrategy;
	}

	/**
	 * Returns the digest algorithm to apply to the plaintext password offered by the user before comparing it
	 * with the value retrieved from the directory. Valid values are those accepted for the algorithm name by the
	 * <code>java.security.MessageDigest</code> class. If not specified the plaintext password is assumed to be
	 * retrieved.
	 *
	 * @return String
	 */
	public String getDigestAlgorithm() {
		return digestAlgorithm;
	}

	/**
	 * Assigns the digest algorithm to apply to the plaintext password offered by the user before comparing it
	 * with the value retrieved from the directory. Valid values are those accepted for the algorithm name by the
	 * <code>java.security.MessageDigest</code> class. If not specified the plaintext password is assumed to be
	 * retrieved.
	 *
	 * @param digestAlgorithm  the field value to assign
	 * @throws NoSuchAlgorithmException  thrown if the specified algorithm is not supported
	 */
	public synchronized void setDigestAlgorithm(String digestAlgorithm) throws NoSuchAlgorithmException {
		this.isInitialized = false;
		this.digestAlgorithm = digestAlgorithm;
		this.passwordValidator = (digestAlgorithm == null) ? null : new PasswordValidator( digestAlgorithm, digestEncoding );
	}

	/**
	 * Returns the encoding character set to use when applying the digest algorithm.
	 *
	 * @return String
	 */
	public String getDigestEncoding() {
		return digestEncoding;
	}

	/**
	 * Assigns the encoding character set to use when applying the digest algorithm.
	 *
	 * @param digestEncoding  the field value to assign
	 */
	public synchronized void setDigestEncoding(String digestEncoding) {
		this.isInitialized = false;
		this.digestEncoding = digestEncoding;
		
		if (passwordValidator != null) {
			passwordValidator.setDigestEncoding( digestEncoding );
		}
	}
	
	/**
	 * Returns the amount of time (in milliseconds) that the results of a user's login
	 * attempt should be cached.
	 *
	 * @return long
	 */
	public long getAuthenticationCacheTimeout() {
		return authenticationCacheTimeout;
	}

	/**
	 * Assigns the amount of time (in milliseconds) that the results of a user's login
	 * attempt should be cached.
	 *
	 * @param connectionCacheTimeout  the field value to assign
	 */
	public void setAuthenticationCacheTimeout(long connectionCacheTimeout) {
		this.authenticationCacheTimeout = connectionCacheTimeout;
	}

	/**
	 * Returns the cached authentication results for the specified user ID.  If a cache entry
	 * does not exist for the user, this method will return null.  Expired entries will be
	 * removed from the cache and not returned by this method.
	 * 
	 * @param userId  the ID of the user for which to retrieve cached authentication results
	 * @return AuthenticationCacheEntry
	 */
	private AuthenticationCacheEntry getCachedAuthentication(String userId) {
		synchronized (authenticationCache) {
			AuthenticationCacheEntry cacheEntry = authenticationCache.get( userId );
			
			if ((cacheEntry != null) && cacheEntry.isExpired()) {
				authenticationCache.remove( userId );
				cacheEntry = null;
			}
			return cacheEntry;
		}
	}
	
	/**
	 * Assigns cached authentication results for the specified user ID.  If a cache entry
	 * does not exist for the user, this method will return null.
	 * 
	 * @param cacheEntry  the authentication results to cache
	 */
	private void setCachedAuthentication(AuthenticationCacheEntry cacheEntry) {
		synchronized (authenticationCache) {
			if ((cacheEntry != null) && !cacheEntry.isExpired()) {
				authenticationCache.put( cacheEntry.getUserId(), cacheEntry );
			}
		}
	}
	
	/**
	 * Cached entry for authentication credentials and login attempts.
	 */
	private class AuthenticationCacheEntry {
		
		private String userId;
		private String encryptedPassword;
		private boolean authenticationSuccessful;
		private long expirationTime;
		
		/**
		 * Constructor that specifies the user ID, encrypted password, and expiration time interval
		 * for the new cache entry.
		 * 
		 * @param userId  the user ID of the account being cached
		 * @param encryptedPassword  the last-used password (encrypted) for the account being cached
		 * @param authenticationSuccessful  indicates whether the last live authentication attempt was successful
		 */
		public AuthenticationCacheEntry(String userId, String encryptedPassword, boolean authenticationSuccessful) {
			this.userId = userId;
			this.encryptedPassword = encryptedPassword;
			this.authenticationSuccessful = authenticationSuccessful;
			this.expirationTime = System.currentTimeMillis() + authenticationCacheTimeout;
		}
		
		/**
		 * Returns the user ID of the account being cached.
		 *
		 * @return String
		 */
		public String getUserId() {
			return userId;
		}

		/**
		 * Returns the last-used password (encrypted) for the account being cached.
		 *
		 * @return String
		 */
		public String getEncryptedPassword() {
			return encryptedPassword;
		}

		/**
		 * Returns true if the last real authentication attempt was successful.
		 *
		 * @return boolean
		 */
		public boolean isAuthenticationSuccessful() {
			return authenticationSuccessful;
		}

		/**
		 * Returns true if this cache entry has expired.
		 * 
		 * @return boolean
		 */
		public boolean isExpired() {
			return System.currentTimeMillis() >= expirationTime;
		}
		
	}
	
}
